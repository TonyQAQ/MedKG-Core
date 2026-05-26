package com.sunsheen.hkks.task.unstructure.sc.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.CheckParametersUtil;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.taskready.service.TaskReadyService;
import com.sunsheen.hkks.task.unstructure.lt.service.LabelTrainingService;
import com.sunsheen.hkks.task.unstructure.sc.entity.SchemaDefineEntity;
import com.sunsheen.hkks.task.unstructure.sc.service.StandardCheckService;
import com.sunsheen.hkks.task.unstructure.ussm.entity.CustomLabelEntity;
//import com.sunsheen.hkks.task.taskready.service.TaskReadyService;
//import com.sunsheen.hkks.task.unstructure.sc.service.StandardCheckService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

@Path("/task/us/sc")
public class StandardCheckApi  extends BaseAPI{
	
	private TaskReadyService taskReadyService = new TaskReadyService() ;
	private StandardCheckService standardCheckService = new StandardCheckService() ;
	private LabelTrainingService labelTrainingService = new LabelTrainingService() ;
	
	//GuideLine预览文件下载
	
	/**
	 * 
	* @Title: publish
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
		String trainCount = request.getParameter("trainCount") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId) || StringUtils.isEmptyOrWhitespaceOnly(trainCount) || Integer.parseInt(trainCount) <= 0){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","提交失败");
		}else{
			params.put("taskId", taskId) ;
			params.put("trainCount", trainCount) ;
			params.put("stateCode", "300000") ; //校验完毕进入知识标注阶段
			params.put("userId", Session.getCurrUser().getId()) ; 
			taskReadyService.updateTaskState(retMap, params);
			if("01".equals(retMap.get("retcode"))){ //发布成功，则进行标注任务分配，否则就不分配
				labelTrainingService.trainDistribution(retMap, params,"00"); //00是标注训练
			}
		}
		return retMap ;
	}
	
	@POST
	@Path("/search/maxTrainCount")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> maxTrainCount(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId) ){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","提交失败");
		}else{
			params.put("taskId", taskId) ;
			labelTrainingService.getMaxTrainCount(retMap, params) ;
		}
		return retMap ;
	}
	
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
		standardCheckService.getPublishMsg(retMap, params);
		return retMap;
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
	@Path("/check/search/guideLine/schema")
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
			standardCheckService.getGuideLineSchema(retMap, params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: saveOrUpdateMarkDatas
	* @Description: 自定义标签信息json保存接口,发布者新增一个自定义的标签信息
	* @author: FengTao
	* @date 2020年8月14日 下午3:24:46
	* @param entityList
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/saveOrUpdate/markDatas")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> saveOrUpdateMarkDatas(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String schemaId = request.getParameter("schemaId") ;
		String tag = request.getParameter("tag") ;
		if(!StringUtils.isEmptyOrWhitespaceOnly(schemaId) && !StringUtils.isEmptyOrWhitespaceOnly(tag)){
			params.put("schemaId", schemaId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			//获取字段规范实体信息
			//unStructureStandardMadeService.saveOrUpdateMarkDatas(retMap,entityList,params);
			}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "保存失败，请检查标注信息") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: 
	* @Description: Schema添加、修改
	* @author: FengTao
	* @date 2020年8月18日 下午5:21:39
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/saveOrUpdate/schema")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> addOrUpdateSchema(SchemaDefineEntity entity){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		CustomLabelEntity labelEntity = null ;
		//任务主体表单校验
		String result = CheckParametersUtil.getInstance()
								.put(entity.getSchemaName(),"schemaName")
								.put(entity.getTypeCode(),"typeCode")
								.put(entity.getTaskId(),"taskId")
								.put(entity.getColor(),"color")
					            .checkParameter();
		if(result != null){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "保存失败,请检查表单完整性") ;
			return  retMap;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(entity.getSchemaId())){
			entity.setSchemaId(null);
			entity.setLabelName(entity.getSchemaName());
			entity.setBorderColor("#"+randomHexStr(6));
			//构建一个label实体
			labelEntity = new CustomLabelEntity() ;
			labelEntity.setLabelId(null);
			labelEntity.setTaskId(entity.getTaskId());
			labelEntity.setLabelName(entity.getLabelName());
			labelEntity.setColor(entity.getColor());
			labelEntity.setTypeCode(entity.getTypeCode());
			labelEntity.setBorderColor(entity.getBorderColor());
			labelEntity.setLastUpdateUserId(Session.getCurrUser().getId());
			labelEntity.setLastUpdateUserName(Session.getCurrUser().getUsername());
			labelEntity.setLastUpdateDate(new Date(System.currentTimeMillis()));
		}
		//附上初始信息
		entity.setLastUpdateUserId(Session.getCurrUser().getId());
		entity.setLastUpdateUserName(Session.getCurrUser().getUsername());
		entity.setLastUpdateDate(new Date(System.currentTimeMillis()));
		//添加任务和人员信息
		standardCheckService.saveOrUpdateSchema(retMap, entity,labelEntity);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: deleteSchema
	* @Description: deleteSchema
	* @author: FengTao
	* @date 2020年8月18日 下午5:21:39
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/delete/schema")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> deleteSchema(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String schemaId = request.getParameter("schemaId") ;
		String taskId = request.getParameter("taskId") ;
		if(!StringUtils.isEmptyOrWhitespaceOnly(schemaId) && !StringUtils.isEmptyOrWhitespaceOnly(taskId) ){
			params.put("schemaId", schemaId) ;
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			standardCheckService.updateTag(retMap,params) ;
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "刪除失败") ;
		}
		standardCheckService.deleteSchema(retMap, params);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: addOrUpdateTag
	* @Description: 修改例子的状态标记信息
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
		String sampleId = request.getParameter("sampleId") ; //schema的ID
		String tag= request.getParameter("tag") ; //状态标记
		if(!StringUtils.isEmptyOrWhitespaceOnly(sampleId) && !StringUtils.isEmptyOrWhitespaceOnly(tag) ){
			params.put("tag", tag) ;
			params.put("sampleId", sampleId) ;
			standardCheckService.updateTag(retMap,params) ;
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
	@Path("/check/search/markDatas")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getMarkDataList(){
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
			standardCheckService.getMarkDataList(retMap,params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: getSchema
	* @Description: 多条件查询Schema信息接口
	* @author: FengTao
	* @date 2020年8月14日 下午3:24:46
	* @param entityList
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/check/search/schema")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getSchema(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String keyWord = request.getParameter("keyWord") ;
		String typeCode = request.getParameter("typeCode") ;
		String isSelect = request.getParameter("isSelect") ;
		if(!StringUtils.isEmptyOrWhitespaceOnly(taskId)  || StringUtils.isEmptyOrWhitespaceOnly(typeCode)){
			if("all".equalsIgnoreCase(typeCode)){
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
			standardCheckService.getSchema(retMap, params);
		}else{
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败") ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: next
	* @Description: 别名规则映射完毕，点击进行下一步
	* @author: FengTao
	* @date 2020年7月22日 上午10:02:16
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/check/next")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> next(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		String litleStateCode = request.getParameter("litleStateCode") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			if(!StringUtils.isEmptyOrWhitespaceOnly(litleStateCode) && !"200203".equals(litleStateCode)){
				retMap.putAll(RetInfo.RETSUCCESS);
				retMap.put("retmsg","已经提交，请勿重复提交" ) ;
				return retMap ;
			}
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			taskReadyService.updateTaskExeState(retMap, params); 
		}
		return retMap;
	}
	
	/**
	 * 
	* @Title: back
	* @Description: 别名规则映射完毕，点击进行下一步
	* @author: FengTao
	* @date 2020年7月22日 上午10:02:16
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/check/back")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> back(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		String litleStateCode = request.getParameter("litleStateCode") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			if(!StringUtils.isEmptyOrWhitespaceOnly(litleStateCode) && !"200204".equals(litleStateCode)){
				retMap.putAll(RetInfo.RETSUCCESS);
				retMap.put("retmsg","已经回退，请勿重复回退" ) ;
				return retMap ;
			}
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			taskReadyService.updateTaskExeBackState(retMap, params); 
		}
		return retMap;
	}
	
	/**
	 * 获取人员信息表信息
	 * @return
	 */
	@GET
	@Path("/query/users")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getUsers(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			standardCheckService.getUsers(retMap,params) ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: produceShcema
	* @Description:生成schema
	* @author: FengTao
	* @date 2020年8月19日 下午3:29:25
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/check/produce/schema")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> produceShcema(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ; //字段ID
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId() ) ;
			params.put("userName", Session.getCurrUser().getUsername()) ;
			standardCheckService.produceShcema(retMap, params); 
		}
		return retMap;
	}
	
	/**
     * 随机颜色
     *
     * @param len
     * @return
     */
    public static String randomHexStr(int len) {
        try {
            StringBuffer result = new StringBuffer();
            for (int i = 0; i < len; i++) {
                //随机生成0-15的数值并转换成16进制
                result.append(Integer.toHexString(new Random().nextInt(16)));
            }
            return result.toString().toUpperCase();
        } catch (Exception e) {
            System.out.println("获取16进制字符串异常，返回默认...");
            return "00CCCC";
        }
    }
}
