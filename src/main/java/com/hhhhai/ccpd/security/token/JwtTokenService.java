package com.hhhhai.ccpd.security.token;


import static com.hhhhai.ccpd.common.constant.RedisConstants.LOGIN_USER_KEY;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import com.hhhhai.ccpd.common.config.JwtProperties;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.common.enums.UserRoleEnum;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService implements TokenService {

  @Resource
  private JwtProperties jwtProperties;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  private SecretKey getSecretKey() {
    return Keys.hmacShaKeyFor(
        jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 生成 token
   */
  @Override
  public String generateToken(UserEntity user) {
    String jti = UUID.randomUUID().toString();

    Date now = new Date();
    Date expire = new Date(now.getTime() + jwtProperties.getTtl() * 1000);

    String token = Jwts.builder()
        .setId(jti)
        .setSubject(String.valueOf(user.getId()))
        .claim("username", user.getUsername())
        .claim("role", user.getRole() != null ? user.getRole().getCode() : null)
        //.claim("schoolId", user.getSchoolId())
        .setIssuedAt(now)
        .setExpiration(expire)
        .signWith(getSecretKey(), SignatureAlgorithm.HS256)
        .compact();

    // Redis 里存登录态
    String redisKey = LOGIN_USER_KEY + jti;
    stringRedisTemplate.opsForValue().set(
        redisKey,
        String.valueOf(user.getId()),
        jwtProperties.getTtl(),
        TimeUnit.SECONDS
    );

    return token;
  }

  /**
   * 从token中提取jti（不验证Redis，即使token过期也能获取jti）
   *
   * @param token JWT 字符串
   * @return token 对应的 jti
   */
  public String getJtiFromToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(getSecretKey())
          .build()
          .parseClaimsJws(token)
          .getBody();
      return claims.getId();
    } catch (ExpiredJwtException e) {
      // 即使token过期，也要从异常中获取jti，用于清理Redis
      return e.getClaims().getId();
    }
  }

  /**
   * 解析token获取用户上下文（不验证Redis，但如果token过期会抛出异常）
   *
   * @param token JWT 字符串
   * @return 用户上下文
   * @throws ExpiredJwtException 如果token已过期
   */
  public UserContext parseToken(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(getSecretKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
    return buildUserContext(claims);
  }

  @Override
  public UserContext parseAndValidate(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(getSecretKey())
        .build()
        .parseClaimsJws(token)
        .getBody();

    String jti = claims.getId();
    if (jti == null || !stringRedisTemplate.hasKey(LOGIN_USER_KEY + jti)) {
      throw new BusinessException(ErrorCode.LOGIN_INVALID);
    }
    return buildUserContext(claims);
  }

  /**
   * 清理Redis中的登录态（用于token过期时清理）
   *
   * @param jti token ID
   */
  public void removeLoginState(String jti) {
    String redisKey = LOGIN_USER_KEY + jti;
    stringRedisTemplate.delete(redisKey);
  }

  private UserContext buildUserContext(Claims claims) {
    Integer roleCode = claims.get("role", Integer.class);
    UserRoleEnum roleEnum = UserRoleEnum.fromCode(roleCode);
    String roleDesc = roleEnum == null ? null : roleEnum.getDescription();
    return new UserContext(
        Long.valueOf(claims.getSubject()),
        claims.get("username", String.class),
        roleDesc);
  }
}
