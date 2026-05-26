package com.sunsheen.hkks.task.taskexecute.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.task.taskexecute.service.TaskExecuteService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: TaskExecuteApi
 * @Description: 任务协同执行
 * @author: FengTao
 * @date 2020年7月23日 上午9:14:07
 */
@Path("/task")
public class TaskExecuteApi extends BaseAPI{
	private TaskExecuteService taskExecuteService = new TaskExecuteService() ;
	@GET
	@Path("/search/exeInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getTaskExeList(){
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
		taskExecuteService.queryTaskExeList(retMap, params);
		return retMap ;
	}
	
	/**
	 * 获取任务类型，任务状态码表信息
	 * @return
	 */
	@GET
	@Path("/exe/codeTable/stateAndType")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getStateCode(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		taskExecuteService.getStateCodeTable(retMap);
		return retMap ;
	}
}
