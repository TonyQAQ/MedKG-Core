package com.sunsheen.hkks.task.taskmt.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.CheckParametersUtil;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.taskmt.entity.TaskEntity;
import com.sunsheen.hkks.task.taskmt.service.TaskMaintenanceService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: TaskMaintenanceApi
 * @Description: 任务维护管理模块
 * @author: FengTao
 * @date 2020年7月13日 上午9:33:13
 */
@Path("/task")
public class TaskMaintenanceApi extends BaseAPI{

	private TaskMaintenanceService taskMaintenanceService = new TaskMaintenanceService() ;

	/**
	 * 删除任务信息，只有未开始的任务才能删除
	 * @return
	 */
	@POST
	@Path("/delete/info")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> deleteTask(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		//参数校验
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
			return retMap ;
		}
		params.put("taskId", taskId) ;
		params.put("userId", Session.getCurrUser().getId()) ; 
		taskMaintenanceService.deleteTask(retMap, params);
		return retMap ;
	}
	/**
	 * 查看当前任务是否能被删除信息
	 * @return
	 */
	@GET
	@Path("/delete/state")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> deleteState(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		//参数校验
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
			return retMap ;
		}
		params.put("taskId", taskId) ;
		params.put("userId", Session.getCurrUser().getId()) ; 
		taskMaintenanceService.queryTaskDeteleState(retMap, params);
		return retMap ;
	}
	/**
	 * 查询数据库配置信息
	 * @return
	 */
	@GET
	@Path("/search/info")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> queryTask(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskName = request.getParameter("taskName") ;
		String taskId = request.getParameter("taskId") ;
		String typeCode = request.getParameter("typeCode") ;
		String stateCode = request.getParameter("stateCode") ;
		String orderColumns = request.getParameter("orderColumns") ;//排序字段
		String orderRule = request.getParameter("orderRule") ;//排序规则,默认倒序
		String pageSize = request.getParameter("pageSize") ;
		String pageCount = request.getParameter("pageCount") ;
		if("all".equals(typeCode)){
			typeCode = "" ;
		}
		if("all".equals(stateCode)){
			stateCode = "" ;
		}
		//参数校验
		if(StringUtils.isEmptyOrWhitespaceOnly(orderColumns)){
			orderColumns = "lastUpdateDate" ;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(orderRule)){
			orderRule = "DESC" ;
		}
		params.put("taskName", taskName) ;
		params.put("taskId", taskId) ;
		params.put("typeCode", typeCode) ;
		params.put("stateCode", stateCode) ;
		params.put("orderColumns", orderColumns) ;
		params.put("orderRule", orderRule) ;
		params.put("userId", Session.getCurrUser().getId()) ; 
		params.put("pageSize", pageSize) ;
		params.put("pageCount", pageCount) ;
		taskMaintenanceService.queryTaskList(retMap, params);
		return retMap ;
	}
	/**
	 * 新增或更新任务信息
	 * @return
	 */
	@POST
	@Path("/addOrUpdate/info")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> addOrUpdateTask(TaskEntity entity){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		//任务执行人员Id
		String userIds = request.getParameter("userIds") ;
		//任务主体表单校验
		String result = CheckParametersUtil.getInstance()
								.put(entity.getTaskName(),"taskName")
								.put(entity.getTypeCode(),"typeCode")
								.put(entity.getStartTime(),"startTime")
								.put(entity.getEndTime(),"endTime")
								.put(entity.getConnectionId(),"connectionId")
					            .checkParameter();
		if(result != null){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "保存失败,请检查表单完整性") ;
			return  retMap;
		}
		//任务执行人校验
		if(StringUtils.isEmptyOrWhitespaceOnly(userIds)){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "保存失败,请添加任务执行者") ;
			return  retMap;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(entity.getTaskId())){
			entity.setTaskId(null);
			entity.setStateCode("100000");
		}
		//附上初始信息
		entity.setLastUpdateUserId(Session.getCurrUser().getId());
		entity.setLastUpdateUserName(Session.getCurrUser().getUsername());
		entity.setLastUpdateDate(new Date(System.currentTimeMillis()));
		//添加任务和人员信息
		taskMaintenanceService.addOrUpdateTask(retMap , userIds, entity);
		return retMap ;
	}
	/**
	 * 删除任务信息,只有已经开始的任务才能取消
	 * @return
	 */
	@POST
	@Path("/cancelTask")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> colseTask(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		//参数校验
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
			return retMap ;
		}
		params.put("taskId", taskId) ;
		params.put("userId", Session.getCurrUser().getId()) ;
		taskMaintenanceService.cancelTask(retMap, params);
		return retMap ;
	}
	/**
	 * 查看任务能否被取消
	 * @return
	 */
	@GET
	@Path("/cancel/state")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> colseState(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		//参数校验
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
			return retMap ;
		}
		params.put("taskId", taskId) ;
		params.put("userId", Session.getCurrUser().getId()) ;
		taskMaintenanceService.cancelState(retMap, params);
		return retMap ;
	}
	
	/**
	 * 获取任务类型，任务状态码表信息
	 * @return
	 */
	@GET
	@Path("/task/state")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getTaskState(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String pageType = request.getParameter("pageType") ; //01发布者，02执行者
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
			return retMap ;
		}else{
			params.put("taskId", taskId) ;
			params.put("pageType", pageType) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			taskMaintenanceService.getTaskState(retMap,params);
		}
		
		return retMap ;
	}
	
	/**
	 * 获取任务类型，任务状态码表信息
	 * @return
	 */
	@GET
	@Path("/codeTable/stateAndType")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getStateCode(){
		String typeCode = request.getParameter("typeCode") ;
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		params.put("typeCode", typeCode) ;
		taskMaintenanceService.getStateCodeTable(retMap,params);
		return retMap ;
	}
	/**
	 * 获取任务类型，任务状态码表信息
	 * @return
	 */
	@GET
	@Path("/query/users")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getUsers(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		params.put("userId", Session.getCurrUser().getId()) ;
		taskMaintenanceService.getUsers(retMap,params) ;
		return retMap ;
	}
}
