package com.hhhhai.ccpd.common.config;

import com.hhhhai.ccpd.interceptor.LoginInterceptor;
import com.hhhhai.ccpd.interceptor.RefreshTokenInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration//配置类
public class MvcConfig implements WebMvcConfigurer {

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  // todo 修改拦截路径
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // token 刷新拦截器
    registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
        .addPathPatterns(
            "/**"
        ).order(0);
    // 登录拦截器
    registry.addInterceptor(new LoginInterceptor())
        .excludePathPatterns(
            "/auth/login",
            "/user/code",
            "/blog/hot",
            "/shop/**",
            "/shop-type/**",
            "/upload/**",
            "/voucher/**"
        ).order(1);
  }
}