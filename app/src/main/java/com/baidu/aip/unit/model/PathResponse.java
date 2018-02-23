package com.baidu.aip.unit.model;

/**
 * Created by yizhi on 2017/10/16.
 */
import java.util.ArrayList;
import java.util.List;
public class PathResponse {
    //public String duration;
    public int distance;
    public List<PathResponse.Step> stepList = new ArrayList<>();
    public static class Step{
        public String stepDeatinationInstrution;
    }
}
