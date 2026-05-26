package com.sunsheen.hkks.task.unstructure.ussm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.Transformers;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.FilterRulesUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.database.mgr.service.DataBaseUtilService;
import com.sunsheen.hkks.task.unstructure.common.entity.AnnotateEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.MarkInfoEntity;
import com.sunsheen.hkks.task.unstructure.ussm.entity.CustomLabelEntity;
import com.sunsheen.hkks.task.unstructure.ussm.entity.MarkSampleInfoEntity;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.system.security.login.Session;
import com.sunsheen.jfids.util.DataBaseUtil;

public class UnStructureStandardMadeService {
	
	/**
	 * 
	* @Title: getMarkDataList
	* @Description: 获取标注信息
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void
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
		List<MarkInfoEntity> markInfoEntityList = new ArrayList<MarkInfoEntity>() ; //标注信息列表
		String fileBasePath = Configs.get("AnnotationFilePath.labelJsonFile");
		String fileRealPath = fileBasePath+params.get("taskId")+"/"+params.get("userId")+"/";  //规范制定阶段的json信息路径
		try{
			//查询标签信息
			labelCategories = session.createDySQLQuery("USSM.selectLabelCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			connectionCategories = session.createDySQLQuery("USSM.selectConnectionCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			//查询过滤规则，拼接处filterSql，放入参数params中
			String filterSql = "" ;
			filters = session.createDySQLQuery("USSM.queryFilter", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			filterSql = FilterRulesUtils.linkFilters((String)params.get("columnName"),filters) ;
			if(!StringUtils.isEmptyOrWhitespaceOnly(filterSql)){
				filterSql = " and " + filterSql ;
			}
			params.put("filterSql", filterSql) ;
			//通过字段信息获取源数据信息
			columnResultMap = dataBaseUtilService.getColumnDataByFilter(retMap, params);
			if("01".equals(columnResultMap.get("retcode")) ){ //查询到数据
				String fileName = "";
				columnData = (List<Object>) columnResultMap.get("data") ;
				//Integer totalCount  = Integer.parseInt(columnResultMap.get("totalCount")+"")  ;
				//计算出数据行号信息
				Integer rowIndex = Integer.parseInt(params.get("pageIndex")+"" )  ;
				for(Object o : columnData){ 
					//构件每条数据的标注信息
					AnnotateEntity annotation = new AnnotateEntity() ;
					//将具体数据丢进去
					params.put("rowIndex", (++rowIndex)+"") ;
					//通过数据行号查询数据信息
					//封装数据
					fileName = (String)session.createDySQLQuery("USSM.selectMarkDatas", params).uniqueResult() ; //文件是XXX.josn
					if(!StringUtils.isEmptyOrWhitespaceOnly(fileName)){
						annotation = JSONObject.parseObject(FileUtils.ReadFile(fileRealPath+fileName),AnnotateEntity.class) ;
					}else{
						annotation.setContent(o+"");
						annotation.setLabels(new ArrayList<>());
						annotation.setConnections(new ArrayList<>());					}
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
					MarkInfoEntity entity = new MarkInfoEntity() ;
					entity.setAttrMappingId(params.get("attrMappingId")+"");
					entity.setRowIndex((rowIndex)+"");
					entity.setAnnotation(annotation);
					markInfoEntityList.add(entity) ;
				}
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("labelCategories",labelCategories) ;
				retMap.put("connectionCategories",connectionCategories) ;
				retMap.put("data", markInfoEntityList) ;
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
	* @Title: saveOrUpdateLabels
	* @Description:保存执行者个标注信息
	* @author: FengTao
	* @date 2020年8月14日 下午2:29:01
	* @param retMap
	* @param columnsInfo void
	* @version
	 */
	public void saveOrUpdateMarkDatas(Map<String,Object> retMap , List<MarkInfoEntity> entityList , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		String fileBasePath = Configs.get("AnnotationFilePath.labelJsonFile");
		String fileRealPath = fileBasePath+params.get("taskId")+"/"+params.get("userId")+"/";  //规范制定阶段的json信息路径
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			for( MarkInfoEntity entity : entityList ){
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
	 * 
	* @Title: saveOrUpdateLabels
	* @Description: 新增或修改标签信息
	* @author: FengTao
	* @date 2020年7月14日 上午9:49:59
	* @param retMap
	* @param userIds
	* @param entity void
	* @version
	 */
	public void saveOrUpdateLabel( Map<String , Object> retMap , CustomLabelEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> params = new HashMap<String,Object>() ;
		try{
			session.beginTransaction();
			//添加或修改标签信息
			params.put("labelName", entity.getLabelName()) ;
			params.put("typeCode", entity.getTypeCode()) ;
			params.put("taskId", entity.getTaskId()) ;
			params.put("userId", entity.getLastUpdateUserId()) ;
			Object num = session.createDySQLQuery("USC.labelCount", params).uniqueResult() ;
			if("0".equals(num.toString())){//查询当前添加的标签是否存在，存在则不添加
				session.saveOrUpdate(entity);
				session.flush();
				session.commit();
				session.close();
				retMap.put("labelId", entity.getLabelId()) ;
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "请勿重复添加标签") ;
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
	* @Description: 新增或修改标签信息
	* @author: FengTao
	* @date 2020年7月14日 上午9:49:59
	* @param retMap
	* @param userIds
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
	
	/**
	 * 
	* @Title: deleteLabel
	* @Description: 删除标签
	* @author: FengTao
	* @date 2020年8月19日 上午10:08:49
	* @param retMap
	* @param params void
	* @version
	 */
	public void deleteLabel(Map<String, Object> retMap, Map<String, Object> params) {
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			Object num = session.createDySQLQuery("USSM.deleteLabel", params).executeUpdate();
			@SuppressWarnings("unused")
			Object num01 = session.createDySQLQuery("USSM.deleteMarkInfoByLabelId", params).executeUpdate();
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if("0".equals(num.toString())){
				retMap.put("retmsg", "删除失败") ;
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
	* @Title: deleteMarkSampleInfo
	* @Description:删除标注信息
	* @author: FengTao
	* @date 2020年8月19日 上午10:08:43
	* @param retMap
	* @param entity void
	* @version
	 */
	public void deleteMarkSampleInfo(Map<String, Object> retMap, List<MarkSampleInfoEntity> entityList) {
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
				params.put("userId", Session.getCurrUser().getId()) ;
				Object num = session.createDySQLQuery("USSM.deleteMarkInfo", params).executeUpdate();
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
			data = session.createDySQLQuery("USSM.selectSchemaTables", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
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
			data = session.createDySQLQuery("USSM.selectSchemaColumns", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
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
	* @Title: getCustomLabels
	* @Description: 获取个人定义的标签表
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getCustomLabels(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			data = session.createDySQLQuery("USSM.selectLabels", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			Object litleStateCode = session.createDySQLQuery("TaskMT.queryExeStateByUserId", params).uniqueResult() ;
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
	* @Title: getSubmitMsg
	* @Description:获取提交确认消息
	* @author: FengTao
	* @date 2020年7月22日 下午1:00:14
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getSubmitMsg(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> temp = new HashMap<String , Object>() ;
		Map<String , Object> data = new HashMap<String , Object>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			data = (Map<String, Object>) session.createDySQLQuery("USSM.queryLabelSubmitMsg", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(data != null && data.size() > 0 && !"0".equals((String)data.get("totalCount")) ){
				temp.put("publishState","00");
				temp.put("publishComfirm", "以下【表】还有未定义标签的【字段】，是否确认提交吗？<br>"+data.get("publishMsg")) ;
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
	* @Title: getLabelTypeCode
	* @Description: 获取标签表码表
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getLabelTypeCode(Map<String,Object> retMap){
		Map<String,Object> params = new HashMap<String,Object>() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			data.addAll(session.createDySQLQuery("USSM.selectLabelTypeCode", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list()) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", data) ;
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

}
