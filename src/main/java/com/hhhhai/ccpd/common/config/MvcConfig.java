package com.hhhhai.ccpd.common.config;

import com.hhhhai.ccpd.interceptor.AccessLogInterceptor;
import com.hhhhai.ccpd.interceptor.LoginInterceptor;
import com.hhhhai.ccpd.interceptor.RefreshTokenInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration//配置类
public class MvcConfig implements WebMvcConfigurer {

  @Resource
  private AccessLogInterceptor accessLogInterceptor;

  @Resource
  private RefreshTokenInterceptor refreshTokenInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 先刷新并恢复用户上下文，便于后续日志记录到用户名
    registry.addInterceptor(refreshTokenInterceptor)
        .addPathPatterns("/**")
        .order(0);

    // 统一记录接口入参与耗时日志
    registry.addInterceptor(accessLogInterceptor)
        .addPathPatterns("/**")
        .order(1);

    // 登录拦截器
    registry.addInterceptor(new LoginInterceptor())
        .addPathPatterns(
            "/forum/post",
            "/forum/post/comment",
            "/forum/post/*/like",
            "/forum/post/*/like/toggle",
            "/forum/post/*/favorite",
            "/forum/post/*/favorite/toggle",
            "/resource",
            "/resource/rating",
            "/resource/comment",
            "/resource/comment/**",
            "/notification/**"
        )
        .excludePathPatterns(
            "/auth/login",
            "/auth/register"
        )
        .order(2);
  }
}