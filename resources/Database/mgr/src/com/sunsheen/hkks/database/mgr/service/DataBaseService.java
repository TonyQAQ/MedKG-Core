package com.sunsheen.hkks.database.mgr.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.AES;
import com.sunsheen.hkks.common.util.AESUtil;
import com.sunsheen.hkks.common.util.CheckParametersUtil;
import com.sunsheen.hkks.common.util.MapBeanUtil;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.database.mgr.entity.DataBaseEntity;
import com.sunsheen.hkks.database.util.ConnectionFactory;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

/**
 * 
 * @ClassName: DataBaseService  
 * @Description: 数据库配置信息增删查改相关接口Service层  
 * @author FengTao
 * @date 2020年7月7日  
 *
 */
@SuppressWarnings("unchecked")
public class DataBaseService {
	
	/**
	 * 测试数据库连接 
	 * @param retMap
	 * @param entity
	 */
	public void testConnection(Map<String,Object> retMap , DataBaseEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Connection conn = null ;
		//如果connnectionId存在，则通过数据库查询获取实体,并且将密码解密
		String connectionId = entity.getConnectionId() ;
		if(!StringUtils.isEmptyOrWhitespaceOnly(connectionId)){
			Map<String,Object> temp= new HashMap<String,Object>() ;
			temp.put("connectionId", connectionId) ;
			temp = (Map<String, Object>) session.createDySQLQuery("DataBase.queryDataBaseByConnId", temp ).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(temp !=null && temp.size() > 0 ){
				entity = (DataBaseEntity) MapBeanUtil.map2Object(temp, DataBaseEntity.class)  ;
				//从数据库取出的密码需要解密，才能进行测试连接
				String password = AESUtil.AESDecode(entity.getPassword()) ;
				entity.setPassword(password);
			}
		}
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
			retMap.put("retmsg", "连接失败,请检查连接信息") ;
			return  ;
        }
		try{
			conn = ConnectionFactory.getConnection(MapBeanUtil.object2Map(entity)) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "连接成功") ;
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "连接失败,请检查连接信息") ;
			e.printStackTrace();
		}finally{
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 删除数据源配置信息
	 * @param retMap
	 * @param params
	 */
	public void deleteDataBase(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			Object num = session.createDySQLQuery("DataBase.deleteDB", params).executeUpdate() ;
			if("0".equals(num.toString())){
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "删除失败") ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg", "删除成功") ;
			}
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断当前数据源是否可以被删除
	 * @param retMap
	 * @param params
	 */
	public void deleteState(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> temp = new HashMap<String , Object>() ;
		List<String> result = new ArrayList<String>() ;
		retMap.putAll(RetInfo.RETSUCCESS) ;
		try{
			//TODO:查询当前数据源是否绑定了任务，绑定了则不能删除
			result = session.createDySQLQuery("DataBase.deleteState", params).list();
			if(result != null && result.size() > 0){
				String name = "" ;
				int sequence = 0 ;
				for(String str:result) {
					name += (++sequence)+"、<strong>【"+str+"】</strong>\n" ;
				}
				temp.put("deleteState","01");
				temp.put("deleteComfirm", "该数据源已绑定相关任务，删除后会终止相关任务，确定删除吗？\n"+name) ;
				retMap.put("data", temp) ;
			}else{
				temp.put("deleteState","00");
				temp.put("deleteComfirm", "确认删除吗？") ;
				retMap.put("data", temp) ;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断当前数据源是否可以被删除
	 * @param retMap
	 * @param params
	 */
	public void updateState(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> temp = new HashMap<String , Object>() ;
		List<String> result = new ArrayList<String>() ;
		retMap.putAll(RetInfo.RETSUCCESS) ;
		try{
			//TODO:查询当前数据源是否绑定了任务，绑定了则不能删除
			result = session.createDySQLQuery("DataBase.deleteState", params).list();
			if(result != null && result.size() > 0){
				String name = "" ;
				int sequence = 0 ;
				for(String str:result) {
					name += (++sequence)+"、<strong>【"+str+"】</strong><br>" ;
				}
				temp.put("updateState","00");
				temp.put("updateComfirm", "该数据源已绑定相关任务，确定要编辑吗？<br>"+name) ;
				retMap.put("data", temp) ;
			}else{
				temp.put("updateState","02");
				temp.put("updateComfirm", "确认编辑吗？") ;
				retMap.put("data", temp) ;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存或更新数据库
	 * @param retMap
	 * @param entity
	 */
	public void saveToDataBase(Map<String,Object> retMap,DataBaseEntity entity){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			session.beginTransaction();
			session.saveOrUpdate(entity);
			session.commit();
			session.close();
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
	 * 获取配置的数据源列表
	 * @param retMap
	 */
	public void getDBConfig(Map<String,Object> retMap){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<String> serviceTypeList = new ArrayList<String>() ;
		List<Map<String,Object>> DBConfigList = new ArrayList<Map<String,Object>>() ;
		try{
			DBConfigList = session.createDySQLQuery("DataBase.queryDBType", new HashMap<String,Object>())
					.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			if(DBConfigList.size() > 0){
				for(Map<String,Object> temp : DBConfigList){
					if(!StringUtils.isEmptyOrWhitespaceOnly((String)temp.get("serviceType"))){
						String[] strs = temp.get("serviceType").toString().split(",") ;
						for(String str : strs){
							serviceTypeList.add(str) ;
						}
						temp.put("serviceType", serviceTypeList) ;
					}
				}
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", DBConfigList) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", DBConfigList) ;
		}
	}
	
	/**
	 * 获取数据源配置列表
	 * @param retMap
	 * @param params
	 */
	public void getDBList(Map<String,Object> retMap , Map<String,Object> params){
		
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("Title.databaseTile")));
		List<Map<String,Object>> DBList = new ArrayList<Map<String,Object>>() ;
		try{
			Object totalCount = session.createDySQLQuery("DataBase.queryDBListCount", params).uniqueResult() ;
			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), (String)totalCount);
			DBList = session.createDySQLQuery("DataBase.queryDBList", params)
							.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			int sequence = 0 ;
			for(Map<String,Object> temp : DBList){
				sequence ++ ;
				temp.put("sequence", sequence) ;
				temp.put("rowKey", temp.get("connectionId")) ;
				if(StringUtils.isEmptyOrWhitespaceOnly((String)temp.get("serviceType"))){
					temp.put("serviceType", "-") ;
				}
				if(StringUtils.isEmptyOrWhitespaceOnly((String)temp.get("memo"))){
					temp.put("memo", "-") ;
				}
				//将密码后端解密，然后为前端加密后返回
				String password = temp.get("password").toString() ;
				password = AESUtil.AESDecode(password) ;
				password = AES.aesEncrypt(password);
				temp.put("password", password) ;
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", DBList) ;
			retMap.put("totalCount", totalCount) ;
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("data", DBList) ;
			retMap.put("totalCount", "0") ;
			e.printStackTrace();
		}
	}
	/**
	 * 获取数据源配置列表
	 * @param retMap
	 * @param params
	 */
	public void getDBCodeTable(Map<String,Object> retMap , Map<String,Object> params){
		
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> DBList = new ArrayList<Map<String,Object>>() ;
		try{
			DBList = session.createDySQLQuery("DataBase.queryDBCodeTable", params)
							.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", DBList) ;
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("data", DBList) ;
			retMap.put("totalCount", "0") ;
			e.printStackTrace();
		}
	}
}
