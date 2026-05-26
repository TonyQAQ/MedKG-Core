package com.sunsheen.hkks.database.util.pool;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

public class ConnectionPool {
	private String driver = ""; // 数据库驱动
    private String url = ""; // 数据 URL
    private String username = ""; // 数据库用户名
    private String password = ""; // 数据库用户密码
    private ComboPooledDataSource cpds = new ComboPooledDataSource(); //连接池
	//private static Logger logger = LoggerFactory.getLogger(DataBaseUtilService.class);
    /**
     * 构造函数，设置连接池属性
     * @param driver
     * @param url
     * @param username
     * @param password
     */
	public ConnectionPool(String driver,String url,String username,String password) {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password= password;
	}
	/**
	 * 创建连接池【线程安全】【本程序默认连接池改为了20+10，原来是10+5】
	 */
	public synchronized void createPool() {
        try {
			//logger.info("创建连接池！！！！！");
            // 数据源进行各种有效的控制：  
            // 设置驱动  
            cpds.setDriverClass(driver);  
            // 设置数据库URL  
            cpds.setJdbcUrl(url);  
            // 设置用户名  
            cpds.setUser(username);  
            // 设置密码  
            cpds.setPassword(password);  
            // 当连接池中的连接用完时，C3PO一次性创建新的连接数目;  
            cpds.setAcquireIncrement(10);  
            // 定义在从数据库获取新的连接失败后重复尝试获取的次数，默认为30;  
            cpds.setAcquireRetryAttempts(1);  
            // 两次连接中间隔时间默认为1000毫秒  
            cpds.setAcquireRetryDelay(100);  
            // 连接关闭时默认将所有未提交的操作回滚 默认为false;  
            cpds.setAutoCommitOnClose(false);  
            // 获取连接失败将会引起所有等待获取连接的线程异常,但是数据源仍有效的保留,并在下次调用getConnection()的时候继续尝试获取连接.如果设为true,那么尝试获取连接失败后该数据源将申明已经断开并永久关闭.默认为false  
            cpds.setBreakAfterAcquireFailure(false);  
            // 当连接池用完时客户端调用getConnection()后等待获取新连接的时间,超时后将抛出SQLException,如设为0则无限期等待.单位毫秒,默认为0  
            cpds.setCheckoutTimeout(0);  
            // 隔多少秒检查所有连接池中的空闲连接,默认为0表示不检查;此处设置10分钟检查一次  
            cpds.setIdleConnectionTestPeriod(600);
            // 最大空闲时间,超过空闲时间的连接将被丢弃.为0或负数据则永不丢弃.默认为0;此处设置12小时，如果用MySQL最好设置不超过8小时 
            cpds.setMaxIdleTime(43200);  
            // 初始化时创建的连接数,应在minPoolSize与maxPoolSize之间取值.默认为3  
            cpds.setInitialPoolSize(20);  
            // 连接池中保留的最大连接数据.默认为15  
            cpds.setMaxPoolSize(200); 
            // 连接池中保留的最小连接数据.默认为15  
            cpds.setMinPoolSize(10);
            // JDBC的标准参数,用以控制数据源内加载的PreparedStatement数据.但由于预缓存的Statement属于单个Connection而不是整个连接池.所以设置这个参数需要考滤到多方面的因素,如果maxStatements  
            // 与maxStatementsPerConnection均为0,则缓存被关闭.默认为0;  
            cpds.setMaxStatements(0);  
            // 连接池内单个连接所拥有的最大缓存被关闭.默认为0;  
            cpds.setMaxStatementsPerConnection(0);  
            // C3P0是异步操作的,缓慢的JDBC操作通过帮助进程完成.扩展这些操作可以有效的提升性能,通过多数程实现多个操作同时被执行.默为为3  
            cpds.setNumHelperThreads(10);  
            // 用户修改系统配置参数执行前最多等待的秒数.默认为300;  
            cpds.setPropertyCycle(300);  
        } catch (PropertyVetoException e) {  
            e.printStackTrace();  
        } 
	}
	/**
	 * 从连接池获取连接
	 */
	public synchronized Connection getConnection() {
		try {
			Connection conn = getCpds().getConnection();//获取链接
			if(conn != null && !conn.isClosed()){//链接可用
				return conn;
			}
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 获取连接池
	 * @return
	 */
	public ComboPooledDataSource getCpds() {
		return cpds;
	}
	/**
	 * 摧毁连接池【线程安全】
	 */
	public synchronized void destroy(){
		try {
			DataSources.destroy(cpds);
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}  
	}
}
