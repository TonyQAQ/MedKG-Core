package com.sunsheen.hkks.database.util.pool;

import java.util.Map;

/**
 * 获取各类数据库配置
 * @author FengTao
 *
 */
public class DataSourceParam {
	
	public static DataSourceEntity getParams(Map<String, Object> DBdata) throws Exception{
		DataSourceEntity dataSource = new DataSourceEntity() ;
        switch (DBdata.get("databaseType").toString()) {
        	case "Oracle":
        		dataSource = DataSourceParam.getOracleParams(DBdata);
        		break;
            case "MySQL":
                dataSource = DataSourceParam.getMySQLParams(DBdata);
                break;
            case "SqlServer":
                dataSource = DataSourceParam.getSqlServerParams(DBdata);
                break;
            case "Postgres":
                dataSource = DataSourceParam.getPostgresParams(DBdata);
                break;
            case "Xugu":
                dataSource = DataSourceParam.getXuguParams(DBdata);
                break;
        }
        return dataSource;
	}
	/**
	 * 获取MySQL配置
	 *
	 * @param retMap
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static DataSourceEntity getMySQLParams(Map<String, Object> data) throws Exception {
		
		DataSourceEntity dataSource = new DataSourceEntity() ;
//		String driver = "oracle.jdbc.driver.OracleDriver" ;
		String driver = "com.mysql.cj.jdbc.Driver" ;
		String url = "jdbc:mysql://" + data.get("ip") + ":" + data.get("port") + "/" + data.get("databaseName") 
						+ "?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC";
		setDataSourceEntity(dataSource,driver,url,data) ;
		return dataSource ;
	}

	/**
	 * 获取Oracle连接配置
	 *
	 * @param retMap
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static DataSourceEntity getOracleParams(Map<String, Object> data) throws Exception {
		
		String driver = "oracle.jdbc.driver.OracleDriver" ;
		String url = "jdbc:oracle:" + "thin:@" + data.get("ip") + ":" + data.get("port") + ":" + data.get("databaseName");
		DataSourceEntity dataSource = new DataSourceEntity() ;
		setDataSourceEntity(dataSource,driver,url,data) ;
		return dataSource ;
	}

	/**
	 * 获取Postgres连接配置
	 *
	 * @param retMap
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static DataSourceEntity getPostgresParams(Map<String, Object> data) throws Exception {
		
		String driver = "org.postgresql.Driver" ;
		String url = "jdbc:postgresql://" + data.get("ip") + ":" + data.get("port") + "/" + data.get("databaseName");
		DataSourceEntity dataSource = new DataSourceEntity() ;
		setDataSourceEntity(dataSource,driver,url,data) ;
		return dataSource ;
	}

	/**
	 * 获取SQLserver连接配置
	 *
	 * @param retMap
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static DataSourceEntity getSqlServerParams(Map<String, Object> data) throws Exception {
		
		String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver" ;
		String url = "jdbc:sqlserver://" + data.get("ip") + ":" + data.get("port") + ";databaseName=" + data.get("databaseName");
		DataSourceEntity dataSource = new DataSourceEntity() ;
		setDataSourceEntity(dataSource,driver,url,data) ;
		return dataSource ;
	}

	/**
	 * 获取Xugu连接配置
	 *
	 * @param retMap
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static DataSourceEntity getXuguParams(Map<String, Object> data) throws Exception {

		String driver = "com.xugu.cloudjdbc.Driver" ;
		String url = "jdbc:xugu://" + data.get("ip") + ":" + data.get("port") + "/" + data.get("databaseName");
		DataSourceEntity dataSource = new DataSourceEntity() ;
		setDataSourceEntity(dataSource,driver,url,data) ;
		return dataSource ;
	}
	
	private static void setDataSourceEntity(DataSourceEntity dataSource,String driver ,String url ,Map<String, Object> data){
		dataSource.setDriver(driver);
		dataSource.setUrl(url);
		dataSource.setUsername((String) data.get("username"));
		dataSource.setPassword((String) data.get("password"));
	}
}
