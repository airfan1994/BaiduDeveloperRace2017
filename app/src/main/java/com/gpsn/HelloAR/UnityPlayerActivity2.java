package com.gpsn.HelloAR;

import com.baidu.aip.chatkit.ImageLoader;
import com.baidu.aip.chatkit.message.MessageInput;
import com.baidu.aip.chatkit.message.MessagesListAdapter;
import com.baidu.aip.chatkit.model.Message;
import com.baidu.aip.chatkit.model.User;
import com.baidu.aip.unit.APIService;
import com.baidu.aip.unit.HintAdapter;
import com.baidu.aip.unit.MainActivity;
import com.baidu.aip.unit.R;
import com.baidu.aip.unit.exception.UnitError;
import com.baidu.aip.unit.listener.OnResultListener;
import com.baidu.aip.unit.listener.VoiceRecognizeCallback;
import com.baidu.aip.unit.loc.ComWithMyServer;
import com.baidu.aip.unit.model.AccessToken;
import com.baidu.aip.unit.model.CommunicateResponse;
import com.baidu.aip.unit.model.Scene;
import com.baidu.aip.unit.voice.VoiceRecognizer;
import com.baidu.aip.unit.widget.BasePopupWindow;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.unity3d.player.*;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static com.baidu.aip.unit.R.id.btn2;
//import static com.baidu.aip.unit.R.id.voiceInput;

public class UnityPlayerActivity2 extends AppCompatActivity
        implements MessagesListAdapter.OnLoadMoreListener, MessageInput.InputListener,
        MessageInput.VoiceInputListener ,SpeechSynthesizerListener
{
    private static final int SCENE_INTRODUCTION = 10400;
    //private static final int SCENE_NAVIGATE = 10320;
    //private static final int SCENE_AREA = 10428;

    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;
    protected HintAdapter hintAdapter;

    //  private TextView titleTv;
    //   private RelativeLayout titleRl;
    // private RelativeLayout rootRl;
    private BasePopupWindow popupWindow;
    //private MessagesList messagesList;
    private MessageInput input;

    private User sender;
    private User cs;
    private String sessionId = "";
    private VoiceRecognizer voiceRecognizer;
    private Scene curScene;
    private int id = 0;
    private String accessToken;
    private List<Message> waitList = new ArrayList<>();
    private Map<Integer, String> sceneMap = new HashMap<Integer, String>();

    private ListView dataListview;
    private List<Scene> dataList;
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    private ImageButton goBack;
    private ImageButton voiceInput;
    private static final String TAG = "MainActivity";

    private SpeechSynthesizer mSpeechSynthesizer;//百度语音合成客户端
    private String mSampleDirPath;
    private static final String SAMPLE_DIR_NAME = "baiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_ch_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_ch_speech_female.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_ch_text.dat";
    private static final String LICENSE_FILE_NAME = "temp_license_2017-10-15";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_ch_speech_female.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String APP_ID = "8997289";//请更换为自己创建的应用
    private static final String API_KEY = "GQIfBgSoxpM2YEXp8twKBn7Iq2tnM8WP";//请更换为自己创建的应用
    private static final String SECRET_KEY = "W3QCoSsyOcIjf1y91qBpmtaZrHNk65WL";//请更换为自己创建的应用
    private Button btn_speak;
    // Setup activity layout
    @Override protected void onCreate (Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy
        mUnityPlayer = new UnityPlayer(this);
        setContentView(R.layout.activity_unity2);

        RelativeLayout mParent=(RelativeLayout)findViewById(R.id.UnityView2);
        mParent.addView(mUnityPlayer.getView());
        mUnityPlayer.requestFocus();

        findView();
        // initAdapter();
        initAccessToken();
        voiceRecognizer = new VoiceRecognizer();
        voiceRecognizer.init(this, input.getVoiceInputButton());
        voiceRecognizer.setVoiceRecognizerCallback(new VoiceRecognizeCallback() {
            @Override
            public void callback(String text) {
                Message message = new Message(String.valueOf(id++), sender, text);
                ///    messagesAdapter.addToStart(message, true);
                //我并不需要展示结果
                //Toast.makeText(UnityPlayerActivity2.this, text, Toast.LENGTH_LONG).show();
                //  mSpeechSynthesizer.speak("故宫");
               //我把这部分接入下面试试
                //mSpeechSynthesizer.speak(text);
                sendMessage(message);
            }
        });
        initialEnv();
        initialTts();
//        input.getInputEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEND){
//                    onSubmit(v.getEditableText());
//                    v.setText("");
//                }
//                return true;
//            }
//        });
        goBack=(ImageButton)findViewById(R.id.goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(UnityPlayerActivity.this, "ready to jump", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(UnityPlayerActivity2.this, MainActivity.class);
                startActivity(intent);
                UnityPlayerActivity2.this.finish();
            }
        });
//        voiceInput = (ImageButton) findViewById(R.id.voiceInput);
//        voiceInput.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
////                if(v.getId() == R.id.voiceInput){
//                if(event.getAction() == MotionEvent.ACTION_UP){//判断按键松开
////                    Toast.makeText(UnityPlayerActivity.this, "UP", Toast.LENGTH_SHORT).show();
//                    UnityPlayer.UnitySendMessage("tourGuide","stopAction","");
//                }
//                if(event.getAction() == MotionEvent.ACTION_DOWN){//判断按键按下
////                    Toast.makeText(UnityPlayerActivity.this, "DOWN", Toast.LENGTH_SHORT).show();
//                    UnityPlayer.UnitySendMessage("tourGuide","startListenAction","");
//                }
////              }
//                return false;
//            }
//        });

    }

    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        voiceRecognizer.onDestroy();
        mUnityPlayer.quit();
        this.mSpeechSynthesizer.release();
        super.onDestroy();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }



    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }

    private void findView() {
        //  this.rootRl = (RelativeLayout) findViewById(R.id.root);
        //  this.titleRl = (RelativeLayout) findViewById(R.id.title_rl);
        //this.titleTv = (TextView) findViewById(R.id.title_tv);
        //this.messagesList = (MessagesList) findViewById(R.id.messagesList);
        input = (MessageInput) findViewById(R.id.input2);

        // input.setInputListener(this);
        input.setAudioInputListener(this);
//        hintRV = (RecyclerView) findViewById(R.id.hint_rv);
        //   titleRl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                popupWindow.getPopupWindow().setAnimationStyle(R.style.animTranslate);
//                popupWindow.showAtLocation(rootRl, Gravity.BOTTOM, 0, 0);
//                WindowManager.LayoutParams lp=getWindow().getAttributes();
//                lp.alpha = 0.3f;
//                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                getWindow().setAttributes(lp);
//            }
//        });

    }

    private void initAccessToken() {
        sender = new User("0", "kf", "", true);
        APIService.getInstance().init(getApplicationContext());
        APIService.getInstance().initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                accessToken = result.getAccessToken();
                Log.i("UnityPlayerActivity2", "AccessToken->" + result.getAccessToken());
//                if (!TextUtils.isEmpty(accessToken)) {
//                    resendWaitList();
//                }

            }

            @Override
            public void onError(UnitError error) {
                Log.i("wtf", "AccessToken->" + error.getErrorMessage());
            }
        },"T04MjgBs6Yx98AFRe2PHHnbZ", "Kjl3nqYfnUFTmI4UueIEtWHvSfsL8iSd");//"YZhGsnq6wXmlGFHUeIanCOBG", "6cb34ecc1229cc1791fdd3c5be73be7f");
        //"jMWIPDmK6zaxiK9KMCbb0hQl", "D4d5DD51gElVGkoHuoOZZfSHFDDT5K5k");
    }

    private void initAdapter() {
        messagesAdapter = new MessagesListAdapter<>(sender.getId(), imageLoader);
        // messagesAdapter.enableSelectionMode(this);
        messagesAdapter.setLoadMoreListener(this);
        messagesAdapter.setOnHintClickListener(new MessagesListAdapter.OnHintClickListener<Message>() {
            @Override
            public void onHintClick(String hint) {
                Message message = new Message(String.valueOf(id++), sender, hint, new Date());
                // sendMessage(message);
                messagesAdapter.addToStart(message, true);
            }
        });
        messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        //                        AppUtils.showToast(UnityPlayerActivity2.this,
                        //                                message.getUser().getName() + " avatar click",
                        //                                false);
                    }
                });
        // this.messagesList.setAdapter(messagesAdapter);

    }

    @Override
    public boolean onSubmit(CharSequence input) {
        return false;
    }

    @Override
    public void onVoiceInputClick() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager
                .PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, 100);
            return;
        }

        voiceRecognizer.onClick();
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        voiceRecognizer.onActivityResult(requestCode, resultCode, data);
    }

    private void initialTts() {
        //获取语音合成对象实例
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        //设置Context
        this.mSpeechSynthesizer.setContext(this);
        //设置语音合成状态监听
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        //文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        //声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        //本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，
        //仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了离线授权，
        //不需要设置该参数，建议将该行代码删除（离线引擎）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/"
                + LICENSE_FILE_NAME);
//        //请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId(APP_ID);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey(API_KEY, SECRET_KEY);
        //发音人（在线引擎），可用参数为0,1,2,3。。。
        //（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "2");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
//        // 授权检测接口(可以不使用，只是验证授权是否成功)
//        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);
//        if (authInfo.isSuccess()) {
//            Log.i(TAG, ">>>auth success.");
//        } else {
//            String errorMsg = authInfo.getTtsError().getDetailMessage();
//            Log.i(TAG, ">>>auth failed errorMsg: " + errorMsg);
//        }
        // 引擎初始化tts接口
        mSpeechSynthesizer.initTts(TtsMode.MIX);
        // 加载离线英文资源（提供离线英文合成功能）
        int result =
                mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        Log.i(TAG, ">>>loadEnglishModel result: " + result);
    }

    @Override
    public void onSynthesizeStart(String s) {
        //监听到合成开始
        Log.i(TAG, ">>>onSynthesizeStart()<<< s: " + s);
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
        //监听到有合成数据到达
        Log.i(TAG, ">>>onSynthesizeDataArrived()<<< s: " + s);
    }

    @Override
    public void onSynthesizeFinish(String s) {
        //监听到合成结束
        Log.i(TAG, ">>>onSynthesizeFinish()<<< s: " + s);
    }

    @Override
    public void onSpeechStart(String s) {
        //监听到合成并开始播放
        Log.i(TAG, ">>>onSpeechStart()<<< s: " + s);
        UnityPlayer.UnitySendMessage("tourGuide","startSpeakAction","");
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {
        //监听到播放进度有变化
        Log.i(TAG, ">>>onSpeechProgressChanged()<<< s: " + s);
    }

    @Override
    public void onSpeechFinish(String s) {
        //监听到播放结束
        Log.i(TAG, ">>>onSpeechFinish()<<< s: " + s);
        UnityPlayer.UnitySendMessage("tourGuide","stopAction","");
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        //监听到出错
        Log.i(TAG, ">>>onError()<<< description: " + speechError.description + ", code: " + speechError.code);
    }

    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        File file = new File(mSampleDirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);
    }


    //与服务器端通信
    private void sendMessage(Message message) {

        if (TextUtils.isEmpty(accessToken)) {
            waitList.add(message);
            return;
        }

        APIService.getInstance().communicate(new OnResultListener<CommunicateResponse>() {
            @Override
            public void onResult(CommunicateResponse result) {
                //现在我针对返回的结果处理
                //Toast.makeText(UnityPlayerActivity2.this, "suc", Toast.LENGTH_LONG).show();
                handleResponse(result);
            }

            @Override
            public void onError(UnitError error) {
                Toast.makeText(UnityPlayerActivity2.this, "fuc", Toast.LENGTH_LONG).show();
            }
            //我直接将场景ID填进来了
        },10400, message.getText(), sessionId);

    }
    //对返回值进行处理
    private void handleResponse(CommunicateResponse result) {
        if (result != null) {
            sessionId = result.sessionId;
            //  如果有对于的动作action，请执行相应的逻辑
            List<CommunicateResponse.Action> actionList = result.actionList;
            //测试部分
            Toast.makeText(UnityPlayerActivity2.this,"正在检索信息", Toast.LENGTH_LONG).show();


            for (CommunicateResponse.Action action : actionList) {


//                if (!TextUtils.isEmpty(action.say)) {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append(action.say);
//
//                    /*
//                    Message message = new Message(String.valueOf(id++), cs, sb.toString(), new Date());
//                    messagesAdapter.addToStart(message, true);
//                    if (action.hintList.size() > 0) {
//                        message.setHintList(action.hintList);
//                    }
//                    */
//
//                }else{
//                    continue;
//                }
                sceneKnowledge(action,result);


                /*
                if ("start_work_satisfy".equals(action.actionId)) {
                    Log.i("wtf", "开始扫地");
                } else if ("stop_work_satisfy".equals(action.actionId)) {
                    Log.i("wtf", "停止工作");
                } else if ("move_action_satisfy".equals(action.actionId)) {
                    Log.i("wtf", "移动");
                } else if ("timed_charge_satisfy".equals(action.actionId)) {
                    Log.i("wtf", "定时充电");
                } else if ("timed_task_satisfy".equals(action.actionId)) {
                    Log.i("wtf", "定时扫地");
                } else if ("sing_song_satisfy".equals(action.actionId)) {
                    Log.i("wtf", "唱歌");
                }
                */
            }
        }
    }
    //UNITY 工程中的解析意图，只回答景点知识
    private void sceneKnowledge(CommunicateResponse.Action action,CommunicateResponse result) {
        ComWithMyServer aLinker = new ComWithMyServer();
        // 执行自己的业务逻辑,下面我先测试一些例子
        //业务逻辑->景点简介
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //传送的参数
        Map<String, String> aMap = new HashMap<String, String>();
        //url初始化
        String url = "http://101.236.23.124:8080/polls/jingdian_knowledge?";
        //String url = "http://knowledgeworks.cn:20313/cndbpedia/api/entityAttribute?";

        String attr = "";
        String name = "";
        CommunicateResponse.Qu_res aSlotWord = result.aQures;
        if (aSlotWord.intent_candidate_List.size() > 0) {
            name = aSlotWord.intent_candidate_List.get(0);
        }
        //name = "故宫";
        //我在头部测试周边意图识别

//        aMap.put("propertyy",attr);
//        aMap.put("name",aSlotWord.intent_candidate_List.get(0));
        if ("description_satisfy".equals(action.actionId)) {
            //只有创建新的线程才能开启新的网络连接--暂时注释
            //StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
            //StrictMode.setThreadPolicy(policy);
            attr = "DESC";
            //if()  return;
            if (aSlotWord.intent_candidate_List.size() > 0 && name.length() != 0) {
                aMap.put("propertyy", attr);
                aMap.put("name", name);
                String res = aLinker.submitPostData(url, aMap, "utf-8");

                mSpeechSynthesizer.speak(res);
                //Toast.makeText(UnityPlayerActivity2.this, res, Toast.LENGTH_LONG).show();

                //Message message = new Message(String.valueOf(id++), cs, res, new Date());
                //messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的建设时间
        else if ("found_time_satisfy".equals(action.actionId)) {
            attr = "FOUND_TIME";
            if (aSlotWord.intent_candidate_List.size() > 0 && name.length() != 0) {
                aMap.put("name", name);
                aMap.put("propertyy", attr);
                String res = aLinker.submitPostData(url, aMap, "utf-8");
                //mSpeechSynthesizer.speak(res);
                mSpeechSynthesizer.speak(res);
                //Toast.makeText(UnityPlayerActivity2.this, res, Toast.LENGTH_LONG).show();

                //Message message = new Message(String.valueOf(id++), cs, res, new Date());
                //messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的门票价格
        else if ("ticket_satisfy".equals(action.actionId)) {
            attr = "TICKET";
            if (aSlotWord.intent_candidate_List.size() > 0 && name.length() != 0) {
                aMap.put("propertyy", attr);
                aMap.put("name", name);
                String res = aLinker.submitPostData(url, aMap, "utf-8");
                //mSpeechSynthesizer.speak(res);
                mSpeechSynthesizer.speak(res);
                //Toast.makeText(UnityPlayerActivity2.this, res, Toast.LENGTH_LONG).show();

                //Message message = new Message(String.valueOf(id++), cs, res, new Date());
                //messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的开放时间
        else if ("time_satisfy".equals(action.actionId)) {
            attr = "TIME";
            if (aSlotWord.intent_candidate_List.size() > 0 && name.length() != 0) {
                aMap.put("name", name);
                aMap.put("propertyy", attr);
                String res = aLinker.submitPostData(url, aMap, "utf-8");
                //mSpeechSynthesizer.speak(res);
                mSpeechSynthesizer.speak(res);
                //Toast.makeText(UnityPlayerActivity2.this, res, Toast.LENGTH_LONG).show();

                //Message message = new Message(String.valueOf(id++), cs, res, new Date());
                //messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的著名景点
        else if ("famous_satisfy".equals(action.actionId)) {
            attr = "FAMOUS";
            if (aSlotWord.intent_candidate_List.size() > 0 && name.length() != 0) {
                aMap.put("name", name);
                aMap.put("propertyy", attr);
                String res = aLinker.submitPostData(url, aMap, "utf-8");
                //mSpeechSynthesizer.speak(res);
                mSpeechSynthesizer.speak(res);
                //Toast.makeText(UnityPlayerActivity2.this, res, Toast.LENGTH_LONG).show();

                //Message message = new Message(String.valueOf(id++), cs, res, new Date());
                //messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的占地面积
        else if ("squre_satisfy".equals(action.actionId)) {
            attr = "SQURE";
            if (aSlotWord.intent_candidate_List.size() > 0 && name.length() != 0) {
                aMap.put("name", name);
                aMap.put("propertyy", attr);
                String res = aLinker.submitPostData(url, aMap, "utf-8");
                //mSpeechSynthesizer.speak(res);
                mSpeechSynthesizer.speak(res);
                //Toast.makeText(UnityPlayerActivity2.this, res, Toast.LENGTH_LONG).show();

                //Message message = new Message(String.valueOf(id++), cs, res, new Date());
                //messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的游玩时间
        else if ("position_satisfy".equals(action.actionId)) {
            attr = "PLAY_TIME";
            if (aSlotWord.intent_candidate_List.size() > 0 && name.length() != 0) {
                aMap.put("name", name);
                aMap.put("propertyy", attr);
                String res = aLinker.submitPostData(url, aMap, "utf-8");
                //mSpeechSynthesizer.speak(res);
                mSpeechSynthesizer.speak(res);
                //Toast.makeText(UnityPlayerActivity2.this, res, Toast.LENGTH_LONG).show();

                //Message message = new Message(String.valueOf(id++), cs, res, new Date());
                //messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的游玩位置
        else if ("play_time_satisfy".equals(action.actionId)) {
            attr = "POSITION";
            if (aSlotWord.intent_candidate_List.size() > 0 && name.length() != 0) {
                aMap.put("name", name);
                aMap.put("propertyy", attr);
                String res = aLinker.submitPostData(url, aMap, "utf-8");
                //mSpeechSynthesizer.speak(res);
                mSpeechSynthesizer.speak(res);
                //Toast.makeText(UnityPlayerActivity2.this, res, Toast.LENGTH_LONG).show();

                //Message message = new Message(String.valueOf(id++), cs, res, new Date());
                //messagesAdapter.addToStart(message, true);
            }
        }
    }
    /**
     * 将工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    public void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
