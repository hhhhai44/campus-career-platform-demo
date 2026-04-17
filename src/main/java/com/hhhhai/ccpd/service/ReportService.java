package com.hhhhai.ccpd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.dto.report.ReportCreateDTO;
import com.hhhhai.ccpd.dto.report.ReportHandleDTO;
import com.hhhhai.ccpd.vo.report.ReportDetailVO;
import com.hhhhai.ccpd.vo.report.ReportListVO;
import org.springframework.stereotype.Service;

@Service
public interface ReportService {

  Long createReport(ReportCreateDTO dto);

  Page<ReportListVO> pageAdminReports(Long page, Long size, String bizType, Integer status, String keyword);

  ReportDetailVO getAdminReportDetail(Long reportId);

  void handleReport(Long reportId, ReportHandleDTO dto);
}

