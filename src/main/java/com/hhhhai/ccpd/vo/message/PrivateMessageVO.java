package com.hhhhai.ccpd.vo.message;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PrivateMessageVO {

  private Long id;
  private Long fromUserId;
  private String fromUsername;
  private Long toUserId;
  private String toUsername;
  private String content;
  private Integer isRead;
  private String isReadDesc;
  private LocalDateTime createTime;
  private Boolean mine;
  private Boolean recalled;
}


