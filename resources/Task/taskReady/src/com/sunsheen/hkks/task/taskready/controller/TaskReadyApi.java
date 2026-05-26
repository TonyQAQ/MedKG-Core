package com.sunsheen.hkks.task.taskready.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.taskready.service.TaskReadyService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: TaskReadyApi
 * @Description:
 * @author: FengTao
 * @date 2020年7月20日 下午4:12:33
 */
@Path("/task")
public class TaskReadyApi extends BaseAPI{
	private TaskReadyService taskReadyService = new TaskReadyService() ;
	
	/**
	 * 
	* @Title: publish
	* @Description: 表，字段信息选择完毕，点击发布
	* @author: FengTao
	* @date 2020年7月22日 上午10:02:16
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/ready/publish")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> publish(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			params.put("stateCode", "200000") ; //任务发布，则任务进入规范制定阶段200000
			taskReadyService.updateTaskState(retMap,params) ;
		}
		return retMap;
		
	}
	/**
	 * 
	* @Title: publish
	* @Description: 发布确认
	* @author: FengTao
	* @date 2020年7月22日 上午10:02:16
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/ready/publishConfirm")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> publishConfirm(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			taskReadyService.getPublishMsg(retMap,params) ;
		}
		return retMap;
		
	}
	/**
	 * 
	* @Title: saveOrUpdateReadyData
	* @Description: 设置表别名
	* @author: FengTao
	* @date 2020年7月21日 下午3:47:35
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/ready/setAlias")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> setTableAlias(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String tableId = request.getParameter("tableId") ; //表ID
		String tableAlias = request.getParameter("tableAlias") ; //表别名
		String taskId = request.getParameter("taskId") ; //任务ID
		if(StringUtils.isEmptyOrWhitespaceOnly(tableId)||
					StringUtils.isEmptyOrWhitespaceOnly(tableAlias)||
							StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("tableId", tableId) ;
			params.put("tableAlias", tableAlias) ;
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			taskReadyService.saveOrUpdateTable(retMap, params);
		}
		return retMap;
		
	}
	/**
	 * 
	* @Title: saveOrUpdateReadyData
	* @Description: 表、字段选择的保存和删除事件
	* @author: FengTao
	* @date 2020年7月21日 下午3:47:35
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/ready/select")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> saveOrDeleteReadyData(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String isSelect = request.getParameter("isSelect") ; //yes代表选中，no代表取消选中
		String tableId = request.getParameter("tableId") ; //表ID
		String tableAlias = request.getParameter("tableAlias") ; //表别名
		String attrId = request.getParameter("attrId") ; //属性ID
		String taskId = request.getParameter("taskId") ; //任务ID
		String type = request.getParameter("type") ; //类型，01结构化
		if(StringUtils.isEmptyOrWhitespaceOnly(isSelect)||
				StringUtils.isEmptyOrWhitespaceOnly(tableId)||
						StringUtils.isEmptyOrWhitespaceOnly(attrId)||
							StringUtils.isEmptyOrWhitespaceOnly(taskId)||
								StringUtils.isEmptyOrWhitespaceOnly(type)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("tableId", tableId) ;
			params.put("tableAlias", tableAlias) ;
			params.put("attrId", attrId) ;
			params.put("taskId", taskId) ;
			params.put("type", type) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			if("no".equalsIgnoreCase(isSelect)){//删除已经选择的字段
				taskReadyService.daleteColumnAndTable(retMap, params);
			}else if("yes".equalsIgnoreCase(isSelect)){//添加已经选择的字段
				taskReadyService.insertColumnAndTable(retMap, params);
			}
		}
		return retMap;
		
	}
	/**
	 * 执行数据表、字段预选接口，将表、字段信息拷贝一份到业务表
	 * @return
	 */
	@POST
	@Path("/ready/exeReady")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> exeReady(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","准备失败");
		}else{
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			taskReadyService.copyTableAndColumnsFromSource(retMap,params);
		}
		return retMap ;
	}
	/**
	 * 获取数据源下的所有数据表信息
	 * @return
	 */
	@POST
	@Path("/ready/search/tables")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getReadyTables(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String keyWord = request.getParameter("keyWord") ;
		String isSelect = request.getParameter("isSelect") ;
		if("all".equalsIgnoreCase(isSelect)){
			isSelect = "" ;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","获取失败");
		}else{
			params.put("taskId", taskId) ;
			params.put("keyWord", keyWord) ;
			params.put("isSelect", isSelect) ;
			taskReadyService.getTables(retMap,params) ;
		}
		return retMap ;
	}
	/**
	 * 获取数据源下的所有数据表信息
	 * @return
	 */
	@POST
	@Path("/ready/search/columns")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getReadyColumns(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String tableId = request.getParameter("tableId") ;
		String keyWord = request.getParameter("keyWord") ;
		String isSelect = request.getParameter("isSelect") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","获取失败");
		}else{
			params.put("taskId", taskId) ;
			params.put("tableId", tableId) ;
			params.put("keyWord", keyWord) ;
			params.put("isSelect", isSelect) ;
			taskReadyService.getColumns(retMap,params) ;
		}
		return retMap ;
	}
	/**
	 * 获取数据源下的所有数据表信息
	 * @return
	 */
	@GET
	@POST
	@Path("/ready/search/statData")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getStatData(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","获取失败");
		}else{
			params.put("taskId", taskId) ;
			taskReadyService.getStatData(retMap,params) ;
		}
		return retMap ;
	}
}
