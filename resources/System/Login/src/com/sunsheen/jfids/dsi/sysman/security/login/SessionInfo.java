package com.sunsheen.jfids.dsi.sysman.security.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;
import com.sunsheen.jfids.system.sysman.IRole;

@Path("session")
public class SessionInfo extends BaseAPI {
	/**
	 * 判断session是否存在
	 * 
	 * @return
	 * @throws IOException
	 */
	@GET
	@POST
	@Path("/isEmpty")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> isEmpty() throws IOException {
		Map<String, Object> retMap = new HashMap<String,Object>() ;
		if (Session.getCurrUser() != null) {
			Set<IRole> roleSet = Session.getCurrUser().getRole();
			for(IRole role : roleSet) {
				retMap.put("roleId",  role.getId());
				retMap.put("roleName", role.getText());
			}
			retMap.put("userId", Session.getCurrUser().getId());
			retMap.put("userName", Session.getCurrUser().getUsername());
			retMap.putAll(RetInfo.RETSUCCESS);
			retMap.put("retmsg", "用户已登录！");
		} else {
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "用户未登陆！");
		}
		return retMap ;
	}
}
