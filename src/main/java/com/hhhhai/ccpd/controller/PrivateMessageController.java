package com.hhhhai.ccpd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.message.PrivateMessageSendDTO;
import com.hhhhai.ccpd.service.PrivateMessageService;
import com.hhhhai.ccpd.vo.message.ConversationVO;
import com.hhhhai.ccpd.vo.message.PrivateMessageVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 私信接口
 */
@RestController
@RequestMapping("/message")
@AllArgsConstructor
public class PrivateMessageController {

  @Resource
  private PrivateMessageService privateMessageService;

  /**
   * 会话分页列表
   */
  @GetMapping("/conversation/page")
  public Result<Page<ConversationVO>> pageConversations(
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long size) {
    return Result.success(privateMessageService.pageConversations(page, size));
  }

  /**
   * 会话消息分页
   */
  @GetMapping("/session/{peerUserId}/page")
  public Result<Page<PrivateMessageVO>> pageSession(
      @PathVariable("peerUserId") Long peerUserId,
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "20") Long size) {
    return Result.success(privateMessageService.pageSessionMessages(peerUserId, page, size));
  }

  /**
   * 发送私信
   */
  @PostMapping("/send")
  public Result<Void> send(@Valid @RequestBody PrivateMessageSendDTO dto) {
    privateMessageService.send(dto);
    return Result.success();
  }

  /**
   * 标记当前会话消息为已读
   */
  @PostMapping("/session/{peerUserId}/read")
  public Result<Void> markSessionRead(@PathVariable("peerUserId") Long peerUserId) {
    privateMessageService.markSessionRead(peerUserId);
    return Result.success();
  }

  /**
   * 置顶/取消置顶会话
   */
  @PostMapping("/session/{peerUserId}/pin")
  public Result<Void> setSessionPinned(@PathVariable("peerUserId") Long peerUserId,
      @RequestParam(defaultValue = "true") Boolean pinned) {
    privateMessageService.setSessionPinned(peerUserId, pinned);
    return Result.success();
  }

  /**
   * 会话免打扰开关
   */
  @PostMapping("/session/{peerUserId}/mute")
  public Result<Void> setSessionMuted(@PathVariable("peerUserId") Long peerUserId,
      @RequestParam(defaultValue = "true") Boolean muted) {
    privateMessageService.setSessionMuted(peerUserId, muted);
    return Result.success();
  }

  /**
   * 撤回消息（仅发送方）
   */
  @PostMapping("/{messageId}/recall")
  public Result<Void> recallMessage(@PathVariable("messageId") Long messageId) {
    privateMessageService.recallMessage(messageId);
    return Result.success();
  }

  /**
   * 删除消息（单侧隐藏）
   */
  @DeleteMapping("/{messageId}")
  public Result<Void> deleteMessageForMe(@PathVariable("messageId") Long messageId) {
    privateMessageService.deleteMessageForMe(messageId);
    return Result.success();
  }

  /**
   * 当前用户未读私信数量
   */
  @GetMapping("/unread/count")
  public Result<Long> unreadCount() {
    return Result.success(privateMessageService.getUnreadCount());
  }
}


