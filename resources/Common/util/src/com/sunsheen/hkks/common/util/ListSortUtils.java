package com.sunsheen.hkks.common.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ListSortUtils {
	public static void sortByValueAsc(List<Map<String,Object>> list , String key){
		Collections.sort(list,new Comparator<Map<String,Object>>() {
            //类型升序排序
            @Override
            public int compare(Map<String, Object> o1,
                               Map<String, Object> o2) {
                return ((String)o1.get(key)).compareTo((String)o2.get(key));
            }
 
        });
	}
	public static void sortByValueDesc(List<Map<String,Object>> list , String key){
		Collections.sort(list,new Comparator<Map<String,Object>>() {
            //类型降序排序
            @Override
            public int compare(Map<String, Object> o1,
                               Map<String, Object> o2) {
                return ((String)o2.get(key)).compareTo((String)o1.get(key));
            }
 
        });
	}
	public static void sortListStrByValueAsc(List<String> list){
		Collections.sort(list,new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if(o1 == null || o2 == null){
                    return -1;
                }
            	o1 = o1.split("\\[")[1] ; 
            	o1 = o1.split("\\]")[0] ;
            	o2 = o2.split("\\[")[1] ; 
            	o2 = o2.split("\\]")[0] ; 
                if( Integer.parseInt(o1) > Integer.parseInt(o2) ){
                    return 1;
                }
                if( Integer.parseInt(o1) < Integer.parseInt(o2) ){
                    return -1;
                }
                if( Integer.parseInt(o1) == Integer.parseInt(o2)  ){
                    return 0;
                }
                return 0;
            }
        });
	}
	public static void sortListByValueAsc(List<String> list){
		Collections.sort(list,new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if(o1 == null || o2 == null){
                    return -1;
                }
                if( Integer.parseInt(o1) > Integer.parseInt(o2) ){
                    return 1;
                }
                if( Integer.parseInt(o1) < Integer.parseInt(o2) ){
                    return -1;
                }
                if( Integer.parseInt(o1) == Integer.parseInt(o2)  ){
                    return 0;
                }
                return 0;
            }
        });
	}
}
