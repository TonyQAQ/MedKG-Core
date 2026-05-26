package com.sunsheen.hkks.task.unstructure.rv.service;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.FilterRulesUtils;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.database.mgr.service.DataBaseUtilService;
import com.sunsheen.hkks.task.taskexecute.service.TaskExecuteService;
import com.sunsheen.hkks.task.taskready.service.TaskReadyService;
import com.sunsheen.hkks.task.unstructure.common.entity.AnnotateEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.RVUserAndAnnotationEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.TrainMarkInfoEntity;
import com.sunsheen.hkks.task.unstructure.common.utils.AnnotionEntityUtils;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.system.security.login.Session;
import com.sunsheen.jfids.util.DataBaseUtil;

/**
 * 
 * @Title: ResultVerificationService
 * @Description: RDF 校验
 * @author: FengTao
 * @date 2020年7月31日 下午6:07:32
 */
public class ResultVerificationService {

	/**
	 * 
	* @Title: getFusionMarkDataList
	* @Description:获取融合的数据标注列表信息，结果校验阶段
	* @author: FengTao
	* @date 2020年8月26日 下午3:24:30
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getFusionMarkDataListNotComment(Map<String,Object> retMap , Map<String,Object> params){
		DataBaseUtilService dataBaseUtilService = new DataBaseUtilService() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> filters = new ArrayList<Map<String,Object>>() ; //该字段的过滤规则
		Map<String,Object> data = new HashMap<String,Object>() ; //封装好的数据信息
		Map<String,Object> columnResultMap = new HashMap<String,Object>() ; //数据查询结果
		List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ; //实体标签
		List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; //关系标签
		List<TrainMarkInfoEntity> markInfoEntityList = new ArrayList<TrainMarkInfoEntity>() ; //训练标注信息列表
		String fileBasePath = Configs.get("AnnotationFilePath.trainJsonFile");
		try{
			labelCategories = session.createDySQLQuery("LT.selectLabelCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			connectionCategories = session.createDySQLQuery("LT.selectConnectionCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			//通过字段attrMappingId，用户userId查询，标注index范围，evalId信息
			Object evalId =  session.createDySQLQuery("USRV.queryEvalId", params).uniqueResult() ;
			String fileRealPath = fileBasePath+params.get("taskId")+"/"+evalId+"/"+params.get("exeUserId")+"/"; //文件列表信息
			data = (Map<String, Object>) session.createDySQLQuery("USRV.queryIndexRangeByAttrMappingIdAndUser", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			List<String> fileList = FileUtils.getAllFile(fileRealPath, true);
			Integer totalCount = Integer.parseInt(data.get("assignCount")+"") ;
			if( data != null && data.size() > 0 ){
				PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount+"");//封装分页参数
				Integer start = Integer.parseInt(params.get("pageIndex")+"") + Integer.parseInt(data.get("rowStart")+"") ;
				Integer tempEnd =  Integer.parseInt(data.get("rowEnd")+"") ;
				Integer size = Integer.parseInt(params.get("pageSize")+"") ;
				Integer end = (size+start) > tempEnd ? (tempEnd+1) : (size+start) ;
				for(int index = start ; index < end ; index ++){
					AnnotateEntity annotation = new AnnotateEntity() ;
					String markFileName = data.get("attrMappingId")+"["+index+"].json" ;
					for(String str : fileList ){
						if(str.endsWith(markFileName)){ //有这个文件
							annotation = JSONObject.parseObject(FileUtils.ReadFile(str),AnnotateEntity.class) ;
						}
					}
					if(annotation!=null && annotation.getContent() != null ){
					}else{
						params.put("columnName", data.get("columnName")) ;
						params.put("tableName", data.get("tableName")) ;
						params.put("pageIndex", index) ;
						//查询过滤规则，拼接处filterSql，放入参数params中
						String filterSql = "" ;
						filters = session.createDySQLQuery("LT.queryFilter", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
						filterSql = FilterRulesUtils.linkFilters((String)params.get("columnName"),filters) ;
						if(!StringUtils.isEmptyOrWhitespaceOnly(filterSql)){
							filterSql = " and " + filterSql ;
						}
						params.put("filterSql", filterSql) ;
						columnResultMap = dataBaseUtilService.getColumnDataByRowIndex(retMap, params);
						annotation.setContent( ((List<Object>) columnResultMap.get("data")).get(0) +"");
						annotation.setLabels(new ArrayList<>());
						annotation.setConnections(new ArrayList<>());
						annotation.setLabelCategories(labelCategories);
						annotation.setConnectionCategories(connectionCategories);
					}
					AnnotionEntityUtils.judgeandAddInitLabelCategories(annotation); //初始化信息
					TrainMarkInfoEntity entity = new TrainMarkInfoEntity() ;
					entity.setAttrMappingId(params.get("attrMappingId")+"");
					entity.setRowIndex(index+"");
					entity.setAnnotation(annotation);
				    entity.setEvalId(evalId+"");
					markInfoEntityList.add(entity) ;
					sortEntityList(markInfoEntityList) ;
				}
			}
			retMap.put("labelCategories",labelCategories) ;
			retMap.put("connectionCategories",connectionCategories) ;
			retMap.put("data", markInfoEntityList) ;
			retMap.put("totalCount", totalCount) ;
			if("01".equals(columnResultMap.get("retcode")) ){ //查询到数据
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "查询成功,无数据") ;
			}
		}catch(Exception e){
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: getFusionMarkDataList
	* @Description:获取融合的数据标注列表信息，结果校验阶段
	* @author: FengTao
	* @date 2020年8月26日 下午3:24:30
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getFusionMarkDataList(Map<String,Object> retMap , Map<String,Object> params){
		DataBaseUtilService dataBaseUtilService = new DataBaseUtilService() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> filters = new ArrayList<Map<String,Object>>() ; //该字段的过滤规则
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ; //封装好的数据信息
		Map<String,Object> columnResultMap = new HashMap<String,Object>() ; //数据查询结果
		List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ; //实体标签
		List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; //关系标签
		List<TrainMarkInfoEntity> markInfoEntityList = new ArrayList<TrainMarkInfoEntity>() ; //训练标注信息列表
		String fileBasePath = Configs.get("AnnotationFilePath.trainJsonFile");
		try{
			//查询schema信息
			labelCategories = session.createDySQLQuery("LT.selectLabelCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			connectionCategories = session.createDySQLQuery("LT.selectConnectionCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			//查询训练总数queryTrainCount
			Object trainCount = session.createDySQLQuery("USRV.queryMarkCount", params).uniqueResult();
			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), trainCount.toString());
			//分页查询训练数据信息，通过isCheck过滤
			data = session.createDySQLQuery("USRV.queryMarkAttrIndex", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			if(data!=null && data.size()>0){
				String fileRealPath = fileBasePath+params.get("taskId")+"/"+data.get(0).get("evalId")+"/";  //标注训练段的json信息路径
				List<String> fileList = FileUtils.getAllFile(fileRealPath, true);
				//遍历data信息逐条查询
				for(Map<String,Object> dataMap : data){
					List<AnnotateEntity> entityList = new ArrayList<AnnotateEntity>() ;
					AnnotateEntity annotation = new AnnotateEntity() ;
					params.put("columnName", dataMap.get("columnName")) ;
					params.put("tableName", dataMap.get("tableName")) ;
					params.put("pageIndex", dataMap.get("rowIndex")) ;
					String attrMappingId = dataMap.get("attrMappingId")+"" ;
					params.put("attrMappingId", attrMappingId) ;
					String rowIndex = dataMap.get("rowIndex")+"" ;
					String evalId = dataMap.get("evalId")+"" ;
					String markFileName = dataMap.get("attrMappingId")+"["+dataMap.get("rowIndex")+"].json" ;
					for(String str : fileList ){
						if(str.endsWith(markFileName)){ //有这个文件
							entityList.add(JSONObject.parseObject(FileUtils.ReadFile(str),AnnotateEntity.class)) ;
						}
					}
					if(entityList!=null && entityList.size() > 0 ){
						annotation = AnnotionEntityUtils.fusionAnnotionAllFromAnnotionList(entityList) ;
					}else{
						//查询过滤规则，拼接处filterSql，放入参数params中
						String filterSql = "" ;
						filters = session.createDySQLQuery("LT.queryFilter", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
						filterSql = FilterRulesUtils.linkFilters((String)params.get("columnName"),filters) ;
						if(!StringUtils.isEmptyOrWhitespaceOnly(filterSql)){
							filterSql = " and " + filterSql ;
						}
						params.put("filterSql", filterSql) ;
						columnResultMap = dataBaseUtilService.getColumnDataByRowIndex(retMap, params);
						annotation.setContent( ((List<Object>) columnResultMap.get("data")).get(0) +"");
						annotation.setLabels(new ArrayList<>());
						annotation.setConnections(new ArrayList<>());
						annotation.setLabelCategories(labelCategories);
						annotation.setConnectionCategories(connectionCategories);
					}
					AnnotionEntityUtils.judgeandAddInitLabelCategories(annotation);
					//查询出相应的数据信息
					TrainMarkInfoEntity entity = new TrainMarkInfoEntity() ;
					entity.setAttrMappingId(attrMappingId);
					entity.setRowIndex(rowIndex);
					entity.setAnnotation(annotation);
				    entity.setEvalId(evalId);
					markInfoEntityList.add(entity) ;
				}
			}
			retMap.put("labelCategories",labelCategories) ;
			retMap.put("connectionCategories",connectionCategories) ;
			retMap.put("data", markInfoEntityList) ;
			retMap.put("totalCount", trainCount) ;
			if("01".equals(columnResultMap.get("retcode")) ){ //查询到数据
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "查询成功,无数据") ;
			}
		}catch(Exception e){
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: getMarkListByPosition
	* @Description: 点击标注标签，弹出的详情页信息
	* @author: FengTao
	* @date 2020年8月27日 上午9:29:09
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getMarkListByPosition(Map<String,Object> retMap , Map<String,Object> params){
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ; //封装好的数据信息
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> users = new ArrayList<Map<String,Object>>() ; 
		List<RVUserAndAnnotationEntity> entityList = new ArrayList<RVUserAndAnnotationEntity>() ; //文件实体列表信息
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("MTC.markHead")));
		String fileBasePath = Configs.get("AnnotationFilePath.trainJsonFile")+params.get("taskId")+File.separator+params.get("evalId")+File.separator;
		String markFileName = params.get("attrMappingId")+"["+params.get("rowIndex")+"].json" ;
		try{
			users = session.createDySQLQuery("USRV.queryUserByEvalId", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			if(users != null && users.size() > 0){
				for(Map<String,Object> user : users){
					String address = fileBasePath + user.get("userId") + File.separator + markFileName ;
					AnnotateEntity annotation = JSONObject.parseObject(FileUtils.ReadFile( address ),AnnotateEntity.class) ;
					if(annotation != null && annotation.toString() != ""){
						RVUserAndAnnotationEntity rvUserAndAnnotationEntity = new RVUserAndAnnotationEntity() ;
						rvUserAndAnnotationEntity.setAnnotation(annotation);
						rvUserAndAnnotationEntity.setUserId(user.get("userId")+"");
						rvUserAndAnnotationEntity.setUserName(user.get("userName")+"");
						entityList.add(rvUserAndAnnotationEntity) ;
					}
				}
				if(entityList!=null && entityList.size() > 0 ){
					data = AnnotionEntityUtils.getStatInfoByFromAndTo( entityList , params.get("fromId")+"" , params.get("toId")+"" ) ;
				}
			}
			retMap.put("data", data) ;
			if(data!=null && data.size() > 0){
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "查询成功，暂无数据") ;
			}
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: submit
	* @Description: 任务提交，部分提交的时候，任务大状态不变
	* @author: FengTao
	* @date 2020年9月3日 下午5:38:06
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void submit(Map<String,Object> retMap , Map<String,Object> params){
		DataBaseUtilService dataBaseUtilService = new DataBaseUtilService() ;
		TaskReadyService taskReadyService = new TaskReadyService() ;
		TaskExecuteService taskExecuteService = new TaskExecuteService() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> assignIds = new ArrayList<Map<String,Object>>() ;
		List<Map<String,Object>> overIds = new ArrayList<Map<String,Object>>() ;
		try{
			//任务结果校验完成，则任务进入任务结束阶段400000
			//判断该未进入任务完成状态的合数，count>0 否则不做任务提交
			if(!StringUtils.isEmptyOrWhitespaceOnly(params.get("backUserIds")+"")){ //部分人回退
				String[] backUserIds = params.get("backUserIds").toString().split(",") ;
				for(String userId : backUserIds){
					params.put("userId", userId) ;
					taskExecuteService.addExeProcessBackState(retMap, params); //只回退执行者单个状态信息
				}
			}
			if(!StringUtils.isEmptyOrWhitespaceOnly(params.get("submitUserIds")+"")){ //部分人提交
				String[] backUserIds = params.get("submitUserIds").toString().split(",") ;
				for(String userId : backUserIds){
					params.put("userId", userId) ;
					System.out.print(userId);
					taskExecuteService.addExeProcessStateByPreCode(retMap, params); //只更新执行者单个状态信息
				}
			}
			//任务参与人分配ID
			assignIds = session.createDySQLQuery("USRV.queryUserByTaskId", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ; //参与的人员ID
			//任务完成人分配Id
			overIds =  session.createDySQLQuery("USRV.queryOverIds", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ; //参与的人员userId
			System.out.print(assignIds+"=----="+overIds);
			if(assignIds.size() == overIds.size()){ //相等，则所有人都完成了
				//修改任务大状态
				params.put("userId", Session.getCurrUser().getId() ) ; //用户id切换为执行人ID信息
				taskReadyService.updateTaskBigState(retMap, params);
				if("01".equals( retMap.get("code") )){//发布任务，发布失败则不销毁数据源
					dataBaseUtilService.destroyPool(retMap, params);
				}else{
					retMap.putAll(RetInfo.RETFAIL);
				}
			}
			retMap.putAll(RetInfo.RETSUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
	/**
	 * 
	* @Title: getUsers
	* @Description: 本次校验的用户信息
	* @author: FengTao
	* @date 2020年9月3日 下午4:01:39
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getUsers( Map<String , Object> retMap , Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			dataList = session.createDySQLQuery("USRV.queryNotOverUsers", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			retMap.put("data", dataList) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
	/**
	 * 
	* @Title: getPublishMsg
	* @Description:获取发布确认消息
	* @author: FengTao
	* @date 2020年7月22日 下午1:00:14
	* @param retMap
	* @param params void
	* @version
	 */
	public void getPublishMsg(Map<String,Object> retMap , Map<String,Object> params){
		Map<String , Object> temp = new HashMap<String , Object>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			temp.put("publishState","00");
			temp.put("publishComfirm", "发布后不可撤回，确认发布吗？") ;
			retMap.put("data", temp) ;	
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
	}
	
	private void sortEntityList(List<TrainMarkInfoEntity> markInfoEntityList){
		Collections.sort(markInfoEntityList, new Comparator<TrainMarkInfoEntity>() {
			 
            @Override
            public int compare(TrainMarkInfoEntity o1, TrainMarkInfoEntity o2) {
                if(o1 == null || o2 == null){
                    return -1;
                }
                if( Integer.parseInt(o1.getRowIndex()) > Integer.parseInt(o2.getRowIndex()) ){
                    return 1;
                }
                if( Integer.parseInt(o1.getRowIndex()) < Integer.parseInt(o2.getRowIndex()) ){
                    return -1;
                }
                if( Integer.parseInt(o1.getRowIndex()) == Integer.parseInt(o2.getRowIndex())  ){
                    return 0;
                }
                return 0;
            }
    });
	}
}
