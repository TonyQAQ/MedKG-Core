package com.sunsheen.hkks.task.unstructure.lt.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.Transformers;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.uitl.algorithm.AlgorithmUtils;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.FilterRulesUtils;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.database.mgr.service.DataBaseUtilService;
import com.sunsheen.hkks.task.unstructure.common.entity.AnnotateEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.TrainMarkInfoEntity;
import com.sunsheen.hkks.task.unstructure.lt.entity.HandleItemDetailEntity;
import com.sunsheen.hkks.task.unstructure.lt.entity.HandleResultEvaluateEntity;
import com.sunsheen.hkks.task.unstructure.ussm.entity.MarkSampleInfoEntity;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.system.security.login.Session;
import com.sunsheen.jfids.util.DataBaseUtil;

public class LabelTrainingService {
	
	
	/**
	 * 
	* @Title: isAllMark
	* @Description: 查询是否还有数据未标注
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void(taskId,pageSize,pageIndex,isMark(01已标注，all全部，02，未标注))
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void isAllMark(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ; //封装好的数据信息
		Map<String , Object> temp = new HashMap<String , Object>() ;
		List<String> fileList = new ArrayList<String>() ; //标注文件列表
		String fileBasePath = Configs.get("AnnotationFilePath.trainJsonFile");
		try{
			int noMarkCount = 0  ;
			//查询训练数据信息，通过isMark过滤
			data = session.createDySQLQuery("LT.queryTrainAttrIndex", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			if(data!=null && data.size()>0){
				String fileRealPath = fileBasePath+params.get("taskId")+"/"+data.get(0).get("evalId")+"/"+params.get("userId")+"/";  //标注训练段的json信息路径
				File fileDir = new File(fileRealPath);
				File[] files = fileDir.listFiles();
				if(files!=null && files.length > 0){
					for (int i = 0; i < files.length; i++) {
						fileList.add(files[i].getName()) ;
					}
				}
				
				Iterator<Map<String,Object>> iterator = data.iterator();
				while(iterator.hasNext()){
					Map<String,Object> map = iterator.next() ;
					String markFileName = map.get("attrMappingId")+"["+map.get("rowIndex")+"].json" ;
					if(!fileList.contains(markFileName)){
						noMarkCount ++ ;	
					}
				}
			}
			if(noMarkCount > 0){
				temp.put("publishState","00");
				temp.put("publishComfirm", "还有【"+noMarkCount+"】条数据未标注，确认提交吗？") ;
			}else{
				temp.put("publishState","00");
				temp.put("publishComfirm", "提交后不可撤回，确认提交吗？") ;
			}
			retMap.put("data", temp) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
		}catch(Exception e){
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: getMarkDataList
	* @Description: 获取标注信息
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void(taskId,pageSize,pageIndex,isMark(01已标注，all全部，02，未标注))
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getMarkDataList(Map<String,Object> retMap , Map<String,Object> params){
		DataBaseUtilService dataBaseUtilService = new DataBaseUtilService() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ; //封装好的数据信息
		List<Map<String,Object>> filters = new ArrayList<Map<String,Object>>() ; //该字段的过滤规则
		Map<String,Object> columnResultMap = new HashMap<String,Object>() ; //数据查询结果
		List<Object> columnData = new ArrayList<Object>() ; //具体数据信息
		List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ; //实体标签
		List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; //关系标签
		List<TrainMarkInfoEntity> markInfoEntityList = new ArrayList<TrainMarkInfoEntity>() ; //训练标注信息列表
		List<String> fileList = new ArrayList<String>() ; //标注文件列表
		String fileBasePath = Configs.get("AnnotationFilePath.trainJsonFile");
		Integer totalCount = 0 ;
		try{
			//查询schema信息
			labelCategories = session.createDySQLQuery("LT.selectLabelCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			connectionCategories = session.createDySQLQuery("LT.selectConnectionCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			//查询训练总数queryTrainCount
			//Object trainCount = session.createDySQLQuery("LT.queryTrainCount", params).uniqueResult();
			//查询训练数据信息，通过isMark过滤
			data = session.createDySQLQuery("LT.queryTrainAttrIndex", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			if(data!=null && data.size()>0){
				String fileRealPath = fileBasePath+params.get("taskId")+"/"+data.get(0).get("evalId")+"/"+params.get("userId")+"/";  //标注训练段的json信息路径
				File fileDir = new File(fileRealPath);
				File[] files = fileDir.listFiles();
				if(files!=null && files.length > 0){
					for (int i = 0; i < files.length; i++) {
						fileList.add(files[i].getName()) ;
					}
				}
				
				Iterator<Map<String,Object>> iterator = data.iterator();
				while(iterator.hasNext()){
					Map<String,Object> map = iterator.next() ;
					String markFileName = map.get("attrMappingId")+"["+map.get("rowIndex")+"].json" ;
					if(fileList!=null && fileList.size() > 0){
						if(!fileList.contains(markFileName) && "01".equals(params.get("isMark"))){	//只留下已标注
							iterator.remove() ;
						}else if(fileList.contains(markFileName) && "02".equals(params.get("isMark"))){ //只留下未标注
							iterator.remove() ;	
						}else{ //默认都留下
							
						}
					}else{ //都是未标注的数据
						if("01".equals(params.get("isMark"))){	//只留下已标注
							iterator.remove() ;	
						}
					}
					if(fileList.contains(markFileName)){
						map.put("isMark", "01") ;
					}else{
						map.put("isMark", "02") ;
					}
				}
				//通过分页参数截取数据信息，截取data信息
				totalCount = data.size() ;
				PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount+"");
				Integer startIndex = Integer.parseInt(params.get("pageIndex")+"");
				Integer endIndex = (Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"") ) > data.size() ? data.size() :
					 Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"");
				data = data.subList(startIndex , endIndex) ;
				//遍历data信息逐条查询
				for(Map<String,Object> dataMap : data){
					params.put("columnName", dataMap.get("columnName")) ;
					params.put("tableName", dataMap.get("tableName")) ;
					params.put("pageIndex", dataMap.get("rowIndex")) ;
					String attrMappingId = dataMap.get("attrMappingId")+"" ;
					params.put("attrMappingId", attrMappingId) ;
					String rowIndex = dataMap.get("rowIndex")+"" ;
					String evalId = dataMap.get("evalId")+"" ;
					//查询过滤规则，拼接处filterSql，放入参数params中
					String filterSql = "" ;
					filters = session.createDySQLQuery("LT.queryFilter", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
					filterSql = FilterRulesUtils.linkFilters((String)params.get("columnName"),filters) ;
					if(!StringUtils.isEmptyOrWhitespaceOnly(filterSql)){
						filterSql = " and " + filterSql ;
					}
					params.put("filterSql", filterSql) ;
					//查询出相应的数据信息
					columnResultMap = dataBaseUtilService.getColumnDataByRowIndex(retMap, params);
					if("01".equals(columnResultMap.get("retcode")) ){ //成功进行了数据查询
						String markFileName = dataMap.get("attrMappingId")+"["+dataMap.get("rowIndex")+"].json" ;
						columnData = (List<Object>) columnResultMap.get("data") ;
						for(Object o : columnData){ 
							//构件每条数据的标注信息
							AnnotateEntity annotation = new AnnotateEntity() ;
							//封装数据,
							if("01".equals(dataMap.get("isMark"))){
								annotation = JSONObject.parseObject(FileUtils.ReadFile(fileRealPath+markFileName),AnnotateEntity.class) ;
							}else{
								annotation.setContent(o+"");
								annotation.setLabels(new ArrayList<>());
								annotation.setConnections(new ArrayList<>());
							}
							//先从数据库获取原来的json文件，没有则构件新的数据文件信息
							annotation.setLabelCategories(labelCategories);
							annotation.setConnectionCategories(connectionCategories);
							//特殊处理，如果labelCategories为空，则初始化一个进去
							if(labelCategories.size() <= 0){
								LabelCategoriesEntity temp = new LabelCategoriesEntity();
								temp.setId("originId");
								temp.setText("初始化");
								temp.setColor("#ffff");
								temp.setBorderColor("#ffff") ;
								labelCategories.add(temp) ;
							}
							TrainMarkInfoEntity entity = new TrainMarkInfoEntity() ;
							entity.setAttrMappingId(attrMappingId);
							entity.setRowIndex(rowIndex);
							entity.setAnnotation(annotation);
							entity.setEvalId(evalId);
							markInfoEntityList.add(entity) ;
						}
					}
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
	* @Title: saveOrUpdateMarkSampleInfo
	* @Description:标注阶段的标注信息保存
	* @author: FengTao
	* @date 2020年8月26日 上午11:06:00
	* @param retMap
	* @param entity void
	* @version
	 */
	public void saveOrUpdateMarkSampleInfo( Map<String , Object> retMap , MarkSampleInfoEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			session.beginTransaction();
			//添加或修改标签信息
			session.saveOrUpdate(entity);
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
	
	public void deleteMarkSampleInfo( Map<String , Object> retMap , List<MarkSampleInfoEntity> entityList){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> params = new HashMap<String,Object>() ;
		session.beginTransaction();
		try{
			int count = 0 ;
			for(MarkSampleInfoEntity entity : entityList){
				params.put("attrMappingId", entity.getAttrMappingId()) ;
				params.put("rowIndex", entity.getRowIndex()) ;
				params.put("labelId", entity.getLabelId()) ;
				params.put("fromId", entity.getFromId()) ;
				params.put("toId", entity.getToId()) ;
				params.put("evalId", entity.getEvalId()) ;
				params.put("userId", Session.getCurrUser().getId()) ;
				Object num = session.createDySQLQuery("LT.deleteMarkInfo", params).executeUpdate();
				count += Integer.parseInt(num+"") ;
			}
			session.flush();
			session.commit();
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if(entityList.size() != count){
				retMap.put("retmsg", "删除失败") ;
				session.rollback();
			}else{
				retMap.put("retmsg", "删除成功") ;
			}
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
	* @Title: saveOrUpdateLabels
	* @Description:保存执行者个标注信息
	* @author: FengTao
	* @date 2020年8月14日 下午2:29:01
	* @param retMap
	* @param columnsInfo void
	* @version
	 */
	public void saveOrUpdateMarkDatas(Map<String,Object> retMap , List<TrainMarkInfoEntity> entityList , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		String fileBasePath = Configs.get("AnnotationFilePath.trainJsonFile");
		String fileRealPath = fileBasePath+params.get("taskId")+"/"+params.get("evalId")+"/"+params.get("userId")+"/";  //标注训练阶段的json信息路径
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			for( TrainMarkInfoEntity entity : entityList ){
				String fileName = entity.getAttrMappingId()+"["+entity.getRowIndex()+"].json" ;
				FileUtils.createJsonFile(entity.getAnnotation(), fileRealPath+fileName) ;
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}
		}catch(Exception e){
			session.rollback();
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取最大训练量
	* @Title: getMaxTrainCount
	* @Description:
	* @author: FengTao
	* @date 2020年9月10日 下午4:59:49
	* @param retMap
	* @param params
	* @param typeCode void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getMaxTrainCount(Map<String,Object> retMap , Map<String,Object> params){
		DataBaseUtilService dataBaseUtilService = new DataBaseUtilService() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ; //属性ID的数据信息
		List<Map<String,Object>> filters = new ArrayList<Map<String,Object>>() ; //该字段的过滤规则
		try{
			//查询该任务下的所有原表名、字段名信息 
			data = session.createDySQLQuery("LT.queryAttrMapIdByTaskID", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			String tableName = "" ; //表名
			String columnName = "" ; //字段名
			String attrMappingId = "" ; //属性ID
			Integer maxTrainCount = 0  ;
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
					maxTrainCount += count ;
					retMap.putAll(RetInfo.RETSUCCESS) ;
				}
			}else{
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "该任务未选择任何标注的字段信息！") ;
			}
			retMap.put("maxTrainCount", maxTrainCount) ;
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}

	}
	
	/**
	 * 
	* @Title: trainDistribution
	* @Description:根据训练量大小，从数据库抽取相应数据
	* @author: FengTao
	* @date 2020年8月21日 下午12:36:33
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void trainDistribution(Map<String,Object> retMap , Map<String,Object> params ,String typeCode){
		DataBaseUtilService dataBaseUtilService = new DataBaseUtilService() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ; //属性ID的数据信息
		List<Map<String,Object>> filters = new ArrayList<Map<String,Object>>() ; //该字段的过滤规则
		Map<String,Integer> columnsCountMap = new HashMap<String,Integer>() ;
		List<String> indexList = new ArrayList<String>() ;
		//参数  trainCount,任务ID，userId
		Integer trainCount = Integer.parseInt( params.get("trainCount").toString() ) ;
		try{
			//查询该任务下的所有原表名、字段名信息 
			data = session.createDySQLQuery("LT.queryAttrMapIdByTaskID", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			String tableName = "" ; //表名
			String columnName = "" ; //字段名
			String attrMappingId = "" ; //属性ID
			if( data!=null && data.size()>0 ){
				HandleResultEvaluateEntity handleResultEvaluateEntity = new HandleResultEvaluateEntity() ;
				List<HandleItemDetailEntity> handleItemDetailEntityList = new ArrayList<HandleItemDetailEntity>() ;
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
					columnsCountMap.put(attrMappingId+"--AA--", count) ;
				}
				//获取随机分配训练数据的下标信息
				indexList = AlgorithmUtils.randomIndexByMap(columnsCountMap, trainCount) ;
				
				//构建并保存训练分配信息
				handleResultEvaluateEntity.setEvalId(null);
				handleResultEvaluateEntity.setIaa(null);
				handleResultEvaluateEntity.setTaskId(params.get("taskId")+"");
				handleResultEvaluateEntity.setTrainCount(trainCount+"");
				handleResultEvaluateEntity.setTypeCode(typeCode); //00是标注训练阶段，01是正式标注阶段
				handleResultEvaluateEntity.setLastUpdateUserId(Session.getCurrUser().getId());
				handleResultEvaluateEntity.setLastUpdateUserName(Session.getCurrUser().getUsername());
				handleResultEvaluateEntity.setLastUpdateDate(new Date(System.currentTimeMillis()));
				saveOrUpdateHandleResultEvaluate(retMap,handleResultEvaluateEntity) ; //插入训练信息
				if(!StringUtils.isEmptyOrWhitespaceOnly(handleResultEvaluateEntity.getEvalId())){ //保存成功
					for(String indexStr : indexList){
						String[] strs = indexStr.split("--AA--") ;
						HandleItemDetailEntity handleItemDetailEntity = new HandleItemDetailEntity() ;
						handleItemDetailEntity.setItemId(null);
						handleItemDetailEntity.setEvalId(handleResultEvaluateEntity.getEvalId());
						handleItemDetailEntity.setAttrMappingId(strs[0]);
						handleItemDetailEntity.setRowIndex(strs[1]);
						handleItemDetailEntityList.add(handleItemDetailEntity) ;
					}
				}else{
					retMap.putAll(RetInfo.RETFAIL) ;
					retMap.put("retmsg", "分配错误，请重新分配！") ;
				}
				saveOrUpdateHandleItemDetailList(retMap,handleItemDetailEntityList) ; //批量插入训练具体数据信息
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "该任务未选择任何标注的字段信息！") ;
			}
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}

	}
	
	/**
	 * 
	* @Title: saveOrUpdateHandleItemDetaiList
	* @Description:
	* @author: FengTao
	* @date 2020年8月24日 下午6:32:47
	* @param retMap
	* @param entity void
	* @version
	 */
	public void saveOrUpdateHandleItemDetailList( Map<String , Object> retMap , List<HandleItemDetailEntity> entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			session.beginTransaction();
			//添加或修改标签信息
			for(HandleItemDetailEntity tempEntity : entity){
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
	* @Title: saveOrUpdateHandleItemDetail
	* @Description:
	* @author: FengTao
	* @date 2020年8月24日 下午6:32:47
	* @param retMap
	* @param entity void
	* @version
	 */
	public void saveOrUpdateHandleItemDetail( Map<String , Object> retMap , HandleItemDetailEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			session.beginTransaction();
			//添加或修改标签信息
			session.saveOrUpdate(entity);
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
	* @Title: saveOrUpdateHandleResultEvaluate
	* @Description:
	* @author: FengTao
	* @date 2020年8月24日 下午6:31:25
	* @param retMap
	* @param entity void
	* @version
	 */
	public void saveOrUpdateHandleResultEvaluate( Map<String , Object> retMap , HandleResultEvaluateEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			session.beginTransaction();
			//添加或修改标签信息
			session.saveOrUpdate(entity);
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
}
