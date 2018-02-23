/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.unit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.baidu.aip.chatkit.ImageLoader;
import com.baidu.aip.chatkit.message.MessageInput;
import com.baidu.aip.chatkit.message.MessagesList;
import com.baidu.aip.chatkit.message.MessagesListAdapter;

import com.baidu.aip.chatkit.model.Message;
import com.baidu.aip.chatkit.model.User;
import com.baidu.aip.unit.exception.UnitError;
import com.baidu.aip.unit.listener.OnResultListener;
import com.baidu.aip.unit.listener.VoiceRecognizeCallback;
import com.baidu.aip.unit.model.AccessToken;
import com.baidu.aip.unit.model.AroundResponse;
import com.baidu.aip.unit.model.CommunicateResponse;
import com.baidu.aip.unit.model.PathResponse;
import com.baidu.aip.unit.model.CommunicateResponse.bot_merged_slots;

import com.baidu.aip.unit.model.Scene;
import com.baidu.aip.unit.parser.CommunicateParser;
import com.baidu.aip.unit.voice.VoiceRecognizer;
import com.baidu.aip.unit.widget.BasePopupWindow;

import com.baidu.aip.unit.loc.ComWithMyServer;
import com.gpsn.HelloAR.MainActivity2;
import com.gpsn.HelloAR.UnityPlayerActivity;
import com.gpsn.HelloAR.UnityPlayerActivity2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
public class MainActivity extends AppCompatActivity
        implements MessagesListAdapter.OnLoadMoreListener, MessageInput.InputListener,
        MessageInput.VoiceInputListener {

    //private static final int SCENE_SWEEPER_ROBOT = 3087;
    //private static final int SCENE_SWEEPER_ROBOT = 10400;
    //private static final int SCENE_NAVIGATE = 3109;

    private static final int SCENE_INTRODUCTION = 10400;
    //private static final int SCENE_NAVIGATE = 10320;
    private static final int SCENE_AREA = 10428;

    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;
    protected HintAdapter hintAdapter;

    private TextView titleTv;
    private RelativeLayout titleRl;
    private RelativeLayout rootRl;
    private BasePopupWindow popupWindow;
    private MessagesList messagesList;
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
    private Button btn1;
    private ImageButton goToAR;
    private LocationManager locationManager;

    private boolean aNviFlag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();

        findView();

        initAdapter();


        voiceRecognizer = new VoiceRecognizer();
        voiceRecognizer.init(this, input.getVoiceInputButton());
        voiceRecognizer.setVoiceRecognizerCallback(new VoiceRecognizeCallback() {
            @Override
            public void callback(String text) {
                Message message = new Message(String.valueOf(id++), sender, text);
                messagesAdapter.addToStart(message, true);
                sendMessage(message);
            }
        });
        //据说是输入的线程冲突，我先关闭输入试试

        input.getInputEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND){
                    onSubmit(v.getEditableText());
                    v.setText("");
                }
                return true;
            }
        });

        //新增部分
        goToAR=(ImageButton)findViewById(R.id.goToAR);
        goToAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input.clearAnimation();
                Toast.makeText(MainActivity.this, "小游正在路上~~", Toast.LENGTH_LONG).show();
                //Intent intent = new Intent(MainActivity.this, MainActivity2.class);
               Intent intent = new Intent(MainActivity.this, UnityPlayerActivity2.class);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });

        hintAdapter = new HintAdapter(this);

        changeScene(dataList.get(0));
        initPopupWindow();

        initAccessToken();
    }

    // 演示数据，开发者可到ai.baidu.com 官网理解与交互技术(UNIT)板块申请并训练自己的场景机器人
    private void initData() {
        sender = new User("0", "kf", "", true);
        cs = new User("1", "客服", "", true);

        //sceneMap.put(SCENE_SWEEPER_ROBOT, "扫地机器人");
        sceneMap.put(SCENE_INTRODUCTION, "小游");
        sceneMap.put(SCENE_AREA, "小游周边");
        //sceneMap.put(SCENE_NAVIGATE, "旅游路线规划");
        //sceneMap.put(SCENE_NAVIGATE, "车载导航");

        dataList = new ArrayList<Scene>();
        //dataList.add(new Scene(SCENE_SWEEPER_ROBOT , "扫地机器人"));
        dataList.add(new Scene(SCENE_INTRODUCTION , "小游"));
        dataList.add(new Scene(SCENE_AREA , "小游周边"));
        //dataList.add(new Scene(SCENE_NAVIGATE , "旅游路线规划"));
        //dataList.add(new Scene(SCENE_NAVIGATE , "车载导航"));

    }

    private void findView() {
        this.rootRl = (RelativeLayout) findViewById(R.id.root);
        this.titleRl = (RelativeLayout) findViewById(R.id.title_rl);
        this.titleTv = (TextView) findViewById(R.id.title_tv);
        this.messagesList = (MessagesList) findViewById(R.id.messagesList);
        input = (MessageInput) findViewById(R.id.input);

        input.setInputListener(this);
        input.setAudioInputListener(this);
//        hintRV = (RecyclerView) findViewById(R.id.hint_rv);
        titleRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.getPopupWindow().setAnimationStyle(R.style.animTranslate);
                popupWindow.showAtLocation(rootRl, Gravity.BOTTOM, 0, 0);
                WindowManager.LayoutParams lp=getWindow().getAttributes();
                lp.alpha = 0.3f;
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                getWindow().setAttributes(lp);
            }
        });

    }

    /**
     * 为了防止破解app获取ak，sk，建议您把ak，sk放在服务器端。
     */
    private void initAccessToken() {
        APIService.getInstance().init(getApplicationContext());
        APIService.getInstance().initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                accessToken = result.getAccessToken();
                Log.i("MainActivity", "AccessToken->" + result.getAccessToken());
                if (!TextUtils.isEmpty(accessToken)) {
                    resendWaitList();
                }

            }

            @Override
            public void onError(UnitError error) {
                Log.i("wtf", "AccessToken->" + error.getErrorMessage());
            }
        },"T04MjgBs6Yx98AFRe2PHHnbZ", "Kjl3nqYfnUFTmI4UueIEtWHvSfsL8iSd");//"YZhGsnq6wXmlGFHUeIanCOBG", "6cb34ecc1229cc1791fdd3c5be73be7f");
                //"jMWIPDmK6zaxiK9KMCbb0hQl", "D4d5DD51gElVGkoHuoOZZfSHFDDT5K5k");
    }

    /**
     * 重发未发送成功的消息
     */
    private void resendWaitList() {
        for (Message message : waitList) {
            sendMessage(message);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Message message = new Message(String.valueOf(id++), cs, "好久不见，旅行者！", new Date());
        messagesAdapter.addToStart(message, true);
    }


    @Override
    public boolean onSubmit(CharSequence input) {

        Message message = new Message(String.valueOf(id++), sender, input.toString(), new Date());
        messagesAdapter.addToStart(message, true);

        sendMessage(message);
        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        voiceRecognizer.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        voiceRecognizer.onDestroy();
    }
    private void initAdapter() {
        messagesAdapter = new MessagesListAdapter<>(sender.getId(), imageLoader);
        // messagesAdapter.enableSelectionMode(this);
        messagesAdapter.setLoadMoreListener(this);
        messagesAdapter.setOnHintClickListener(new MessagesListAdapter.OnHintClickListener<Message>() {
            @Override
            public void onHintClick(String hint) {
                Message message = new Message(String.valueOf(id++), sender, hint, new Date());
                sendMessage(message);
                messagesAdapter.addToStart(message, true);
            }
        });
        messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        //                        AppUtils.showToast(MainActivity.this,
                        //                                message.getUser().getName() + " avatar click",
                        //                                false);
                    }
                });
        this.messagesList.setAdapter(messagesAdapter);

    }

//    private void initSpeak() {
//        messagesAdapter = new MessagesListAdapter<>(sender.getId(), imageLoader);
//        // messagesAdapter.enableSelectionMode(this);
//        messagesAdapter.setLoadMoreListener(this);
//        messagesAdapter.setOnHintClickListener(new MessagesListAdapter.OnHintClickListener<Message>() {
//            @Override
//            public void onHintClick(String hint) {
//                Message message = new Message(String.valueOf(id++), sender,"需要我为您讲解吗？(点击确认)", new Date());
//                //sendMessage(message);
//                、、mSpeechSynthesizer.speak(res);
//                messagesAdapter.addToStart(message, true);
//            }
//        });
//        messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
//                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
//                    @Override
//                    public void onMessageViewClick(View view, Message message) {
//                        //                        AppUtils.showToast(MainActivity.this,
//                        //                                message.getUser().getName() + " avatar click",
//                        //                                false);
//                    }
//                });
//        this.messagesList.setAdapter(messagesAdapter);
//
//    }
//    private void initEachRound() {
//        messagesAdapter = new MessagesListAdapter<>(sender.getId(), imageLoader);
//        // messagesAdapter.enableSelectionMode(this);
//        messagesAdapter.setLoadMoreListener(this);
//        messagesAdapter.setOnHintClickListener(new MessagesListAdapter.OnHintClickListener<Message>() {
//            @Override
//            public void onHintClick(String hint) {
//                Message message = new Message(String.valueOf(id++), sender, hint, new Date());
//                sendMessage(message);
//                messagesAdapter.addToStart(message, true);
//            }
//        });
//        messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
//                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
//                    @Override
//                    public void onMessageViewClick(View view, Message message) {
//                        //                        AppUtils.showToast(MainActivity.this,
//                        //                                message.getUser().getName() + " avatar click",
//                        //                                false);
//                    }
//                });
//        this.messagesList.setAdapter(messagesAdapter);
//
//    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }

    /**
     * 切换场景
     *
     * @param scene
     */
    private void changeScene(Scene scene) {

        curScene = scene;
        titleTv.setText(scene.getName());

        sessionId = "";
        Message message = new Message(String.valueOf(id++), sender, "你好", new Date());
        sendMessage(message);
    }

    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<Message>() {
            @Override
            public String format(Message message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) {
                    text = "[attachment]";
                }

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }

    private void sendMessage(Message message) {

        if (TextUtils.isEmpty(accessToken)) {
            waitList.add(message);
            return;
        }

        APIService.getInstance().communicate(new OnResultListener<CommunicateResponse>() {
            @Override
            public void onResult(CommunicateResponse result) {

                handleResponse(result);
            }

            @Override
            public void onError(UnitError error) {

            }
        }, curScene.getId(), message.getText(), sessionId);

    }

    private void handleResponse(CommunicateResponse result) {
        if (result != null) {
            sessionId = result.sessionId;
            //  如果有对于的动作action，请执行相应的逻辑
            List<CommunicateResponse.Action> actionList = result.actionList;
            for (CommunicateResponse.Action action : actionList) {
//
//                if (!TextUtils.isEmpty(action.actionId)) {
//                    //StringBuilder sb = new StringBuilder();
//                    //sb.append(action.say);
//
//                    /*
//                    Message message = new Message(String.valueOf(id++), cs, sb.toString(), new Date());
//                    messagesAdapter.addToStart(message, true);
//                    */
//                     if (action.hintList.size() > 0) {
//                        message.setHintList(action.hintList);
//                    }
//
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
    private void sceneKnowledge(CommunicateResponse.Action action,CommunicateResponse result){
        ComWithMyServer aLinker = new ComWithMyServer();
        // 执行自己的业务逻辑,下面我先测试一些例子
        //业务逻辑->景点简介
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //传送的参数
        Map<String,String> aMap = new HashMap<String,String>();
        List<String> aLint = new ArrayList<>();
        //url初始化
        String url = "http://101.236.23.124:8080/polls/jingdian_knowledge?";
        //String url = "http://knowledgeworks.cn:20313/cndbpedia/api/entityAttribute?";
        String attr = "";
        String name = "";
        CommunicateResponse.Qu_res aSlotWord = result.aQures;
        if(aSlotWord.intent_candidate_List.size() > 0){
            name = aSlotWord.intent_candidate_List.get(0);
        }
        if(action == null || "faq_reply_satisfy".equals(action.actionId)){
            Message message = new Message(String.valueOf(id++), cs,"您需要我做什么呢~", new Date());
            messagesAdapter.addToStart(message, true);
        }

        //导航部分
        if("navigation_satisfy".equals(action.actionId) || aNviFlag){
            //String attr = "POSITION";
            url = "http://101.236.23.124:8080/polls/path_arrange_v1?";
            CommunicateResponse.Schema aSchema = result.aSchema;
            //从一轮对话中获得
            boolean flag = false;
            String loc1 = "";
            String loc2 = "";
            //从一轮对话中获得
            if(aSlotWord.intent_candidate_List.size() == 2){
                loc1 = aSlotWord.intent_candidate_List.get(0);
                loc2 = aSlotWord.intent_candidate_List.get(1);
                flag = true;
            }
            //从多轮对话中获得
            else if(aSchema.botMergedSlots.size() > 0){
                //从词槽里面获取起始点
                loc1 = aSlotWord.intent_candidate_List.get(0);
                for(int i=0;i <aSchema.botMergedSlots.size() ;i++) {
                    if (!aSchema.botMergedSlots.get(i).normalized_word.equals(loc1)) {
                        loc2 = aSchema.botMergedSlots.get(i).normalized_word;
                        break;
                    }
                }
                flag = true;
            }

            //如果正确获得地点->进行导航操作
            if(flag){
                aMap.put("origin",loc1);
                aMap.put("destination",loc2);
                PathResponse res =  aLinker.submitPostDataPath(url,aMap,"utf-8");
                if(res == null) return;
                Message message = new Message(String.valueOf(id++), cs,"您好，小游为您本次行程导航。本次行程距离："+res.distance + "米，出发地点:"+loc1+" 目的地: "+loc2, new Date());
                messagesAdapter.addToStart(message, true);
                String steps = "";
                for(int i=0;i < res.stepList.size();i++){
                    steps = steps + "\n" +res.stepList.get(i).stepDeatinationInstrution;
                }
                message = new Message(String.valueOf(id++), cs,steps, new Date());
                message.setHintList(action.hintList);
                messagesAdapter.addToStart(message, true);
                aNviFlag = false;
            }else{
                Message message = new Message(String.valueOf(id++), cs,"很抱歉，请再说一遍好吗？", new Date());
                messagesAdapter.addToStart(message, true);
            }


        }
        //景点介绍
        else if("description_satisfy".equals(action.actionId)){
            //只有创建新的线程才能开启新的网络连接--暂时注释
            attr = "DESC";
            if(aSlotWord.intent_candidate_List.size() > 0 && name.length() != 0){
                aMap.put("propertyy",attr);
                aMap.put("name",name);
                String res  = aLinker.submitPostData(url,aMap,"utf-8");
                //对输出的数据出库
                res = res.replace("\\n","");
                Message message = new Message(String.valueOf(id++), cs,res, new Date());
                //对话引导模块
                //aLint.add("故宫门票");
                //aLint.add("故宫开放时间");
                message.setHintList(action.hintList);
                messagesAdapter.addToStart(message, true);

            }
        }
        //业务逻辑->景点的建设时间
        else if("found_time_satisfy".equals(action.actionId)){
            attr = "FOUND_TIME";
            if(aSlotWord.intent_candidate_List.size() > 0&& name.length() != 0){
                aMap.put("name",name);
                aMap.put("propertyy",attr);
                String res  = aLinker.submitPostData(url,aMap,"utf-8");
                Message message = new Message(String.valueOf(id++), cs,res, new Date());
                message.setHintList(action.hintList);
                messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的门票价格
        else if("ticket_satisfy".equals(action.actionId)){
            attr = "TICKET";
            if(aSlotWord.intent_candidate_List.size() > 0&& name.length() != 0){
                aMap.put("propertyy",attr);
                aMap.put("name",name);
                String res  = aLinker.submitPostData(url,aMap,"utf-8");
                Message message = new Message(String.valueOf(id++), cs,res, new Date());
                message.setHintList(action.hintList);
                messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的开放时间
        else if("time_satisfy".equals(action.actionId)){
            attr = "TIME";
            if(aSlotWord.intent_candidate_List.size() > 0&& name.length() != 0){
                aMap.put("name",name);
                aMap.put("propertyy",attr);
                String res  = aLinker.submitPostData(url,aMap,"utf-8");
                Message message = new Message(String.valueOf(id++), cs,res, new Date());
                message.setHintList(action.hintList);
                messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的著名景点
        else if("famous_satisfy".equals(action.actionId)){
            attr = "FAMOUS";
            if(aSlotWord.intent_candidate_List.size() > 0&& name.length() != 0){
                aMap.put("name",name);
                aMap.put("propertyy",attr);
                String res  = aLinker.submitPostData(url,aMap,"utf-8");
                Message message = new Message(String.valueOf(id++), cs,res, new Date());
                message.setHintList(action.hintList);
                messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的占地面积
        else if("squre_satisfy".equals(action.actionId)){
            attr = "SQURE";
            if(aSlotWord.intent_candidate_List.size() > 0&& name.length() != 0){
                aMap.put("name",name);
                aMap.put("propertyy",attr);
                String res  = aLinker.submitPostData(url,aMap,"utf-8");
                Message message = new Message(String.valueOf(id++), cs,res, new Date());
                message.setHintList(action.hintList);
                messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->景点的游玩位置

        else if("position_satisfy".equals(action.actionId)){
            attr = "POSITION";
            if(aSlotWord.intent_candidate_List.size() > 0&& name.length() != 0){
                aMap.put("name",name);
                aMap.put("propertyy",attr);
                String res  = aLinker.submitPostData(url,aMap,"utf-8");
                Message message = new Message(String.valueOf(id++), cs,res, new Date());
                message.setHintList(action.hintList);
                messagesAdapter.addToStart(message, true);
            }
        }
//业务逻辑->景点的游玩时间
        else if("play_time_satisfy".equals(action.actionId)){
            attr = "PLAY_TIME";
            if(aSlotWord.intent_candidate_List.size() > 0&& name.length() != 0){
                aMap.put("name",name);
                aMap.put("propertyy",attr);
                String res  = aLinker.submitPostData(url,aMap,"utf-8");
                Message message = new Message(String.valueOf(id++), cs,res, new Date());
                message.setHintList(action.hintList);
                messagesAdapter.addToStart(message, true);
            }
        }
        //业务逻辑->导航

        //导航声明,获取更多的词槽
        else if("navigation_user_attraction_clarify".equals(action.actionId)){
            //String attr = "POSITION";

            Message message = new Message(String.valueOf(id++), cs,"请问您是从哪个地方出发呢？", new Date());
            //message.setHintList(action.hintList);
            messagesAdapter.addToStart(message, true);
            aNviFlag = true;
        }
        // 必须从问题抽取特征，不然会获得场景信息,现在是不要场景信息的几日游推荐
        //业务逻辑->几日游推荐
        else if("day_level_plan_satisfy".equals(action.actionId)){
            //String attr = "POSITION";
            url = "http://101.236.23.124:8080/polls/day_level_plan?";
            if(result.aSchema.botMergedSlots.size() > 0){
//                CommunicateResponse.bot_merged_slots aSlotWord1 = result.aSchema.botMergedSlots.get(0);
//                //为了获得几天的信息
//                CommunicateResponse.bot_merged_slots aSlotWord2 = aSlotWord1;
//                if(result.aSchema.botMergedSlots.size()>=3){
//                    aSlotWord2 = result.aSchema.botMergedSlots.get(result.aSchema.botMergedSlots.size()-1);
//                }
                aMap.put("target_place",aSlotWord.original_word_List.get(0));
                aMap.put("days",aSlotWord.original_word_List.get(1));
                String res  = aLinker.submitPostDataDayPlan(url,aMap,"utf-8");
                Message message = new Message(String.valueOf(id++), cs,res, new Date());
                message.setHintList(action.hintList);
                messagesAdapter.addToStart(message, true);

//                if(result.aSchema.botMergedSlots.size()>=3){
//                    Message message = new Message(String.valueOf(id++), cs,"entity="+aSlotWord1.normalized_word+"&play_day="+aSlotWord2.normalized_word, new Date());
//                    messagesAdapter.addToStart(message, true);
//                }else{
//                    Message message = new Message(String.valueOf(id++), cs,"entity="+aSlotWord1.normalized_word+"&play_day="+"0", new Date());
//                    messagesAdapter.addToStart(message, true);
//                }

            }
        }
        //周边推荐的意图澄清
        else if("arround_user_attraction_clarify".equals(action.actionId)){
            Message message = new Message(String.valueOf(id++), cs,"请问您在什么位置呢？", new Date());
//            List<String> aHint = new ArrayList<>();
//            aHint.add("我要吃饭");
//            aHint.add("周边的厕所");
//            message.setHintList(aHint);
            messagesAdapter.addToStart(message, true);
        }
        else if("arround_user_interest_place_clarify".equals(action.actionId)){
            Message message = new Message(String.valueOf(id++), cs,"请问您想去周围哪些设施呢？", new Date());
            List<String> aHint = new ArrayList<>();
            aHint.add("我要吃饭");
            aHint.add("周边的厕所");
            message.setHintList(aHint);
            messagesAdapter.addToStart(message, true);
        }
        //周边推荐->餐馆
        else if("want_satisfy".equals(action.actionId) ){
            //attr = "TIME";
            url = "http://101.236.23.124:8080/polls/surrounding_suggestion?";
            //调用手机GPS获取经纬度信息---------------------------------------------------------------------
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //获取所有可用的位置提供器
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            double longtitude=0; // 经度
            double latitude=0; //维度
            //获取经纬度信息
            if (isNetworkEnabled) {
                String locationProvider = LocationManager.NETWORK_PROVIDER;
                try {
                    Location location = locationManager.getLastKnownLocation(locationProvider);
                    longtitude = location.getLongitude();
                    latitude = location.getLatitude();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }



            //CommunicateResponse.Schema aSchema = result.aSchema;
            //调用手机GPS获取经纬度信息---------------------------------------------------------------------

            if(aSlotWord.intent_candidate_List.size() > 0){
                aMap.put("lng",Double.toString(longtitude));
                aMap.put("lat",Double.toString(latitude));
                aMap.put("target_place","餐馆");
                //aMap.put("propertyy",attr);
                //List<String> aHint = new ArrayList<>();
                //CommunicateResponse.Schema aSchema = result.aSchema;

                AroundResponse res  = aLinker.submitPostDataAround(url,aMap,"utf-8");
                //Message message = new Message(String.valueOf(id++), cs,"lng:"+Double.toString(longtitude)+",lat:"+Double.toString(latitude)+"target_place:"+name, new Date());
                //为周边推荐加入导航信息
                for(AroundResponse.Shop aShop : res.stopList){
                    String ans = "为您推荐商家："+aShop.name+"\n商家服务电话："+aShop.telephone+"\n商家地址："+aShop.address;
                    Message message = new Message(String.valueOf(id++), cs,ans, new Date());
                    //aHint.add("您想前往吗？");
                    //message.setHintList(aHint);
                    messagesAdapter.addToStart(message, true);
                    //aHint.remove(0);
                }
//                Message message = new Message(String.valueOf(id++), cs,"您想前往哪家餐馆？", new Date());
//                message.setHintList(aHint);
//                messagesAdapter.addToStart(message, true);
            }
        }
        //附近推荐厕所 ,, 这两种动作都归为一种
        else if("arround_satisfy".equals(action.actionId) || "arround_user_interest_place_clarify".equals(action.actionId)){
            //attr = "TIME";
            url = "http://101.236.23.124:8080/polls/surrounding_suggestion?";
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //获取所有可用的位置提供器
            //调用手机GPS获取经纬度信息---------------------------------------------------------------------
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            double longtitude=0; // 经度
            double latitude=0; //维度
            //获取经纬度信息
            if (isNetworkEnabled) {
                String locationProvider = LocationManager.NETWORK_PROVIDER;
                try {
                    Location location = locationManager.getLastKnownLocation(locationProvider);
                    //String locationStr = "维度：" + location.getLatitude() +"\n"+ "经度：" + location.getLongitude();
                    longtitude = location.getLongitude();
                    latitude = location.getLatitude();
// Toast.makeText(MainActivity.this, locationStr, Toast.LENGTH_LONG).show();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
            //调用手机GPS获取经纬度信息---------------------------------------------------------------------
            if(aSlotWord.intent_candidate_List.size() > 0){
                aMap.put("lng",Double.toString(longtitude));
                aMap.put("lat",Double.toString(latitude));
                aMap.put("target_place","厕所");
                //aMap.put("propertyy",attr);

                AroundResponse res  = aLinker.submitPostDataAround(url,aMap,"utf-8");
                //Message message = new Message(String.valueOf(id++), cs,"lng:"+Double.toString(longtitude)+",lat:"+Double.toString(latitude)+"target_place:"+name, new Date());
                //Message message = new Message(String.valueOf(id++), cs,res, new Date());
                //messagesAdapter.addToStart(message, true);
                for(AroundResponse.Shop aShop : res.stopList){
                   String ans = "为您推荐最近的卫生间："+aShop.name+"\n地址："+aShop.address;
                    Message message = new Message(String.valueOf(id++), cs,ans, new Date());
                    messagesAdapter.addToStart(message, true);
                }

            }
        }
    }
    private void initPopupWindow() {
        // get the height of screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 * 2 + 3, metrics);
        // create popup window
        popupWindow = new BasePopupWindow(this, R.layout.scene_list_view, ViewGroup.LayoutParams.MATCH_PARENT,
                height) {
            @Override
            protected void initView() {
                View view=getContentView();
                dataListview = (ListView) view.findViewById(R.id.data_list);
                dataListview.setAdapter(new ArrayAdapter<Scene>(MainActivity.this, R.layout.item_popup_view, dataList));
            }

            @Override
            protected void initEvent() {
                dataListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        popupWindow.getPopupWindow().dismiss();
                        Scene scene = dataList.get(position);
                        changeScene(scene);
                    }
                });
            }

            @Override
            protected void initWindow() {
                super.initWindow();
                PopupWindow instance = getPopupWindow();
                instance.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        WindowManager.LayoutParams lp = getWindow().getAttributes();
                        lp.alpha = 1.0f;
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        getWindow().setAttributes(lp);
                    }
                });
            }
        };
    }

}
