package com.sunsheen.hkks.task.unstructure.fm.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.taskexecute.service.TaskExecuteService;
import com.sunsheen.hkks.task.unstructure.fm.service.FormalMarkService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

@Path("/task/us/fm")
public class FormalMarkController extends BaseAPI{
	
	private FormalMarkService formalMarkService = new FormalMarkService() ;
	private TaskExecuteService taskExecuteService = new TaskExecuteService() ; //任务提交
	
	/**
	 * 
	* @Title: getMarkDataList
	* @Description:根据分配的任务查询数据标注信息,用于正式标注
	* @author: FengTao
	* @date 2020年8月26日 上午9:26:49
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/search/markDatas")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getMarkDataList(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String pageSize = request.getParameter("pageSize") ;
		String pageCount = request.getParameter("pageCount") ;
		String tableName = request.getParameter("tableName") ;
		String columnName = request.getParameter("columnName") ;
		String attrMappingId = request.getParameter("attrMappingId") ;
		String isMark = request.getParameter("isMark") ; //01已标注 ，02 未标注
		if(!StringUtils.isEmptyOrWhitespaceOnly(attrMappingId)&&
				!StringUtils.isEmptyOrWhitespaceOnly(tableName)&&
					!StringUtils.isEmptyOrWhitespaceOnly(columnName)&&
						!StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			//获取字段规范实体信息
			params.put("pageSize", pageSize) ;
			params.put("pageCount", pageCount) ;
			params.put("attrMappingId", attrMappingId) ;
			params.put("tableName", tableName) ;
			params.put("columnName", columnName) ;
			params.put("taskId", taskId) ;
			params.put("isMark", isMark) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			formalMarkService.getFmMarkDataList(retMap,params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: submit
	* @Description: 标注完成，提交至校验
	* @author: FengTao
	* @date 2020年8月26日 上午9:53:29
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/submit")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> submit(){
		Map<String , Object > retMap = new HashMap<String,Object>() ;
		Map<String , Object > params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;

		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			taskExecuteService.addExeProcessState(retMap, params);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: submitConfirm
	* @Description:训练标注完成，提交至校验
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
		formalMarkService.getSubmitMsg(retMap, params);
		return retMap;
	}
	/**
	 * 
	* @Title: searchTables
	* @Description: 多条件查询正式标注表信息
	* @author: FengTao
	* @date 2020年8月13日 上午11:04:49
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/search/tables")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> searchTables(){
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
			formalMarkService.getTables(retMap, params);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: searchColumns
	* @Description: 多条件查询正式标注字段信息
	* @author: FengTao
	* @date 2020年8月13日 上午11:04:27
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/search/columns")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> searchColumns(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String tableMappingId = request.getParameter("tableMappingId") ;
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
			params.put("tableMappingId", tableMappingId) ;
			params.put("keyWord", keyWord) ;
			params.put("isSelect", isSelect) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			formalMarkService.getColumns(retMap, params);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: getGuideLineSchema
	* @Description: 多条件查询guideLine的Schema信息接口,GuideLine预览接口
	* @author: FengTao
	* @date 2020年8月14日 下午3:24:46
	* @param entityList
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/search/guideLine/schema")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getGuideLineSchema(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String keyWord = request.getParameter("keyWord") ;
		String typeCode = request.getParameter("typeCode") ;
		String isSelect = request.getParameter("isSelect") ;
		if(!StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			if("all".equalsIgnoreCase(typeCode) || StringUtils.isEmptyOrWhitespaceOnly(typeCode)){
				typeCode = "01" ;
			}
			if("all".equalsIgnoreCase(isSelect)){
				isSelect = "" ;
			}
			//获取字段规范实体信息
			params.put("taskId", taskId) ;
			params.put("keyWord", keyWord) ;
			params.put("typeCode", typeCode) ;
			params.put("isSelect", isSelect) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			formalMarkService.getGuideLineSchema(retMap, params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: getMarkDataList
	* @Description: 通过标签、状态、用户查询各个执行者提交的例子，通过过滤规则查询出相应的标注数据信息（分页），结合数据过滤规则
	* @author: FengTao
	* @date 2020年8月14日 下午3:24:46
	* @param entityList
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/search/guideLine/markDatas")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getGuideLineMarkDataList(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String schemaId = request.getParameter("schemaId") ; //schema的ID
		String pageSize = request.getParameter("pageSize") ; //分页参数
		String pageCount = request.getParameter("pageCount") ; //分页参数
		String attendUserId = request.getParameter("attendUserId") ; //用户筛选
		String tag= request.getParameter("tag") ; //标记
		if(!StringUtils.isEmptyOrWhitespaceOnly(schemaId)){
			if("all".equalsIgnoreCase(tag)){
				tag = "" ;
			}
			if("all".equalsIgnoreCase(attendUserId)){
				attendUserId = "" ;
			}
			//获取字段规范实体信息
			params.put("pageSize", pageSize) ;
			params.put("pageCount", pageCount) ;
			params.put("schemaId", schemaId) ;
			params.put("attendUserId", attendUserId) ;
			params.put("tag", tag) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			formalMarkService.getGuideLineMarkDataList(retMap,params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
}
