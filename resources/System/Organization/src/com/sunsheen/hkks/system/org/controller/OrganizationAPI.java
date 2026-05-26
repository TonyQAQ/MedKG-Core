package com.sunsheen.hkks.system.org.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;




import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.CheckParametersUtil;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.system.org.entity.OrganizationEntity;
import com.sunsheen.hkks.system.org.service.OrganizationService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: OrganizationAPI
 * @Description: 组织管理
 * @author: FengTao
 * @date 2020年9月21日 下午4:30:28
 */
@Path("task/us/org")
public class OrganizationAPI extends BaseAPI {
	
	private OrganizationService organizationService = new OrganizationService() ;
	
	/**
	 * 
	* @Title: saveOrUpdateOrg
	* @Description: 新增或修改组织信息
	* @author: FengTao
	* @date 2020年9月21日 下午4:34:45
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/saveOrUpdate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> saveOrUpdateOrg(OrganizationEntity entity){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		String result = CheckParametersUtil.getInstance()
				.put(entity.getOrgName(),"orgName")
	            .checkParameter();
		if(result != null){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "保存失败,请检查表单完整性") ;
			return  retMap;
		}else{
			if(StringUtils.isEmptyOrWhitespaceOnly(entity.getOrganizationId())){
				entity.setOrganizationId(null);
			}
			entity.setOrgLevel(0);
			entity.setOrgCode(entity.getOrgName());
			entity.setOrgManager(Session.getCurrUser().getId());
			organizationService.addOrgInfo(retMap, entity);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: deleteOrg
	* @Description:删除组织
	* @author: FengTao
	* @date 2020年9月21日 下午4:35:13
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> deleteOrg(OrganizationEntity entity){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		params.put("userId", Session.getCurrUser().getId()) ;
		String result = CheckParametersUtil.getInstance()
						.put(entity.getOrganizationId(), "organizationId") 
						.checkParameter();
		if(result != null){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "删除失败，请选择需要删除的组织信息！") ;
			return  retMap;
		}else{
			organizationService.deleteOrgInfo(retMap, entity);
		}
		return retMap ;
	}
	
	@GET
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> searchOrg(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		params.put("keyWord", request.getParameter("keyWord")) ;
		params.put("userId", Session.getCurrUser().getId()) ;	
		organizationService.searchOrgInfo(retMap, params);
		return retMap ;
	}
	
}
