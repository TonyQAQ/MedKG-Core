package com.sunsheen.hkks.task.structure.sc.controller;

import java.util.HashMap;
import java.util.List;
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
import com.sunsheen.hkks.task.structure.sc.service.StandardCheckService;
import com.sunsheen.hkks.task.structure.ssm.entity.SSMEntity;
import com.sunsheen.hkks.task.taskready.service.TaskReadyService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: StandardCheckApi
 * @Description: 规范校验
 * @author: FengTao
 * @date 2020年7月30日 下午3:48:49
 */
@Path("/task/sc")
public class StandardCheckApi extends BaseAPI {
	
	private StandardCheckService standardCheckService = new StandardCheckService() ;
	//包含了任务发布的service
	private TaskReadyService taskReadyService = new TaskReadyService() ;
	
	/**
	 * 
	* @Title: saveOrUpdateColumns
	* @Description:校验结果保存接口
	* @author: FengTao
	* @date 2020年7月29日 上午10:23:46
	* @param columnsInfo
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/saveOrUpdate/columns")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> saveOrUpdateColumns(List<SSMEntity> columnsInfo){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		if(columnsInfo != null && columnsInfo.size() > 0){			
			for(SSMEntity entity : columnsInfo){
				String result  = CheckParametersUtil.getInstance()
						.put(entity.getAttrMappingId(), "attrMappingId") 
						.put(entity.getTableMappingId(), "tableMappingId") 
						.checkParameter() ;
				if(result != null){
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "保存失败，请检查表单") ;
				}
			}
			//获取字段规范实体信息
			standardCheckService.saveOrUpdateSC(retMap, columnsInfo);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "保存失败，请检查表单") ;
		}
		return retMap ;
	}
	/**
	 * 
	* @Title: getReadyTables
	* @Description: 发布者校验表查询
	* @author: FengTao
	* @date 2020年7月24日 上午10:13:52
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/search/tables")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getCheckTables(){
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
			params.put("userId", Session.getCurrUser().getId()) ; 
			standardCheckService.getTables(retMap,params) ;
			
		}
		return retMap ;
	}
	/**
	 * 
	* @Title: getReadyColumns
	* @Description:执行者字段多条件查询 /exe/search/columns
	* @author: FengTao
	* @date 2020年7月24日 下午3:16:16
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/search/columns")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getCheckColumns(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String tableMappingId = request.getParameter("tableMappingId") ;
		String keyWord = request.getParameter("keyWord") ;
		String isCheck = request.getParameter("isCheck") ;
		if("all".equalsIgnoreCase(isCheck)){
			isCheck = "" ;
		}
		params.put("tableMappingId", tableMappingId) ;
		params.put("keyWord", keyWord) ;
		params.put("isCheck", isCheck) ;
		standardCheckService.getColumns(retMap,params) ;
		return retMap ;
	}
	
	/**
	 * 
	* @Title: saveOrUpdateColumns
	* @Description:校验完成，发布确认接口
	* @author: FengTao
	* @date 2020年7月27日 上午9:19:03
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/publishConfirm")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> submitConfirm(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
		}
		standardCheckService.getPulishMsg(retMap, params);
		return retMap;
	}
	/**
	 * 
	* @Title: getReadyTables
	* @Description: 校验完成，提交接口
	* @author: FengTao
	* @date 2020年7月24日 上午10:13:52
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/publish")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> publish(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","获取失败");
		}else{
			params.put("taskId", taskId) ;
			params.put("stateCode", "300000") ; //校验完毕进入知识标注阶段
			params.put("userId", Session.getCurrUser().getId()) ; 
			taskReadyService.updateTaskState(retMap, params); ;
			
		}
		return retMap ;
	}
	
	@GET
	@Path("/check/search/statData")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getStatData(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if( StringUtils.isEmptyOrWhitespaceOnly(taskId) ){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","获取失败");
		}else{
			params.put("taskId", taskId) ;
			standardCheckService.getStatData(retMap,params) ;
		}
		return retMap ;
	}
}
