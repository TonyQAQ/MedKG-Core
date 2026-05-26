package com.sunsheen.hkks.task.unstructure.lt.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.CheckParametersUtil;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.taskexecute.service.TaskExecuteService;
import com.sunsheen.hkks.task.unstructure.common.entity.TrainMarkInfoEntity;
import com.sunsheen.hkks.task.unstructure.lt.service.LabelTrainingService;
import com.sunsheen.hkks.task.unstructure.ussm.entity.MarkSampleInfoEntity;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

@Path("/task/us/lt")
public class LabelTrainingController extends BaseAPI {
	
	private LabelTrainingService labelTrainingService = new LabelTrainingService() ;
	private TaskExecuteService taskExecuteService = new TaskExecuteService() ; //任务提交
	
	/**
	 * 
	* @Title: getMarkDataList
	* @Description:根据分配的任务查询数据标注信息
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
		String taskId = request.getParameter("taskId") ; //任务ID
		String isMark = request.getParameter("isMark") ; //是否标注的ID
		String pageSize = request.getParameter("pageSize") ; //分页参数
		String pageCount = request.getParameter("pageCount") ; //分页参数
		if(!StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			if("all".equalsIgnoreCase(isMark)){
				isMark = "" ;
			}
			//获取字段规范实体信息
			params.put("pageSize", pageSize) ;
			params.put("pageCount", pageCount) ;
			params.put("taskId", taskId) ;
			params.put("isMark", isMark) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			labelTrainingService.getMarkDataList(retMap,params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
	
	/**
	 * 
	* @Title: saveOrUpdateTrainMarks
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
	public Map<String,Object> saveOrUpdateTrainMarks(List<TrainMarkInfoEntity> entityList){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(entityList != null && entityList.size() > 0){			
			for(TrainMarkInfoEntity entity : entityList){
				String result  = CheckParametersUtil.getInstance()
						.put(entity.getAttrMappingId(), "attrMappingId") 
						.put(entity.getRowIndex(), "rowIndex") 
						.put(entity.getAnnotation(), "annotation")
						.put(entity.getEvalId(), "evalId")
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
			params.put("evalId", entityList.get(0).getEvalId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			//获取字段规范实体信息
			labelTrainingService.saveOrUpdateMarkDatas(retMap,entityList,params);
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
								.put(entity.getEvalId(), "evalId")
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
		labelTrainingService.saveOrUpdateMarkSampleInfo(retMap, entity);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: deleteMmarkSampleInfo
	* @Description: 删除标注训练的标签信息
	* @author: FengTao
	* @date 2020年8月26日 上午11:24:35
	* @param entityList
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
									.put(entity.getEvalId(), "evalId")
							           .checkParameter();
			if(result != null){
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "删除失败") ;
				return  retMap;
			}
		}
			}
		//添加任务和人员信息
		labelTrainingService.deleteMarkSampleInfo(retMap, entityList);
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
		labelTrainingService.isAllMark(retMap, params);
		return retMap;
	}
	
}
