package com.chris.tts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.baidu.tts.BaiDuTtsUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class MainActivity extends AppCompatActivity {
  private Button btn_speak;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initPermission();
    btn_speak = findViewById(R.id.btn_speak);
    btn_speak.setOnClickListener(v -> BaiDuTtsUtil.speak("高超 呆逼"));
  }

  @SuppressLint("CheckResult")
  private void initPermission() {
    RxPermissions rxPermissions=new RxPermissions(MainActivity.this);
    rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe(aBoolean -> MyApp.initBaiDuTts());
  }
}
