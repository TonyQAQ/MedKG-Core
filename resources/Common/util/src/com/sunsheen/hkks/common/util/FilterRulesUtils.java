package com.sunsheen.hkks.common.util;

import java.util.List;
import java.util.Map;

import com.mysql.cj.util.StringUtils;


public class FilterRulesUtils {
	/**
	 * 
	* @Title: analysisFilter
	* @Description: 将规则转换为sql语句
	* @author: FengTao
	* @date 2020年8月18日 上午10:05:56
	* @param attr
	* @param filter
	* @return String
	* @version
	 */
	public static String analysisFilter(String attr, Map<String, Object> filter) {
        String ret = "";
        String method = filter.get("method").toString();
        String value = filter.get("filterValue").toString();
        switch (method) {
            case "analysisMoreThan":
                ret = attr + " > " + value;
                break;
            case "analysisLessThan":
                ret = attr + " < " + value;
                break;
            case "analysisNoMoreThan":
                ret = attr + " <= " + value;
                break;
            case "analysisNoLessThan":
                ret = attr + " >= " + value;
                break;
            case "analysisEqual":
                ret = attr + " = " + value;
                break;
            case "analysisNoEqual":
                ret = attr + " <> " + value;
                break;
            case "analysisIn":
                ret = attr + " LIKE '%" + value + "%' ";
                break;
            case "analysisNoIn":
                ret = attr + " NOT LIKE '%" + value + "%' ";
                break;
        }
        return ret;
    }

	public static String linkFilters(String attr, List<Map<String, Object>> filter){
		String filterSql = "" ;
		for(Map<String, Object> temp : filter){
			filterSql += " "+ FilterRulesUtils.analysisFilter(attr , temp) +" and";
		}
		if(!StringUtils.isEmptyOrWhitespaceOnly(filterSql)){
			filterSql = filterSql.substring(0, filterSql.length()-3) ;
		}
		return  filterSql;
	}
	
}
