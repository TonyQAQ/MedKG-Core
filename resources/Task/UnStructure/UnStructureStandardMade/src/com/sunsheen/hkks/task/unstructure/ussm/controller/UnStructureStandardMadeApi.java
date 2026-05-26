package com.sunsheen.hkks.task.unstructure.ussm.controller;

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
import com.sunsheen.hkks.task.taskexecute.service.TaskExecuteService;
import com.sunsheen.hkks.task.unstructure.common.entity.MarkInfoEntity;
import com.sunsheen.hkks.task.unstructure.ussm.entity.CustomLabelEntity;
import com.sunsheen.hkks.task.unstructure.ussm.entity.MarkSampleInfoEntity;
import com.sunsheen.hkks.task.unstructure.ussm.service.UnStructureStandardMadeService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: UnStructureStandardMadeApi
 * @Description: 非结构化标签定义（别名规则映射重用结构化）
 * @author: FengTao
 * @date 2020年8月10日 下午5:58:21
 */
@Path("/task/us/ussm")
public class UnStructureStandardMadeApi extends BaseAPI{
	
	private TaskExecuteService taskExecuteService = new TaskExecuteService() ;
	private UnStructureStandardMadeService unStructureStandardMadeService = new UnStructureStandardMadeService() ;
	
	/**
	 * 标签定义完成，任务提交确认消息
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
		unStructureStandardMadeService.getSubmitMsg(retMap, params);
		return retMap;
	}
	
	/**
	 * 
	* @Title: getMarkDataList
	* @Description: 通过过滤规则查询出相应的标注数据信息（分页），结合数据过滤规则
	* @author: FengTao
	* @date 2020年8月14日 下午3:24:46
	* @param entityList
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
			params.put("userId", Session.getCurrUser().getId()) ; 
			unStructureStandardMadeService.getMarkDataList(retMap,params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: saveOrUpdateLeabels
	* @Description: 标注文件json保存接口
	* @author: FengTao
	* @date 2020年8月14日 下午3:24:46
	* @param entityList
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/saveOrUpdate/markDatas")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> saveOrUpdateLeabels(List<MarkInfoEntity> entityList){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(entityList != null && entityList.size() > 0){			
			for(MarkInfoEntity entity : entityList){
				String result  = CheckParametersUtil.getInstance()
						.put(entity.getAttrMappingId(), "attrMappingId") 
						.put(entity.getRowIndex(), "rowIndex") 
						.put(entity.getAnnotation(), "annotation")
						.checkParameter() ;
				if(result != null){
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "保存失败，请检查标注信息") ;
					return retMap ;
				}
			}
			if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
				retMap.putAll(RetInfo.RETFAIL);
				retMap.put("retmsg", "保存失败，请检查标注信息") ;
				return retMap ;
			}
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			//获取字段规范实体信息
			unStructureStandardMadeService.saveOrUpdateMarkDatas(retMap,entityList,params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "保存失败，请检查标注信息") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: addOrUpdateMmarkSampleInfo
	* @Description: 保存逐个标注信息
	* @author: FengTao
	* @date 2020年8月18日 下午5:46:40
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/saveOrUpdate/markSampleInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> addOrUpdateMmarkSampleInfo(MarkSampleInfoEntity entity){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		//任务主体表单校验
		String result = CheckParametersUtil.getInstance()
								.put(entity.getAttrMappingId(),"attrMappingId")
								.put(entity.getRowIndex(),"rowIndex")
								.put(entity.getLabelId(), "labelId")
								.put(entity.getFromId(),"fromId")
								.put(entity.getToId(), "toId")
					            .checkParameter();
		if(result != null){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "保存失败,请检查表单完整性") ;
			return  retMap;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(entity.getSampleId())){
			entity.setSampleId(null);
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(entity.getTag())){
			entity.setTag("00");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(entity.getMarkFileName())){
			entity.setMarkFileName(entity.getAttrMappingId()+"["+entity.getRowIndex()+"].json");
		}
		//附上初始信息
		entity.setLastUpdateUserId(Session.getCurrUser().getId());
		entity.setLastUpdateUserName(Session.getCurrUser().getUsername());
		entity.setLastUpdateDate(new Date(System.currentTimeMillis()));
		//添加任务和人员信息
		unStructureStandardMadeService.saveOrUpdateMarkSampleInfo(retMap, entity);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: deleteMmarkSampleInfo
	* @Description: 删除标注信息
	* @author: FengTao
	* @date 2020年8月19日 上午10:05:28
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/delete/markSampleInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> deleteMmarkSampleInfo(List<MarkSampleInfoEntity> entityList){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		//任务主体表单校验
		if(entityList != null && entityList.size() > 0){			
			for(MarkSampleInfoEntity entity : entityList){
				String result = CheckParametersUtil.getInstance()
										.put(entity.getAttrMappingId(),"attrMappingId")
										.put(entity.getRowIndex(),"rowIndex")
										.put(entity.getLabelId(), "labelId")
										.put(entity.getFromId(),"fromId")
										.put(entity.getToId(), "toId")
							            .checkParameter();
				if(result != null){
					retMap.putAll(RetInfo.RETFAIL) ;
					retMap.put("retmsg", "删除失败") ;
					return  retMap;
				}
			}
		}
		//添加任务和人员信息
		unStructureStandardMadeService.deleteMarkSampleInfo(retMap, entityList);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: addOrUpdatelabel
	* @Description: 保存、修改标签
	* @author: FengTao
	* @date 2020年8月18日 下午5:21:39
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/saveOrUpdate/label")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> addOrUpdateLabel(CustomLabelEntity entity){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		//任务主体表单校验
		String result = CheckParametersUtil.getInstance()
								.put(entity.getLabelName(),"labelName")
								.put(entity.getTypeCode(),"typeCode")
								.put(entity.getTaskId(),"taskId")
					            .checkParameter();
		if(result != null){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "保存失败,请检查表单完整性") ;
			return  retMap;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(entity.getLabelId())){
			entity.setLabelId(null);
		}
		//附上初始信息
		entity.setLastUpdateUserId(Session.getCurrUser().getId());
		entity.setLastUpdateUserName(Session.getCurrUser().getUsername());
		entity.setLastUpdateDate(new Date(System.currentTimeMillis()));
		//添加任务和人员信息
		unStructureStandardMadeService.saveOrUpdateLabel(retMap, entity);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: deleteLabel
	* @Description:删除标签
	* @author: FengTao
	* @date 2020年8月19日 上午10:05:04
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/delete/label")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> deleteLabel(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		//任务主体表单校验
		String labelId = request.getParameter("labelId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(labelId)){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "删除失败") ;
			return  retMap;
		}
		params.put("labelId", labelId) ;
		params.put("userId", Session.getCurrUser().getId()) ;
		//添加任务和人员信息
		unStructureStandardMadeService.deleteLabel(retMap, params);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: getCustomLabels
	* @Description: 多条件查询标签信息接口
	* @author: FengTao
	* @date 2020年8月14日 下午3:24:46
	* @param entityList
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/search/labels")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getCustomLabels(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String keyWord = request.getParameter("keyWord") ;
		String typeCode = request.getParameter("typeCode") ;
		if(!StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			if("all".equalsIgnoreCase(typeCode)){
				typeCode = "" ;
			}
			//获取字段规范实体信息
			params.put("taskId", taskId) ;
			params.put("keyWord", keyWord) ;
			params.put("typeCode", typeCode) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			unStructureStandardMadeService.getCustomLabels(retMap, params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: searchTables
	* @Description: 多条件查询标签定义的表信息,由上一步确认
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
			unStructureStandardMadeService.getTables(retMap, params);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: searchColumns
	* @Description: 多条件查询标签定义的字段信息
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
			params.put("userId", Session.getCurrUser().getId()) ; 
			unStructureStandardMadeService.getColumns(retMap, params);
		}
		return retMap ;
	}
	/**
	 * 
	* @Title: getLabelCodes
	* @Description: 获取标签码表
	* @author: FengTao
	* @date 2020年8月11日 下午3:20:26
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@Path("/exe/search/codes")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getLabelCodes(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		unStructureStandardMadeService.getLabelTypeCode(retMap);
		return retMap;
	}
	/**
	 * 
	* @Title: publish
	* @Description: 任务执行者回退到上一步
	* @author: FengTao
	* @date 2020年8月11日 下午3:20:26
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/exe/back")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> publish(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		String litleStateCode = request.getParameter("litleStateCode") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			if(!StringUtils.isEmptyOrWhitespaceOnly(litleStateCode) && !"200202".equals(litleStateCode)){
				retMap.putAll(RetInfo.RETSUCCESS);
				retMap.put("retmsg", "已经提交，请勿重复提交") ;
				return retMap ;
			}
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
		}
		taskExecuteService.addExeProcessBackState(retMap, params);
		return retMap;
	}	
	
}
