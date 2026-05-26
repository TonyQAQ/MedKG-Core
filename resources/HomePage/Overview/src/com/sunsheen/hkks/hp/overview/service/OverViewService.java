package com.sunsheen.hkks.hp.overview.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.Transformers;

import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

/**
 * 
 * @Title: OverViewService
 * @Description: 首页统计信息
 * @author: FengTao
 * @date 2020年9月21日 上午10:07:49
 */
public class OverViewService {
	
	/**
	 * 
	* @Title: statTopInfoService
	* @Description: 顶部统计信息
	* @author: FengTao
	* @date 2020年9月21日 上午10:08:07
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void statTopInfoService( Map<String , Object> retMap , Map<String , Object> params ){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> data = new HashMap<String,Object>() ;
		try{
			data.putAll( (Map<? extends String, ? extends Object>) session.createDySQLQuery("OV.queryTaskStat", params)
							.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() );
			data.putAll( (Map<? extends String, ? extends Object>) session.createDySQLQuery("OV.queryMapStat", params)
					.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ) ;
			data.putAll( (Map<? extends String, ? extends Object>) session.createDySQLQuery("OV.queryEntityAndRelCount", params)
					.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ) ;
			
			retMap.putAll(RetInfo.RETSUCCESS);
			if(data != null && data.size() > 0 ){
				retMap.put("retmsg" , "查询成功") ;
			}else{
				retMap.put("retmsg" , "查询成功,无数据！") ;
			}
			retMap.put("data" , data);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: statMidInfoService
	* @Description: 统计中部信息
	* @author: FengTao
	* @date 2020年9月21日 上午10:08:27
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void statMidInfoService( Map<String , Object> retMap , Map<String , Object> params ){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> markTaskList = new ArrayList<Map<String,Object>>() ;
		List<Map<String,Object>> words = new ArrayList<Map<String,Object>>() ;
		Map<String,Object> data = new HashMap<String,Object>() ;
		try{
			markTaskList = session.createDySQLQuery("OV.queryEntityAndRelCountTop5", params)
								.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			words = session.createDySQLQuery("OV.queryWords", params)
					.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			data.put("words", words) ;
			data.put("markTaskList", markTaskList) ;
			retMap.putAll(RetInfo.RETSUCCESS);
			if(data != null && data.size() > 0 ){
				retMap.put("retmsg" , "查询成功") ;
			}else{
				retMap.put("retmsg" , "查询成功,无数据！") ;
			}
			retMap.put("data" , data);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败，请重试") ;
			e.printStackTrace();
		}
	}

	/**
	 * 
	* @Title: statBottomInfoService
	* @Description: 统计底部信息
	* @author: FengTao
	* @date 2020年9月21日 上午10:08:42
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void statBottomInfoService( Map<String , Object> retMap , Map<String , Object> params ){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> updateTaskList = new ArrayList<Map<String,Object>>() ;
		List<Map<String,Object>> taskStatList = new ArrayList<Map<String,Object>>() ;
		Map<String,Object> taskStat = new HashMap<String,Object>() ;
		List<String> xData = new ArrayList<String>();
		List<String> yData = new ArrayList<String>();
		Map<String,Object> data = new HashMap<String,Object>() ;
		try{
			updateTaskList = session.createDySQLQuery("OV.queryUpdateTaskTop5", params)
								.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			taskStatList = session.createDySQLQuery("OV.taskStatList", params)
					.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			for(Map<String,Object> map : taskStatList){
				xData.add(map.get("stateName")+"") ;
				yData.add(map.get("totalCount")+"") ;
			}
			taskStat.put("xData", xData) ;
			taskStat.put("yData", yData) ;
			data.put("updateTaskList", updateTaskList) ;
			data.put("taskStatList", taskStat) ;
			retMap.putAll(RetInfo.RETSUCCESS);
			if(data != null && data.size() > 0 ){
				retMap.put("retmsg" , "查询成功") ;
			}else{
				retMap.put("retmsg" , "查询成功,无数据！") ;
			}
			retMap.put("data" , data);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败，请重试") ;
			e.printStackTrace();
		}
	}
}
