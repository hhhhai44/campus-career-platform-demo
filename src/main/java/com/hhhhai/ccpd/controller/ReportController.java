package com.hhhhai.ccpd.controller;

import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.report.ReportCreateDTO;
import com.hhhhai.ccpd.service.ReportService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户举报接口
 */
@RestController
@RequestMapping("/report")
public class ReportController {

  @Resource
  private ReportService reportService;

  @PostMapping
  public Result<Long> create(@Valid @RequestBody ReportCreateDTO dto) {
    return Result.success(reportService.createReport(dto));
  }
}

