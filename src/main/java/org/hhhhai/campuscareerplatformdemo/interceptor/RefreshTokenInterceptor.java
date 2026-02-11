package org.hhhhai.campuscareerplatformdemo.interceptor;



import static org.hhhhai.campuscareerplatformdemo.constant.RedisConstants.LOGIN_USER_KEY;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.hhhhai.campuscareerplatformdemo.common.context.UserContext;
import org.hhhhai.campuscareerplatformdemo.common.context.UserContextHolder;
import org.hhhhai.campuscareerplatformdemo.config.JwtProperties;
import org.hhhhai.campuscareerplatformdemo.security.token.JwtTokenService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * token刷新拦截器
 */
@Slf4j
public class RefreshTokenInterceptor implements HandlerInterceptor {

  private final StringRedisTemplate stringRedisTemplate;

  @Resource
  private JwtTokenService jwtTokenService;

  @Resource
  private JwtProperties jwtProperties;

  public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    log.info("token 刷新拦截器执行了");
    // 1.获取请求头中的token
    String token = request.getHeader("authorization");
    if (StrUtil.isBlank(token)) {
      return true;
    }
    
    try {
      // 2.从token中解析出jti（token ID），用于构建Redis key
      // 注意：即使token过期，getJtiFromToken也能获取jti
      String jti = jwtTokenService.getJtiFromToken(token);
      String redisKey = LOGIN_USER_KEY + jti;
      
      // 3.尝试解析token获取用户上下文
      UserContext context;
      try {
        context = jwtTokenService.parseToken(token);
      } catch (ExpiredJwtException e) {
        // token已过期，清理Redis中的登录态
        log.debug("token已过期，清理Redis key: {}", redisKey);
        jwtTokenService.removeLoginState(jti);
        return true;
      }
      
      // 4.判断Redis中是否存在该登录态
      if (!stringRedisTemplate.hasKey(redisKey)) {
        return true;
      }
      
      // 5.将用户信息保存到ThreadLocal
      UserContextHolder.saveUser(context);

      // 6.刷新token有效期（使用配置的ttl，而不是硬编码的常量）
      stringRedisTemplate.expire(redisKey, jwtProperties.getTtl(), TimeUnit.SECONDS);
    } catch (Exception e) {
      // token解析失败（格式错误、签名错误等），不拦截，让后续拦截器处理
      log.debug("token解析失败: {}", e.getMessage());
      return true;
    }
    
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {
    // 清理ThreadLocal，防止内存泄漏
    UserContextHolder.removeUser();
  }
}
