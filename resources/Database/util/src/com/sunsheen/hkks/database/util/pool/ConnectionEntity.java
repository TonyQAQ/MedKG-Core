package com.sunsheen.hkks.database.util.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 专门存放连接池,根据不同的Key区分不同的数据源信息
 * @author FengTao
 *
 */
public class ConnectionEntity {
	public static Map<String,ConnectionPool> pools = new ConcurrentHashMap<String,ConnectionPool>();//连接池，线程安全,list是CopyOnWriteArrayList是线程安全的
	
	public static Map<String,ConnectionPool> getPools() {
		return pools;
	}
	public static void setPools( Map<String,ConnectionPool> poolss) {
		pools = poolss;
	} 
}
