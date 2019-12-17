package com.chris.tts;


import android.app.Application;
import android.content.Context;

import com.baidu.tts.BaiDuTtsUtil;

import java.lang.ref.WeakReference;


/**
 * @author chenjunwei
 * @desc
 * @date 2019/8/12
 */
public class MyApp extends Application {
  private static WeakReference<Context> mContext;

  @Override
  public void onCreate() {
    super.onCreate();
    mContext = new WeakReference<>(getApplicationContext());
  }

  public static void initBaiDuTts() {
    //百度語音
    BaiDuTtsUtil baiDuTtsUtil = BaiDuTtsUtil.getInstance();
    baiDuTtsUtil.setDebug(BuildConfig.DEBUG);
    baiDuTtsUtil.setConfig("18045692", "0IzDSK1tafNv3GEVTGtZItNG", "i08RPxlxX9DQkK79uDU65A8rLtGWFqTe", "");
    BaiDuTtsUtil.getInstance().initialTts(mContext.get());
  }

}
