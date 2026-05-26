package com.sunsheen.hkks.task.structure.ssm.controller;

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
import com.sunsheen.hkks.task.structure.ssm.entity.SSMEntity;
import com.sunsheen.hkks.task.structure.ssm.service.StructureStandardMadeService;
import com.sunsheen.hkks.task.taskexecute.service.TaskExecuteService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: StructureStandardMadeApi
 * @Description: 结构化别名规则映射
 * @author: FengTao
 * @date 2020年7月24日 下午3:16:35
 */
@Path("/task/ssm")
public class StructureStandardMadeApi extends BaseAPI{

	private StructureStandardMadeService structureStandardMadeService = new StructureStandardMadeService() ;
	private TaskExecuteService taskExecuteService = new TaskExecuteService() ;
	
	/**
	 * 提交确认接口(修改每个人的执行状态为规则校验  /exe/submit)
	* @Title: saveOrUpdateColumns
	* @Description:
	* @author: FengTao
	* @date 2020年7月27日 上午9:19:03
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/submitConfirm")
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
		structureStandardMadeService.getSubmitMsg(retMap, params);
		return retMap;
	}
	/**
	 * 提交接口(修改每个人的执行状态为规则校验  /exe/submit)
	* @Title: saveOrUpdateColumns
	* @Description:
	* @author: FengTao
	* @date 2020年7月27日 上午9:19:03
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/submit")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> publish(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		String litleStateCode = request.getParameter("litleStateCode") ; //字段ID
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			if(!StringUtils.isEmptyOrWhitespaceOnly(litleStateCode) && !"200201".equals(litleStateCode)){
				retMap.putAll(RetInfo.RETSUCCESS);
				retMap.put("retmsg", "已经提交，请勿重复提交") ;
				return retMap ;
			}
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
		}
		taskExecuteService.addExeProcessState(retMap, params);
		return retMap;
	}

	/**
	 * 字段、别名、规则映射提交接口
	* @Title: saveOrUpdateColumns
	* @Description:
	* @author: FengTao
	* @date 2020年7月27日 上午9:18:17
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/saveOrUpdate/Columns")
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
			structureStandardMadeService.saveOrUpdateSSM(retMap, columnsInfo);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "保存失败，请检查表单") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: getReadyTables
	* @Description:执行者表多条件查询，/exe/search/tables
	* @author: FengTao
	* @date 2020年7月24日 上午10:13:52
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/search/tables")
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
			params.put("userId", Session.getCurrUser().getId()) ; 
			structureStandardMadeService.getTables(retMap,params) ;
			
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
	@Path("/exe/search/columns")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getReadyColumns(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String tableId = request.getParameter("tableId") ;
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
			params.put("tableId", tableId) ;
			params.put("keyWord", keyWord) ;
			params.put("isSelect", isSelect) ;
			params.put("userId", Session.getCurrUser().getId()) ; //用于查询每个人的别名和规则信息
			structureStandardMadeService.getColumns(retMap,params) ;
		}
		return retMap ;
	}
	/**
	 * 
	* @Title: getStatData
	* @Description:已经规范字段数查询接口 /exe/search/statData
	* @author: FengTao
	* @date 2020年7月24日 下午4:52:30
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@Path("/exe/search/statData")
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
			params.put("userId", Session.getCurrUser().getId()) ;  //用于查询每个人的别名和规则信息
			structureStandardMadeService.getStatData(retMap,params) ;
		}
		return retMap ;
	}
	/**
	 * 获取过滤规则码表信息
	 * @return
	 */
	@GET
	@Path("/exe/search/filterCodeTable")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getUsers(){
		Map<String,Object> params = new HashMap<String,Object>() ;
		String attrType = request.getParameter("attrType") ;
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		params.put("attrType", attrType) ;
		structureStandardMadeService.getFilterCodeTable(retMap,params) ;
		return retMap ;
	}
}
