package com.sunsheen.hkks.common.util;

import java.util.Map;

import com.mysql.cj.util.StringUtils;

/**
 * 
 * @ClassName: PageParamsUtils  
 * @Description: 分页参数信息 
 * @author FengTao
 * @date 2020年7月7日  
 *
 */
public class PageParamsUtils {
	/**
	 * 
	 * @Title: pageParamsDetail  
	 * @Description: map类型的分页参数封装  
	 * @param @param params
	 * @param @param pageCountTemp
	 * @param @param pageSizeTemp
	 * @param @param totalCountTemp    参数  
	 * @return void    返回类型  
	 * @throws
	 */
	public static void pageParamsDetail(Map<String,Object> params , String pageCountTemp , String pageSizeTemp , String totalCountTemp){
		if(StringUtils.isEmptyOrWhitespaceOnly(pageSizeTemp)){
			pageSizeTemp = "10" ;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(pageCountTemp)){
			pageCountTemp = "1" ;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(totalCountTemp)){
			totalCountTemp = "0" ;
		}
		int pageCount = Integer.parseInt(pageCountTemp) ;
		int pageSize = Integer.parseInt(pageSizeTemp) ;
		int totalCount = Integer.parseInt(totalCountTemp) ;
		int pageIndex = (   ((pageCount-1)*pageSize)> totalCount ? (totalCount/pageSize) :  (pageCount-1)  )*pageSize;
		params.put("pageCount", pageCount) ;
		params.put("pageSize", pageSize) ;
		params.put("pageIndex", pageIndex) ;
	}
}
