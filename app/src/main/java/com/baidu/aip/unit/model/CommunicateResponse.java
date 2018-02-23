/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.unit.model;

import java.util.ArrayList;
import java.util.List;


public class CommunicateResponse  extends ResponseResult {

    public List<CommunicateResponse.Action> actionList = new ArrayList<>();

    public String log_id;
    public String msg;

    public Qu_res aQures = new Qu_res();
    public Schema aSchema;
    public String status;
    public String msg_type;
    public String bot_type;
    public String sessionId;


    public static class Qu_res{
        public List<String> intent_candidate_List = new ArrayList<>();
        public List<String> original_word_List = new ArrayList<>();
    }
    public static class single_intent_candidate{
        public String extra_info;
        public String from_who;
        public String func_slot;
        public String intent;
        public int intent_confidence;
        public boolean intent_need_clarify;
        public String match_info;
        public List<CommunicateResponse.slots> aSlotsList = new ArrayList<>();

    }
    public static class slots{
        public int confidence;
        public int length;
        public boolean need_clarify;
        public String normalized_word;
        public int offset;
        public String original_word;
        public String type;
        public String word_type;
    }

    public static class Action {
        public String actionId;
        public ActionType actionType;
        public List argList = new ArrayList<>();
        // public CodeAction codeAction;
        public int confidence;
        public List exeStatusList = new ArrayList<>();
        public List<String> hintList = new ArrayList<String>();
        public String mainExe;
        public String say;
    }

    public static class ActionType {
        public String target;
        public String targetDetail;
        public String type;
        public String typeDetail;
    }

    // public static class CodeAction {}

    public static class Schema {
        public List<CommunicateResponse.bot_merged_slots> botMergedSlots = new ArrayList<>();
        public String currentQueryInent;
        public int intentConfidence;
    }

    public static class bot_merged_slots{
        public int begin;
        public int confidence;
        public int length;
        public String merge_method;
        public String normalized_word;
        public String original_word;
        public int session_offset;
        public String type;
        public String word_type;


    }

}
