package com.sunsheen.hkks.task.unstructure.mtc.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.CheckParametersUtil;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.taskready.service.TaskReadyService;
import com.sunsheen.hkks.task.unstructure.fm.service.FormalMarkService;
import com.sunsheen.hkks.task.unstructure.lt.service.LabelTrainingService;
import com.sunsheen.hkks.task.unstructure.mtc.service.MarkTrainningCheckService;
import com.sunsheen.hkks.task.unstructure.ussm.entity.MarkSampleInfoEntity;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

@Path("task/us/mtc")
public class MarkTrainningCheckController extends BaseAPI{

	private MarkTrainningCheckService markTrainningCheckService = new MarkTrainningCheckService() ;
	private TaskReadyService taskReadyService = new TaskReadyService() ;
	private LabelTrainingService labelTrainingService = new LabelTrainingService() ; //标注训练，含有随机分配任务的方法
	private FormalMarkService formalMarkService = new FormalMarkService() ; //正式标注，含有任务分配的方法
	
	/**
	 * 
	* @Title: getMarkListByPosition
	* @Description:根据分配的任务查询训练校验阶段的标注信息
	* @author: FengTao
	* @date 2020年8月26日 上午9:26:49
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/search/position/markList")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> getMarkListByPosition(MarkSampleInfoEntity entity){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String result = CheckParametersUtil.getInstance()
								.put(entity.getAttrMappingId(),"attrMappingId")
								.put(entity.getRowIndex(),"rowIndex")
								//.put(entity.getLabelId(), "labelId")
								.put(entity.getFromId(),"fromId")
								.put(entity.getToId(), "toId")
								.put(entity.getEvalId(), "evalId")
					            .checkParameter();
		if(result != null){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "保存失败,请检查表单完整性") ;
			return  retMap;
		}	
		params.put("evalId", entity.getEvalId() ) ;
		params.put("labelId", entity.getLabelId() ) ;
		params.put("attrMappingId", entity.getAttrMappingId() ) ;
		params.put("rowIndex", entity.getRowIndex() ) ;
		params.put("fromId", entity.getFromId() ) ;
		params.put("toId", entity.getToId() ) ;
		markTrainningCheckService.getMarkListByPosition(retMap,params);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: addOrUpdateTag
	* @Description: 标注信息的状态标记信息（标注训练的校验）
	* @author: FengTao
	* @date 2020年8月20日 下午4:04:55
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/update/tag")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> updateTag(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String sampleIds = request.getParameter("sampleIds") ; //schema的ID
		String tag= request.getParameter("tag") ; //状态标记
		if(!StringUtils.isEmptyOrWhitespaceOnly(sampleIds) && !StringUtils.isEmptyOrWhitespaceOnly(tag) ){
			String ids =  "" ;
			for(String id : sampleIds.split(",")){
				ids += "'"+id +"'," ;
			}
			ids = ids.substring(0, ids.length()-1) ;
			params.put("tag", tag) ;
			
			params.put("sampleIds", ids) ;
			System.out.print(ids);
			markTrainningCheckService.updateTag(retMap,params) ;
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: getMarkDataList
	* @Description:根据分配的任务查询训练校验阶段的标注信息，做融合后返回
	* @author: FengTao
	* @date 2020年8月26日 上午9:26:49
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/search/markDatas")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getMarkDataList(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //任务ID
		String isCheck = request.getParameter("isCheck") ; //是否验收 01 已验收，02 未验收
		String pageSize = request.getParameter("pageSize") ; //分页参数
		String pageCount = request.getParameter("pageCount") ; //分页参数
		if(!StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			if("all".equalsIgnoreCase(isCheck)){
				isCheck = "" ;
			}
			//获取字段规范实体信息
			params.put("pageSize", pageSize) ;
			params.put("pageCount", pageCount) ;
			params.put("taskId", taskId) ;
			params.put("isCheck", isCheck) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			markTrainningCheckService.getFusionMarkDataList(retMap,params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: back
	* @Description: 退回，重新标注
	* @author: FengTao
	* @date 2020年8月26日 下午3:08:13
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/back")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> back(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		String trainCount = request.getParameter("trainCount") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId) || StringUtils.isEmptyOrWhitespaceOnly(trainCount) ){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			params.put("trainCount", trainCount) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			taskReadyService.updateTaskExeBackState(retMap, params); 
			if("01".equals(retMap.get("retcode"))){ //发布成功，则进行标注任务分配，否则就不分配
				labelTrainingService.trainDistribution(retMap, params,"00");
			}
		}
		return retMap;
	}
	
	/**
	 * 
	* @Title: publish
	* @Description: 校验完成，提交接口,只跟新执行人状态，不更新大任务状态，还是300000
	* 为正式表标注分配共同标注和非共同标注的任务信息
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
		String trainCount = request.getParameter("trainCount") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId) || StringUtils.isEmptyOrWhitespaceOnly(trainCount) || Integer.parseInt(trainCount) <= 0 ){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","提交失败");
		}else{
			params.put("taskId", taskId) ;
			params.put("trainCount", trainCount) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			retMap.putAll(RetInfo.RETSUCCESS);
			retMap.put("retmsg","提交成功，提交测试信息！");
			taskReadyService.updateTaskExeState(retMap, params); 
			if("01".equals(retMap.get("retcode"))){ //发布成功，则进行正式标注阶段的共同标注任务分配，否则就不分配,并进行每个字段的数据标注均分配
				labelTrainingService.trainDistribution(retMap, params,"01"); //01是正式标注阶段，分配共同标注
				if("01".equals(retMap.get("retcode"))){ //分配共同标注任务成功，则分配每个人的标注任务
					formalMarkService.markDistribution(retMap, params);
				}
			}
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: publishConfirm
	* @Description: 
	* @author: FengTao
	* @date 2020年8月26日 下午3:12:43
	* @return Map<String,Object>s
	* @version
	 */
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
			params.put("userId", Session.getCurrUser().getId() ) ;
		}
		markTrainningCheckService.getPublishMsg(retMap, params);
		return retMap;
	}
	
}
