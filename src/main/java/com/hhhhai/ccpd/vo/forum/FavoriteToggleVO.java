package com.hhhhai.ccpd.vo.forum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏 Toggle 接口返回：当前是否已收藏
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteToggleVO {

  /** 当前是否已收藏 */
  private Boolean favorited;
}
