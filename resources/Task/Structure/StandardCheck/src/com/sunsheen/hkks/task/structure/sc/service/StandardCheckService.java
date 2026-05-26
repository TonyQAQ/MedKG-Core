package com.sunsheen.hkks.task.structure.sc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.ListSortUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.common.util.TreeMapSortUtils;
import com.sunsheen.hkks.task.structure.ssm.entity.Filter;
import com.sunsheen.hkks.task.structure.ssm.entity.SSMEntity;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.system.security.login.Session;
import com.sunsheen.jfids.util.DataBaseUtil;

/**
 * 
 * @Title: StandardCheckService
 * @Description:规则校验
 * @author: FengTao
 * @date 2020年7月28日 下午6:47:03
 */
public class StandardCheckService {
	
	/**
	 * 
	* @Title: saveOrUpdateSC
	* @Description:保存校验数据
	* @author: FengTao
	* @date 2020年7月29日 上午10:25:04
	* @param retMap
	* @param columnsInfo void
	* @version
	 */
	public void saveOrUpdateSC(Map<String,Object> retMap , List<SSMEntity> columnsInfo){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> params = new HashMap<String,Object>() ;
		params.put("userId", Session.getCurrUser().getId() ) ;
		params.put("userName", Session.getCurrUser().getUsername()) ;
		session.beginTransaction();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			int index = 0 ;
			for( SSMEntity entity : columnsInfo ){
				System.out.print("entity ->"+entity);
				params.put("attrMappingId", entity.getAttrMappingId()) ;
				params.put("tableMappingId", entity.getTableMappingId()) ;
				params.put("attrAlias", entity.getAttrAlias()) ;
				if(index < 1){//遍历第一次，则先删除以前的规则信息，更新以前的别名信息
					session.createDySQLQuery("StandardCheckDao.deleteFilterByMapId", params).executeUpdate() ;
				}
				if(!StringUtils.isEmptyOrWhitespaceOnly(entity.getAttrAlias())){
					//清空以前数据后，添加此次的数据信息
					Object num =  session.createDySQLQuery("StandardCheckDao.UpdateAilasByMapId", params).executeUpdate() ;
					if("0".equals(num.toString())){
						session.rollback();
						retMap.putAll(RetInfo.RETFAIL) ;
						return ;
					}	
				}
				Map<String,Object> temp = new HashMap<String,Object>() ;
				for(Filter filter : entity.getFilterList() ){
					temp.put(filter.getFilterId()+"AA-AA"+filter.getFilterValue(), filter.getFilterValue()) ;
				}
				for(Map.Entry<String, Object> map : temp.entrySet()){
					params.put("filterId", map.getKey().split("AA-AA")[0]) ;
					params.put("filterValue", map.getValue()) ;
					Object filterNum =  session.createDySQLQuery("StandardCheckDao.insertFilter", params).executeUpdate() ;
					if("0".equals(filterNum.toString())){
						session.rollback();
						retMap.putAll(RetInfo.RETFAIL) ;
						return ;
					}
				}
				index++ ;
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}
			session.commit();
		}catch(Exception e){
			session.rollback();
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: getTables
	* @Description: 获取表信息
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getTables(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			data = session.createDySQLQuery("StandardCheckDao.selectCheckTables", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			Object litleStateCode = session.createDySQLQuery("TaskMT.queryExeState", params).uniqueResult() ;
			System.out.print("litleStateCode"+litleStateCode);
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("litleStateCode", litleStateCode) ;
			retMap.put("data", data) ;
			for(Map<String,Object> temp : data){
				temp.put("isEdit", false) ;
			}
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
		}catch(Exception e){
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: getColumns
	* @Description:获取字段信息
	* @author: FengTao
	* @date 2020年7月29日 上午8:55:35
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getColumns(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("StandardCheckConfig.checkColumnsTitle")));
		try{
			data = session.createDySQLQuery("StandardCheckDao.selectCheckColumns", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			int sequence = 0 ;
			for(Map<String,Object> temp : data){
				//为表格添加数据信息
				temp.put("sequence", ++sequence) ;
				temp.put("rowKey", temp.get("attrMappingId")) ;
				
				//解析规则数据信息
				String attrFilters = (String)temp.get("filterList") ;
				List<Map<String,Object>> filterList = new ArrayList<Map<String,Object>>() ;
				if(!StringUtils.isEmptyOrWhitespaceOnly(attrFilters)){
					String[] filters = attrFilters.split("--A--") ;//每个过滤规则
					for(String str : filters){
						Map<String,Object> filterMap = new HashMap<String,Object>() ;
						String[] filter = str.split("AA-AA") ;//过滤规则
						if(filter.length > 4){
							filterMap.put("relId", filter[0]) ;
							filterMap.put("filterId", filter[1]) ;
							filterMap.put("filterValue", filter[2]) ;
							filterMap.put("typeCode", filter[3]) ;
							filterMap.put("filterName", filter[4]) ;
							filterMap.put("filterTip", filter[4]+filter[2]) ;
							filterList.add(filterMap) ;
						}
						
					}
				}
				temp.put("filterList", filterList) ;
				
				//查询并解析数据过滤规则信息
				//统计，并且获取排序后的list
				String attrTmpFilters= (String)temp.get("filterTmpList") ;
				temp.put("titleHead", JSONArray.fromObject(Configs.get("StandardCheckConfig.filterTmpListTitle")));
				List<Map<String,Object>> filterTmpOriList = new ArrayList<Map<String,Object>>() ;
				if(!StringUtils.isEmptyOrWhitespaceOnly(attrTmpFilters)){
					String[] filters = attrTmpFilters.split("--A--") ;//每个过滤规则
					for(String str : filters){
						Map<String,Object> filterMap = new HashMap<String,Object>() ;
						String[] filter = str.split("AA-AA") ;//过滤规则
						if(filter.length > 4){
							filterMap.put("filterId", filter[1]) ;
							filterMap.put("filterValue", filter[2]) ;
							filterMap.put("typeCode", filter[3]) ;
							filterMap.put("filterName", filter[4]) ;
							filterMap.put("typeName", filter[5]) ;
							filterMap.put("count", "0") ;
							filterTmpOriList.add(filterMap) ;
						}
						
					}
				}
				temp.put("filterTmpList", statAndSortList(filterTmpOriList)) ;
				
				//解析别名信息
				String attrAliasList = (String)temp.get("aliasList") ;
				List<Map<String,String>> aliasList = new ArrayList<Map<String,String>>() ;
				Map<String,String> filterMapTemp = new TreeMap<String,String>() ;
				if(!StringUtils.isEmptyOrWhitespaceOnly(attrAliasList)){
					String[] filters = attrAliasList.split("--A--") ;//每个过滤规则
					for(String str : filters){
						if(filterMapTemp.containsKey(str.split("AA-AA")[0])){
							filterMapTemp.put(str.split("AA-AA")[0], (Integer.parseInt(filterMapTemp.get(str.split("AA-AA")[0]))+1)+"") ;
						}else{
							filterMapTemp.put(str.split("AA-AA")[0], 1+"" ) ;
						}
					}
				}
				for(Map.Entry<String,String> map : TreeMapSortUtils.treeMapSort(filterMapTemp)){
					Map<String,String> filterTemp = new HashMap<String,String>() ;
					filterTemp.put("code", map.getKey()) ;
					filterTemp.put("value", map.getKey()+"("+map.getValue()+"人)") ;
					aliasList.add(filterTemp) ;
				}
				temp.put("aliasList", aliasList) ;
				
			}
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
			retMap.put("data", data) ;
		}catch(Exception e){
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: getPulishMsg
	* @Description: 获取发布确认消息
	* @author: FengTao
	* @date 2020年7月29日 上午10:16:05
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getPulishMsg(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> temp = new HashMap<String , Object>() ;
		Map<String , Object> data = new HashMap<String , Object>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			data = (Map<String, Object>) session.createDySQLQuery("StandardCheckDao.queryTaskPublishMsg", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(data != null && data.size() > 0 && !"0".equals((String)data.get("totalCount")) ){
				temp.put("publishState","00");
				temp.put("publishComfirm", "以下【表】还有未处理【别名】和【数据过滤】的字段，是否确认提交吗？<br>"+data.get("publishMsg")) ;
			}else{
				temp.put("publishState","00");
				temp.put("publishComfirm", "发布后不可撤回，确认发布吗？") ;
			}
			retMap.put("data", temp) ;	
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: getStatData
	* @Description: 获取统计信息
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getStatData(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> columnsData = new HashMap<String,Object>() ;
		Map<String,Object> data = new HashMap<String,Object>() ;
		try{
			columnsData = (Map<String, Object>) session.createDySQLQuery("StandardCheckDao.queryStatColumnData", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(columnsData!=null && columnsData.size() > 0){
				Integer totalCount = Integer.parseInt((String) columnsData.get("totalCount")) ;
				Integer selectCount = Integer.parseInt((String) columnsData.get("checkCount")) ;
				data.put("columnTotalCount", totalCount) ;
				data.put("columnCheckCount", selectCount) ;
				data.put("columnNoCheckCount", totalCount-selectCount ) ;
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", data) ;
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
		}catch(Exception e){
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: statAndSortList
	* @Description:
	* @author: FengTao
	* @date 2020年7月30日 上午9:58:24
	* @param filterList 统计前的数据
	* @param filterTmpList 统计后的数据
	* @version
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String,Object>> statAndSortList( List<Map<String,Object>> filterList){
		
		Set<Map<String,Object>> mapSet = new HashSet<Map<String,Object>>() ;
		mapSet.addAll(filterList) ;
		//统计数据
		for(Map<String,Object> setMap : mapSet){
			for(Map<String,Object> temp : filterList){
				if(setMap.get("filterId").equals(temp.get("filterId")) && 
							setMap.get("filterValue").equals(temp.get("filterValue")) && 
											setMap.get("typeCode").equals(temp.get("typeCode"))){
					setMap.put("count", (Integer.parseInt(setMap.get("count").toString())+1)+"" ) ;
				}
			}
		}
		filterList.clear();
		filterList = new ArrayList(mapSet);
		ListSortUtils.sortByValueDesc(filterList, "count");
		ListSortUtils.sortByValueAsc(filterList, "typeCode");
		for(Map<String,Object> temp : filterList){
			temp.put("count", temp.get("count")+" 人") ;
		}
		return filterList ;
	}
}
