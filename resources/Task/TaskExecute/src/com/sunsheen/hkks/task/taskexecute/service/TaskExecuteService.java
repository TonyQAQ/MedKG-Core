package com.sunsheen.hkks.task.taskexecute.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.Transformers;

import net.sf.json.JSONArray;

import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

/**
 * 
 * @Title: TaskExecuteService
 * @Description: 任务执行Service
 * @author: FengTao
 * @date 2020年7月23日 上午9:19:31
 */
public class TaskExecuteService {
	
	
	/**
	 * 
	* @Title: updateTaskState
	* @Description: 更新任务执行流程表,统一更新，执行者使用，只更新每个人的小状态信息
	* @author: FengTao
	* @date 2020年7月22日 上午11:19:23
	* @param retMap
	* @param params void
	* @version
	 */
	public void addExeProcessStateByPreCode(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		try{
			//向流程表添加每个人的任务流程信息
				Object assignId = session.createDySQLQuery("Task.queryUser", params).uniqueResult() ;
				params.put("assignId", assignId) ;
				Object effectNum = session.createDySQLQuery("Task.insertUserProcessExeByPreCode", params).executeUpdate() ;
				if("0".equals(effectNum.toString())){
					session.rollback();
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "提交失败，请重试") ;
				}else{
					retMap.putAll(RetInfo.RETSUCCESS);
				}
				session.commit();
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "提交失败，请重试") ;
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: updateTaskState
	* @Description: 回退任务执行流程表,统一更新，执行者使用，只回退每个人的小状态信息
	* @author: FengTao
	* @date 2020年7月22日 上午11:19:23
	* @param retMap
	* @param params void
	* @version
	 */
	public void addExeProcessBackState(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		try{
			//向流程表添加每个人的任务流程信息
			Object assignId = session.createDySQLQuery("Task.queryUser", params).uniqueResult() ;
			params.put("assignId", assignId) ;
			Object effectNum = session.createDySQLQuery("Task.insertUserBackProcessExe", params).executeUpdate() ;
			if("0".equals(effectNum.toString())){
				session.rollback();
				retMap.putAll(RetInfo.RETFAIL);
				retMap.put("retmsg", "回退失败，请重试") ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS);
			}
			session.commit();
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "回退失败，请重试") ;
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: updateTaskState
	* @Description: 更新任务执行流程表,统一更新，执行者使用，只更新每个人的小状态信息
	* @author: FengTao
	* @date 2020年7月22日 上午11:19:23
	* @param retMap
	* @param params void
	* @version
	 */
	public void addExeProcessState(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		try{
			//向流程表添加每个人的任务流程信息
				Object assignId = session.createDySQLQuery("Task.queryUser", params).uniqueResult() ;
				params.put("assignId", assignId) ;
				Object effectNum = session.createDySQLQuery("Task.insertUserProcessExe", params).executeUpdate() ;
				if("0".equals(effectNum.toString())){
					session.rollback();
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "提交失败，请重试") ;
				}else{
					retMap.putAll(RetInfo.RETSUCCESS);
				}
				session.commit();
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "提交失败，请重试") ;
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: queryTaskExeList
	* @Description: 查询任务状态
	* @author: FengTao
	* @date 2020年7月23日 下午4:19:35
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void queryTaskExeList(Map<String,Object> retMap , Map<String,Object> params ){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String , Object>> dataList = new ArrayList<Map<String , Object>>() ;
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("TaskExecute.taskHead")));
		try{
			Object num = session.createDySQLQuery("TaskExecute.queryExecuteListCount",	params).uniqueResult() ;
			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), num.toString());
			dataList = session.createDySQLQuery("TaskExecute.queryExecuteList",	params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			if(dataList != null && dataList.size() > 0){
				int sequence = 0 ;
				for(Map<String,Object> temp : dataList){
					temp.put("sequence", ++sequence) ;
					temp.put("rowKey", temp.get("taskId")) ;
				}
			}
			retMap.put("data", dataList) ;
			retMap.put("totalCount", num.toString()) ;
			retMap.putAll(RetInfo.RETSUCCESS);
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
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
		List<Map<String , Object>> StructureCodeTable = new ArrayList<Map<String , Object>>() ;
		List<Map<String , Object>> UnStructureCodeTable = new ArrayList<Map<String , Object>>() ;
		List<Map<String , Object>> SemiStructureCodeTable = new ArrayList<Map<String , Object>>() ;
		List<Map<String , Object>> typeList = new ArrayList<Map<String , Object>>() ;
		Map<String , Object> temp = new HashMap<String , Object>() ;
		Map<String , Object> params = new HashMap<String , Object>() ;
		try{
			params.put("typeCode", "01") ;
			StructureCodeTable = session.createDySQLQuery("TaskExecute.queryExeStateTable", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			params.put("typeCode", "02") ;
			UnStructureCodeTable = session.createDySQLQuery("TaskExecute.queryExeStateTable", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			params.put("typeCode", "03") ;
			SemiStructureCodeTable = session.createDySQLQuery("TaskExecute.queryExeStateTable", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			typeList = session.createDySQLQuery("TaskExecute.queryTypeTable", retMap).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			retMap.putAll(RetInfo.RETSUCCESS) ;
			temp.put("StructureCodeTable", StructureCodeTable) ;
			temp.put("UnStructureCodeTable", UnStructureCodeTable) ;
			temp.put("SemiStructureCodeTable", SemiStructureCodeTable) ;
			temp.put("typeCodeTable", typeList) ;
			retMap.put("data", temp) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
}
