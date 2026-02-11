package org.hhhhai.campuscareerplatformdemo.common.utils;


import cn.hutool.core.util.RandomUtil;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class PasswordEncoder {

  public String encode(String password) {
    // 生成盐
    String salt = RandomUtil.randomString(20);
    // 加密
    return encode(password, salt);
  }

  private String encode(String password, String salt) {
    // 加密
    return salt + "@" + DigestUtils.md5DigestAsHex(
        (password + salt).getBytes(StandardCharsets.UTF_8));
  }

  public Boolean matches(String encodedPassword, String rawPassword) {
    if (encodedPassword == null || rawPassword == null) {
      return false;
    }
    if (!encodedPassword.contains("@")) {
      throw new RuntimeException("密码格式不正确！");
    }
    String[] arr = encodedPassword.split("@");
    // 获取盐
    String salt = arr[0];
    // 比较
    return encodedPassword.equals(encode(rawPassword, salt));
  }
}
