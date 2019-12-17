package com.baidu.tts;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.baidu.tts.control.InitConfig;
import com.baidu.tts.control.NonBlockSyntherizer;
import com.baidu.tts.listener.MessageListener;
import com.baidu.tts.listener.OnSpeechListener;
import com.baidu.tts.util.AutoCheck;
import com.baidu.tts.util.IOfflineResourceConst;
import com.baidu.tts.util.OfflineResource;
import com.baidu.tts.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author chenjunwei
 * 1.调用setConfig 初始化配合参数
 * 2.调用initialTts
 * @date 2019/5/27
 */
public class BaiDuTtsUtil {

  private static final String TAG = BaiDuTtsUtil.class.getSimpleName().trim();

  private static final int MAX_MSG_LENGTH = 512;

  public static BaiDuTtsUtil instance;

  public static NonBlockSyntherizer nonBlockSyntherizer;
  /**
   * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
   * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
   */
  private String appId = "";

  private String appKey = "";

  private String secretKey = "";

  /**
   * 纯离线合成SDK授权码；离在线合成SDK免费，没有此参数
   */
  private String sn = "";

  /**
   * TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； TtsMode.OFFLINE 纯离线合成，需要纯离线SDK
   */
  private TtsMode ttsMode = IOfflineResourceConst.DEFAULT_OFFLINE_TTS_MODE;

  /**
   * 离线发音选择，VOICE_FEMALE即为离线女声发音。
   */
  private String offlineVoice = OfflineResource.VOICE_FEMALE;

  private boolean isDebug;

  private static MessageListener listener;


  public static BaiDuTtsUtil getInstance() {
    if (instance == null) {
      synchronized (BaiDuTtsUtil.class) {
        if (instance == null) {
          instance = new BaiDuTtsUtil();
        }
      }
    }
    return instance;
  }

  /**
   * 初始化配置参数
   *
   * @param appId
   * @param appKey
   * @param secretKey
   */
  public void setConfig(String appId, String appKey, String secretKey, String sn) {
    this.appId = appId;
    this.appKey = appKey;
    this.secretKey = secretKey;
    this.sn = sn;
  }

  /**
   * 设置模式
   *
   * @param ttsMode
   */
  public void setTtsMode(TtsMode ttsMode) {
    this.ttsMode = ttsMode;
  }

  /**
   * 是否调试
   *
   * @param debug
   */
  public void setDebug(boolean debug) {
    isDebug = debug;
  }

  /**
   * 初始化引擎，需要的参数均在InitConfig类里
   * <p>
   * DEMO中提供了3个SpeechSynthesizerListener的实现
   * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
   * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
   * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
   * mainHandler处理UI变化和播放状态监听
   */
  public void initialTts(Context context) {
    //解决重启没有释放问题
    release();
    // 日志打印在logcat中
    LoggerProxy.printable(true);
    // 设置初始化参数
    // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
    listener = new MessageListener();
    InitConfig initConfig = getInitConfig(context, listener);
    // 此处可以改为MySyntherizer 了解调用过程
    nonBlockSyntherizer = new NonBlockSyntherizer(context, initConfig, null);
  }

  /**
   * 合成的参数，可以初始化时填写，也可以在合成前设置。
   *
   * @return 合成参数Map
   */
  protected Map<String, String> getParams(Context context) {
    Map<String, String> params = new HashMap<>();
    // 以下参数均为选填
    // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
    params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
    // 设置合成的音量，0-9 ，默认 5
    params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
    // 设置合成的语速，0-9 ，默认 5
    params.put(SpeechSynthesizer.PARAM_SPEED, "5");
    // 设置合成的语调，0-9 ，默认 5
    params.put(SpeechSynthesizer.PARAM_PITCH, "5");

    params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
    // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
    // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
    // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
    // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
    // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

    // params.put(SpeechSynthesizer.PARAM_MIX_MODE_TIMEOUT, SpeechSynthesizer.PARAM_MIX_TIMEOUT_TWO_SECOND);
    // 离在线模式，强制在线优先。在线请求后超时2秒后，转为离线合成。

    // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
    OfflineResource offlineResource = createOfflineResource(context, offlineVoice);
    // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
    params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
    params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
            offlineResource.getModelFilename());
    return params;
  }

  protected InitConfig getInitConfig(Context context, SpeechSynthesizerListener listener) {
    Map<String, String> params = getParams(context);
    // 添加你自己的参数
    InitConfig initConfig;
    // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
    if (TextUtils.isEmpty(sn)) {
      setTtsMode(TtsMode.MIX);
      initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
    } else {
      setTtsMode(TtsMode.OFFLINE);
      initConfig = new InitConfig(appId, appKey, secretKey, sn, ttsMode, params, listener);
    }
    if (isDebug) {
      // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
      // 上线时请删除AutoCheck的调用
      AutoCheck.getInstance(context).check(initConfig, new Handler() {
        @Override
        public void handleMessage(Message msg) {
          if (msg.what == 100) {
            AutoCheck autoCheck = (AutoCheck) msg.obj;
            synchronized (autoCheck) {
              String message = autoCheck.obtainDebugMessage();
              print(message); // 可以用下面一行替代，在logcat中查看代码
              // Log.w("AutoCheckMessage", message);
            }
          }
        }

      });
    }
    return initConfig;
  }



  private OfflineResource createOfflineResource(Context context, String voiceType) {
    OfflineResource offlineResource = null;
    try {
      offlineResource = new OfflineResource(context, voiceType);
    } catch (IOException e) {
      // IO 错误自行处理
      e.printStackTrace();
      print("【error】:copy files from assets failed." + e.getMessage());
    }
    return offlineResource;
  }

  /**
   * 批量播报
   *
   * @param list
   */
  public static void batchSpeak(List<String> list) {
    if (null != list && list.size() > 0) {
      List<Pair<String, String>> texts = new ArrayList<>();
      for (int i = 0; i < list.size(); i++) {
        Pair<String, String> text = new Pair<>(list.get(i), i + "");
        texts.add(text);
      }
      int result = nonBlockSyntherizer.batchSpeak(texts);
      checkResult(result, "batchSpeak");
    }
  }


  public static void speak(String msg) {
    if (nonBlockSyntherizer == null) {
      print("[ERROR], 初始化失败");
      return;
    }
    if (msg.length() > MAX_MSG_LENGTH) {
      List list = StringUtil.getListFromContent(msg, MAX_MSG_LENGTH);
      batchSpeak(list);
    } else {
      int result = nonBlockSyntherizer.speak(msg);
      checkResult(result, "speak");
    }

  }

  public static void speak(String msg, OnSpeechListener onSpeechListener) {
    speak(msg);
    if (null != listener) {
      listener.setOnSpeechListener(onSpeechListener);
    }
  }

  public static void print(String message) {
    Log.d(TAG, message);
  }

  public static void checkResult(int result, String method) {
    if (result != 0) {
      print("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
    }
  }

  public static void stop() {
    print("停止合成引擎 按钮已经点击");
    int result = nonBlockSyntherizer.stop();
    checkResult(result, "stop");
  }

  public static void release() {
    try {
      if (nonBlockSyntherizer != null) {
        nonBlockSyntherizer.release();
        nonBlockSyntherizer = null;
        print("释放资源成功");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
