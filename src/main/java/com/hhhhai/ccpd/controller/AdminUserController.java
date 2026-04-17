package com.hhhhai.ccpd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.user.UserAdminStatusDTO;
import com.hhhhai.ccpd.service.UserAdminService;
import com.hhhhai.ccpd.vo.user.UserAdminDetailVO;
import com.hhhhai.ccpd.vo.user.UserAdminListVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员用户管理接口
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

  @Resource
  private UserAdminService userAdminService;

  @GetMapping("/page")
  public Result<Page<UserAdminListVO>> page(
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String role,
      @RequestParam(required = false) String status) {
    Integer roleValue = parseNullableInt(role);
    Integer statusValue = parseNullableInt(status);
    return Result.success(userAdminService.pageUsers(page, size, keyword, roleValue, statusValue));
  }

  @GetMapping("/{id}")
  public Result<UserAdminDetailVO> detail(@PathVariable("id") Long id) {
    return Result.success(userAdminService.getUserDetail(id));
  }

  @PutMapping("/{id}/status")
  public Result<Void> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody UserAdminStatusDTO dto) {
    userAdminService.updateUserStatus(id, dto.getStatus());
    return Result.success();
  }

  private Integer parseNullableInt(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return Integer.valueOf(raw);
    } catch (NumberFormatException ignore) {
      return null;
    }
  }
}

