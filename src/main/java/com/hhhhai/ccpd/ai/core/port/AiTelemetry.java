package com.hhhhai.ccpd.ai.core.port;

/**
 * 类的含义：智能问答可观测性接口。
 * 类的作用：记录模型调用成功/失败、耗时与流式状态，便于监控与告警。
 */
public interface AiTelemetry {

  AiTelemetry NO_OP = new AiTelemetry() {
  };

  default void recordSuccess(String provider, String model, boolean stream, long costMs) {
    // no-op
  }

  default void recordFailure(String provider, String model, boolean stream, long costMs, Exception ex) {
    // no-op
  }
}
