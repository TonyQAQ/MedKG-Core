package com.sunsheen.hkks.task.unstructure.mtc.service;

import java.util.ArrayList;
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
import com.sunsheen.hkks.task.unstructure.common.entity.AnnotateEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.TrainMarkInfoEntity;
import com.sunsheen.hkks.task.unstructure.common.utils.AnnotionEntityUtils;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

public class MarkTrainningCheckService {

	/**
	 * 
	* @Title: getFusionMarkDataList
	* @Description:获取融合的数据标注列表信息
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
			Object trainCount = session.createDySQLQuery("MTC.queryTrainCount", params).uniqueResult();
			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), trainCount.toString());
			//分页查询训练数据信息，通过isCheck过滤
			data = session.createDySQLQuery("MTC.queryTrainAttrIndex", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
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
					if(annotation != null && (annotation.getLabelCategories()==null || annotation.getLabelCategories().size() <= 0)){
						LabelCategoriesEntity temp = new LabelCategoriesEntity();
						temp.setId("originId");
						temp.setText("初始化");
						temp.setColor("#ffff");
						temp.setBorderColor("#ffff") ;
						annotation.setLabelCategories(new ArrayList<LabelCategoriesEntity>() );
						annotation.getLabelCategories().add(temp) ;
					}
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
	* @Title: updateTag
	* @Description:更新标注训练的状态
	* @author: FengTao
	* @date 2020年8月20日 下午4:09:34
	* @param retMap
	* @param params void
	* @version
	 */
	public void updateTag(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			Object num =   session.createDySQLQuery("MTC.updateTag", params).executeUpdate() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if("0".equals(num.toString())){
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "操作失败") ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "操作成功") ;
			}
		}catch(Exception e){
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
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ; //封装好的数据信息
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("MTC.markHead")));
		try{
			data =   session.createDySQLQuery("MTC.statMarkListByPosition", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
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
	* @Title: getPublishMsg
	* @Description: 发布确认msg
	* @author: FengTao
	* @date 2020年8月21日 下午12:32:28
	* @param retMap
	* @param params void
	* @version
	 */
//	@SuppressWarnings("unchecked")
	public void getPublishMsg(Map<String,Object> retMap , Map<String,Object> params){
//		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> temp = new HashMap<String , Object>() ;
		Map<String , Object> data = new HashMap<String , Object>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			//data = (Map<String, Object>) session.createDySQLQuery("MTC.queryPublishSubmitMsg", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(data != null && data.size() > 0 && !"0".equals((String)data.get("totalCount")) ){
				temp.put("publishState","00");
				temp.put("publishComfirm", "还有【"+"???"+"】条数据未标注，确认提交吗？") ;
			}else{
				temp.put("publishState","00");
				temp.put("publishComfirm", "提交后不可撤回，确认提交吗？") ;
			}
			retMap.put("data", temp) ;	
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
	}

}
