package com.sunsheen.hkks.database.util;

import java.sql.Connection;
import java.util.Map;

public class ConnectionFactory {
    // 初始连接池
    //private static final ThreadLocal<Connection> connHolder = new ThreadLocal<Connection>();
    //private static final BasicDataSource dataSource = new BasicDataSource();
    /**
     *
     * @param DBdata 数据库信息参数
     * @return 返回数据库连接
     * @throws Exception 异常 手动获取单个连接
     */
    public static Connection getConnection(Map<String, Object> DBdata) throws Exception {
        Connection connection = null;
        if (connection == null) {
            switch (DBdata.get("databaseType").toString()) {
                case "Oracle":
                    connection = DBConnectionComponent.getOracleConnection(DBdata);
                    break;
                case "MySQL":
                    connection = DBConnectionComponent.getMySQLConnection(DBdata);
                    break;
                case "SqlServer":
                    connection = DBConnectionComponent.getSqlServerConnection(DBdata);
                    break;
                case "Postgres":
                    connection = DBConnectionComponent.getPostgresConnection(DBdata);
                    break;
                case "Xugu":
                    connection = DBConnectionComponent.getXuguConnection(DBdata);
                    break;
            }
        }
        return connection;
    }


    //TODO 后续开发
	/*public static Connection getConnectionPool() throws SQLException {
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/mytable");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		// / 设置空闲和借用的连接的最大总数量，同时可以激活。
		dataSource.setMaxActive(60);
		// 设置初始大小
		dataSource.setInitialSize(5);
		// 最小空闲连接
		dataSource.setMinIdle(8);
		// 最大空闲连接
		dataSource.setMaxIdle(16);
		// 超时等待时间毫秒
		dataSource.setMaxWait(2 * 10000);
		// 只会发现当前连接失效，再创建一个连接供当前查询使用
		dataSource.setTestOnBorrow(true);
		// removeAbandonedTimeout ：超过时间限制，回收没有用(废弃)的连接（默认为 300秒，调整为180）
		dataSource.setRemoveAbandonedTimeout(180);
		// 没用连接（废弃）的回收（默认为false，调整为true)
		dataSource.setRemoveAbandoned(true);
		// testWhileIdle
		dataSource.setTestOnReturn(true);
		// testOnReturn
		dataSource.setTestOnReturn(true);
		// 记录日志
		dataSource.setLogAbandoned(true);
		dataSource.setDefaultAutoCommit(true);
		Connection connection = connHolder.get();
		if (connection == null) {
			try {
				connection = dataSource.getConnection();
				System.out.println("get connection success");
			} catch (SQLException e) {
				System.out.println("get connection failure:" + e);
			} finally {
				connHolder.set(connection);
			}
		}
		return connection;
	}*/
}
