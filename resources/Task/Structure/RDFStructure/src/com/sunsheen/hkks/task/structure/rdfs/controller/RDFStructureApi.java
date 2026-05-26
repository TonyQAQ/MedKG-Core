package com.sunsheen.hkks.task.structure.rdfs.controller;

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
import com.sunsheen.hkks.task.structure.rdfs.entity.StructureRelEntity;
import com.sunsheen.hkks.task.structure.rdfs.service.RDFStructureService;
import com.sunsheen.hkks.task.taskexecute.service.TaskExecuteService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: RDFStructureApi
 * @Description: 结构化RDF关系构建Controller
 * @author: FengTao
 * @date 2020年7月30日 下午1:35:26
 */
@Path("/task/rdfs")
public class RDFStructureApi extends BaseAPI {
	
	private RDFStructureService rdfStructureService = new RDFStructureService() ;
	private TaskExecuteService taskExecuteService = new TaskExecuteService() ;

	/**
	 * 
	* @Title: getTablesAndSolumns
	* @Description: RDF任务提交
	* @author: FengTao
	* @date 2020年7月30日 下午3:49:17
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/submit")
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
	* @Title: getTablesAndSolumns
	* @Description: 任务提交确认
	* @author: FengTao
	* @date 2020年7月30日 下午3:49:17
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/submitConfirm")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getSubmitConfirm(){
		Map<String , Object > retMap = new HashMap<String,Object>() ;
		Map<String , Object > params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;

		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			rdfStructureService.getSubmitMsg(retMap, params);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: addOrUpdateRel
	* @Description: RDF 关系保存,关系修改
	* @author: FengTao
	* @date 2020年7月30日 下午6:30:29
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/addOrUpdate/rel")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> addOrUpdateRel(StructureRelEntity entity){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		//任务主体表单校验
		String result = CheckParametersUtil.getInstance()
								.put(entity.getRel(),"rel")
								.put(entity.getAttrStart(),"attrStart")
								.put(entity.getAttrEnd(),"attrEnd")
								.put(entity.getTaskId(),"taskId")
					            .checkParameter();
		if(result != null){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "保存失败,请检查表单完整性") ;
			return  retMap;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(entity.getResultId())){
			entity.setResultId(null);
		}
		//附上初始信息
		entity.setUserId(Session.getCurrUser().getId());
		entity.setUserName(Session.getCurrUser().getUsername());
		entity.setLastUpdateDate(new Date(System.currentTimeMillis()));
		//添加任务和人员信息
		rdfStructureService.addOrUpdateRel(retMap, entity);
		return retMap ;
	}

	/**
	 * 
	* @Title: deleteRel
	* @Description: RDF 关系删除
	* @author: FengTao
	* @date 2020年7月30日 下午6:30:14
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/delete/rel")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> deleteRel(){
		String resultIds = request.getParameter("resultIds") ;
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		if(StringUtils.isEmptyOrWhitespaceOnly(resultIds)){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "删除失败,请指定需要删除的数据") ;
			return  retMap;
		}
		//删除关系
		String ids = "" ;
		for(String resultId : resultIds.split(",")){
			ids += "'"+resultId+"'," ;
		}
		params.put("resultIds", ids.substring(0, ids.length()-1)) ;
		params.put("userId", Session.getCurrUser().getId()) ;
		rdfStructureService.deleteRel(retMap, params);
		return retMap ;
	}
	/**
	 * 
	* @Title: getTablesAndSolumns
	* @Description:多条件查询表信息，表下字段信息
	* @author: FengTao
	* @date 2020年7月30日 下午3:49:17
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/search/tablsAndColumns")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getTablesAndSolumns(){
		Map<String , Object > retMap = new HashMap<String,Object>() ;
		Map<String , Object > params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String isSelect = request.getParameter("isSelect") ;
		String keyWord = request.getParameter("keyWord") ;
		if("all".equalsIgnoreCase(isSelect)){
			isSelect = "" ;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			params.put("isSelect", isSelect) ;
			params.put("keyWord", keyWord) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			rdfStructureService.getTablesAndColumns(retMap, params);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: getRel
	* @Description:多条件RDF关系查询
	* @author: FengTao
	* @date 2020年7月30日 下午3:49:17
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/search/rel")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getRel(){
		Map<String , Object > retMap = new HashMap<String,Object>() ;
		Map<String , Object > params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String rel = request.getParameter("rel") ;
		String attrName = request.getParameter("attrName") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			params.put("rel", rel) ;
			params.put("attrName", attrName) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			rdfStructureService.getRel(retMap, params);
		}
		return retMap ;
	}
}
