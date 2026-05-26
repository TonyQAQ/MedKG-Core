package com.sunsheen.hkks.system.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.system.user.dao.UserInfoEntity;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

public class UserManagerService {
	
	
	/**
	 * 
	* @Title: deleteUserInfo
	* @Description: 删除用户
	* @author: FengTao
	* @date 2020年9月23日 下午4:37:27
	* @param retMap
	* @param params void
	* @version
	 */
	public void deleteUserInfo(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		try{
			session.createDySQLQuery("UM.updateUserState", params).executeUpdate() ;
			session.commit();
			session.close();
			retMap.putAll(RetInfo.RETSUCCESS);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "更新失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: updatePassword
	* @Description:更新密码
	* @author: FengTao
	* @date 2020年9月23日 下午3:35:08
	* @param retMap
	* @param params void
	* @version
	 */
	public void updateUserPassword(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		try{
			int a = session.createDySQLQuery("UM.updatePassword2", params).executeUpdate() ;
			session.commit();
			session.close();
			if( a == 1 ){
				retMap.putAll(RetInfo.RETSUCCESS);
			}else{
				retMap.putAll(RetInfo.RETFAIL);
				retMap.put("retmsg", "输入的旧密码错误！") ;
			}
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "删除失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: updateuserInfo
	* @Description:更新账号，用户名。密码信息
	* @author: FengTao
	* @date 2020年9月23日 下午3:35:08
	* @param retMap
	* @param params void
	* @version
	 */
	public void updateuserInfo(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		try{
			Object num = session.createDySQLQuery("UM.queryUserByAccountAndId", params).uniqueResult() ;
			if("0".equals(num.toString())){
				session.createDySQLQuery("UM.updateTsUser", params).executeUpdate() ;
				session.createDySQLQuery("UM.updateTsEmployee", params).executeUpdate() ;
			}else{
				retMap.putAll(RetInfo.RETFAIL);
				retMap.put("retmsg", "该账号已经被使用，请尝试更换账号！") ;
				return ;
			}
			session.commit();
			session.close();
			retMap.putAll(RetInfo.RETSUCCESS);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "更新失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: updateUserRole
	* @Description: 更新用户角色信息
	* @author: FengTao
	* @date 2020年9月23日 下午3:34:22
	* @param retMap
	* @param params void
	* @version
	 */
	public void updateUserRole(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		try{
			session.createDySQLQuery("UM.updateUserRole", params).executeUpdate() ;
			session.commit();
			session.close();
			retMap.putAll(RetInfo.RETSUCCESS);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "更新失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: updateRoleEmp
	* @Description: 更新人员部门信息
	* @author: FengTao
	* @date 2020年9月23日 下午3:31:53
	* @param retMap
	* @param entity void
	* @version
	 */
	public void updateUserEmp(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		try{
			session.createDySQLQuery("UM.updateUserEmp", params).executeUpdate() ;
			session.commit();
			session.close();
			retMap.putAll(RetInfo.RETSUCCESS);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "更新失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: saveUserInfo
	* @Description: 保存用户信息
	* @author: FengTao
	* @date 2020年9月22日 下午5:18:23
	* @param retMap
	* @param entity void
	* @version
	 */
	public void saveUserInfo(Map<String,Object> retMap , UserInfoEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object>  params = new HashMap<String,Object>() ;
		session.beginTransaction();
		try{
			params.put("userId", entity.getUserId()) ;
			params.put("userName", entity.getUserName()) ;
			params.put("roleId", entity.getRoleId()) ;
			params.put("organizationId", entity.getOrganizationId()) ;
			params.put("password", entity.getPassword()) ;
			params.put("account", entity.getAccount()) ;
			Object num = session.createDySQLQuery("UM.queryUserByAccount", params).uniqueResult() ;
			if("0".equals(num.toString())){
				session.createDySQLQuery("UM.insertUser", params).executeUpdate() ;
				session.createDySQLQuery("UM.insertEmployee", params).executeUpdate() ;
				session.createDySQLQuery("UM.insertRoleEmp", params).executeUpdate() ;
				session.createDySQLQuery("UM.insertOrgEmp", params).executeUpdate() ;
			}else{
				retMap.putAll(RetInfo.RETFAIL);
				retMap.put("retmsg", "该账号已经被使用，请尝试更换账号！") ;
				return ;
			}
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
	* @Title: searchUserRole
	* @Description: 查询角色码表
	* @author: FengTao
	* @date 2020年9月22日 下午3:38:02
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void searchTeamUsers(Map<String,Object> retMap , Map<String,Object>  params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("UMConfig.title")));
		try{
			Object count = session.createDySQLQuery("UM.queryUsersCountByOrgId", params).uniqueResult() ;
			PageParamsUtils.pageParamsDetail(params, params.get("pageCount")+"", params.get("pageSize")+"", count.toString());
			data = session.createDySQLQuery("UM.queryUsersByOrgId", params)
							.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS);
			int sequence = 0  ;
			for(Map<String,Object> temp : data){
				temp.put("sequence", ++sequence) ;
				temp.put("rowKey", temp.get("userId")) ;
			}
			if(data != null && data.size() > 0 ){
				retMap.put("retmsg" , "查询成功") ;
			}else{
				retMap.put("retmsg" , "查询成功,无数据！") ;
			}

			retMap.put("totalCount",count);
			retMap.put("data" , data);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: searchUserRole
	* @Description: 查询角色码表
	* @author: FengTao
	* @date 2020年9月22日 下午3:38:02
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void searchUserRole(Map<String,Object> retMap , Map<String,Object>  params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			data = session.createDySQLQuery("UM.queryRoles", params)
							.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS);
			retMap.put("data" , data);
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "查询失败，请重试") ;
			e.printStackTrace();
		}
	}
	
}
