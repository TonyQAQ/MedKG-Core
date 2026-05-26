package com.sunsheen.hkks.database.mgr.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.Transformers;




import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.AESUtil;
import com.sunsheen.hkks.common.util.MapBeanUtil;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.database.mgr.entity.DataBaseEntity;
import com.sunsheen.hkks.database.util.DBConnectionComponent;
import com.sunsheen.hkks.database.util.pool.DataSourceEntity;
import com.sunsheen.hkks.database.util.pool.DataSourceParam;
import com.sunsheen.hkks.database.util.pool.JDBCPoolUtils;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

/**
 * 
 * @Title: DataBaseReadService
 * @Description: 获取数据库表、字段、数据信息
 * @author: FengTao
 * @date 2020年7月9日 下午3:32:44
 */
@SuppressWarnings("unchecked")
public class DataBaseUtilService {

	/**
	 * 
	* @Title: destroyPool
	* @Description:关闭数据源连接池
	* @author: FengTao
	* @date 2020年8月4日 下午3:24:43
	* @param retMap
	* @param params void
	* @version
	 */
	public void destroyPool(Map<String,Object> retMap , Map<String,Object> params ){
		Map<String,Object> DBdata = new HashMap<String,Object>() ;
		JDBCPoolUtils jdbcPool = new JDBCPoolUtils() ;
		DataSourceEntity dataSource = null ;
		try{
			DBdata = getDBdataMap(params) ;
			//建立连接池需要的参数信息
			dataSource = DataSourceParam.getParams(DBdata) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			jdbcPool.destroyPool(dataSource.getUrl(), dataSource.getUsername());
		}catch(SQLException e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "报错信息:"+e.getMessage()) ;
			e.printStackTrace();
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "销毁连接池失败") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: getColumnDataByRowIndexs
	* @Description: 通过多个行号直接获取数据信息
	* @author: FengTao
	* @date 2020年7月9日 下午3:37:31 void
	* @version
	 */
	public Map<String,Object> getColumnDataByRowIndexs(Map<String,Object> retMap , Map<String,Object> params ){
		Map<String,Object> DBdata = new HashMap<String,Object>() ;
		List<Object> data = new ArrayList<Object>() ;
		JDBCPoolUtils jdbcPool = new JDBCPoolUtils() ;
		DataSourceEntity dataSource = null ;
		try{
			if( StringUtils.isEmptyOrWhitespaceOnly((String)params.get("connectionId")) ){
				DBdata = getDBdataMap(params) ;
			}else{
				DBdata = getDBdataMapByConnId(params) ;
			}
			//建立连接池需要的参数信息
			dataSource = DataSourceParam.getParams(DBdata) ;
			//从连接池建立连接信息,不存在则会新建，存在则什么都不做
			jdbcPool.getConnection(dataSource.getDriver(), dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
			//查询指定列数据的SQL
			String columnDataSql = DBConnectionComponent.getQueryFieldDataSqlByRowIndexs(DBdata) ;
			data = jdbcPool.excuteSQLGetListStr(columnDataSql) ;
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
			//jdbcPool.destroyPool(dataSource.getUrl(), dataSource.getUsername());
		}catch(SQLException e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "报错信息:"+e.getMessage()) ;
			e.printStackTrace();
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "数据获取失败") ;
			e.printStackTrace();
		}
		return retMap ;
	 }
	
	/**
	 * 
	* @Title: getColumnDataByRowIndex
	* @Description: 通过行号直接获取数据信息
	* @author: FengTao
	* @date 2020年7月9日 下午3:37:31 void
	* @version
	 */
	public Map<String,Object> getColumnDataByRowIndex(Map<String,Object> retMap , Map<String,Object> params ){
		Map<String,Object> DBdata = new HashMap<String,Object>() ;
		List<Object> data = new ArrayList<Object>() ;
		JDBCPoolUtils jdbcPool = new JDBCPoolUtils() ;
		DataSourceEntity dataSource = null ;
		try{
			if( StringUtils.isEmptyOrWhitespaceOnly((String)params.get("connectionId")) ){
				DBdata = getDBdataMap(params) ;
			}else{
				DBdata = getDBdataMapByConnId(params) ;
			}
			//建立连接池需要的参数信息
			dataSource = DataSourceParam.getParams(DBdata) ;
			//从连接池建立连接信息,不存在则会新建，存在则什么都不做
			jdbcPool.getConnection(dataSource.getDriver(), dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
			//查询指定列数据的SQL
			String columnDataSql = DBConnectionComponent.getQueryFieldDataSqlByRowIndex(DBdata) ;
			data = jdbcPool.excuteSQLGetListStr(columnDataSql) ;
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
			//jdbcPool.destroyPool(dataSource.getUrl(), dataSource.getUsername());
		}catch(SQLException e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "报错信息:"+e.getMessage()) ;
			e.printStackTrace();
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "数据获取失败") ;
			e.printStackTrace();
		}
		return retMap ;
	 }
	
	/**
	 * 
	* @Title: getColumnData
	* @Description: 获取数据信息
	* @author: FengTao
	* @date 2020年7月9日 下午3:37:31 void
	* @version
	 */
	public void getColumnData(Map<String,Object> retMap , Map<String,Object> params ){
		Map<String,Object> DBdata = new HashMap<String,Object>() ;
		List<Map<String,Object>> dataCount = new ArrayList<Map<String,Object>>() ;
		List<Object> data = new ArrayList<Object>() ;
		JDBCPoolUtils jdbcPool = new JDBCPoolUtils() ;
		DataSourceEntity dataSource = null ;
		try{
			if( StringUtils.isEmptyOrWhitespaceOnly((String)params.get("connectionId")) ){
				DBdata = getDBdataMap(params) ;
			}else{
				DBdata = getDBdataMapByConnId(params) ;
			}
			//查询指定列数据量的SQL
			String columnDataCountSql = DBConnectionComponent.getQueryFieldDataCountSql(DBdata);
			//建立连接池需要的参数信息
			dataSource = DataSourceParam.getParams(DBdata) ;
			//从连接池建立连接信息,不存在则会新建，存在则什么都不做
			jdbcPool.getConnection(dataSource.getDriver(), dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
			//执行sql
			dataCount = jdbcPool.excuteSQL(columnDataCountSql) ;
			//分页参数封装
			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), dataCount.get(0).get("totalCount").toString());
			DBdata.putAll(params);
			//查询指定列数据的SQL
			String columnDataSql = DBConnectionComponent.getQueryFieldDataSql(DBdata) ;
			data = jdbcPool.excuteSQLGetListStr(columnDataSql) ;
			
			retMap.put("data", data) ;
			retMap.put("totalCount", dataCount.get(0).get("totalCount").toString()) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
			//jdbcPool.destroyPool(dataSource.getUrl(), dataSource.getUsername());
		}catch(SQLException e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "报错信息:"+e.getMessage()) ;
			e.printStackTrace();
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "数据获取失败") ;
			e.printStackTrace();
		}
	 }
	/**
	 * 
	* @Title: getColumnDataByFilter
	* @Description: 根据过滤规则获取数据信息
	* @author: FengTao
	* @date 2020年7月9日 下午3:37:31 void
	* @version
	 */
	public Map<String,Object> getColumnDataByFilter(Map<String,Object> retMap , Map<String,Object> params ){
		Map<String,Object> DBdata = new HashMap<String,Object>() ;
		List<Map<String,Object>> dataCount = new ArrayList<Map<String,Object>>() ;
		List<Object> data = new ArrayList<Object>() ;
		JDBCPoolUtils jdbcPool = new JDBCPoolUtils() ;
		DataSourceEntity dataSource = null ;
		try{
			if( StringUtils.isEmptyOrWhitespaceOnly((String)params.get("connectionId")) ){
				DBdata = getDBdataMap(params) ;
			}else{
				DBdata = getDBdataMapByConnId(params) ;
			}
			//DBdata添加过滤规则
			DBdata.put("filterSql", params.get("filterSql")) ;
			//查询指定列数据量的SQL
			String columnDataCountSql = DBConnectionComponent.getQueryFieldDataCountByFilterSql(DBdata);
			//建立连接池需要的参数信息
			dataSource = DataSourceParam.getParams(DBdata) ;
			//从连接池建立连接信息,不存在则会新建，存在则什么都不做
			jdbcPool.getConnection(dataSource.getDriver(), dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
			//执行sql
			System.out.print("columnDataCountSql -- > "+columnDataCountSql);
			dataCount = jdbcPool.excuteSQL(columnDataCountSql) ;
			//分页参数封装
			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), dataCount.get(0).get("totalCount").toString());
			DBdata.putAll(params);
			//查询指定列数据的SQL
			String columnDataSql = DBConnectionComponent.getQueryFieldDataByFilterSql(DBdata) ;
			data = jdbcPool.excuteSQLGetListStr(columnDataSql) ;
			
			retMap.put("data", data) ;
			retMap.put("totalCount", dataCount.get(0).get("totalCount").toString()) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
			//jdbcPool.destroyPool(dataSource.getUrl(), dataSource.getUsername());
		}catch(SQLException e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "报错信息:"+e.getMessage()) ;
			e.printStackTrace();
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "数据获取失败") ;
			e.printStackTrace();
		}
		return retMap ;
	 }
	/**
	 * 
	* @Title: getColumnDataByFilter
	* @Description: 根据过滤规则获取数据信息
	* @author: FengTao
	* @date 2020年7月9日 下午3:37:31 void
	* @version
	 */
	public String getColumnDataCountByFilter(Map<String,Object> retMap , Map<String,Object> params ){
		Map<String,Object> DBdata = new HashMap<String,Object>() ;
		List<Map<String,Object>> dataCount = new ArrayList<Map<String,Object>>() ;
		String totalCount = "0" ;
		JDBCPoolUtils jdbcPool = new JDBCPoolUtils() ;
		DataSourceEntity dataSource = null ;
		try{
			if( StringUtils.isEmptyOrWhitespaceOnly((String)params.get("connectionId")) ){
				DBdata = getDBdataMap(params) ;
			}else{
				DBdata = getDBdataMapByConnId(params) ;
			}
			//DBdata添加过滤规则
			DBdata.put("filterSql", params.get("filterSql")) ;
			//查询指定列数据量的SQL
			String columnDataCountSql = DBConnectionComponent.getQueryFieldDataCountByFilterSql(DBdata);
			//建立连接池需要的参数信息
			dataSource = DataSourceParam.getParams(DBdata) ;
			//从连接池建立连接信息,不存在则会新建，存在则什么都不做
			jdbcPool.getConnection(dataSource.getDriver(), dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
			//执行sql
			dataCount = jdbcPool.excuteSQL(columnDataCountSql) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			totalCount = dataCount.get(0).get("totalCount").toString() ;
		}catch(SQLException e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "报错信息:"+e.getMessage()) ;
			e.printStackTrace();
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "数据获取失败") ;
			e.printStackTrace();
		}
		return totalCount ;
	 }
	/**
	 * 
	* @Title: getColumns
	* @Description: 获取表的列信息
	* @author: FengTao
	* @date 2020年7月9日 下午3:37:48 void
	* @version
	 */
	public List<Map<String,Object>> getColumns(Map<String,Object> retMap  , Map<String,Object> params ){
		
		Map<String,Object> DBdata = new HashMap<String,Object>() ;
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		JDBCPoolUtils jdbcPool = new JDBCPoolUtils() ;
		DataSourceEntity dataSource = null ;
		//retMap.put("columnsHead", JSONArray.fromObject(Configs.get("Title.columnsHead")));	
		try{
			if( StringUtils.isEmptyOrWhitespaceOnly((String)params.get("connectionId")) ){
				DBdata = getDBdataMap(params) ;
			}else{
				DBdata = getDBdataMapByConnId(params) ;
			}
			//查询的sql
			String columnsSql = DBConnectionComponent.getQueryFieldSql(DBdata) ;
			//建立连接池需要的参数信息
			dataSource = DataSourceParam.getParams(DBdata) ;
			//从连接池建立连接信息,不存在则会新建，存在则什么都不做
			jdbcPool.getConnection(dataSource.getDriver(), dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
			//执行sql
			data = jdbcPool.excuteSQL(columnsSql) ;
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			//jdbcPool.destroyPool(dataSource.getUrl(), dataSource.getUsername());
		}catch(SQLException e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "报错信息:"+e.getMessage()) ;
			e.printStackTrace();
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "字段信息获取失败") ;
			e.printStackTrace();
		}
		return data ;
	}
	/**
	 * 
	* @Title: getTables
	* @Description: 获取数据源的表信息
	* @author: FengTao
	* @date 2020年7月9日 下午3:38:05 void
	* @version
	 */
	public List<Map<String,Object>> getTables(Map<String,Object> retMap , Map<String,Object> params ){
		
		Map<String,Object> DBdata = new HashMap<String,Object>() ;
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		JDBCPoolUtils jdbcPool = new JDBCPoolUtils() ;
		DataSourceEntity dataSource = null ;
		//retMap.put("tableHead", JSONArray.fromObject(Configs.get("Title.tableHead")));
		try{
			if( StringUtils.isEmptyOrWhitespaceOnly((String)params.get("connectionId")) ){
				DBdata = getDBdataMap(params) ;
			}else{
				DBdata = getDBdataMapByConnId(params) ;
			}
			//查询表的sql
			String tableSql = DBConnectionComponent.getQueryTableSql(DBdata) ;
			//建立连接池需要的参数信息
			dataSource = DataSourceParam.getParams(DBdata) ;
			//从连接池建立连接信息,不存在则会新建，存在则什么都不做
			jdbcPool.getConnection(dataSource.getDriver(), dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
			//执行sql
			data = jdbcPool.excuteSQL(tableSql) ;
			//销毁连接池
			//jdbcPool.destroyPool(dataSource.getUrl(), dataSource.getUsername());
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
		}catch(SQLException e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "报错信息:"+e.getMessage()) ;
			e.printStackTrace();
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "表信息获取失败") ;
			e.printStackTrace();
		}
		return data ;
	}
	
	/**
	 * 
	* @Title: getDBdataMap
	* @Description:获取查询参数信息
	* @author: FengTao
	* @date 2020年7月9日 下午4:15:22
	* @param params
	* @return
	* @throws Exception Map<String,Object>
	* @version
	 */
	public Map<String,Object> getDBdataMap(Map<String,Object> params) throws Exception{
		
		Map<String,Object> temp = new HashMap<String,Object>() ;
		Map<String,Object> DBdata = new HashMap<String,Object>() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		DataBaseEntity entity = null ;
		temp = (Map<String, Object>) session.createDySQLQuery("DataBase.queryDataBaseById", params ).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
		if(temp !=null && temp.size() > 0 ){
			entity = (DataBaseEntity) MapBeanUtil.map2Object(temp, DataBaseEntity.class)  ;
			//从数据库取出的密码需要解密，才能进行正常连接
			String password = AESUtil.AESDecode(entity.getPassword()) ;//解密
			entity.setPassword(password);
			DBdata = MapBeanUtil.object2Map(entity) ;
			DBdata.putAll(params);
		}
		return DBdata ;
	}
	/**
	 * 
	* @Title: getDBdataMapByConnId
	* @Description:获取查询参数信息,数据源ID查询
	* @author: FengTao
	* @date 2020年8月6日 下午4:03:35
	* @param params
	* @return
	* @throws Exception Map<String,Object>
	* @version
	 */
	public Map<String,Object> getDBdataMapByConnId(Map<String,Object> params) throws Exception{
		
		Map<String,Object> temp = new HashMap<String,Object>() ;
		Map<String,Object> DBdata = new HashMap<String,Object>() ;
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		DataBaseEntity entity = null ;
		temp = (Map<String, Object>) session.createDySQLQuery("DataBase.queryDataBaseByConnId", params ).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
		if(temp !=null && temp.size() > 0 ){
			entity = (DataBaseEntity) MapBeanUtil.map2Object(temp, DataBaseEntity.class)  ;
			//从数据库取出的密码需要解密，才能进行正常连接
			String password = AESUtil.AESDecode(entity.getPassword()) ;//解密
			entity.setPassword(password);
			DBdata = MapBeanUtil.object2Map(entity) ;
			DBdata.putAll(params);
		}
		return DBdata ;
	}
	
}
