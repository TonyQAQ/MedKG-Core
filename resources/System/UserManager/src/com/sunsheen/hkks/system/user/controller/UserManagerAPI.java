package com.sunsheen.hkks.system.user.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.CheckParametersUtil;
import com.sunsheen.hkks.common.util.MD5;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.system.user.dao.UserInfoEntity;
import com.sunsheen.hkks.system.user.service.UserManagerService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

@Path("task/us/um")
public class UserManagerAPI  extends BaseAPI{
	
	private UserManagerService userManagerService = new UserManagerService() ;
	
	
	/**
	 * 
	* @Title: saveUser
	* @Description: 修改用户信息
	* @author: FengTao
	* @date 2020年9月22日 下午5:11:45
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> deleteUser(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String userId = request.getParameter("userId") ;
		//更新用户基础信息
		params.put("userId", userId) ;
		userManagerService.deleteUserInfo(retMap, params);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: updatePassword
	* @Description: 修改用户密码
	* @author: FengTao
	* @date 2020年9月22日 下午5:11:45
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/update/password")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> updatePassword(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String oldPassword = request.getParameter("oldPassword") ;
		String newPassword = request.getParameter("newPassword") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(oldPassword) || StringUtils.isEmptyOrWhitespaceOnly(newPassword) ){
			retMap.putAll(RetInfo.RETFAIL);
			return retMap ;
		}else if( oldPassword.equals(newPassword)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "新旧密码不能相同！") ;
			return retMap ;
		}else{
			//更新用户基础信息
			params.put("userId", Session.getCurrUser().getId()) ;
			params.put("newPassword",  new MD5().getMD5ofStr( newPassword )) ;
			params.put("oldPassword",  new MD5().getMD5ofStr( oldPassword )) ;
			userManagerService.updateUserPassword(retMap, params);;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: saveUser
	* @Description: 修改用户信息
	* @author: FengTao
	* @date 2020年9月22日 下午5:11:45
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> updateUser(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String username = request.getParameter("username") ;
		String account = request.getParameter("account") ;
		String userId = request.getParameter("userId") ;
		String organizationId = request.getParameter("organizationId") ;
		String newOrganizationId = request.getParameter("newOrganizationId") ;
		String roleId = request.getParameter("roleId") ;
		String newRoleId = request.getParameter("newRoleId") ;
		
		String reset = request.getParameter("reset") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(userId)){
			retMap.putAll(RetInfo.RETFAIL);
			return retMap ;
		}else{
			params.put("userId", userId) ;
			params.put("account", account) ;
			params.put("username", username) ;
			params.put("organizationId", organizationId) ;
			params.put("newOrganizationId", newOrganizationId) ;
			params.put("roleId", roleId) ;
			params.put("newRoleId", newRoleId) ;
			if("yes".equals(reset)){ //重置密码
				String password = "" ;
				password = "E1843E00896" ;
				params.put("password", password) ;
				userManagerService.updateUserPassword(retMap, params);
			}else{
				if(StringUtils.isEmptyOrWhitespaceOnly(account) || 
						StringUtils.isEmptyOrWhitespaceOnly(username)){
					retMap.putAll(RetInfo.RETFAIL);
					return retMap ;
				}else{
					//更新用户基础信息
					userManagerService.updateuserInfo(retMap, params);	
				}
				//以下是更新用户角色信息
				if(!StringUtils.isEmptyOrWhitespaceOnly(newRoleId)){
					userManagerService.updateUserRole(retMap, params);
				}
				if(!StringUtils.isEmptyOrWhitespaceOnly(newOrganizationId)){
					userManagerService.updateUserEmp(retMap, params);
				}
			}
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: saveUser
	* @Description: 新增用户信息
	* @author: FengTao
	* @date 2020年9月22日 下午5:11:45
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/save")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> saveUser(UserInfoEntity entity){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		String result = CheckParametersUtil.getInstance()
				.put(entity.getUserName(),"userName")
				.put(entity.getAccount(),"account")
				.put(entity.getOrganizationId(),"organizationId")
				.put(entity.getRoleId(),"roleId")
	            .checkParameter();
		if(result != null){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "保存失败,请检查表单完整性") ;
			return  retMap;
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(entity.getPassword()) ){
			entity.setPassword("E1843E00896");
		}else{
			MD5 m = new MD5() ;
			entity.setPassword( m.getMD5ofStr(entity.getPassword()) );
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(entity.getUserId())){
			entity.setUserId(UUID.randomUUID().toString().replace("-", ""));
		}
		userManagerService.saveUserInfo(retMap, entity);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: searchTeamUser
	* @Description: 查询团队成员
	* @author: FengTao
	* @date 2020年9月23日 上午9:30:01
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> searchTeamUser(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String organizationId = request.getParameter("organizationId") ;
		String keyWord = request.getParameter("keyWord") ;
		String userId = request.getParameter("userId") ;
		String pageSize = request.getParameter("pageSize") ;
		String pageCount = request.getParameter("pageCount") ;
		if("all".equals(organizationId)){
			organizationId = "" ;
		}
		params.put("organizationId", organizationId) ;
		params.put("pageSize", pageSize) ;
		params.put("pageCount", pageCount) ;
		params.put("keyWord", keyWord) ;
		params.put("userId", userId) ;
		params.put("managerId", Session.getCurrUser().getId()) ;
		userManagerService.searchTeamUsers(retMap, params);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: searchRoles
	* @Description: 查询角色信息
	* @author: FengTao
	* @date 2020年9月22日 下午5:11:45
	* @param entity
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/roles")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> searchRoles(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		userManagerService.searchUserRole(retMap, params);
		return retMap ;
	}
	
	
}
