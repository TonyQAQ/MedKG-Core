package com.sunsheen.hkks.system.org.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.system.org.entity.OrganizationEntity;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

public class OrganizationService {

	/**
	 * 
	* @Title: addOrgInfo
	* @Description: 添加或修改组织信息
	* @author: FengTao
	* @date 2020年9月21日 下午5:36:12
	* @param retMap
	* @param params void
	* @version
	 */
	public void addOrgInfo(Map<String,Object> retMap , OrganizationEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object>  params = new HashMap<String,Object>() ;
		session.beginTransaction();
		try{
			if(StringUtils.isEmptyOrWhitespaceOnly(entity.getOrganizationId())){
				params.put("userId", entity.getOrgManager()) ;
				params.put("orgName", entity.getOrgName()) ;
				Object result = session.createDySQLQuery("OrganizationDao.queryOrganization", params).uniqueResult() ;
				if( result != null && result != "" ){
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "已经存在，请勿重复添加") ;
					return  ;
				}
			}
			session.saveOrUpdate(entity);
			session.commit();
			session.close();
			retMap.putAll(RetInfo.RETSUCCESS);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "保存失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: deleteOrgInfo
	* @Description:
	* @author: FengTao
	* @date 2020年9月21日 下午8:46:45
	* @param retMap
	* @param entity void
	* @version
	 */
	public void deleteOrgInfo(Map<String,Object> retMap , OrganizationEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object>  params = new HashMap<String,Object>() ;
		session.beginTransaction();
		try{
			params.put("userId", entity.getOrgManager()) ;
			params.put("orgName", entity.getOrgName()) ;
			Object result = session.createDySQLQuery("OrganizationDao.queryOrganization", params).uniqueResult() ;
			if(  result != null && result != "" ){
				session.delete( session.merge(entity) );
				session.flush();
				session.commit();
				session.close();
			}else{
				retMap.put("retmsg", "该团队已经有人员信息，无法直接删除！") ;
				return ;
			}
			retMap.putAll(RetInfo.RETSUCCESS);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "删除失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: searchOrgInfo
	* @Description:
	* @author: FengTao
	* @date 2020年9月22日 上午9:43:20
	* @param retMap
	* @param entity void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void searchOrgInfo(Map<String,Object> retMap , Map<String,Object>  params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("OrgConfig.title")));
		try{
			data = session.createDySQLQuery("OrganizationDao.queryOrganization", params)
							.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS);
			int sequence = 0  ;
			for(Map<String,Object> temp : data){
				temp.put("sequence", ++sequence) ;
				temp.put("rowKey", temp.get("code")) ;
			}
			if(data != null && data.size() > 0 ){
				retMap.put("retmsg" , "查询成功") ;
			}else{
				retMap.put("retmsg" , "查询成功,无数据！") ;
			}
			retMap.put("data" , data);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败，请重试") ;
			e.printStackTrace();
		}
	}
	
}
