package com.hhhhai.ccpd.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类列表项 VO（帖子分类、资源分类通用），只返回 id 与 name
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListItemVO {

  private Long id;
  private String name;
}
