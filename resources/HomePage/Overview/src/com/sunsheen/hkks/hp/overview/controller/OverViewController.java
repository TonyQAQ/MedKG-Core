package com.sunsheen.hkks.hp.overview.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sunsheen.hkks.hp.overview.service.OverViewService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

@Path("task/us/ov")
public class OverViewController extends BaseAPI {
	 
	private OverViewService overViewService = new OverViewService() ;
	//统计任务信息
	//统计图谱信息
	//统计团队信息
	//统计标注信息
	//统计近期任务信息
	//统计团队完成任务信息

	/**
	 * 
	* @Title: statTopInfo
	* @Description:统计顶部信息
	* @author: FengTao
	* @date 2020年9月21日 上午10:01:17
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/stat/top")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> statTopInfo(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		params.put("userId", Session.getCurrUser().getId()) ;
		overViewService.statTopInfoService(retMap, params);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: statMidInfo
	* @Description:统计中部信息
	* @author: FengTao
	* @date 2020年9月21日 上午10:04:29
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/stat/mid")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> statMidInfo(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		params.put("userId", Session.getCurrUser().getId()) ;
		overViewService.statMidInfoService(retMap, params);
		return retMap ;
	}
	
	/**
	 * 
	* @Title: statBottomInfo
	* @Description: 统计底部信息
	* @author: FengTao
	* @date 2020年9月21日 上午10:05:25
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/stat/bottom")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> statBottomInfo(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		params.put("userId", Session.getCurrUser().getId()) ;
		overViewService.statBottomInfoService(retMap, params);
		return retMap ;
	}
	
}
