package com.baidu.aip.unit.model;

/**
 * Created by yizhi on 2017/10/16.
 */
import java.util.ArrayList;
import java.util.List;

public class AroundResponse {
    //public String duration;
    //public int distance;
    public List<AroundResponse.Shop> stopList = new ArrayList<>();
    public static class Shop{
        public String name;
        public String telephone;
        public String address;
    }
}
