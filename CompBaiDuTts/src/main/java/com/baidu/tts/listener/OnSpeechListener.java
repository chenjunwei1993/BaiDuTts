package com.baidu.tts.listener;

/**
 * @author chenjunwei
 * @desc
 * @date 2019/5/27
 */
public interface OnSpeechListener {
  /**
   *开始
   */
  void onSpeechStart();

  /**
   * 完成
   */
  void onSpeechFinish();

  /**
   * 错误
   */
  void onError();
}
