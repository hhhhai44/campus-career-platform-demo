package com.hhhhai.ccpd.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ccp.jwt")
@Data
public class JwtProperties {

  /**
   * jwt 令牌生成相关配置
   */
  private String secretKey;
  private long ttl;
  private String tokenName;

}
