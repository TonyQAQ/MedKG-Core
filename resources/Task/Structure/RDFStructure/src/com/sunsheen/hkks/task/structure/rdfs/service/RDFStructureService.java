package com.sunsheen.hkks.task.structure.rdfs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.structure.rdfs.entity.StructureRelEntity;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

/**
 * 
 * @Title: RDFStructureService
 * @Description: RDF关系构件Service
 * @author: FengTao
 * @date 2020年7月30日 下午1:36:17
 */
public class RDFStructureService {
	
	/**
	 * 
	* @Title: addOrUpdateRel
	* @Description: RDF保存
	* @author: FengTao
	* @date 2020年7月30日 下午6:14:49
	* @param retMap
	* @param userIds
	* @param entity void
	* @version
	 */
	public void addOrUpdateRel( Map<String , Object> retMap , StructureRelEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			session.beginTransaction();
			//添加任务信息
			session.saveOrUpdate(entity);
			session.flush();
			session.commit();
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
	* @Title: deleteRel
	* @Description: 删除RDF
	* @author: FengTao
	* @date 2020年7月30日 下午6:23:28
	* @param retMap
	* @param entity void
	* @version
	 */
	public void deleteRel( Map<String , Object> retMap , Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			Object num = session.createDySQLQuery("RDFStructureDao.deleteRelById", params).executeUpdate();
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
	* @Title: getTablesAndColumns
	* @Description: 获取表和字段信息
	* @author: FengTao
	* @date 2020年7月30日 下午3:28:08
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getTablesAndColumns(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			String titleR = "[{\n" +
	                		"    \"title\":\"" ;
			String titleL = "\",\n" +
			                "    \"dataIndex\":\"attrName\",\n" +
			                "    \"key\":\"attrName\"\n" +
			                "}]\n" ;
			
			data = session.createDySQLQuery("RDFStructureDao.queryTablesAndColumns", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			//处理表格数据信息
			for(Map<String,Object> temp : data){
				Map<String , Object > attrTitleHead = new HashMap<String,Object>() ;
				List<Map<String,Object>> attrList = new ArrayList<Map<String,Object>>() ;
				attrTitleHead.put("titleHead", JSONArray.fromObject( (Object)(titleR+temp.get("tableName")+titleL) ));
				String[] attrListStr = temp.get("attrList").toString().split("--A--") ;
				int sequence = 0 ;
				for( String tempStr : attrListStr ){
					String[] datas = tempStr.split("AA-AA") ;
					if(datas.length > 1){
						Map<String , Object > attrTempMap = new HashMap<String,Object>() ;
					attrTempMap.put("sequence", ++sequence) ;
					attrTempMap.put("rowKey", datas[0]) ;
					attrTempMap.put("attrMappingId", datas[0]) ;
					attrTempMap.put("attrName", datas[1]) ;
					attrTempMap.put("levitationTips", datas[2]) ;
					attrList.add(attrTempMap) ;
					}
				}
				temp.putAll(attrTitleHead);
				temp.put("attrList",attrList);
				temp.put("isEdit", false) ;
				
			}
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
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
	* @Title: getRel
	* @Description: 获取关系列表
	* @author: FengTao
	* @date 2020年7月30日 下午5:26:58
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getRel(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("RDF.relHead") )); 
		try{
			data = session.createDySQLQuery("RDFStructureDao.queryRelByTaskId", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			int sequence = 0 ;
			for(Map<String,Object> temp : data){
				temp.put("sequence", ++sequence) ;
				temp.put("rowKey", temp.get("resultId")) ;
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
	* @Title: getSubmitMsg
	* @Description: 提交RDF构建任务的提示
	* @author: FengTao
	* @date 2020年7月31日 上午10:06:29
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
			data = (Map<String, Object>) session.createDySQLQuery("RDFStructureDao.querySubmitMsg", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(data != null && data.size() > 0 && !"0".equals((String)data.get("totalCount")) ){
				temp.put("publishState","00");
				temp.put("publishComfirm", "以下【表】还没有建立RDF，是否确认提交吗？<br>"+data.get("publishMsg")) ;
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
