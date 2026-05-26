package com.sunsheen.hkks.task.unstructure.rv.controller;

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
import com.sunsheen.hkks.task.unstructure.rv.service.ResultVerificationService;
import com.sunsheen.hkks.task.unstructure.ussm.entity.MarkSampleInfoEntity;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: ResultVerificationApi
 * @Description: 结果校验API
 * @author: FengTao
 * @date 2020年7月31日 下午3:45:53
 */
@Path("task/us/rv")
public class ResultVerificationApi extends BaseAPI {
	
	private ResultVerificationService resultVerificationService = new ResultVerificationService() ;
	
	/**
	 * 
	* @Title: getMarkListByPosition
	* @Description:点击position查看标注信息
	* @author: FengTao
	* @date 2020年9月15日 上午9:38:17
	* @param entity
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
		String taskId = request.getParameter("taskId") ;
		String result = CheckParametersUtil.getInstance()
								.put(entity.getAttrMappingId(),"attrMappingId")
								.put(entity.getRowIndex(),"rowIndex")
								.put(entity.getFromId(),"fromId")
								.put(entity.getToId(), "toId")
								.put(entity.getEvalId(), "evalId")
					            .checkParameter();
		if(result != null){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "保存失败,请检查表单完整性") ;
			return  retMap;
		}	
		
		params.put("taskId", taskId ) ;
		params.put("evalId", entity.getEvalId() ) ;
		params.put("labelId", entity.getLabelId() ) ;
		params.put("attrMappingId", entity.getAttrMappingId() ) ;
		params.put("rowIndex", entity.getRowIndex() ) ;
		params.put("fromId", entity.getFromId() ) ;
		params.put("toId", entity.getToId() ) ;
		resultVerificationService.getMarkListByPosition(retMap,params);
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
		String isCommon = request.getParameter("isCommon") ; //是否共同标注 01 共同，02 非共同
		String exeUserId = request.getParameter("exeUserId") ;
		String pageSize = request.getParameter("pageSize") ; //分页参数
		String pageCount = request.getParameter("pageCount") ; //分页参数
		String attrMappingId = request.getParameter("attrMappingId") ;
		if(!StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			if("all".equalsIgnoreCase(isCommon)){
				isCommon = "" ;
			}
			//获取字段规范实体信息
			params.put("pageSize", pageSize) ;
			params.put("pageCount", pageCount) ;
			params.put("taskId", taskId) ;
			params.put("isCommon", isCommon) ;
			params.put("attrMappingId", attrMappingId) ;
			params.put("exeUserId", exeUserId) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			//根据条件查询信息
			if("01".equals(isCommon)){ //查询共同标注的信息
				resultVerificationService.getFusionMarkDataList(retMap, params);	
			}else if("02".equals(isCommon)){ //查询非共同标注的信息
				if(StringUtils.isEmptyOrWhitespaceOnly(exeUserId)){
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "非共同标注，请选择需要查询的用户！") ;
				}else{
					resultVerificationService.getFusionMarkDataListNotComment(retMap, params) ;
				}
			}
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
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
	@Path("/check/submit")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> submitAndBack(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		String backUserIds = request.getParameter("backUserIds") ;
		String submitUserIds = request.getParameter("submitUserIds") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			params.put("backUserIds", backUserIds) ;
			params.put("submitUserIds", submitUserIds) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			params.put("stateCode", "400000") ; 
			resultVerificationService.submit(retMap, params);
		}
		return retMap;
	}
	
	/**
	 * 
	* @Title: getUsers
	* @Description: 查询结果校验阶段的用户信息
	* @author: FengTao
	* @date 2020年9月3日 下午4:00:55
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/query/users")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getUsers(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		params.put("taskId", taskId) ;
		resultVerificationService.getUsers(retMap,params) ;
		return retMap ;
	}
	
	/**
	 * 
	* @Title: submitConfirm
	* @Description:校验完成，任务结束
	* @author: FengTao
	* @date 2020年7月27日 上午9:19:03
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/submitConfirm")
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
		resultVerificationService.getPublishMsg(retMap, params);
		return retMap;
	}
}
