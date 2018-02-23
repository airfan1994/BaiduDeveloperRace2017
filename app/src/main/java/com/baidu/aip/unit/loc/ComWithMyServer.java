package com.baidu.aip.unit.loc;
import android.util.Log;

import com.baidu.aip.unit.exception.UnitError;
import com.baidu.aip.unit.model.AroundResponse;
import com.baidu.aip.unit.model.PathResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.net.URLEncoder;
import java.io.ByteArrayOutputStream;
/**
 * Created by yizhi on 2017/10/11.
 */

public class ComWithMyServer {
    /*
     * Function  :   发送Post请求到服务器
     * Param     :   params请求体内容，encode编码格式
     */
    public static String submitPostData(String strUrlPath,Map<String, String> params, String encode) {

        byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体
        try {

            //String urlPath = "http://192.168.1.9:80/JJKSms/RecSms.php";
            URL url = new URL(strUrlPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);     //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            //if(response == HttpURLConnection.HTTP_OK) {
            if(true) {
                InputStream inptStream = httpURLConnection.getInputStream();

                //String res = dealResponseResult(inptStream);\
                //字符串处理
                String res =decode(dealResponseResult(inptStream));

                return resProcess(res);                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            //e.printStackTrace();
            return "err: " + e.getMessage().toString();
        }
        return "-1";
    }

    public static PathResponse submitPostDataPath(String strUrlPath,Map<String, String> params, String encode) {

        byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体
        PathResponse resPath = new PathResponse();
        try {

            //String urlPath = "http://192.168.1.9:80/JJKSms/RecSms.php";
            URL url = new URL(strUrlPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);     //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            //if(response == HttpURLConnection.HTTP_OK) {
            if(true) {
                InputStream inptStream = httpURLConnection.getInputStream();

                String res = dealResponseResultPath(inptStream);
                //resPath = res;
                resPath = pathParse(res.substring(1,res.length()-1));
                //字符串处理
                //String res =decode(dealResponseResult(inptStream));

                //return res.substring(1,res.length()-1);
                return resPath;                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
        return null;
    }
    public static String submitPostDataDayPlan(String strUrlPath,Map<String, String> params, String encode) {

        byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体
        try {

            //String urlPath = "http://192.168.1.9:80/JJKSms/RecSms.php";
            URL url = new URL(strUrlPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);     //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            //if(response == HttpURLConnection.HTTP_OK) {
            if(true) {
                InputStream inptStream = httpURLConnection.getInputStream();

                //String res = dealResponseResult(inptStream);\
                //字符串处理
                String res =decode(dealResponseResult(inptStream));

                return res;                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            //e.printStackTrace();
            return "err: " + e.getMessage().toString();
        }
        return "-1";
    }
    public static AroundResponse submitPostDataAround(String strUrlPath,Map<String, String> params, String encode) {

        byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体
        AroundResponse resPath = new AroundResponse();
        try {

            //String urlPath = "http://192.168.1.9:80/JJKSms/RecSms.php";
            URL url = new URL(strUrlPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);     //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            //if(response == HttpURLConnection.HTTP_OK) {
            if(true) {
                InputStream inptStream = httpURLConnection.getInputStream();

                String res = dealResponseResultPath(inptStream);
                //resPath = res;
                resPath = aroundParse("{\"results\":"+res+"}");
                //字符串处理
                //String res =decode(dealResponseResult(inptStream));

                //return res.substring(1,res.length()-1);
                return resPath;                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
        return null;
    }

    public static String resProcess(String rawRes){
        String res = rawRes.substring(2,rawRes.length()-2);
        return res;
    }
    /*
     * Function  :   封装请求体信息
     * Param     :   params请求体内容，encode编码格式
     */
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    public static PathResponse pathParse(String json){
        Log.e("xx", "CommunicateParser:" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

//            if (jsonObject.has("error_code")) {
//                UnitError error = new UnitError(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
//                throw error;
//            }
            //算了把所有字段解析出来好了
            PathResponse result = new PathResponse();
            result.distance = jsonObject.optInt("distance");

            List<PathResponse.Step> stepList = result.stepList;
            JSONArray actionListArray = jsonObject.optJSONArray("steps");
            if (actionListArray != null) {
                for (int i = 0; i < actionListArray.length(); i++) {
                    JSONObject actionListObject = actionListArray.optJSONObject(i);
                    if (actionListObject == null) {
                        continue;
                    }
                    PathResponse.Step aStep = new PathResponse.Step();
                    aStep.stepDeatinationInstrution = actionListObject.optString("stepDestinationInstruction");
                    result.stepList.add(aStep);
                }
            }
            //导入词槽部分

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            //UnitError error = new UnitError(UnitError.ErrorCode.JSON_PARSE_ERROR, "Json parse error:" + json, e);
            //throw error;
        }
        return null;
    }

    public static AroundResponse aroundParse(String json){
        Log.e("xx", "CommunicateParser:" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

//            if (jsonObject.has("error_code")) {
//                UnitError error = new UnitError(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
//                throw error;
//            }
            //算了把所有字段解析出来好了
            AroundResponse result = new AroundResponse();
            //result.distance = jsonObject.optInt("distance");

            //List<PathResponse.Step> stepList = result.stepList;
            JSONArray actionListArray = jsonObject.optJSONArray("results");
            if (actionListArray != null) {
                for (int i = 0; i < actionListArray.length(); i++) {
                    JSONObject actionListObject = actionListArray.optJSONObject(i);
                    if (actionListObject == null) {
                        continue;
                    }
                    AroundResponse.Shop aStop = new AroundResponse.Shop();
                    aStop.name = actionListObject.optString("name");
                    aStop.telephone = actionListObject.optString("telephone");
                    aStop.address = actionListObject.optString("address");
                    // /aStop.stepDeatinationInstrution = actionListObject.optString("stepDestinationInstruction");
                    result.stopList.add(aStop);
                }
            }
            //导入词槽部分

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            //UnitError error = new UnitError(UnitError.ErrorCode.JSON_PARSE_ERROR, "Json parse error:" + json, e);
            //throw error;
        }
        return null;
    }
    /*
     * Function  :   处理服务器的响应结果（将输入流转化成字符串）
     * Param     :   inputStream服务器的响应输入流
     */
    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }
    public static String dealResponseResultPath(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }
    public static String decode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuffer retBuf = new StringBuffer();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                if ((i < maxLoop - 5)
                        && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr
                        .charAt(i + 1) == 'U')))
                    try {
                        retBuf.append((char) Integer.parseInt(
                                unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(unicodeStr.charAt(i));
                    }
                else
                    retBuf.append(unicodeStr.charAt(i));
            } else {
                retBuf.append(unicodeStr.charAt(i));
            }
        }
        return retBuf.toString();
    }


}
