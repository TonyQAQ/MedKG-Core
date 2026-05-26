package com.sunsheen.hkks.map.mgr.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sunsheen.hkks.map.mgr.service.MapManagerService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

@Path("/map")
public class MapManagerController extends BaseAPI {

	private MapManagerService mapManagerService = new MapManagerService() ;
	
	/**
	 * 
	* @Title: getMapList
	* @Description: 查询图谱列表信息
	* @author: FengTao
	* @date 2020年8月7日 上午9:43:18
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/search/mapList")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String , Object> getMapList(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String stateCode = request.getParameter("stateCode") ;
		String keyWord = request.getParameter("keyWord") ;
		if("all".equalsIgnoreCase(stateCode)){
			stateCode = "" ;
		}
		params.put("mapStateCode", stateCode) ;
		params.put("keyWord", keyWord) ;
		params.put("userId", Session.getCurrUser().getId() ) ;
		mapManagerService.getMapListByUserId(retMap, params);
		return retMap ;
	}
	
	/**
	 * 获取任务类型，任务状态码表信息
	 * @return
	 */
	@GET
	@Path("/codeTable/state")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getStateCode(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		mapManagerService.getStateCodeTable(retMap);
		return retMap ;
	}
	
	@GET
	@Path("/search/taskMap")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String , Object> getKnowledgeMap(){
		 
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		params.put("userId", Session.getCurrUser().getId() ) ;
		mapManagerService.getTaskMaps(retMap, params);
		return retMap ;
	}
}
