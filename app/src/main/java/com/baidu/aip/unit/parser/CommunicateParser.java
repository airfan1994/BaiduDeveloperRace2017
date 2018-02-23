/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.unit.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.aip.unit.exception.UnitError;
import com.baidu.aip.unit.model.CommunicateResponse;
import com.baidu.aip.unit.model.PathResponse;
import com.baidu.aip.unit.model.PathResponse;

import android.util.Log;

public class CommunicateParser implements Parser<CommunicateResponse> {

    @Override
    public CommunicateResponse parse(String json) throws UnitError {
        Log.e("xx", "CommunicateParser:" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("error_code")) {
                UnitError error = new UnitError(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
                throw error;
            }

            CommunicateResponse result = new CommunicateResponse();
            result.setLogId(jsonObject.optLong("log_id"));
            result.setJsonRes(json);

            //解析action
            JSONObject resultObject = jsonObject.getJSONObject("result");
            List<CommunicateResponse.Action> actionList = result.actionList;
            JSONArray actionListArray = resultObject.optJSONArray("action_list");
            if (actionListArray != null) {
                for (int i = 0; i < actionListArray.length(); i++) {
                    JSONObject actionListObject = actionListArray.optJSONObject(i);
                    if (actionListObject == null) {
                        continue;
                    }
                    CommunicateResponse.Action action = new CommunicateResponse.Action();
                    action.actionId = actionListObject.optString("action_id");
                    JSONObject actionTypeObject = actionListObject.optJSONObject("action_type");

                    action.actionType = new CommunicateResponse.ActionType();
                    action.actionType.target = actionTypeObject.optString("act_target");
                    action.actionType.targetDetail = actionTypeObject.optString("act_target_detail");
                    action.actionType.type = actionTypeObject.optString("act_type");
                    action.actionType.typeDetail = actionTypeObject.optString("act_type_detail");

                    action.confidence = actionListObject.optInt("confidence");
                    action.say = actionListObject.optString("say");

                    JSONArray hintListArray = actionListObject.optJSONArray("hint_list");
                    if (hintListArray != null) {
                        for (int j = 0; j < hintListArray.length(); j++) {
                            JSONObject hintQuery =  hintListArray.optJSONObject(j);
                            if (hintQuery != null) {
                                action.hintList.add(hintQuery.optString("hint_query"));
                            }
                        }
                    }

                    actionList.add(action);
                }
            }
            //从原始语境中解析词槽-- 我解析 original_word试试
            JSONArray tmpListArray = resultObject.getJSONObject("qu_res").optJSONArray("intent_candidates");
            if(tmpListArray != null){
                JSONArray queListArray = tmpListArray.optJSONObject(0).optJSONArray("slots");
                if(queListArray != null){
                    for(int i=0;i < queListArray.length();i++){
                        JSONObject queListObject = queListArray.optJSONObject(i);
                        //先获取标准的单词
                        String tmp = queListObject.optString("normalized_word");
                        if(tmp == null ||tmp.length() == 0){
                            //如果没有标准的单词，原始的单词也行
                            tmp = queListObject.optString("original_word");
                        }
                        result.aQures.intent_candidate_List.add(tmp);
                        result.aQures.original_word_List.add(queListObject.optString("original_word"));
                    }

                }
            }

            //导入词槽部分-包含上下文语境
            CommunicateResponse.Schema aSchema = new CommunicateResponse.Schema();
            JSONObject aRawSchema  = resultObject.getJSONObject("schema");
            if(aRawSchema != null){
                List<CommunicateResponse.bot_merged_slots> botMergedSlots = new ArrayList<>();
                JSONArray mergedListArray = aRawSchema.optJSONArray("bot_merged_slots");
                //CommunicateResponse.bot_merged_slots aSingleMerged;
                if(mergedListArray != null){
                    for(int i=0;i < mergedListArray.length();i++) {
                        JSONObject aMerged = mergedListArray.optJSONObject(i);
                        if (aMerged != null) {
                            CommunicateResponse.bot_merged_slots aSingleMerged = new CommunicateResponse.bot_merged_slots();
                            aSingleMerged.begin = aMerged.optInt("begin");
                            aSingleMerged.confidence = aMerged.optInt("confidence");
                            aSingleMerged.length = aMerged.optInt("length");
                            aSingleMerged.merge_method = aMerged.optString("merge_method");
                            aSingleMerged.normalized_word = aMerged.optString("normalized_word");
                            aSingleMerged.original_word = aMerged.optString("original_word");
                            aSingleMerged.session_offset = aMerged.optInt("session_offset");
                            aSingleMerged.type = aMerged.optString("type");
                            aSingleMerged.word_type = aMerged.optString("word_type");
                            aSchema.botMergedSlots.add(aSingleMerged);
                        }
                    }
                }

                aSchema.currentQueryInent = aRawSchema.optString("current_qu_intent");
                aSchema.intentConfidence = aRawSchema.optInt("intent_confidence");
            }
            result.aSchema = aSchema;

            result.sessionId = resultObject.optString("session_id");

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            UnitError error = new UnitError(UnitError.ErrorCode.JSON_PARSE_ERROR, "Json parse error:" + json, e);
            throw error;
        }
    }

//    public PathResponse pathParse(String json) throws UnitError {
//        Log.e("xx", "CommunicateParser:" + json);
//        try {
//            JSONObject jsonObject = new JSONObject(json);
//
//            if (jsonObject.has("error_code")) {
//                UnitError error = new UnitError(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
//                throw error;
//            }
//            //算了把所有字段解析出来好了
//            PathResponse result = new PathResponse();
//            result.distance = jsonObject.optInt("distance");
//
//            List<PathResponse.Step> stepList = result.stepList;
//            JSONArray actionListArray = jsonObject.optJSONArray("steps");
//            if (actionListArray != null) {
//                for (int i = 0; i < actionListArray.length(); i++) {
//                    JSONObject actionListObject = actionListArray.optJSONObject(i);
//                    if (actionListObject == null) {
//                        continue;
//                    }
//                    PathResponse.Step aStep = new PathResponse.Step();
//                    aStep.stepDeatinationInstrution = actionListObject.optString("stepDeatinationInstrution");
//                    result.stepList.add(aStep);
//                }
//            }
//            //导入词槽部分
//
//            return result;
//        } catch (JSONException e) {
//            e.printStackTrace();
//            UnitError error = new UnitError(UnitError.ErrorCode.JSON_PARSE_ERROR, "Json parse error:" + json, e);
//            throw error;
//        }
//    }
}
