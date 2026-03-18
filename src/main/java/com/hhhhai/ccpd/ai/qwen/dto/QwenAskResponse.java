package com.hhhhai.ccpd.ai.qwen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 千问问答响应。
 *
 * <p>TODO(Agent): 后续可加入 usage（token）、引用来源（RAG）、工具调用轨迹、思考链路（如需）等字段。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QwenAskResponse {
  private String answer;
  private String model;
}


