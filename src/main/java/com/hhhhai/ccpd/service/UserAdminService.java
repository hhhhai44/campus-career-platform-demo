package com.hhhhai.ccpd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.vo.user.UserAdminDetailVO;
import com.hhhhai.ccpd.vo.user.UserAdminListVO;
import org.springframework.stereotype.Service;

@Service
public interface UserAdminService {

  Page<UserAdminListVO> pageUsers(Long page, Long size, String keyword, Integer role, Integer status);

  UserAdminDetailVO getUserDetail(Long userId);

  void updateUserStatus(Long userId, Integer status);
}

