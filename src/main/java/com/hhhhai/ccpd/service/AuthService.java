package com.hhhhai.ccpd.service;

import com.hhhhai.ccpd.dto.LoginDTO;
import com.hhhhai.ccpd.vo.LoginVO;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
  public LoginVO login(LoginDTO request);
}
