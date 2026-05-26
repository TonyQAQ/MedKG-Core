package com.sunsheen.hkks.task.structure.rv.controller;

import java.util.Date;
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
import com.sunsheen.hkks.database.mgr.service.DataBaseUtilService;
import com.sunsheen.hkks.task.structure.rv.entity.StructureRelCheckEntity;
import com.sunsheen.hkks.task.structure.rv.service.ResultVerificationService;
import com.sunsheen.hkks.task.taskready.service.TaskReadyService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: ResultVerificationApi
 * @Description: 结果校验API
 * @author: FengTao
 * @date 2020年7月31日 下午3:45:53
 */
@Path("task/rv")
public class ResultVerificationApi extends BaseAPI {
	
	private ResultVerificationService resultVerificationService = new ResultVerificationService() ;
	private TaskReadyService taskReadyService = new TaskReadyService() ;
	private DataBaseUtilService dataBaseUtilService = new DataBaseUtilService() ;
	/**
	 * 
	* @Title: getCheckTables
	* @Description:多条件校验表查询,表字段选择阶段，未选表的将不会查询出来
	* @author: FengTao
	* @date 2020年8月3日 上午9:13:31
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
			resultVerificationService.getTables(retMap,params) ;
		}
		return retMap ;
	}
	/**
	 * 
	* @Title: GetRDFList
	* @Description:多条件校验关系查询，以当前表为气势节点，查询相关的RDF信息
	* @author: FengTao
	* @date 2020年8月3日 上午9:38:42
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/search/rdf")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> GetRDFList(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String tableMappingId = request.getParameter("tableMappingId") ;
		String isUsed = request.getParameter("isUsed") ;
		String keyWord = request.getParameter("keyWord") ;
		if("all".equalsIgnoreCase(isUsed)){
			isUsed = "" ;
		}
		params.put("isUsed", isUsed) ;
		params.put("keyWord", keyWord) ;
		params.put("tableMappingId", tableMappingId) ;
		resultVerificationService.getRDFList(retMap,params) ;
		return retMap ;
	}
	
	/**
	 * 
	* @Title: saveOrUpdateRDF
	* @Description: 批量保存
	* @author: FengTao
	* @date 2020年8月3日 上午9:59:59
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/saveOrUpdateRDF")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> saveOrUpdateRDF(List<StructureRelCheckEntity> entityList){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String tableMappingId = request.getParameter("tableMappingId") ;
		for(StructureRelCheckEntity entity : entityList){
			//任务主体表单校验
			String result = CheckParametersUtil.getInstance()
								.put(entity.getRel(),"rel")
								.put(entity.getAttrStart(),"attrStart")
								.put(entity.getAttrEnd(),"attrEnd")
								.put(entity.getTaskId(),"taskId")
								.put(entity.getIsUsed(),"isUsed")
							    .checkParameter();
			if(result != null){
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "保存失败,请检查表单完整性") ;
				return  retMap;
			}
			entity.setResultId(null);//设置为空，全部都是添加操作
			//附上初始信息
			entity.setUserId(Session.getCurrUser().getId());
			entity.setUserName(Session.getCurrUser().getUsername());
			entity.setLastUpdateDate(new Date(System.currentTimeMillis()));
		}
		params.put("tableMappingId", tableMappingId) ;
		params.put("userId", Session.getCurrUser().getId() ) ;
		resultVerificationService.addOrUpdateRelByRelList(retMap, params, entityList);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: PublishRDF
	* @Description: 结构化校验完毕，任务完成，任务状态为-结束任务,并且销毁数据源
	* @author: FengTao
	* @date 2020年8月3日 上午9:56:04
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/publish/rdf")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> PublishRDF(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			params.put("stateCode", "400000") ; //任务结果校验完成，则任务进入任务结束阶段400000
			taskReadyService.updateTaskState(retMap,params) ;//发布任务，发布失败则不销毁数据源
			if("01".equals( retMap.get("code") )){
				dataBaseUtilService.destroyPool(retMap, params);
			}
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
	@Path("/check/publishConfirm")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> publishConfirm(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			resultVerificationService.getPublishMsg(retMap,params) ;
		}
		return retMap;
	}
}
