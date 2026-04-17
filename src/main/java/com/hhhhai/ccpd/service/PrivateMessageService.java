package com.hhhhai.ccpd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.dto.message.PrivateMessageSendDTO;
import com.hhhhai.ccpd.vo.message.ConversationVO;
import com.hhhhai.ccpd.vo.message.PrivateMessageVO;
import org.springframework.stereotype.Service;

@Service
public interface PrivateMessageService {

  Page<ConversationVO> pageConversations(Long page, Long size);

  Page<PrivateMessageVO> pageSessionMessages(Long peerUserId, Long page, Long size);

  void send(PrivateMessageSendDTO dto);

  void markSessionRead(Long peerUserId);

  void setSessionPinned(Long peerUserId, Boolean pinned);

  void setSessionMuted(Long peerUserId, Boolean muted);

  void recallMessage(Long messageId);

  void deleteMessageForMe(Long messageId);

  Long getUnreadCount();
}


