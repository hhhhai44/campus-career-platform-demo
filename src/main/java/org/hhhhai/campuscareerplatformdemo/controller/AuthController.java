package org.hhhhai.campuscareerplatformdemo.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.hhhhai.campuscareerplatformdemo.common.result.Result;
import org.hhhhai.campuscareerplatformdemo.dto.LoginDTO;
import org.hhhhai.campuscareerplatformdemo.service.AuthService;
import org.hhhhai.campuscareerplatformdemo.vo.LoginVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器
 */
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

  @Resource
  private AuthService authService;

  @PostMapping("/login")
  public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
    LoginVO vo = authService.login(dto);
    return Result.success(vo);
  }
}
