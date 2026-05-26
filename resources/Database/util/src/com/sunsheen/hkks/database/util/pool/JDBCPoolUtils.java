package com.sunsheen.hkks.database.util.pool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sunsheen.hkks.common.util.HumpStringUtils;


/**
 * JDBC工具类
 * @author FengTao
 *
 */
public class JDBCPoolUtils {
	public String key = "";//查询连接池的key
	private List<String> sqls = new ArrayList<String>();//sql多条的时候用
	/**
	 * 执行SQL，执行完回收
	 * @param sql
	 * @return 返回执行结果，默认字段大写
	 */
	public List<Map<String,Object>> excuteSQL(String sql) throws Exception{
		List<Map<String, Object>> listmap = new ArrayList<Map<String, Object>>();
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			Map<String,ConnectionPool> pools = ConnectionEntity.getPools();
			// 1.获取Connection
			conn = pools.get(key).getConnection();
			// 2.获取Statement
			statement = conn.prepareStatement(sql);
			statement.setFetchSize(1000);//设置缓存，本地缓存1000条，提升遍历结果集效率
			// 拼装SQL
			rs = statement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			// 返回结果
			while (rs.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int j = 1; j <= columnCount; j++) {
					map.put(HumpStringUtils.underlineToHump(rsmd.getColumnLabel(j).toLowerCase()),
							rs.getObject(rsmd.getColumnLabel(j)));
				}
				listmap.add(map);
			}
		} finally {
			// 6.关闭结果集
			try {
				release(rs, statement, conn);
			} catch (SQLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		return listmap;
	}
	/**
	 * 执行SQL，执行完回收
	 * @param sql
	 * @return 返回执行结果，默认字段大写
	 */
	public List<Object> excuteSQLGetListStr(String sql) throws Exception{
		List<Object> listStr= new ArrayList<Object>();
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			Map<String,ConnectionPool> pools = ConnectionEntity.getPools();
			// 1.获取Connection
			conn = pools.get(key).getConnection();
			// 2.获取Statement
			statement = conn.prepareStatement(sql);
			statement.setFetchSize(1000);//设置缓存，本地缓存1000条，提升遍历结果集效率
			// 拼装SQL
			rs = statement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			// 返回结果
			while (rs.next()) {
				for (int j = 1; j <= columnCount; j++) {
					Object o = rs.getObject(rsmd.getColumnLabel(j)) ;
					if(o instanceof Timestamp){
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				        String data = simpleDateFormat.format(new Date(((Timestamp)o).getTime()));
				        listStr.add(data) ;
					}else if(o instanceof Date){
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				        String data = simpleDateFormat.format(o);
				        listStr.add(data) ;
					}else{
						listStr.add(o) ;
					}
					
				}
			}
		} finally {
			// 6.关闭结果集
			try {
				release(rs, statement, conn);
			} catch (SQLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		return listStr;
	}
	/**
	 * 执行SQL，执行完回收
	 * @param sql
	 * @return 返回执行结果，默认字段大写
	 */
	public List<Map<String,Object>> excuteSQLS(List<String> sql){
		List<Map<String, Object>> listmap = new CopyOnWriteArrayList<Map<String, Object>>();//返回值,线程安全
		try {
			// 创建线程池
			if(sql.size() > 10){//大于10则分成对应的份
				int size = sql.size() / 10;//看看分为10分，几个一组
				String sqlNew = "";
				for (int i = 0; i < sql.size(); i++) {
					if(sql.get(i).toUpperCase().contains("ORDER")){//union不能在里面排序
						sqlNew = sqlNew + " union " + sql.get(i).toUpperCase().substring(0,sql.get(i).toUpperCase().lastIndexOf("ORDER"));
					} else {
						sqlNew = sqlNew + " union " + sql.get(i).toUpperCase();
					}
					if ((i != 0 && i % size == 0) || i == sql.size() - 1) {//size个一批次，且第一个不要放一个批次里
						sqlNew = sqlNew.substring(sqlNew.indexOf("union")+ 5);//去除第一个
						sqls.add(sqlNew);
						sqlNew = "";
					}
				}
			} else {
				sqls.addAll(sql);
			}
			int nThreads = sqls.size();//线程个数
			ExecutorService service;
			if(nThreads < 10){//小于10个，可以使用可变线程池
				service = Executors.newCachedThreadPool();//创建线程池
			} else {//超过10个，使用定长线程池，防止出现OOM
				service = Executors.newFixedThreadPool(10);//创建线程池
			}
			List<Future<List<Map<String,Object>>>> futureList = new ArrayList<>();
			// 创建XX个SQL任务
			for (int i = 0; i < sqls.size(); i++) {
				CallableTask task = new CallableTask(i);
				futureList.add(service.submit(task));// 按顺序放
			}
			for (int j = 0; j < futureList.size(); j++) {
				listmap.addAll(futureList.get(j).get());
			}
			service.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return listmap;
	}
	/**
	 * 关闭连接及结果集
	 * @param rs
	 * @param statement
	 * @param con
	 * @throws SQLException
	 */
	public void  release(ResultSet rs, Statement statement, Connection con)
			throws SQLException {

		try {
			if (rs != null)
				rs.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			if (statement != null)
				statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null){
				//以下为使用新方法时
				con.close();// 关闭连接对象
			}
		}
	}
	/**
	 * 关闭结果集
	 * @param rs
	 * @throws SQLException
	 */
	public static void release(ResultSet rs)
			throws SQLException {

		try {
			if (rs != null)
				rs.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	/**
	 * 方法名：release
	 * 
	 * @param statement
	 * @param con
	 * @throws SQLException
	 */
	public  void release(Statement statement, Connection con)
			throws SQLException {
		try {
			if (statement != null)
				statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null){
				//以下为使用新方法时
				con.close();// 关闭连接对象
			}
		}
	}
	/**
	 * 配合多线程使用，按顺序返回值
	 * @author SEELE
	 *
	 */
	private class CallableTask implements Callable<List<Map<String,Object>>> {
		Integer temp;//序号
		public CallableTask(Integer d) {
			temp = d;
		}
		@Override
		public List<Map<String,Object>> call() throws Exception {
			List<Map<String,Object>> listmap = new ArrayList<Map<String,Object>>();//返回值
			Statement statement = null;
			//拿取链接
			Map<String,ConnectionPool> pools = ConnectionEntity.getPools();
			Connection conn = pools.get(key).getConnection();
			statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY );
			statement.setFetchSize(1000);//设置缓存，本地缓存1000条，提升遍历结果集效率
			ResultSet rs = statement.executeQuery(sqls.get(temp));
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			// 返回结果
			while (rs.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int j = 1; j <= columnCount; j++) {
					map.put(rsmd.getColumnName(j).toUpperCase(),
							rs.getObject(rsmd.getColumnName(j)));
				}
				listmap.add(map);
			}	
			release(rs, statement, conn);
			return listmap;
		}
	}
	/**
	 * 获取连接【从连接池获取，按照URL+用户名分】
	 * 初始化key ，若连接所在的key不存在，则创建一个连接池
	 * @param jdbcUrl 数据库连接地址
	 * @param db_user 数据库用户名
	 * @param db_pwd 数据库密码
	 * @return
	 * @throws Exception
	 */
	public void getConnection(String driver , String url , String username , String password) throws Exception {
		//使用新的链接
		key = url+"+"+username;//按链接和用户名加起来作为key
		Map<String,ConnectionPool> pools = ConnectionEntity.getPools();
		if(pools.get(key) != null){//连接池存在，直接获取
			System.out.print("神秘代码 ： 不创建--> { "+ key + " }");
		} else {//不存在创建，下次就可以拿到了
			System.out.print("神秘代码 ： 创建了连接池--> { "+ key + " }");
			pools.put(key, new ConnectionPool(driver, url, username, password));
			pools.get(key).createPool();
		}
	}
	/**
	 * 销毁指定key的连接池
	 * @param driver
	 * @param url
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public void destroyPool(String url , String username) throws Exception {
		//初始化key 
		key = url+"+"+username;//按链接和用户名加起来作为key
		Map<String,ConnectionPool> pools = ConnectionEntity.getPools();
		if(pools.get(key) == null){//连接池不存在,不需要销毁
		} else {
			System.out.print("神秘代码 ： 销毁了连接池--> { "+ key + " }");
			//存在，则销毁连接池
			pools.get(key).destroy();
			//将key从连接池移除
			pools.remove(key) ;
		}
	}
}
