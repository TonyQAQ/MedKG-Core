package com.sunsheen.hkks.task.structure.rv.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.ListSortUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.common.util.TreeMapSortUtils;
import com.sunsheen.hkks.task.structure.rv.entity.StructureRelCheckEntity;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
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
	* @Title: getTables
	* @Description: 获取RDF结构化校验表信息
	* @author: FengTao
	* @date 2020年7月31日 下午6:10:38
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getTables(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			data = session.createDySQLQuery("ResultVerificationDao.selectCheckTables", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
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
	* @Title: getRDFList
	* @Description: 获取RDF关系
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:15
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getRDFList(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("ResultVerificationDao.rdfTitle")));
		try{
			data = session.createDySQLQuery("ResultVerificationDao.selectRDF", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			//data数据拿去去重，统计
			data = statAndSortList(data) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			int sequence = 0 ;
			for(Map<String,Object> temp : data){
				String uuid = UUID.randomUUID().toString().replace("-", "") ;
				temp.put("sequence", ++sequence) ;
				temp.put("rowKey", uuid) ;
			}
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
	* @Title: addOrUpdateRel
	* @Description: RDF保存
	* @author: FengTao
	* @date 2020年7月30日 下午6:14:49
	* @param retMap
	* @param userIds
	* @param entity void
	* @version
	 */
	public void addOrUpdateRel( Map<String , Object> retMap , StructureRelCheckEntity entity){
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
			Object num = session.createDySQLQuery("ResultVerificationDao.deleteRelById", params).executeUpdate();
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
	* @Title: deleteRel
	* @Description: 删除RDF
	* @author: FengTao
	* @date 2020年7月30日 下午6:23:28
	* @param retMap
	* @param entity void
	* @version
	 */
	public void addOrUpdateRelByRelList( Map<String , Object> retMap , Map<String , Object> params , List<StructureRelCheckEntity> entityList){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		try{
			Object num = session.createDySQLQuery("ResultVerificationDao.deleteRelByTableId", params).executeUpdate();
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if(Integer.parseInt(num.toString()) < 0){
				session.rollback();
				retMap.put("retmsg", "删除失败") ;
			}else{
				retMap.put("retmsg", "删除成功") ;
				for(StructureRelCheckEntity entity : entityList){
					//添加实体信息
					session.saveOrUpdate(entity);
				}
				session.flush();
				session.commit();
				retMap.put("retmsg", "保存成功") ;
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
	* @Title: getPublishMsg
	* @Description:获取发布确认消息
	* @author: FengTao
	* @date 2020年7月22日 下午1:00:14
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
			data = (Map<String, Object>) session.createDySQLQuery("ResultVerificationDao.queryTaskPublishMsg", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(data != null && data.size() > 0 && !"0".equals((String)data.get("totalCount")) ){
				temp.put("publishState","00");
				temp.put("publishComfirm", "以下【表】还有未处理的【三元组】，是否确认提交吗？<br>"+data.get("publishMsg")) ;
			}else{
				temp.put("publishState","00");
				temp.put("publishComfirm", "发布后不可撤回，确认发布吗？") ;
			}
			retMap.put("data", temp) ;	
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: statAndSortList
	* @Description:
	* @author: FengTao
	* @date 2020年7月30日 上午9:58:24
	* @param filterList 统计前的数据
	* @param filterTmpList 统计后的数据
	* @version
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String,Object>> statAndSortList( List<Map<String,Object>> filterList){
		
		Set<Map<String,Object>> mapSet = new HashSet<Map<String,Object>>() ;
		for(Map<String,Object> temp : filterList){
			Map<String , Object> dataMap = new HashMap<String , Object>() ;
			dataMap.putAll(temp);
			dataMap.remove("relList") ;
			mapSet.add(dataMap);
		}
		//统计数据
		for(Map<String,Object> setMap : mapSet){
			for(Map<String,Object> temp : filterList){
				if(setMap.get("attrStart").equals(temp.get("attrStart")) && 
							setMap.get("attrEnd").equals(temp.get("attrEnd"))){
					if(StringUtils.isEmptyOrWhitespaceOnly((String)setMap.get("relList"))){
						setMap.put("relList", temp.get("relList") ) ;
					}else{
						setMap.put("relList", setMap.get("relList")+"--A--"+temp.get("relList") ) ;
					}
				}
			}
		}
		for(Map<String,Object> setMap : mapSet){
			List<Map<String,Object>> relList = new ArrayList<Map<String,Object>>() ;
			Map<String,Object> tempMap = new HashMap<String,Object>() ;
			String relListStr = (String)setMap.get("relList") ;
			if(!StringUtils.isEmptyOrWhitespaceOnly(relListStr)){
				for(String strs : relListStr.split("--A--")){
					String[] str = strs.split("AA-AA") ;
					if(tempMap.containsKey(str[0])){
						tempMap.put(str[0], Integer.parseInt((String) tempMap.get(str[0]))+1 ) ;
					}else{
						tempMap.put(str[0], "1" ) ;
					}
				}
			}
			for(Map.Entry<String, Object> map : TreeMapSortUtils.treeMapSortObject(tempMap)){
				Map<String,Object> relMap = new HashMap<String,Object>() ;
				relMap.put("code", map.getKey()) ;
				relMap.put("value", map.getKey()+"("+map.getValue()+"人)") ;
				relList.add(relMap) ;
			}
			setMap.put("relList",relList) ;
		}
		filterList.clear();
		filterList = new ArrayList(mapSet) ;
		ListSortUtils.sortByValueAsc(filterList, "endName");
		ListSortUtils.sortByValueAsc(filterList, "startName");
		return filterList ;
	}
	
	/**
	 * 
	* @Title: deepCopy
	* @Description:数组深拷贝
	* @author: FengTao
	* @date 2020年8月4日 下午7:00:33
	* @param src
	* @return
	* @throws IOException
	* @throws ClassNotFoundException List<Map<String,Object>>
	* @version
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> deepCopy(List<Map<String,Object>> src){
		  ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		  ObjectOutputStream out;
		  List<Map<String,Object>> dest = new  ArrayList<Map<String,Object>>();
		try {
			out = new ObjectOutputStream(byteOut);
			out.writeObject(src);
			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in =new ObjectInputStream(byteIn);
			dest = (List<Map<String,Object>>)in.readObject();
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		  return dest;
	}
}
