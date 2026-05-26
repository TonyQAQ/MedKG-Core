package com.sunsheen.hkks.task.unstructure.fm.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.Transformers;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.uitl.algorithm.AlgorithmUtils;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.FilterRulesUtils;
import com.sunsheen.hkks.common.util.ListSortUtils;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.database.mgr.service.DataBaseUtilService;
import com.sunsheen.hkks.task.unstructure.common.entity.AnnotateEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.SchemaJsonEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.TrainMarkInfoEntity;
import com.sunsheen.hkks.task.unstructure.common.utils.AnnotionEntityUtils;
import com.sunsheen.hkks.task.unstructure.fm.entity.HandleUsAssignEntity;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

public class FormalMarkService {

	/**
	 * 
	* @Title: getMarkDataList
	* @Description: 获取正式标注分配的数据信息，用于正式标注
	* @author: FengTao
	* @date 2020年8月28日 上午11:28:57 void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getFmMarkDataList(Map<String,Object> retMap , Map<String,Object> params){
		DataBaseUtilService dataBaseUtilService = new DataBaseUtilService() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ; //实体标签
		List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; //关系标签
		List<Map<String,Object>> randomDataList = new ArrayList<Map<String,Object>>() ; //共同标注信息
		List<Map<String,Object>> rangeDataList = new ArrayList<Map<String,Object>>() ; //范围分配的标注信息
		List<TrainMarkInfoEntity> markInfoEntityList = new ArrayList<TrainMarkInfoEntity>() ; //正式标注复用训练标注实体
		String attrMappingId = params.get("attrMappingId")+"" ;
		String fileBasePath = Configs.get("AnnotationFilePath.trainJsonFile");
		Integer totalCount = 0  ;
		try{
			//查询schema信息
			labelCategories = session.createDySQLQuery("LT.selectLabelCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			connectionCategories = session.createDySQLQuery("LT.selectConnectionCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			//根据字段信息，查询随机分配的任务信息，范围分配的任务信息
			String evalId =   session.createDySQLQuery("FM.queryEvalId", params).uniqueResult()+"" ;
			String fileRealPath = fileBasePath+params.get("taskId")+"/"+evalId+"/"+params.get("userId")+"/";  //正式标注训练阶段的json信息路径
			List<String> fileList = FileUtils.getAllFile(fileRealPath, true);
			if( "01".equals(params.get("isMark")) ){ //01 已标注,不用查数据库
				totalCount = fileList.size() ; //数据总量
				PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount+"");
				Integer startIndex = Integer.parseInt(params.get("pageIndex")+"");
				Integer endIndex = (Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"") ) > fileList.size() ? fileList.size() :
					 Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"");
				fileList = fileList.subList(startIndex , endIndex) ;
				for(String fileAddr : fileList){
					AnnotateEntity annotation = new AnnotateEntity() ;
					TrainMarkInfoEntity trainMarkInfoEntity = new TrainMarkInfoEntity() ;
					String rowIndex = fileAddr.split("\\[")[1].split("\\]")[0] ;
					annotation = JSONObject.parseObject(FileUtils.ReadFile(fileAddr),AnnotateEntity.class) ;
					AnnotionEntityUtils.judgeandAddInitLabelCategories(annotation); //判断是否存在labelCategores ，不存在则初始化一个，否则什么都不做
					trainMarkInfoEntity.setAnnotation(annotation);
					trainMarkInfoEntity.setAttrMappingId(attrMappingId);
					trainMarkInfoEntity.setRowIndex(rowIndex);
					trainMarkInfoEntity.setEvalId(evalId);
					markInfoEntityList.add(trainMarkInfoEntity) ;
				}
			}else{//查询未标注和全部
				List<String> fileMarkFileNames = new ArrayList<String>() ;  //该字段所有的数据
				List<Map<String,Object>> filters = new ArrayList<Map<String,Object>>() ; //该字段的过滤规则
				Map<String,Object> columnResultMap = new HashMap<String,Object>() ;
				String filterSql = "" ;
				filters = session.createDySQLQuery("LT.queryFilter", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
				filterSql = FilterRulesUtils.linkFilters((String)params.get("columnName"),filters) ;
				if(!StringUtils.isEmptyOrWhitespaceOnly(filterSql)){
					filterSql = " and " + filterSql ;
				}
				params.put("filterSql", filterSql) ;
				randomDataList = session.createDySQLQuery("FM.queryMarkAttrIndex", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
				rangeDataList = session.createDySQLQuery("FM.queryRangeIndex", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
				for(Map<String,Object> randomMap : randomDataList ){
					fileMarkFileNames.add(attrMappingId+"["+randomMap.get("rowIndex")+"].json") ;
				}
				Integer assignCount = 0 ;
				Integer rowStart = 0 ;
				if(rangeDataList.size() > 0){
					assignCount = Integer.parseInt(rangeDataList.get(0).get("assignCount")+"") ;
					rowStart = Integer.parseInt(rangeDataList.get(0).get("rowStart")+"") ; //从分配的位置开始
					for(int i = rowStart ; i < assignCount+rowStart ; i ++){
						fileMarkFileNames.add(attrMappingId+"["+i+"].json") ;
					}
				}
				if( "02".equals(params.get("isMark")) ){ //02 未标注
					FileUtils.removeHasFileName(fileList, fileMarkFileNames); //经处理后，只留下未标注的文件信息
					totalCount = fileMarkFileNames.size() ; //数据总数
				}else{
					totalCount = fileMarkFileNames.size() ;
				}
				PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount+""); //封装分页参数
				Integer startIndex = Integer.parseInt(params.get("pageIndex")+"");
				Integer pageSize = Integer.parseInt(params.get("pageSize")+"") ;
				Integer endIndex = ( startIndex+pageSize ) > fileMarkFileNames.size() ? fileMarkFileNames.size() : (startIndex+pageSize) ;
				fileMarkFileNames = fileMarkFileNames.subList(startIndex , endIndex) ;
				List<String> idList = new ArrayList<String>() ;
				String rowIndexs = "" ; //处理出rowIndexs
				for( String fileName : fileMarkFileNames ){
					String rowIndex = fileName.split("\\[")[1].split("\\]")[0] ;
					String markFileName = attrMappingId +"["+rowIndex+"].json" ;
					AnnotateEntity annotation = new AnnotateEntity() ;
					TrainMarkInfoEntity trainMarkInfoEntity = new TrainMarkInfoEntity() ;
					for(String str : fileList ){
						if(str.endsWith(markFileName)){ //有这个文件
							annotation = JSONObject.parseObject(FileUtils.ReadFile(str),AnnotateEntity.class) ;
							annotation.setLabelCategories(labelCategories);
							annotation.setConnectionCategories(connectionCategories);
							AnnotionEntityUtils.judgeandAddInitLabelCategories(annotation);
							trainMarkInfoEntity.setAnnotation(annotation);
							trainMarkInfoEntity.setAttrMappingId(attrMappingId);
							trainMarkInfoEntity.setRowIndex(rowIndex);
							trainMarkInfoEntity.setEvalId(evalId);	
							markInfoEntityList.add(trainMarkInfoEntity) ;
						}
					}
					if( annotation == null || StringUtils.isEmptyOrWhitespaceOnly(annotation.getContent()) ){
						 addUniqueString(idList,rowIndex) ;
					}
				}
				ListSortUtils.sortListByValueAsc(idList);
				rowIndexs = getDistinctByFor(idList) ;
				if(!StringUtils.isEmptyOrWhitespaceOnly(rowIndexs)){ //当有文件未标注即rowIndexs不为空，则去数据库查询
					params.put("pageIndexs", rowIndexs ) ;
					columnResultMap = dataBaseUtilService.getColumnDataByRowIndexs(retMap, params) ;
					String[] pageIndex = rowIndexs.replace("'", "").split(",") ;
					int count = 0 ;
					for(Object o : (List<Object>)columnResultMap.get("data")){ //随机标注的数据信息
						AnnotateEntity annotation = new AnnotateEntity() ;
						TrainMarkInfoEntity trainMarkInfoEntity = new TrainMarkInfoEntity() ;
						annotation.setContent( o +"");
						annotation.setLabels(new ArrayList<>());
						annotation.setConnections(new ArrayList<>());
						annotation.setLabelCategories(labelCategories);
						annotation.setConnectionCategories(connectionCategories);
						AnnotionEntityUtils.judgeandAddInitLabelCategories(annotation);
						trainMarkInfoEntity.setAnnotation(annotation);
						trainMarkInfoEntity.setAttrMappingId(attrMappingId);
						trainMarkInfoEntity.setRowIndex(pageIndex[count]);
						trainMarkInfoEntity.setEvalId(evalId);	
						markInfoEntityList.add(trainMarkInfoEntity) ;
						count ++ ;
					}
				}
			}
			sortEntityList(markInfoEntityList) ;
			retMap.put("labelCategories",labelCategories) ;
			retMap.put("connectionCategories",connectionCategories) ;
			retMap.put("data", markInfoEntityList) ;
			retMap.put("totalCount", totalCount) ;
			if( markInfoEntityList!=null && markInfoEntityList.size()>0 ){ //查询到数据
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "查询成功,无数据") ;
			}
		}catch(Exception e ){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: markDistribution
	* @Description: 正式标注任务的分配
	* @author: FengTao
	* @date 2020年8月27日 下午4:55:04
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void markDistribution(Map<String,Object> retMap , Map<String,Object> params){
		DataBaseUtilService dataBaseUtilService = new DataBaseUtilService() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ; //属性ID的数据信息
		List<Map<String,Object>> filters = new ArrayList<Map<String,Object>>() ; //该字段的过滤规则
		Map<String,Integer> columnsCountMap = new HashMap<String,Integer>() ;
		List<String> users = new ArrayList<String>() ;
		List<String> rangeList = new ArrayList<String>() ;
		List<HandleUsAssignEntity> handleUsAssignEntityList = new ArrayList<HandleUsAssignEntity>() ;
		//参数  trainCount,任务ID，userId
		try{
			//查询该任务下的所有原表名、字段名信息 
			data = session.createDySQLQuery("LT.queryAttrMapIdByTaskID", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			String tableName = "" ; //表名
			String columnName = "" ; //字段名
			String attrMappingId = "" ; //属性ID
			if( data!=null && data.size()>0 ){
				for(Map<String,Object> map : data){
					//for循环查询字段信息下的过滤规则
				    //查询出逐个字段的总数信息
				    //向arrayList添加元素（AttrMappingId+"AA--AA"+rowIndex）
					//随机筛选list元素，插入数据库，作为分配的标注训练任务
					Integer count = 0 ;
					String filterSql = "" ;
					tableName = map.get("tableOrigin")+"" ;
					columnName = map.get("columnName")+"" ;
					attrMappingId = map.get("attrMappingId") + "" ;
					params.put("tableName", tableName) ;
					params.put("columnName", columnName) ;
					params.put("attrMappingId", attrMappingId) ;
					filters = session.createDySQLQuery("LT.queryFilter", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
					filterSql = FilterRulesUtils.linkFilters((String)params.get("columnName"),filters) ;
					if(!StringUtils.isEmptyOrWhitespaceOnly(filterSql)){
						filterSql = " and " + filterSql ;
					}
					params.put("filterSql", filterSql) ;
					count = Integer.parseInt( dataBaseUtilService.getColumnDataCountByFilter(retMap, params) ) ;
					columnsCountMap.put(attrMappingId, count) ;
				}
				users = session.createDySQLQuery("FM.queryExeUsers", params).list() ;
				//获取随机分配训练数据的下标范围信息
				rangeList = AlgorithmUtils.randomRangeByMap(columnsCountMap, users) ;
				for(String str : rangeList){
					String[] elements = str.split("--AA--") ;
					HandleUsAssignEntity handleUsAssignEntity = new HandleUsAssignEntity() ;
					handleUsAssignEntity.setAssignId(null);
					handleUsAssignEntity.setAttrMappingId(elements[0]);
					handleUsAssignEntity.setUserId(elements[1]);
					handleUsAssignEntity.setRowStart(elements[2]);
					handleUsAssignEntity.setRowEnd(elements[3]);
					handleUsAssignEntity.setAssignCount(elements[4]);
					handleUsAssignEntity.setLastUpdateDate(new Date(System.currentTimeMillis()));
					handleUsAssignEntity.setTaskId(params.get("taskId")+"");
					handleUsAssignEntityList.add(handleUsAssignEntity) ;
				}
				saveOrUpdateHandleUsAssignEntity(retMap,handleUsAssignEntityList) ;
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "分配成功！") ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "该任务未选择任何标注的字段信息！") ;
			}
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	public void saveOrUpdateHandleUsAssignEntity( Map<String , Object> retMap , List<HandleUsAssignEntity> entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			session.beginTransaction();
			//添加任务分配信息
			for(HandleUsAssignEntity tempEntity : entity){
				session.saveOrUpdate(tempEntity);
			}
			session.flush();
			session.commit();
			session.close();
			retMap.putAll(RetInfo.RETSUCCESS) ;
		}catch(Exception e){
			session.rollback();
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}finally{
			session.close();
		}
	}
	
	/**
	 * 
	* @Title: getSubmitMsg
	* @Description: 获取提交信息
	* @author: FengTao
	* @date 2020年8月27日 下午4:52:07
	* @param retMap
	* @param params void
	* @version
	 */
	public void getSubmitMsg(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> temp = new HashMap<String , Object>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		String fileBasePath = Configs.get("AnnotationFilePath.trainJsonFile");
		try{
			String evalId =   session.createDySQLQuery("FM.queryEvalId", params).uniqueResult()+"" ;
			String fileRealPath = fileBasePath+params.get("taskId")+"/"+evalId+"/"+params.get("userId")+"/";  //正式标注训练阶段的json信息路径
			//String markFileName = attrMappingId+"["+rowIndex+"].json" ;
			List<String> fileList = FileUtils.getAllFile(fileRealPath, true);
			Object count01 = session.createDySQLQuery("FM.selectMarkCount", params).uniqueResult() ;
			Integer assignCount = Integer.parseInt(count01.toString()) ;
			Object count02 =  session.createDySQLQuery("FM.selectTrainCount", params).uniqueResult() ;
			Integer trainCount = Integer.parseInt(count02.toString()) ;
			Integer count = assignCount+trainCount- fileList.size() ;
			if( count > 0 ){
				temp.put("publishState","00");
				temp.put("publishComfirm", "还有【"+count+"】条数据未标注，确认提交吗？") ;
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
	/**
	 * 
	* @Title: getTables
	* @Description: 获取表信息
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getTables(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			data = session.createDySQLQuery("FM.selectMarkTables", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", data) ;
			for(Map<String,Object> temp : data){
				temp.put("isEdit", false) ;
			}
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
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
	* @Title: getTables
	* @Description: 获取字段信息
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getColumns(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			data = session.createDySQLQuery("FM.selectMarkColumns", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", data) ;
			for(Map<String,Object> temp : data){
				temp.put("isEdit", false) ;
			}
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
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
	* @Title: getGuideLineSchema
	* @Description: 获取getGuideLineSchema列表
	* @author: FengTao
	* @date 2020年8月19日 下午5:14:49
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getGuideLineSchema(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			data = session.createDySQLQuery("FM.queryGuideLineSchemaByTaskId", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			Object litleStateCode = session.createDySQLQuery("TaskMT.queryExeState", params).uniqueResult() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("litleStateCode", litleStateCode) ;
			retMap.put("data", data) ;
			for(Map<String,Object> temp : data){
				temp.put("isEdit", false) ;
			}
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
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
	* @Title: getMarkDataList
	* @Description: 通过schemaId获取标注信息，guideLine信息查询
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getGuideLineMarkDataList(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ; //封装好的数据信息
		List<SchemaJsonEntity> schemaJsonEntityList = new ArrayList<SchemaJsonEntity>() ; //标注信息列表

		List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ; //实体标签
		List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; //关系标签
		String fileBasePath = Configs.get("AnnotationFilePath.labelJsonFile");
		String fileBaseTrainPath = Configs.get("AnnotationFilePath.trainJsonFile");
		try{
			Object totalCount = session.createDySQLQuery("FM.querySchemaJsonCount", params).uniqueResult();
			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount.toString());
			//通过参数查询JSON信息,通过处理状态，用户和标签ID筛选出数据信息
			data = session.createDySQLQuery("FM.querySchemaJson", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			for(Map<String,Object> temp : data){
				params.put("labelId", temp.get("labelId")) ;
				params.put("taskId", temp.get("taskId")) ;
				String evalId =   session.createDySQLQuery("FM.queryEvalIdWhenTrain", params).uniqueResult()+"" ;
				List<String> labelCateIds = new ArrayList<String>() ;
				String fromId = temp.get("fromId")+"";
				String toId = temp.get("toId")+"" ;
				labelCateIds.add(temp.get("labelId")+"") ;
				AnnotateEntity annotation = new AnnotateEntity() ;
				String fileRealPath = fileBasePath+temp.get("taskId")+File.separator+temp.get("userId")+File.separator;  //规范制定阶段的json信息路径
				String fileRealTrainPath = fileBaseTrainPath+temp.get("taskId")+File.separator+evalId+File.separator+temp.get("userId")+File.separator;  //标注训练阶段的json信息路径
				String fileName = temp.get("markFileName")+"" ;
				annotation = JSONObject.parseObject(FileUtils.ReadFile(fileRealPath+fileName),AnnotateEntity.class) ; //先从fileRealPath获取
				if(annotation == null || StringUtils.isEmptyOrWhitespaceOnly(annotation.toString())){
					annotation = JSONObject.parseObject(FileUtils.ReadFile(fileRealTrainPath+fileName),AnnotateEntity.class) ; 
				}
				labelCategories = session.createDySQLQuery("USC.selectLabelCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
				connectionCategories = session.createDySQLQuery("USC.selectConnectionCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
				//初始化信息
				//依据fromId/toId/标签ID ，截取字符信息
				if("01".equals(temp.get("typeCode"))){ //实体标签
					//创建实体标签实体
					AnnotionEntityUtils.subAnnotionByEntityFromAndTo(annotation, labelCateIds, Integer.parseInt(fromId), Integer.parseInt(toId)) ;
					annotation.setLabelCategories(labelCategories);
				}else if("02".equals(temp.get("typeCode"))){ //关系标签
					annotation.setConnectionCategories(connectionCategories);
					//将fromId,toId替换真实Id
					String[] ids = AnnotionEntityUtils.replaceFromToId(annotation, labelCateIds, fromId, toId) ;
					AnnotionEntityUtils.subAnnotionByRelFromAndTo(annotation, labelCateIds, Integer.parseInt(ids[0]) , Integer.parseInt(ids[1]) ) ;
				}
				SchemaJsonEntity schemaJsonEntity = new SchemaJsonEntity() ;
				schemaJsonEntity.setSampleId(temp.get("sampleId")+"");
				schemaJsonEntity.setTag(temp.get("tag")+"");
				schemaJsonEntity.setAnnotation(annotation);
				schemaJsonEntityList.add(schemaJsonEntity) ;
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", schemaJsonEntityList) ;
			retMap.put("totalCount", totalCount) ;
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
		}catch(Exception e){
			retMap.put("data", schemaJsonEntityList) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "查询失败") ;
			e.printStackTrace();
		}
	}
	
	private String getDistinctByFor(List<String> idSet){
        String rowIndexs = "" ;
		if(idSet.size() > 0){
			for(String id : idSet){
				rowIndexs += "'"+id +"'," ;
			}
			rowIndexs = rowIndexs.substring(0, rowIndexs.length()-1) ;
		}
		return rowIndexs ;
    } 
	
	private void addUniqueString(List<String> idList,String rowIndex){
		if(!idList.contains((Object)rowIndex)){
			idList.add(rowIndex) ;
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
