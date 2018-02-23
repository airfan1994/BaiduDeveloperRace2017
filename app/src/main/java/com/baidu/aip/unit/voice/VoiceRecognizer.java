/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.unit.voice;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.ImageView;

import com.baidu.aip.unit.R;
import com.baidu.aip.unit.listener.VoiceRecognizeCallback;
import com.baidu.speech.VoiceRecognitionService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VoiceRecognizer implements RecognitionListener {
    private static final String TAG = "Sdk2Api";
    private static final int REQUEST_UI = 1;

    public static final int STATUS_None = 0;
    public static final int STATUS_WaitingReady = 2;
    public static final int STATUS_Ready = 3;
    public static final int STATUS_Speaking = 4;
    public static final int STATUS_Recognition = 5;
    private SpeechRecognizer speechRecognizer;
    private int status = STATUS_None;

    private long speechEndTime = -1;
    private static final int EVENT_ERROR = 11;
    private Context context;
    private ImageView voiceInput;
    private VoiceRecognizeCallback callback;

    public void init(final Context context, ImageView voiceInput) {
        this.voiceInput = voiceInput;
        this.context = context;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context,
                new ComponentName(context, VoiceRecognitionService.class));

        speechRecognizer.setRecognitionListener(this);
        // btn.setText("点击开始");

    }

    public void onClick() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean api = sp.getBoolean("api", false);
        if (api) {
            switch (status) {
                case STATUS_None:
                    start();
                    // btn.setText("取消");
                    status = STATUS_WaitingReady;
                    break;
                case STATUS_WaitingReady:
                    cancel();
                    status = STATUS_None;
                    // btn.setText("点击开始");
                    break;
                case STATUS_Ready:
                    cancel();
                    status = STATUS_None;
                    // btn.setText("点击开始");
                    break;
                case STATUS_Speaking:
                    stop();
                    status = STATUS_Recognition;
                    // btn.setText("识别中");
                    break;
                case STATUS_Recognition:
                    cancel();
                    status = STATUS_None;
                    // btn.setText("点击开始");
                    break;
                default:
                    break;
            }
        } else {
            start();
        }
    }

    public void onDestroy() {
        speechRecognizer.destroy();
    }

    public void setVoiceRecognizerCallback(VoiceRecognizeCallback callback) {
        this.callback = callback;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            onResults(data.getExtras());
        }
    }

    public void bindParams(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean("tips_sound", true)) {
            intent.putExtra(Constant.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
            intent.putExtra(Constant.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
            intent.putExtra(Constant.EXTRA_SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
            intent.putExtra(Constant.EXTRA_SOUND_ERROR, R.raw.bdspeech_recognition_error);
            intent.putExtra(Constant.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
        }
        if (sp.contains(Constant.EXTRA_INFILE)) {
            String tmp = sp.getString(Constant.EXTRA_INFILE, "").replaceAll(",.*", "").trim();
            intent.putExtra(Constant.EXTRA_INFILE, tmp);
        }
        if (sp.getBoolean(Constant.EXTRA_OUTFILE, false)) {
            intent.putExtra(Constant.EXTRA_OUTFILE, "sdcard/outfile.pcm");
        }
        if (sp.getBoolean(Constant.EXTRA_GRAMMAR, false)) {
            intent.putExtra(Constant.EXTRA_GRAMMAR, "assets:///baidu_speech_grammar.bsg");
        }
        if (sp.contains(Constant.EXTRA_SAMPLE)) {
            String tmp = sp.getString(Constant.EXTRA_SAMPLE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_SAMPLE, Integer.parseInt(tmp));
            }
        }
        if (sp.contains(Constant.EXTRA_LANGUAGE)) {
            String tmp = sp.getString(Constant.EXTRA_LANGUAGE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_LANGUAGE, tmp);
            }
        }
        if (sp.contains(Constant.EXTRA_NLU)) {
            String tmp = sp.getString(Constant.EXTRA_NLU, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_NLU, tmp);
            }
        }

        if (sp.contains(Constant.EXTRA_VAD)) {
            String tmp = sp.getString(Constant.EXTRA_VAD, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_VAD, tmp);
            }
        }
        String prop = null;
        if (sp.contains(Constant.EXTRA_PROP)) {
            String tmp = sp.getString(Constant.EXTRA_PROP, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_PROP, Integer.parseInt(tmp));
                prop = tmp;
            }
        }

    }


    private void start() {
        //        print("点击了“开始”");
        Intent intent = new Intent();
        bindParams(intent);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        {

            String args = sp.getString("args", "");
            if (null != args) {
                //                print("参数集：" + args);
                intent.putExtra("args", args);
            }
        }
        boolean api = sp.getBoolean("api", false);
        if (api) {
            speechEndTime = -1;
            speechRecognizer.startListening(intent);
        } else {
            intent.setAction("com.baidu.action.RECOGNIZE_SPEECH");
            ((Activity) context).startActivityForResult(intent, REQUEST_UI);
        }

    }

    private void stop() {
        speechRecognizer.stopListening();
        //        print("点击了“说完了”");
    }

    private void cancel() {
        speechRecognizer.cancel();
        status = STATUS_None;
        //        print("点击了“取消”");
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        status = STATUS_Ready;
        //        print("准备就绪，可以开始说话");
    }

    @Override
    public void onBeginningOfSpeech() {
        status = STATUS_Speaking;
        // btn.setText("说完了");
        //        print("检测到用户的已经开始说话");
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        speechEndTime = System.currentTimeMillis();
        status = STATUS_Recognition;
        //        print("检测到用户的已经停止说话");
        // btn.setText("识别中");
    }

    @Override
    public void onError(int error) {
        status = STATUS_None;
        StringBuilder sb = new StringBuilder();
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append("音频问题");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append("没有语音输入");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append("其它客户端错误");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append("权限不足");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append("网络问题");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append("没有匹配的识别结果");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append("引擎忙");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append("服务端错误");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append("连接超时");
                break;
            default:
                break;
        }
        sb.append(":" + error);
        //        print("识别失败：" + sb.toString());
        // btn.setText("点击开始");
    }

    @Override
    public void onResults(Bundle results) {
        long end2finish = System.currentTimeMillis() - speechEndTime;
        status = STATUS_None;
        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        //        print("识别成功：" + Arrays.toString(nbest.toArray(new String[nbest.size()])));
        // String json_res = results.getString("origin_result");

        // btn.setText("点击开始");
        String strEnd2Finish = "";
        if (end2finish < 60 * 1000) {
            strEnd2Finish = "(waited " + end2finish + "ms)";
        }

        if (callback != null) {
            callback.callback(nbest.get(0));
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> nbest = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        switch (eventType) {
            case EVENT_ERROR:
                String reason = params.get("reason") + "";
                break;
            case VoiceRecognitionService.EVENT_ENGINE_SWITCH:
                int type = params.getInt("engine_type");
                break;
            default:
                break;
        }
    }

}
