package com.sunsheen.hkks.task.unstructure.sc.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.transform.Transformers;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.unstructure.common.entity.AnnotateEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.SchemaJsonEntity;
import com.sunsheen.hkks.task.unstructure.common.utils.AnnotionEntityUtils;
import com.sunsheen.hkks.task.unstructure.sc.entity.SchemaDefineEntity;
import com.sunsheen.hkks.task.unstructure.ussm.entity.CustomLabelEntity;
import com.sunsheen.hkks.task.unstructure.ussm.service.UnStructureStandardMadeService;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.system.security.login.Session;
import com.sunsheen.jfids.util.DataBaseUtil;

public class StandardCheckService {

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
	@SuppressWarnings("unchecked")
	public void getPublishMsg(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> temp = new HashMap<String , Object>() ;
		Map<String , Object> data = new HashMap<String , Object>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			data = (Map<String, Object>) session.createDySQLQuery("USC.queryPublishSubmitMsg", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(data != null && data.size() > 0 && !"0".equals((String)data.get("totalCount")) ){
				temp.put("publishState","00");
				temp.put("publishComfirm", "以下标签还未定义Schema，是否确认提交吗？<br>"+data.get("publishMsg")) ;
			}else{
				temp.put("publishState","02");
				temp.put("publishComfirm", "正常完成") ;
			}
			retMap.put("data", temp) ;	
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: getMarkDataList
	* @Description: 通过schemaId获取标注信息
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getMarkDataList(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ; //封装好的数据信息
		List<SchemaJsonEntity> schemaJsonEntityList = new ArrayList<SchemaJsonEntity>() ; //标注信息列表
		List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ; //实体标签
		List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; //关系标签
		String fileBasePath = Configs.get("AnnotationFilePath.labelJsonFile");
		try{
			Object totalCount = session.createDySQLQuery("USC.querySchemaJsonCount", params).uniqueResult();
			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount.toString());
			//通过参数查询JSON信息,通过处理状态，用户和标签ID筛选出数据信息
			data = session.createDySQLQuery("USC.querySchemaJson", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			for(Map<String,Object> temp : data){
				params.put("labelId", temp.get("labelId")) ;
				params.put("taskId", temp.get("taskId")) ;
				List<String> labelCateIds = new ArrayList<String>() ;
				String fromId = temp.get("fromId")+"" ;
				String toId = temp.get("toId")+"" ;
				labelCateIds.add(temp.get("labelId")+"") ;
				
				AnnotateEntity annotation = new AnnotateEntity() ;
				String fileRealPath = fileBasePath+temp.get("taskId")+"/"+temp.get("userId")+"/";  //规范制定阶段的json信息路径
				String fileName = temp.get("markFileName")+"" ;
				annotation = JSONObject.parseObject(FileUtils.ReadFile(fileRealPath+fileName),AnnotateEntity.class) ;
				//初始化信息
				//依据fromId/toId/标签ID ，截取字符信息
				labelCategories = session.createDySQLQuery("USC.selectLabelCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
				connectionCategories = session.createDySQLQuery("USC.selectConnectionCategories", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
				if("01".equals(temp.get("typeCode"))){ //实体标签
					//创建实体标签实体
					AnnotionEntityUtils.subAnnotionByEntityFromAndTo(annotation, labelCateIds, Integer.parseInt(fromId), Integer.parseInt(toId) ) ;
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
			data = session.createDySQLQuery("USC.queryGuideLineSchemaByTaskId", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
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
	* @Title: getSchema
	* @Description: 获取Schema列表
	* @author: FengTao
	* @date 2020年8月19日 下午5:14:49
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getSchema(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			data = session.createDySQLQuery("USC.querySchemaByTaskId", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
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
	
	public void deleteSchema(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			//先删除label信息
			//删除发布者schema下的label标签信息
			//查询当前登录人是不是任务执行者
			Object num01 = session.createDySQLQuery("USC.labelCount", params).uniqueResult() ;
			if("0".equals(num01.toString())){//当前人不在执行者列表，才能删除label信息
				session.createDySQLQuery("USC.deleteLabelByLabelName", params).executeUpdate() ;
			}
			//删除schema信息,删除之前，先判断有无关联的关系标签信息
			Object num = 0 ;
			
			num = session.createDySQLQuery("USC.deleteSchemaById", params).executeUpdate() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if("0".equals(num.toString())){
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "刪除失败") ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "刪除成功") ;
			}
			session.flush();
			session.commit();
			session.close();
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: saveOrUpdateSchema
	* @Description: 新增或修改Schema信息
	* @author: FengTao
	* @date 2020年7月14日 上午9:49:59
	* @param retMap
	* @param entity void
	* @version
	 */
	public void saveOrUpdateSchema( Map<String , Object> retMap , SchemaDefineEntity entity,CustomLabelEntity labelEntity){
		UnStructureStandardMadeService unStructureStandardMadeService = new UnStructureStandardMadeService() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> params = new HashMap<String,Object>() ;
		try{
			session.beginTransaction();
			//添加或修改标签信息
			//查询schema是否存在
			params.put("typeCode", entity.getTypeCode()) ;
			params.put("taskId", entity.getTaskId()) ;
			params.put("schemaName", entity.getSchemaName()) ;
			Object num = session.createDySQLQuery("USC.hasSchemaByName", params).uniqueResult() ;
			System.out.print(num);
			if("0".equals(num.toString())){
				session.saveOrUpdate(entity);
				session.flush();
				if(labelEntity != null){
					//查询label是否存在
					params.put("labelName", labelEntity.getLabelName()) ;
					params.put("typeCode", labelEntity.getTypeCode()) ;
					params.put("taskId", labelEntity.getTaskId()) ;
					params.put("userId", labelEntity.getLastUpdateUserId()) ;
					Object num01 = session.createDySQLQuery("USC.labelCount", params).uniqueResult() ;
					if("0".equals(num01.toString())){ //没有才能新增
						unStructureStandardMadeService.saveOrUpdateLabel(retMap, labelEntity);
					}
				}
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{
				if(!StringUtils.isEmptyOrWhitespaceOnly(entity.getSchemaId())){ //是修改操作
					session.saveOrUpdate(entity);
					session.flush();
					retMap.putAll(RetInfo.RETSUCCESS) ;
				}else{
					retMap.putAll(RetInfo.RETFAIL) ;
					retMap.put("retmsg", "请勿重复添加schema") ;
				}
			}
			session.commit();
			session.close();
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
	* @Title: updateTag
	* @Description:更新状态码表
	* @author: FengTao
	* @date 2020年8月20日 下午4:09:34
	* @param retMap
	* @param params void
	* @version
	 */
	public void updateTag(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			Object num =   session.createDySQLQuery("USC.updateTag", params).executeUpdate() ;
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
	* @Title: getUsers
	* @Description: 获取Schema列表
	* @author: FengTao
	* @date 2020年8月19日 下午5:14:49
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getUsers(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> all = new HashMap<String,Object>() ;
		Map<String,Object> user = new HashMap<String,Object>() ;
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			all.put("code", "all") ;
			all.put("value", "全部") ;
			data.add(all) ;
			user.put("code", Session.getCurrUser().getId()) ;
			user.put("value", Session.getCurrUser().getUsername()) ;
			data.addAll( session.createDySQLQuery("USC.queryUserList", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ) ;
			if(!data.contains(user)){
				data.add(user) ; //发布人不在此次任务执行，则添加发布人信息
			}
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
	
	/**
	 * 
	* @Title: produceShcema
	* @Description:生成此次任务的Schema初版
	* @author: FengTao
	* @date 2020年8月19日 下午5:12:30
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void produceShcema(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		//session.beginTransaction();
		try{
			retMap.putAll(RetInfo.RETSUCCESS) ;
			Object num = session.createDySQLQuery("USC.hasSchema", params).uniqueResult() ;
			if("0".equals(num.toString())){ //还没有生成Schema
				data = session.createDySQLQuery("USC.queryUniqueLabelList", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
				for(Map<String, Object> temp : data){
					SchemaDefineEntity entity = new SchemaDefineEntity() ;
					//附上初始信息
					entity.setSchemaId(null);
					entity.setDefinition(null);
					entity.setLastUpdateUserId(Session.getCurrUser().getId());
					entity.setLastUpdateUserName(Session.getCurrUser().getUsername());
					entity.setLastUpdateDate(new Date(System.currentTimeMillis()));
					//自定义信息
					entity.setTaskId(params.get("taskId")+"");
					entity.setSchemaName(temp.get("labelName")+"");
					entity.setLabelName(temp.get("labelName")+"");
					entity.setTypeCode(temp.get("typeCode")+"");
					entity.setSchemaName(temp.get("labelName")+"");
					entity.setColor("#"+randomHexStr(6));
					entity.setBorderColor("#"+randomHexStr(6));
					session.saveOrUpdate(entity);
					if(StringUtils.isEmptyOrWhitespaceOnly(entity.getSchemaId())){ //id为空，则新增失败
						session.rollback();
						retMap.putAll(RetInfo.RETFAIL) ;
						return  ;
					}
				}
			}else{
				retMap.put("retmsg", "已经生成了Schema") ;
			}
			session.flush();
			session.commit();
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
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
