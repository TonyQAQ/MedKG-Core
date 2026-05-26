package com.sunsheen.hkks.map.mgr.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;
import com.sunsheen.jfids.util.StringUtil;

/**
 * 
 * @Title: MapManagerService
 * @Description: 图谱维护Service
 * @author: FengTao
 * @date 2020年8月7日 上午9:43:47
 */
public class MapManagerService {
	
	/**
	 * 
	* @Title: getMapListByUserId
	* @Description:
	* @author: FengTao
	* @date 2020年8月7日 下午3:32:47
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getMapListByUserId(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("MapConfigs.MapTitle")));
		try{
			data = session.createDySQLQuery("Map.queryMapList", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETSUCCESS);
			if(data != null && data.size() > 0){
				int sequence = 0 ;
				for(Map<String,Object> temp : data){
					temp.put("sequence", ++sequence) ;
					temp.put("rowKey", temp.get("taskId")) ;
				}
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL);
		}
	}
	
	/**
	 * 
	* @Title: getTimer
	* @Description:获取任务类型，任务状态码表信息
	* @author: FengTao
	* @date 2020年7月14日 下午4:15:33
	* @param retMap void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getStateCodeTable(Map<String , Object> retMap){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String , Object>> stateList = new ArrayList<Map<String , Object>>() ;
		Map<String , Object> temp = new HashMap<String , Object>() ;
		try{
			stateList = session.createDySQLQuery("Map.queryStateTable", retMap).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			retMap.putAll(RetInfo.RETSUCCESS) ;
			temp.put("stateCodeTable", stateList) ;
			retMap.put("data", temp) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void getTaskMaps(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String , Object>> mapList = new ArrayList<Map<String , Object>>() ;
		try{
//			mapList = session.createDySQLQuery("Map.queryTaskMap", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			
			String s = "气象服务产品图谱,业务标签图谱,气象服务场景图谱";
			String id = "8ab0a7377aa93205017aa94446520001,yewubiaoqianke1122,fakeID";
			String[] split = s.split(",");
			String[] ids = id.split(",");
			
			//TASK_ID: "6804509102dc4ffcb9952e01cd75ddd3"
//			TASK_NAME: "文本标注"
//				TYPE_CODE: "04"
			//111111,222222,333333
			for(int i=0; i<split.length; i++){
				HashMap<String, Object> map = new HashMap<String,Object>();
				map.put("TASK_ID",ids[i]);
				map.put("TASK_NAME", split[i]);
				map.put("TYPE_CODE", "04");
				mapList.add(map);
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", mapList) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}

}
