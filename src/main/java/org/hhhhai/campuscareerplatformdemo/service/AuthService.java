package org.hhhhai.campuscareerplatformdemo.service;

import org.hhhhai.campuscareerplatformdemo.dto.LoginDTO;
import org.hhhhai.campuscareerplatformdemo.vo.LoginVO;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
  public LoginVO login(LoginDTO request);
}
