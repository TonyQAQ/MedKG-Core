package com.sunsheen.hkks.task.taskmt.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.taskmt.entity.TaskEntity;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

/**
 * 
 * @Title: TaskMaintenanceService
 * @Description: 任务管理Service
 * @author: FengTao
 * @date 2020年7月14日 上午9:49:44
 */
public class TaskMaintenanceService {
	
	/**
	 * 
	* @Title: queryTaskList
	* @Description: 根据任务名称、任务类型、状态查询任务信息
	* @author: FengTao
	* @date 2020年7月14日 上午9:59:56
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void queryTaskList( Map<String , Object> retMap , Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String , Object>> dataList = new ArrayList<Map<String , Object>>() ;
		List<Map<String,Object>> assignIds = new ArrayList<Map<String,Object>>() ;
		List<Map<String,Object>> overIds = new ArrayList<Map<String,Object>>() ;
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("TaskTitle.taskTitle")));
		try{
			Object num = session.createDySQLQuery("TaskMT.queryTaskListCount", params).uniqueResult() ;
			if(Integer.parseInt(num.toString()) > 0){
				PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), num.toString());
				dataList = session.createDySQLQuery("TaskMT.queryTaskList", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			}
			if(dataList != null && dataList.size() > 0){
				//有数据则加上排序号
				int sequence = 0 ;
				for(Map<String,Object> temp : dataList){
					sequence ++ ;
					temp.put("sequence", sequence) ;
					temp.put("rowKey", temp.get("taskId")) ;
					if(StringUtils.isEmptyOrWhitespaceOnly((String)temp.get("memo"))){
						temp.put("memo", "-") ;
					}
					//
					params.put("taskId", temp.get("taskId")) ;
					Object state = session.createDySQLQuery("TaskMT.queryCheckState", params).uniqueResult() ;
					Object litleStateCode = session.createDySQLQuery("TaskMT.queryExeState", params).uniqueResult() ;
					if("400201".equals(litleStateCode)){//当前任务有人到达了400201，则还需要加判断条件，还有人为完成标注，则重新查询校验状态
						assignIds = session.createDySQLQuery("Task.queryExeUser", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
						overIds =  session.createDySQLQuery("TaskMT.queryExeStateOverInfo", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
						if(assignIds.size() != overIds.size()){
							litleStateCode = session.createDySQLQuery("TaskMT.queryExeStateNotOver", params).uniqueResult() ;
							state = session.createDySQLQuery("TaskMT.queryCheckStateNotOver", params).uniqueResult() ;
							System.out.print(litleStateCode);
						}
					}
					temp.put("litleStateCode", litleStateCode) ;
					if("0".equals(state.toString())){//校验状态
						temp.put("checkState", "00") ;
					}else{
						temp.put("checkState", "01") ;
					}
				}
				retMap.putAll(RetInfo.RETSUCCESS);
			}else{
				retMap.putAll(RetInfo.RETSUCCESS);
				retMap.put("retmsg", "查询成功,暂无数据");
			}
			retMap.put("totalCount", num.toString()) ;
			retMap.put("data", dataList) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	/**
	 * 
	* @Title: addOrUpdateTask
	* @Description: 新增或修改任务信息
	* @author: FengTao
	* @date 2020年7月14日 上午9:49:59
	* @param retMap
	* @param userIds
	* @param entity void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void addOrUpdateTask( Map<String , Object> retMap , String userIds , TaskEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> params = new HashMap<String , Object>() ; 
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			session.beginTransaction();
			//添加任务信息
			session.saveOrUpdate(entity);
			session.flush();
			session.commit();
			//添加人员信息
			params.put("taskId", entity.getTaskId()) ;
			params.put("lastUpdateUserId", entity.getLastUpdateUserId()) ;
			params.put("lastUpdateUserName", entity.getLastUpdateUserName()) ;
			session.createDySQLQuery("TaskMT.deleteTaskUsers", params).executeUpdate() ;//删除任务下原来的人员信息
			for(String temp : userIds.split(",")){
				params.put("userId", temp);
				//添加人员信息
				Object num = session.createDySQLQuery("TaskMT.insertTaskUser", params).executeUpdate() ;
				if("0".equals(num.toString())){
					session.rollback();
					retMap.putAll(RetInfo.RETFAIL) ;
					return ;
				}
			}
			data = session.createDySQLQuery("Task.queryExeUser", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			if(data != null && data.size()>0){
				for( Map<String,Object> temp : data){
					//为每个人插入表当前任务状态状态
					params.put("assignId", temp.get("assignId")) ;
					Object effectNum = session.createDySQLQuery("Task.insertUserProcess", params).executeUpdate() ;
					if("0".equals(effectNum.toString())){
						session.rollback();
					}
				}
			}
			session.commit();
			session.close();
			retMap.putAll(RetInfo.RETSUCCESS) ;
		}catch(Exception e){
			session.rollback();
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}finally{
			session.close();
		}
	}
	
	/**
	 * 
	* @Title: queryTaskDeteleState
	* @Description: 查询任务是否能被删除,正在执行状态的任务不可删除
	* @author: FengTao
	* @date 2020年7月14日 上午9:50:20
	* @param retMap
	* @param params void
	* @version
	 */
	public void queryTaskDeteleState( Map<String , Object> retMap , Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> temp = new HashMap<String , Object>() ;
		try{
			//查询该任务是否是未开始和已结束阶段，是则返回1，否则是0（不可删除）
			Object num = session.createDySQLQuery("TaskMT.queryTaskDeteleState", params).uniqueResult();
			if(!"0".equals(num.toString())){
				temp.put("deleteState","01");
				temp.put("deleteComfirm", "任务进行中，不能删除。") ;
				retMap.put("data", temp) ;
			}else{
				temp.put("deleteState","00");
				temp.put("deleteComfirm", "删除后不可撤回，确定删除该任务吗？") ;
				retMap.put("data", temp) ;
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETSUCCESS) ;
		}
	}
	/**
	 * 
	* @Title: deleteTask
	* @Description: 删除任务信息
	* @author: FengTao
	* @date 2020年7月14日 上午9:51:23
	* @param retMap
	* @param params void
	* @version
	 */
	public void deleteTask( Map<String , Object> retMap , Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			Object num = session.createDySQLQuery("TaskMT.deleteTask", params).executeUpdate();
			session.createDySQLQuery("TaskMT.deleteTaskUsers", params).executeUpdate();//删除从表信息
			if(!"0".equals(num.toString())){
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "删除成功") ;
				return ;
			}else{
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "删除失败") ;
			}
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	/**
	 * 
	* @Title: closeTask
	* @Description: 关闭任务
	* @author: FengTao
	* @date 2020年7月14日 上午9:51:23
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void cancelTask( Map<String , Object> retMap , Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		session.beginTransaction();
		try{
			Object num = session.createDySQLQuery("TaskMT.cancelTask", params).executeUpdate();
			if(!"0".equals(num.toString())){
				session.flush();
				session.commit();
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "取消成功") ;
				data = session.createDySQLQuery("Task.queryExeUser", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
				if(data != null && data.size()>0){
					for( Map<String,Object> temp : data){
						//为每个人插入表当前任务状态状态
						params.put("assignId", temp.get("assignId")) ;
						Object effectNum = session.createDySQLQuery("Task.insertUserProcess", params).executeUpdate() ;
						if("0".equals(effectNum.toString())){
							session.rollback();
						}
					}
				}
			}else{
				session.rollback();
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "取消失败") ;
				return ;
			}
			session.commit();
		}catch(Exception e){
			session.rollback();
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	/**
	 * 
	* @Title: closeState
	* @Description: 查看任务是否能关闭
	* @author: FengTao
	* @date 2020年7月14日 上午9:51:23
	* @param retMap
	* @param params void
	* @version
	 */
	public void cancelState( Map<String , Object> retMap , Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> temp = new HashMap<String , Object>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			Object num = session.createDySQLQuery("TaskMT.cancelState", params).uniqueResult();
			Object isCancel = session.createDySQLQuery("TaskMT.isCancel", params).uniqueResult();
			if("0".equals(num.toString())){
				temp.put("deleteState","01");
				temp.put("deleteComfirm", "任务未开始，无法取消。") ;
				retMap.put("data", temp) ;
				return ;
			}
			if(!"0".equals(isCancel.toString())){
				temp.put("deleteState","01");
				temp.put("deleteComfirm", "请勿重复取消。") ;
				retMap.put("data", temp) ;
				return ;
			}
			temp.put("deleteState","00");
			temp.put("deleteComfirm", "取消后不可撤回，确认取消该任务吗？") ;
			retMap.put("data", temp) ;			
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}

	/**
	 * 
	* @Title: getTaskState
	* @Description: 获取任务的大状态，小状态信息
	* @author: FengTao
	* @date 2020年7月14日 上午9:51:23
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getTaskState( Map<String , Object> retMap , Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> data = new HashMap<String,Object>() ;
		List<Map<String,Object>> assignIds = new ArrayList<Map<String,Object>>() ;
		List<Map<String,Object>> overIds = new ArrayList<Map<String,Object>>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			Object stateCode = session.createDySQLQuery("TaskMT.queryTaskState", params).uniqueResult() ;
			Object littleStateCode = null ;
			if("01".equals(params.get("pageType"))){
				littleStateCode = session.createDySQLQuery("TaskMT.queryExeState", params).uniqueResult() ; //有人到达了最新状态
				if("400201".equals(littleStateCode)){//当前任务有人到达了400201，则还需要加判断条件，还有人为完成标注，则重新查询除开400201的小状态
					assignIds = session.createDySQLQuery("Task.queryExeUser", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
					overIds =  session.createDySQLQuery("TaskMT.queryExeStateOverInfo", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
					if(assignIds.size() != overIds.size()){
						littleStateCode = session.createDySQLQuery("TaskMT.queryExeStateNotOver", params).uniqueResult() ;	
					}
				}
			}else if("02".equals(params.get("pageType"))){
				littleStateCode = session.createDySQLQuery("TaskMT.queryExeStateByUserId", params).uniqueResult() ;
			}
			data.put("stateCode", stateCode) ;
			data.put("littleStateCode", littleStateCode) ;
			retMap.put("data", data) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
	/**
	 * 
	* @Title: deleteTask
	* @Description: 获取参与人信息
	* @author: FengTao
	* @date 2020年7月14日 上午9:51:23
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getUsers( Map<String , Object> retMap , Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			dataList = session.createDySQLQuery("TaskMT.queryUsers", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			retMap.put("data", dataList) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
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
	public void getStateCodeTable(Map<String , Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String , Object>> stateList = new ArrayList<Map<String , Object>>() ;
		List<Map<String , Object>> typeList = new ArrayList<Map<String , Object>>() ;
		Map<String , Object> temp = new HashMap<String , Object>() ;
		try{
			stateList = session.createDySQLQuery("TaskMT.queryStateTable", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			typeList = session.createDySQLQuery("TaskMT.queryTypeTable", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			retMap.putAll(RetInfo.RETSUCCESS) ;
			temp.put("stateCodeTable", stateList) ;
			temp.put("typeCodeTable", typeList) ;
			retMap.put("data", temp) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
}
