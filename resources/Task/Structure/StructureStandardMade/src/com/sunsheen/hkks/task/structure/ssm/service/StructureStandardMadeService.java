package com.sunsheen.hkks.task.structure.ssm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.structure.ssm.entity.Filter;
import com.sunsheen.hkks.task.structure.ssm.entity.SSMEntity;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.system.security.login.Session;
import com.sunsheen.jfids.util.DataBaseUtil;

@SuppressWarnings("serial")
public class StructureStandardMadeService {

	
	public final Set<String> fuzzy = new HashSet<String>(){
		{
			add("varchar") ;
			add("varchar2") ;
			add("char") ;
			add("longtext") ;
			add("tinytext") ;
			add("text") ;
		}
	}; //模糊匹配
	
	public final Set<String> range = new HashSet<String>(){
		{
			add("int") ;
			add("tinyint") ;
			add("smallint") ;
			add("mediumint") ;
			add("number") ;
			add("long") ;
			add("float") ;
			add("double") ;
			add("integer") ;
			add("tinyint") ;
			add("bigint") ;
			add("decimal") ;
			//add("datetime") ;
			//add("date") ;
			//add("datetime") ;
			//add("timestamp") ;
		}
	}; //取值范围
	
	/**
	 * 
	* @Title: saveOrUpdateSSM
	* @Description: 保存表字段别名信息
	* @author: FengTao
	* @date 2020年7月27日 下午4:22:19
	* @param retMap
	* @param columnsInfo void
	* @version
	 */
	public void saveOrUpdateSSM(Map<String,Object> retMap , List<SSMEntity> columnsInfo){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> params = new HashMap<String,Object>() ;
		params.put("userId", Session.getCurrUser().getId() ) ;
		params.put("userName", Session.getCurrUser().getUsername()) ;
		session.beginTransaction();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			int index = 0 ;
			for( SSMEntity entity : columnsInfo ){
				params.put("attrMappingId", entity.getAttrMappingId()) ;
				params.put("tableMappingId", entity.getTableMappingId()) ;
				params.put("attrAlias", entity.getAttrAlias()) ;
				if(index < 1){//遍历第一次，则先删除以前的数据信息，
					session.createDySQLQuery("StructureStandardMadeDao.deleteFilterByMapId", params).executeUpdate() ;
					session.createDySQLQuery("StructureStandardMadeDao.deleteAilasByMapId", params).executeUpdate() ;
				}
				if(!StringUtils.isEmptyOrWhitespaceOnly(entity.getAttrAlias())){
					//清空以前数据后，添加此次的数据信息
					Object num =  session.createDySQLQuery("StructureStandardMadeDao.insertAilas", params).executeUpdate() ;
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
					Object filterNum =  session.createDySQLQuery("StructureStandardMadeDao.insertFilter", params).executeUpdate() ;
					if("0".equals(filterNum.toString())){
						session.rollback();
						retMap.putAll(RetInfo.RETFAIL) ;
						return ;
					}
				}
				index++ ;
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}
			session.flush();
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
			data = session.createDySQLQuery("StructureStandardMadeDao.selectExeTables", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			Object litleStateCode = session.createDySQLQuery("TaskMT.queryExeStateByUserId", params).uniqueResult() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", data) ;
			retMap.put("litleStateCode", litleStateCode) ;
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
		
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("TaskSSM.ColumnsTitle")));
		try{
			data = session.createDySQLQuery("StructureStandardMadeDao.selectExeColumns", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", data) ;
			int sequence = 0 ;
			for(Map<String,Object> temp : data){
				String attrFilters = (String)temp.get("attrFilters") ;
				List<Map<String,Object>> filterList = new ArrayList<Map<String,Object>>() ;
				if(!StringUtils.isEmptyOrWhitespaceOnly(attrFilters)){
					String[] filters = attrFilters.split("--A--") ;//每个过滤规则
					for(String str : filters){
						Map<String,Object> filterMap = new HashMap<String,Object>() ;
						String[] filter = str.split("AA-AA") ;//过滤规则
						filterMap.put("relId", filter[0]) ;
						filterMap.put("filterId", filter[1]) ;
						filterMap.put("filterValue", filter[2]) ;
						filterMap.put("typeCode", filter[3]) ;
						filterMap.put("filterName", filter[4]) ;
						filterMap.put("filterTip", filter[4]+filter[2]) ;
						filterList.add(filterMap) ;
					}
				}
				temp.put("filterList", filterList) ;
				temp.put("sequence", ++sequence) ;
				temp.put("rowKey", temp.get("attrId")) ;
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
		Map<String,Object> tableData = new HashMap<String,Object>() ;
		Map<String,Object> columnsData = new HashMap<String,Object>() ;
		Map<String,Object> data = new HashMap<String,Object>() ;
		try{
			tableData = (Map<String, Object>) session.createDySQLQuery("StructureStandardMadeDao.queryStatTableData", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(tableData!=null && tableData.size() > 0){
				Integer totalCount = Integer.parseInt((String) tableData.get("totalCount")) ;
				Integer selectCount = Integer.parseInt((String) tableData.get("standardCount")) ;
				data.put("tableTotalCount", totalCount) ;
				data.put("tableStandardCount", selectCount) ;
				data.put("tableNoStandardCount", totalCount-selectCount ) ;
			}
			columnsData = (Map<String, Object>) session.createDySQLQuery("StructureStandardMadeDao.queryStatColumnData", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(columnsData!=null && columnsData.size() > 0){
				Integer totalCount = Integer.parseInt((String) columnsData.get("totalCount")) ;
				Integer selectCount = Integer.parseInt((String) columnsData.get("standardCount")) ;
				data.put("columnTotalCount", totalCount) ;
				data.put("columnStandardCount", selectCount) ;
				data.put("columnNoStandardCount", totalCount-selectCount ) ;
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
	* @Title: getFilterCodeTable
	* @Description: 获取规则过滤码表信息
	* @author: FengTao
	* @date 2020年7月24日 下午5:04:45
	* @param retMap void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getFilterCodeTable(Map<String , Object> retMap,Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String , Object>> stateList = new ArrayList<Map<String , Object>>() ;
		List<Map<String , Object>> typeList = new ArrayList<Map<String , Object>>() ;
		Map<String , Object> temp = new HashMap<String , Object>() ;
		try{
			//根据类型，获取类型信息
			String attrType = (String)params.get("attrType") ;
			String typeCode = "" ;
			if(!StringUtils.isEmptyOrWhitespaceOnly(attrType)){
				if(range.contains(attrType.toLowerCase())){
					typeCode += "01','" ;
				}
				if(fuzzy.contains(attrType.toLowerCase())){
					typeCode += "02','" ;
				}
				if(!range.contains(attrType.toLowerCase()) && !fuzzy.contains(attrType.toLowerCase())){
					typeCode += "00','" ;
				}
				if(!StringUtils.isEmptyOrWhitespaceOnly(typeCode)){
					typeCode = typeCode.substring(0, typeCode.length()-3) ;
				}
			}
			params.put("typeCode", typeCode) ;
			typeList = session.createDySQLQuery("StructureStandardMadeDao.getFilterType", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			Map<String , Object> stateListMap = new HashMap<String , Object>() ;
			for( Map<String,Object> map : typeList){
				params.put("code", map.get("code")) ;
				stateList = session.createDySQLQuery("StructureStandardMadeDao.queryFilterCodeTable", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
				stateListMap.put((String)map.get("code") , stateList ) ;
			}
			temp.put("filterCodeTable", stateListMap) ;
			temp.put("typeCodeTable", typeList) ;
			retMap.put("data", temp) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	/**
	 * 
	* @Title: getSubmitMsg
	* @Description:获取提交确认消息
	* @author: FengTao
	* @date 2020年7月22日 下午1:00:14
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getSubmitMsg(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> temp = new HashMap<String , Object>() ;
		Map<String , Object> data = new HashMap<String , Object>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			data = (Map<String, Object>) session.createDySQLQuery("StructureStandardMadeDao.queryTaskSubmitMsg", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(data != null && data.size() > 0 && !"0".equals((String)data.get("totalCount")) ){
				temp.put("publishState","00");
				temp.put("publishComfirm", "以下【表】尚未处理【字段别名】和【数据过滤规则】，是否确认提交吗？<br>"+data.get("publishMsg")) ;
			}else{
				temp.put("publishState","00");
				temp.put("publishComfirm", "提交后不可撤回，确认提交吗？") ;
			}
			retMap.put("data", temp) ;	
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
	}
}
