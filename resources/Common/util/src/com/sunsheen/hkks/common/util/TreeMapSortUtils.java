package com.sunsheen.hkks.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TreeMapSortUtils {
	public static List<Map.Entry<String,String>> treeMapSort(Map<String, String> map){
		//这里将map.entrySet()转换成list
        List<Map.Entry<String,String>> list = new ArrayList<Map.Entry<String,String>>(map.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list,new Comparator<Map.Entry<String,String>>() {
            //降序排序
            @Override
            public int compare(Map.Entry<String, String> o1,
                               Map.Entry<String, String> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
 
        });
		return list ;
	}
	public static List<Map.Entry<String,Object>> treeMapSortObject(Map<String, Object> map){
		//这里将map.entrySet()转换成list
        List<Map.Entry<String,Object>> list = new ArrayList<Map.Entry<String,Object>>(map.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list,new Comparator<Map.Entry<String,Object>>() {
            //降序排序
            @Override
            public int compare(Map.Entry<String, Object> o1,
                               Map.Entry<String, Object> o2) {
                return o2.getValue().toString().compareTo(o1.getValue().toString());
            }
 
        });
		return list ;
	}
}
