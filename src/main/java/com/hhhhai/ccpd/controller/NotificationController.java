package com.hhhhai.ccpd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.service.NotificationService;
import com.hhhhai.ccpd.vo.notification.NotificationVO;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息通知相关接口
 */
@RestController
@RequestMapping("/notification")
@AllArgsConstructor
public class NotificationController {

  @Resource
  private NotificationService notificationService;

  /**
   * 分页查询当前用户的通知列表
   */
  @GetMapping("/page")
  public Result<Page<NotificationVO>> page(
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) Integer type,
      @RequestParam(required = false) Integer isRead) {
    Page<NotificationVO> result =
        notificationService.pageUserNotifications(page, size, type, isRead);
    return Result.success(result);
  }

  /**
   * 将指定通知标记为已读
   */
  @PostMapping("/{id}/read")
  public Result<Void> markRead(@PathVariable("id") Long id) {
    notificationService.markRead(id);
    return Result.success();
  }

  /**
   * 将当前用户的所有通知标记为已读
   */
  @PostMapping("/read/all")
  public Result<Void> markAllRead() {
    notificationService.markAllRead();
    return Result.success();
  }

  /**
   * 获取当前用户未读通知数量
   */
  @GetMapping("/unread/count")
  public Result<Long> unreadCount() {
    Long count = notificationService.getUnreadCount();
    return Result.success(count);
  }
}

