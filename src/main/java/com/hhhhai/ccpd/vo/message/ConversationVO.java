package com.hhhhai.ccpd.vo.message;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConversationVO {

  private Long peerUserId;
  private String peerUsername;
  private Long lastSenderId;
  private String lastMessage;
  private LocalDateTime lastTime;
  private Long unreadCount;
  private Boolean pinned;
  private Boolean muted;
}


