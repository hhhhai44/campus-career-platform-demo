package com.hhhhai.ccpd.mapper.projection;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConversationDigest {

  private Long peerUserId;
  private Long lastMessageId;
  private LocalDateTime lastTime;
  private Long unreadCount;
}

