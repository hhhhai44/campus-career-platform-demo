package com.hhhhai.ccpd.vo.forum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞 Toggle 接口返回：当前是否已赞 + 最新点赞数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeToggleVO {

  /** 当前是否已点赞 */
  private Boolean liked;
  /** 最新点赞数量 */
  private Long likeCount;
}
