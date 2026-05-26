package com.sunsheen.hkks.database.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @ClassName: DBConnectionComponent  
 * @Description: 数据库连接工具类  
 * @author FengTao
 * @date 2020年7月7日  
 *
 */
public class DBConnectionComponent {
  /**
   * 获取MySQL
   *
   * @param data
   * @return
   * @throws Exception
   */
  public static Connection getMySQLConnection(Map<String, Object> data) throws Exception {
    Connection con = null;
    Class.forName("com.mysql.jdbc.Driver");
    String url = "jdbc:mysql://" + data.get("ip") + ":" + data.get("port") + "/" + data.get("databaseName")+"?useUnicode=true&characterEncoding=UTF-8"
    										+"&useSSL=false&serverTimezone=GMT&connectTimeout=2500&socketTimeout=3000&zeroDateTimeBehavior=round";
    String user = (String) data.get("username");
    String password = (String) data.get("password");
    DriverManager.setLoginTimeout(3);
    con = DriverManager.getConnection(url, user, password);
    return con;
  }

  /**
   * 获取Oracle连接
   *
   * @param data
   * @return
   * @throws Exception
   */
  public static Connection getOracleConnection(Map<String, Object> data) throws Exception {
    Connection con = null;
    Class.forName("oracle.jdbc.driver.OracleDriver");
    String url = "jdbc:oracle:" + "thin:@" + data.get("ip") + ":" + data.get("port") + ":" + data.get("databaseName");
    String user = (String) data.get("username");
    String password = (String) data.get("password");
    DriverManager.setLoginTimeout(3);
    con = DriverManager.getConnection(url, user, password);
    return con;
  }

  /**
   * 获取Postgres连接
   *
   * @param data
   * @return
   * @throws Exception
   */
  public static Connection getPostgresConnection(Map<String, Object> data) throws Exception {
    Connection con = null;
    Class.forName("org.postgresql.Driver");
    String url = "jdbc:postgresql://" + data.get("ip") + ":" + data.get("port") + "/" + data.get("databaseName");
    String user = (String) data.get("username");
    String password = (String) data.get("password");
    DriverManager.setLoginTimeout(3);
    con = DriverManager.getConnection(url, user, password);
    return con;
  }

  /**
   * 获取SQLserver连接
   *
   * @param data
   * @return
   * @throws Exception
   */
  public static Connection getSqlServerConnection(Map<String, Object> data) throws Exception {
    Connection con = null;
    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    String url = "jdbc:sqlserver://" + data.get("ip") + ":" + data.get("port") + ";databaseName=" + data.get("databaseName");
    String user = (String) data.get("username");
    String password = (String) data.get("password");
    DriverManager.setLoginTimeout(3);
    con = DriverManager.getConnection(url, user, password);
    return con;
  }

  /**
   * 获取Xugu连接
   *
   * @param data
   * @return
   * @throws Exception
   */
  public static Connection getXuguConnection(Map<String, Object> data) throws Exception {
    Connection con = null;
    Class.forName("com.xugu.cloudjdbc.Driver");
    String url = "jdbc:xugu://" + data.get("ip") + ":" + data.get("port") + "/" + data.get("databaseName");
    String user = (String) data.get("username");
    String password = (String) data.get("password");
    DriverManager.setLoginTimeout(3);
    con = DriverManager.getConnection(url, user, password);
    return con;
  }

  /**
   * 获取查询tables SQL语句
   *
   * @return
   */
  public static String getQueryTableSql(Map<String, Object> DBdata) {
    String sql = "";
    switch (DBdata.get("databaseType").toString()) {
      case "Oracle":
        sql = "SELECT B.TABLE_NAME,B.COMMENTS as TABLE_COMMENT FROM USER_TABLES A LEFT JOIN ALL_TAB_COMMENTS B ON A.TABLE_NAME = B.TABLE_NAME ";
        break;
      case "MySQL":
        sql = "SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' and TABLE_SCHEMA = '" + DBdata.get("databaseName") + "'";
        break;
      case "SqlServer":
        sql = "SELECT Name FROM SysObjects Where XType='U' ORDER BY Name";
        break;
      case "Postgres":
        sql = "SELECT relname AS table_name, CAST ( obj_description (relfilenode,'pg_class') AS VARCHAR ) AS table_comment FROM pg_class  WHERE relkind = 'r' AND relname NOT LIKE 'pg_%' AND relname NOT LIKE 'sql_%' ORDER BY relname";
        break;
      case "Xugu":
        sql = "SELECT table_name,comments as table_comment FROM user_tables order by table_name" ;
    }
    return sql;
  }

  /**
   * 根据表名查询表字段信息
   * @param DBdata
   * @return
   */
  public static String getQueryFieldSql(Map<String, Object> DBdata){
    String sql = "";
    String tableName = DBdata.get("tableName").toString();
    String databaseName = DBdata.get("databaseName").toString();
    switch (DBdata.get("databaseType").toString()) {
      case "Oracle":
        sql = "SELECT b.COLUMN_NAME as column_name,b.DATA_TYPE as column_type,A.COMMENTS as column_comment FROM USER_TAB_COLUMNS b,USER_COL_COMMENTS A WHERE b.TABLE_NAME = '"+ tableName +"' AND b.TABLE_NAME = A .TABLE_NAME AND b.COLUMN_NAME = A .COLUMN_NAME";
        break;
      case "MySQL":
        sql = "SELECT a.COLUMN_NAME AS column_name,a.DATA_TYPE AS column_type,a.COLUMN_COMMENT AS column_comment FROM information_schema. COLUMNS a where  a.table_name = '"+tableName+"' and a.table_schema = '"+databaseName+"';";
        break;
      case "SqlServer":
        sql = "select cast([value] as varchar(500)) from_name ,a.name from_code,a.xprec from_length,c.name from_type from syscolumns a left join sys.extended_properties b  on  a.id=b.major_id and a.colid=b.minor_id left join systypes c on a.xtype=c.xtype where id=object_id('"+tableName+"');";
        break;
      case "Postgres":
        sql = "SELECT A.attname AS column_name,T.typname AS column_type,A.atttypmod AS from_length,	b.description AS column_comment FROM	pg_class C INNER JOIN pg_attribute A ON C .oid = A .attrelid INNER JOIN pg_type T ON A .atttypid = T .oid LEFT JOIN pg_description b ON b.objsubid = A .attnum AND C .oid = b.objoid WHERE	A .attnum > 0 AND C .relname = '"+tableName+"' ORDER BY A .attnum ;";
        break;
      case "Xugu" :
        sql = "SELECT x.COL_NAME AS column_name, x.TYPE_NAME AS column_type, x.SCALE AS length, x.COMMENTS AS column_comment FROM user_columns as x where table_id = ( select table_id from user_tables where table_name='"+tableName+"' );" ;
        break;
    }
    return sql;
  }
  
  /**
   * 根据表名查询表字段下的数据信息
   * @param DBdata
   * @return
   */
  public static String getQueryFieldDataSql(Map<String, Object> DBdata){
    String sql = "";
    String pageIndex = DBdata.get("pageIndex").toString();
    String pageSize = DBdata.get("pageSize").toString();
    String columnName = DBdata.get("columnName").toString();
    String tableName = DBdata.get("tableName").toString();
    switch (DBdata.get("databaseType").toString()) {
      case "Oracle":
        sql = "select " + columnName +" from (select  rownum rn , "+columnName+" from " +tableName+ " where "+columnName+" is not null and rownum < "+( Integer.parseInt(pageIndex)+Integer.parseInt(pageSize)+1 )+") where rn > "+pageIndex ;
        break;
      case "MySQL":
        sql = "select T."+columnName+" from " +tableName+ " as T where T."+columnName+" is not null and T." +columnName+ " != '' limit "+pageIndex+","+pageSize ; 
        break;
      case "SqlServer":
        sql = "";
        break;
      case "Postgres":
        sql = "";
        break;
      case "Xugu" :
    	sql = "select M." + columnName +" from (select  rownum rn , T."+columnName+" from " +tableName+ " as T where T."+columnName+" is not null and rownum < "+( Integer.parseInt(pageIndex)+Integer.parseInt(pageSize)+1 )+") M where rn > "+pageIndex ;
        break;
    }

    System.out.print(sql);
    return sql;
  }
  /**
   * 根据表名查询表字段数据量信息
   * @param DBdata
   * @return
   */
  public static String getQueryFieldDataCountSql(Map<String, Object> DBdata){
    String sql = "";
    String tableName = DBdata.get("tableName").toString();
    String columnName = DBdata.get("columnName").toString();
    switch (DBdata.get("databaseType").toString()) {
      case "Oracle":
        sql = "select  count(*) as total_count from " +tableName+ " where "+columnName+" is not null ";
        break;
      case "MySQL":
    	  sql = "select count(*) as total_count from " +tableName+ " as T where T."+columnName+" is not null and T." +columnName+ " != '' ";
    	  break;
      case "SqlServer":
        sql = "";
        break;
      case "Postgres":
        sql = "";
        break;
      case "Xugu" :
        sql = "select count(*) as total_count from " +tableName + " as T where T."+columnName+" is not null and T." + columnName+ " != '' ";
        break;
    }
    return sql;
  }
  /**
   * 根据字段名表名和过滤规则查询字段数据信息
   * @param DBdata
   * @return
   */
  public static String getQueryFieldDataByFilterSql(Map<String, Object> DBdata){
    String sql = "";
    String pageIndex = DBdata.get("pageIndex").toString();
    String pageSize = DBdata.get("pageSize").toString();
    String columnName = DBdata.get("columnName").toString();
    String tableName = DBdata.get("tableName").toString();
    String filterSql = DBdata.get("filterSql").toString();
    switch (DBdata.get("databaseType").toString()) {
      case "Oracle":
        sql = "select " + columnName +" from (select  rownum rn , "+columnName+" from " +tableName+ " where "+columnName+" is not null "+filterSql+" and rownum < "+( Integer.parseInt(pageIndex)+Integer.parseInt(pageSize)+1 )+") where rn > "+pageIndex ;
        break;
      case "MySQL":
    	  filterSql = filterSql.replace(columnName, "T."+columnName) ;
        sql = "select T."+columnName+" from " +tableName+ " as T where T."+columnName+" is not null and T." +columnName+ " != '' " +filterSql+" limit "+pageIndex+","+pageSize ; 
        break;
      case "SqlServer":
        sql = "";
        break;
      case "Postgres":
        sql = "";
        break;
      case "Xugu" :
    	  filterSql = filterSql.replace(columnName, "T."+columnName) ;
    	  sql = "select M." + columnName +" from (select  rownum rn , T."+columnName+" from " +tableName+ " as T where T."+columnName+" is not null "+filterSql+" and rownum < "+( Integer.parseInt(pageIndex)+Integer.parseInt(pageSize)+1 )+") M where rn > "+pageIndex ;
          break;
    }

    System.out.print(sql);
    return sql;
  }
  /**
   * 根据字段名表名和过滤规则查询字段数据量信息
   * @param DBdata
   * @return
   */
  public static String getQueryFieldDataCountByFilterSql(Map<String, Object> DBdata){
    String sql = "";
    String tableName = DBdata.get("tableName").toString();
    String columnName = DBdata.get("columnName").toString();
    String filterSql = DBdata.get("filterSql").toString();
    switch (DBdata.get("databaseType").toString()) {
      case "Oracle":
        sql = "select  count(*) as total_count from " +tableName+ " where "+columnName+" is not null "+ filterSql;
        break;
      case "MySQL":
    	  filterSql = filterSql.replace(columnName, "T."+columnName) ;
    	  sql = "select count(*) as total_count from " +tableName + " as T where T."+columnName+" is not null and T." +columnName+ " != '' "+filterSql;
    	  break;
      case "SqlServer":
        sql = "";
        break;
      case "Postgres":
        sql = "";
        break;
      case "Xugu" :
    	  filterSql = filterSql.replace(columnName, "T."+columnName) ;
        sql = "select count(*) as total_count from " +tableName + " as T where T."+columnName+" is not null and T." + columnName+ " != '' "+filterSql;
        break;
    }
    return sql;
  }
  /**
   * 根据表名查询表字段下的数据信息
   * @param DBdata
   * @return
   */
  public static String getQueryFieldDataSqlByRowIndex(Map<String, Object> DBdata){
    String sql = "";
    String pageIndex = DBdata.get("pageIndex").toString();
    //String pageSize = DBdata.get("pageSize").toString();
    String columnName = DBdata.get("columnName").toString();
    String tableName = DBdata.get("tableName").toString();
    String filterSql = DBdata.get("filterSql").toString();
    switch (DBdata.get("databaseType").toString()) {
      case "Oracle":
        sql = "select " + columnName +" from (select  rownum rn , "+columnName+" from " +tableName+ " where "+columnName+" is not null "+filterSql+" and rownum < "+( Integer.parseInt(pageIndex)+1+1 )+") where rn > "+pageIndex ;
        break;
      case "MySQL":
    	filterSql = filterSql.replace(columnName, "T."+columnName) ;
        sql = "select T."+columnName+" from " +tableName+ " as T where T."+columnName+" is not null "+filterSql+"  and T." +columnName+ " != '' limit "+pageIndex+",1" ; 
        break;
      case "SqlServer":
        sql = "";
        break;
      case "Postgres":
        sql = "";
        break;
      case "Xugu" :
    	filterSql = filterSql.replace(columnName, "T."+columnName) ;
    	sql = "select M." + columnName +" from (select  rownum rn , T."+columnName+" from " +tableName+ " as T where T."+columnName+" is not null "+filterSql+" and rownum < "+( Integer.parseInt(pageIndex)+1+1 )+") M where rn > "+pageIndex ;
        break;
    }
    System.out.print(sql);
    return sql;
  }
  /**
   * 依据数据行号查询数据
   * @param DBdata
   * @return
   */
  public static String getQueryFieldDataSqlByRowIndexs(Map<String, Object> DBdata){
	dealRowIndexByDBType(DBdata) ;//根据不同数据库处理不同下标信息
    String sql = "";
    String pageIndexs = DBdata.get("pageIndexs").toString();
    //String pageSize = DBdata.get("pageSize").toString();
    String columnName = DBdata.get("columnName").toString();
    String tableName = DBdata.get("tableName").toString();
    String filterSql = DBdata.get("filterSql").toString();
    switch (DBdata.get("databaseType").toString()) {
      case "Oracle":
        sql = "select " + columnName +" from (select  rownum rn , "+columnName+" from " +tableName+ " where "+columnName+" is not null "+filterSql+") where rn in ("+ pageIndexs +")";
        break;
      case "MySQL":
    	 filterSql = filterSql.replace(columnName, "T."+columnName) ;
        sql = "select M."+columnName+" from (select T."+columnName+",@rownum:=@rownum+1 as rownum from " +tableName+ " as T , (SELECT @rownum:=-1) as rn where T."+columnName+" is not null "+filterSql+"  and T." +columnName+ " != '' ) M where rownum in("+pageIndexs+")"  ; 
        break;
      case "SqlServer":
        sql = "";
        break;
      case "Postgres":
        sql = "";
        break;
      case "Xugu" :
    	filterSql = filterSql.replace(columnName, "T."+columnName) ;
    	sql = "select M." + columnName +" from (select  rownum rn , T."+columnName+" from " +tableName+ " as T where T."+columnName+" is not null "+filterSql+") M where rn in ("+ pageIndexs +")";
        break;
    }
    System.out.print(sql);
    return sql;
  }
  
  
  public static String dealRowIndexByDBType(Map<String, Object> DBdata){
	  String sql = "" ;
	  String pageIndexs = DBdata.get("pageIndexs").toString();
	  List<String> ids = new ArrayList<String>() ;
	  switch (DBdata.get("databaseType").toString()) {
	     case "Oracle":
	    	//Oracle下标从1开始，处理一下
	    	System.out.print(pageIndexs);
	    	for(String str : pageIndexs.split(",")){
	    		Integer rowIndex = Integer.parseInt( str.replace("'", "") ) ;
	    		ids.add( (rowIndex+1)+"") ;
	    	}
	    	pageIndexs = "" ;
	    	for(String temp : ids){
	    		pageIndexs += "'"+temp+"'," ;
	    	}
	    	if( pageIndexs.length() > 0 ){
	    		pageIndexs = pageIndexs.substring(0, pageIndexs.length()-1) ;
	    	}
	    	DBdata.put( "pageIndexs",pageIndexs ) ;
	        break;
	     case "MySQL":
	    	//Mysql查询下标从0 开始，不需要处理
	    	break;
	     case "SqlServer":
	        break;
	     case "Postgres":
	        break;
	     case "Xugu" :
	    	//Xugu下标从1开始，处理一下
		    System.out.print(pageIndexs);
		    for(String str : pageIndexs.split(",")){
		    	Integer rowIndex = Integer.parseInt( str.replace("'", "") ) ;
		    	ids.add( (rowIndex+1)+"") ;
		    }
		    pageIndexs = "" ;
		    for(String temp : ids){
		    	pageIndexs += "'"+temp+"'," ;
		    }
		    if( pageIndexs.length() > 0 ){
		    	pageIndexs = pageIndexs.substring(0, pageIndexs.length()-1) ;
		    }
		    DBdata.put( "pageIndexs",pageIndexs ) ;
	        break;
	    }
	    return sql;
  }
}
