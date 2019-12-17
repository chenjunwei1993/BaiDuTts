package com.baidu.tts.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abc on 2019/6/5.
 */

public class StringUtil {
  /**
   * 给定一个长字符串内容，返回一个数组
   *
   * @param content 所有的内容
   * @param count   需要每段截取的长度
   * @return 所有分段的数组list
   */
  public static List getListFromContent(String content, int count) {
    List list = new ArrayList();
    // 获取String的总长度
    int contentLength = content.length();
    if (contentLength < count) {
      list.add(content);
    } else {
      int begin = 0;
      // 获取需要切割多少段
      int cutCount = contentLength / count;
      int cutCounts = contentLength % count;
      // 获取切割段的长度
      if (cutCounts != 0) {
        cutCount++;
      }
      for (int i = 1; i <= cutCount; i++) {
        String temp;
        // 不是最后一段
        if (i != cutCount) {
          temp = content.substring(begin, count * i);
        } else {
          temp = content.substring(begin, contentLength);
        }
        begin = count * i;
        list.add(temp);
      }
    }
    return list;
  }
}



