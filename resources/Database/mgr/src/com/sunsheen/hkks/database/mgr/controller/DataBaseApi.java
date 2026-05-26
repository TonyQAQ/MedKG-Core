package com.sunsheen.hkks.database.mgr.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.AES;
import com.sunsheen.hkks.common.util.AESUtil;
import com.sunsheen.hkks.common.util.CheckParametersUtil;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.database.mgr.entity.DataBaseEntity;
import com.sunsheen.hkks.database.mgr.service.DataBaseService;
import com.sunsheen.hkks.database.mgr.service.DataBaseUtilService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @ClassName: DataBaseApi  
 * @Description: 数据库配置信息增删查改相关接口  
 * @author FengTao  
 * @date 2020年7月7日  
 *
 */
@Path("/db")
public class DataBaseApi extends BaseAPI{
	
	private DataBaseService databaseService = new DataBaseService() ;
	private DataBaseUtilService databaseUtilService = new DataBaseUtilService() ;
	
	/**
	 * 获取选择的数据列具体数据
	 * @return
	 */
	@POST
	@Path("/search/columnData")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getColumnData(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String tableName = request.getParameter("tableName") ;
		String columnName = request.getParameter("columnName") ;
		String pageCount = request.getParameter("pageCount") ;
		String pageSize = request.getParameter("pageSize") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)||StringUtils.isEmptyOrWhitespaceOnly(tableName)||StringUtils.isEmptyOrWhitespaceOnly(columnName)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","获取失败");
		}else{
			params.put("taskId", taskId) ;
			params.put("tableName", tableName) ;
			params.put("columnName", columnName) ;
			params.put("pageCount", pageCount) ;
			params.put("pageSize", pageSize) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			databaseUtilService.getColumnData(retMap, params);
		}
		return retMap ;
	}
	/**
	 * 获取数据表下的字段信息
	 * @return
	 */
	@POST
	@Path("/search/columns")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getColumns(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String connectionId = request.getParameter("connectionId") ;
		String tableName = request.getParameter("tableName") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(connectionId)||StringUtils.isEmptyOrWhitespaceOnly(tableName)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","获取失败");
		}else{
			params.put("connectionId", connectionId) ;
			params.put("tableName", tableName) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			databaseUtilService.getColumns(retMap,params);
		}
		return retMap ;
	}
	/**
	 * 获取数据源下的所有数据表信息
	 * @return
	 */
	@POST
	@Path("/search/tables")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getTables(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String connectionId = request.getParameter("connectionId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(connectionId)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg","获取失败");
		}else{
			params.put("connectionId", connectionId) ;
			params.put("userId", Session.getCurrUser().getId()) ;
			databaseUtilService.getTables(retMap,params);
		}
		return retMap ;
	}
	/**
	 * 新增或更新数据库配置信息
	 * @return
	 */
	@POST
	@Path("/test/connection")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> testConnection(DataBaseEntity entity ){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		retMap.put("data", new ArrayList<String>()) ;
		if(entity == null){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "连接失败,请检查连接信息") ;
			return retMap ;
		}else{
			//将密码解密，放入实体中，用于测试连接
			if(!StringUtils.isEmptyOrWhitespaceOnly(entity.getPassword())){
				String passwordTemp = AES.aesDecrypt(entity.getPassword()) ;
				entity.setPassword(passwordTemp);
			}
			databaseService.testConnection(retMap, entity);
		}
		
		return retMap ;
	}
	/**
	 * 新增或更新数据库配置信息
	 * @return
	 */
	@POST
	@Path("/saveOrUpdate/database")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> addDataBase(DataBaseEntity entity){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		if(StringUtils.isEmptyOrWhitespaceOnly(entity.getConnectionId())){
			entity.setConnectionId(null);//新增
		}
		//初始赋值
		entity.setState("00") ;
		entity.setLastUpdateUserId(Session.getCurrUser().getId());
		entity.setLastUpdateUserName(Session.getCurrUser().getUsername());
		entity.setLastUpdateDate(new Date(System.currentTimeMillis()));
		if(!StringUtils.isEmptyOrWhitespaceOnly(entity.getPassword())){
			String passwordTemp = AES.aesDecrypt(entity.getPassword()) ;
			String password = AESUtil.AESEncode(passwordTemp) ;//加密
			entity.setPassword(password);
			String result = CheckParametersUtil.getInstance()
					.put(entity.getConnectionName(),"connectionName")
					.put(entity.getDatabaseName(),"databaseName")
					.put(entity.getDatabaseType(),"databaseType")
					.put(entity.getIp(),"ip")
					.put(entity.getPort(),"port")
		            .put(entity.getUsername(),"username")
		            .put(entity.getPassword(),"password")
		            .put(entity.getDriver(),"driver")
		            .checkParameter();
			if(result != null){
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "保存失败,请检查表单完整性") ;
				return  retMap;
			}
			databaseService.saveToDataBase(retMap,entity);
		}
		return retMap ;
	}
	/**
	 * 删除数据库配置信息
	 * @return
	 */
	@POST
	@Path("/delete/database")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> deleteDataBase(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String connectionId = request.getParameter("connectionId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(connectionId)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "请选择需要删除的数据") ;
			return retMap ;
		}
		params.put("userId", Session.getCurrUser().getId()) ;
		params.put("connectionId", connectionId) ;
		databaseService.deleteDataBase(retMap, params);
		return retMap ;
	}
	/**
	 * 删除数据库配置信息
	 * @return
	 */
	@GET
	@POST
	@Path("/delete/state")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> deleteState(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String connectionId = request.getParameter("connectionId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(connectionId)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "请选择需要删除的数据") ;
			return retMap ;
		}
		params.put("connectionId", connectionId) ;
		databaseService.deleteState(retMap, params);
		return retMap ;
	}
	/**
	 * 删除数据库配置信息
	 * @return
	 */
	@GET
	@POST
	@Path("/update/state")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> updateState(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		String connectionId = request.getParameter("connectionId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(connectionId)){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "请选择需要编辑的数据") ;
			return retMap ;
		}
		params.put("userId", Session.getCurrUser().getId()) ;
		params.put("connectionId", connectionId) ;
		databaseService.updateState(retMap, params);
		return retMap ;
	}
	/**
	 * 查询数据库配置列表
	 * @return
	 */
	@GET
	@POST
	@Path("/search/database")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getDataBaseList(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		//可通过数据库类型和连接名查询
		String connectionId = request.getParameter("connectionId") ;
		String connectionName = request.getParameter("connectionName") ;
		String databaseType = request.getParameter("databaseType") ;
		params.put("pageCount", request.getParameter("pageCount")) ;
		params.put("pageSize", request.getParameter("pageSize")) ;
		params.put("userId", Session.getCurrUser().getId()) ;
		if("all".equals(databaseType)){
			databaseType = "" ;
		}
		params.put("connectionId", connectionId);
		params.put("connectionName", connectionName) ;
		params.put("databaseType", databaseType) ;
		databaseService.getDBList(retMap, params);
		return retMap ;
	}
	/**
	 * 查询数据库配置码表
	 * @return
	 */
	@GET
	@POST
	@Path("/codeTable/database")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getDataBaseTimer(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		Map<String,Object> params = new HashMap<String,Object>() ;
		//可通过数据库类型和连接名查询
		params.put("userId", Session.getCurrUser().getId()) ;
		databaseService.getDBCodeTable(retMap, params);
		return retMap ;
	}
	/**
	 * 查询数据库配置码表信息
	 * @return
	 */
	@GET
	@POST
	@Path("/search/dbType")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getDataBaseType(){
		Map<String,Object> retMap = new HashMap<String,Object>() ;
		databaseService.getDBConfig(retMap);
		return retMap ;
	}
	
}
