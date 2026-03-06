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
  private RefreshTokenInterceptor refreshTokenInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // token 刷新拦截器
    registry.addInterceptor(refreshTokenInterceptor)
        .addPathPatterns(
            // 帖子相关：发帖、评论、点赞、收藏
            "/forum/post",
            "/forum/post/comment",
            "/forum/post/*/like",
            "/forum/post/*/like/toggle",
            "/forum/post/*/favorite",
            "/forum/post/*/favorite/toggle",
            // 资源相关：上传、评分
            "/resource",
            "/resource/rating",
            // 通知相关：当前用户通知
            "/notification/**"
        ).order(0);
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
            "/notification/**"
        )
        // 登录、注册接口不需要拦截
        .excludePathPatterns(
            "/auth/login",
            "/auth/register"
        )
        .order(1);
  }
}