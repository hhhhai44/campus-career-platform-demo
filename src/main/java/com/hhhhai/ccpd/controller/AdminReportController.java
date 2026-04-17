package com.hhhhai.ccpd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.report.ReportHandleDTO;
import com.hhhhai.ccpd.service.ReportService;
import com.hhhhai.ccpd.vo.report.ReportDetailVO;
import com.hhhhai.ccpd.vo.report.ReportListVO;
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
 * 管理员举报管理接口
 */
@RestController
@RequestMapping("/admin/report")
public class AdminReportController {

  @Resource
  private ReportService reportService;

  @GetMapping("/page")
  public Result<Page<ReportListVO>> page(
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) String bizType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Integer statusValue = parseNullableInt(status);
    return Result.success(reportService.pageAdminReports(page, size, bizType, statusValue, keyword));
  }

  @GetMapping("/{id}")
  public Result<ReportDetailVO> detail(@PathVariable("id") Long id) {
    return Result.success(reportService.getAdminReportDetail(id));
  }

  @PutMapping("/{id}/handle")
  public Result<Void> handle(@PathVariable("id") Long id, @Valid @RequestBody ReportHandleDTO dto) {
    reportService.handleReport(id, dto);
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

