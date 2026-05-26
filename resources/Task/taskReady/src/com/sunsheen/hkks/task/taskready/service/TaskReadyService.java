package com.sunsheen.hkks.task.taskready.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONArray;

import org.hibernate.jdbc.Work;
import org.hibernate.transform.Transformers;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.database.mgr.service.DataBaseUtilService;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

/**
 * 
 * @Title: TaskReadyService
 * @Description: 任务准备相关service方法
 * @author: FengTao
 * @date 2020年7月22日 上午10:00:03
 */
public class TaskReadyService {
	
	private DataBaseUtilService databaseUtilService = new DataBaseUtilService() ;
	private List<Map<String,Object>> columnsList = new ArrayList<Map<String,Object>>() ;
	private List<Map<String,Object>> tablesList = new ArrayList<Map<String,Object>>() ;

	/**
	 * 
	* @Title: updateTaskState
	* @Description: 更新任务状态,统一更新，
	* 				发布者使用,只更新大任务状态，不更新小状态信息
	* @author: FengTao
	* @date 2020年7月22日 上午11:19:23
	* @param retMap
	* @param params void
	* @version
	 */
	public void updateTaskBigState(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.beginTransaction();
		try{
			Object num = session.createDySQLQuery("Task.updateTaskState", params).executeUpdate() ;
			if(!"0".equals(num.toString())){
				session.flush();
				session.commit();
				retMap.putAll(RetInfo.RETSUCCESS);
				retMap.put("retmsg", "发布成功") ;
			}else{
				session.rollback();
				retMap.putAll(RetInfo.RETFAIL);
				retMap.put("retmsg", "发布失败，请重试") ;
			}
			if(!StringUtils.isEmptyOrWhitespaceOnly((String)params.get("stateCode")) && "400000".equals(params.get("stateCode")+"") ){ //任务结束，则修改为结束时间
				session.createDySQLQuery("Task.updateTaskEndTime", params).executeUpdate() ;
			}
			if(!StringUtils.isEmptyOrWhitespaceOnly((String)params.get("stateCode")) && "500000".equals(params.get("stateCode")+"") ){ //任务取消，则修改为结束时间
				session.createDySQLQuery("Task.updateTaskEndTime", params).executeUpdate() ;
			}
			session.flush();
			session.commit();
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "发布失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: updateTaskExeBackState
	* @Description: 回退任务状态,统一回退，
	* 				发布者使用,不更新大任务状态，只为每个人回退上一个任务状态信息
	* @author: FengTao
	* @date 2020年7月22日 上午11:19:23
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void updateTaskExeBackState(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		session.beginTransaction();
		try{
			//向流程表添加每个人的任务流程信息
			//"queryExeUser"
			data = session.createDySQLQuery("Task.queryExeUser", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			if(data != null && data.size()>0){
				for( Map<String,Object> temp : data){
					//为每个人插入表别名选择状态
					params.put("assignId", temp.get("assignId")) ;
					Object effectNum = session.createDySQLQuery("Task.insertUserBackProcessExe", params).executeUpdate() ;
					if("0".equals(effectNum.toString())){
						session.rollback();
					}
				}
			}
			if(!StringUtils.isEmptyOrWhitespaceOnly((String)params.get("stateCode")) && "400000".equals(params.get("stateCode")+"") ){ //任务结束，则修改为结束时间
				session.createDySQLQuery("Task.updateTaskEndTime", params).executeUpdate() ;
			}
			if(!StringUtils.isEmptyOrWhitespaceOnly((String)params.get("stateCode")) && "500000".equals(params.get("stateCode")+"") ){ //任务取消，则修改为结束时间
				session.createDySQLQuery("Task.updateTaskEndTime", params).executeUpdate() ;
			}
			retMap.putAll(RetInfo.RETSUCCESS);
			session.flush();
			session.commit();
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "回退失败，请重试") ;
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: updateTaskExeState
	* @Description: 更新任务状态,统一更新，
	* 				发布者使用,不更新大任务状态，只为每个人添加大任务的下一个任务状态信息
	* @author: FengTao
	* @date 2020年7月22日 上午11:19:23
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void updateTaskExeState(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		session.beginTransaction();
		try{
			//向流程表添加每个人的任务流程信息
			//"queryExeUser"
			data = session.createDySQLQuery("Task.queryExeUser", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			if(data != null && data.size()>0){
				for( Map<String,Object> temp : data){
					//为每个人插入下一个状态状态
					params.put("assignId", temp.get("assignId")) ;
					Object effectNum = session.createDySQLQuery("Task.insertUserProcessExe", params).executeUpdate() ;
					if("0".equals(effectNum.toString())){
						session.rollback();
						retMap.putAll(RetInfo.RETFAIL);
						retMap.put("retmsg", "提交失败，请重试") ;
					}
				}
			}
			if(!StringUtils.isEmptyOrWhitespaceOnly((String)params.get("stateCode")) && "400000".equals(params.get("stateCode")+"") ){ //任务结束，则修改为结束时间
				session.createDySQLQuery("Task.updateTaskEndTime", params).executeUpdate() ;
			}
			if(!StringUtils.isEmptyOrWhitespaceOnly((String)params.get("stateCode")) && "500000".equals(params.get("stateCode")+"") ){ //任务取消，则修改为结束时间
				session.createDySQLQuery("Task.updateTaskEndTime", params).executeUpdate() ;
			}
			session.flush();
			session.commit();
			retMap.putAll(RetInfo.RETSUCCESS);
			retMap.put("retmsg", "提交成功") ;
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "提交失败，请重试") ;
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: updateTaskState
	* @Description: 更新任务状态,统一更新，
	* 				发布者使用,更新大任务状态，并且为每个人添加下一个任务状态信息
	* @author: FengTao
	* @date 2020年7月22日 上午11:19:23
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void updateTaskState(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		session.beginTransaction();
		try{
			//向流程表添加每个人的任务流程信息
			//"queryExeUser"
			data = session.createDySQLQuery("Task.queryExeUser", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			if(data != null && data.size()>0){
				Object num = session.createDySQLQuery("Task.updateTaskState", params).executeUpdate() ;
				if(!"0".equals(num.toString())){
					session.flush();
					session.commit();
					retMap.putAll(RetInfo.RETSUCCESS);
					retMap.put("retmsg", "发布成功") ;
				}else{
					session.rollback();
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "发布失败，请重试") ;
				}
				for( Map<String,Object> temp : data){
					//为每个人插入表别名选择状态
					params.put("assignId", temp.get("assignId")) ;
					Object effectNum = session.createDySQLQuery("Task.insertUserProcess", params).executeUpdate() ;
					if("0".equals(effectNum.toString())){
						session.rollback();
					}
				}
			}
			if(!StringUtils.isEmptyOrWhitespaceOnly((String)params.get("stateCode")) && "400000".equals(params.get("stateCode")+"") ){ //任务结束，则修改为结束时间
				session.createDySQLQuery("Task.updateTaskEndTime", params).executeUpdate() ;
			}
			if(!StringUtils.isEmptyOrWhitespaceOnly((String)params.get("stateCode")) && "500000".equals(params.get("stateCode")+"") ){ //任务取消，则修改为结束时间
				session.createDySQLQuery("Task.updateTaskEndTime", params).executeUpdate() ;
			}
			session.flush();
			session.commit();
		}catch(Exception e){
			session.rollback();
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("retmsg", "发布失败，请重试") ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: getPublishMsg
	* @Description:获取发布确认消息
	* @author: FengTao
	* @date 2020年7月22日 下午1:00:14
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getPublishMsg(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> temp = new HashMap<String , Object>() ;
		Map<String , Object> data = new HashMap<String , Object>() ;
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			data = (Map<String, Object>) session.createDySQLQuery("TaskReady.queryTaskPublishMsg", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(data != null && data.size() > 0 && !"0".equals((String)data.get("totalCount")) ){
				temp.put("publishState","00");
				temp.put("publishComfirm", "以下已选中的【表】未选择【字段】，是否确认发布吗？<br>"+data.get("publishMsg")) ;
			}else{
				temp.put("publishState","00");
				temp.put("publishComfirm", "发布后不可撤回，确认发布吗？") ;
			}
			retMap.put("data", temp) ;	
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: saveOrUpdateTable
	* @Description: 保存过更新别名信息
	* @author: FengTao
	* @date 2020年7月22日 上午11:20:22
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void saveOrUpdateTable(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> oraginTableMap = new HashMap<String,Object>() ;
		params.putAll(RetInfo.RETSUCCESS);
		try{
			oraginTableMap = (Map<String, Object>) session.createDySQLQuery("TaskReady.selectTableOriginById", params)
														  .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(oraginTableMap != null && oraginTableMap.size() > 0){
				Object tableNum = session.createDySQLQuery("TaskReady.queryTableCountById", params).uniqueResult() ;
				if("0".equals(tableNum.toString())){ //添加表信息
					String tableMappingId = UUID.randomUUID().toString().replace("-", "") ;//table表的主键ID、字段表的外键ID
					params.put("tableMappingId", tableMappingId) ;
					params.put("tableName", oraginTableMap.get("tableName"));
					params.put("tableComment", oraginTableMap.get("tableComment")) ;
					params.put("connectionId", oraginTableMap.get("connectionId")) ;
					Object effectNum = session.createDySQLQuery("TaskReady.insertTable", params).executeUpdate() ;
					if("0".equals(effectNum.toString())){
						retMap.putAll(RetInfo.RETFAIL);
						params.put("retmsg","添加表信息失败！");
					}else{
						retMap.putAll(RetInfo.RETSUCCESS);
						params.put("retmsg","修改表别名成功！");
					}
				}else if("1".equals(tableNum.toString())){//修改表信息
					Object effectNum = session.createDySQLQuery("TaskReady.updateTable", params).executeUpdate() ;
					if("0".equals(effectNum.toString())){
						retMap.putAll(RetInfo.RETFAIL);
						params.put("retmsg","添加表信息失败！");
					}else{
						retMap.putAll(RetInfo.RETSUCCESS);
						params.put("retmsg","修改表别名成功！");
					}
				}
			}
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: insertColumnAndTable
	* @Description: 插入选择的字段，如果该字段的表信息已经存在，则不需要插入表信息
	* @author: FengTao
	* @date 2020年7月21日 下午6:06:38
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void insertColumnAndTable(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> oraginTableMap = new HashMap<String,Object>() ;
		Map<String,Object> oraginColumnMap = new HashMap<String,Object>() ;
		params.putAll(RetInfo.RETSUCCESS);
		try{
			oraginTableMap = (Map<String, Object>) session.createDySQLQuery("TaskReady.selectTableOriginById", params)
														  .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			oraginColumnMap = (Map<String, Object>) session.createDySQLQuery("TaskReady.selectColumnOriginById", params)
					                                       .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(oraginTableMap != null && oraginColumnMap != null && oraginColumnMap.size() > 0 && oraginTableMap.size() > 0){
				Object tableNum = session.createDySQLQuery("TaskReady.queryTableCountById", params).uniqueResult() ;
				Object cloumnNum = session.createDySQLQuery("TaskReady.queryColumnCountById", params).uniqueResult() ;
				if("0".equals(tableNum.toString())){ //添加表信息
					String tableMappingId = UUID.randomUUID().toString().replace("-", "") ;//table表的主键ID、字段表的外键ID
					params.put("tableMappingId", tableMappingId) ;
					params.put("tableName", oraginTableMap.get("tableName"));
					params.put("tableComment", oraginTableMap.get("tableComment")) ;
					params.put("connectionId", oraginTableMap.get("connectionId")) ;
					Object effectNum = session.createDySQLQuery("TaskReady.insertTable", params).executeUpdate() ;
					if("0".equals(effectNum.toString())){
						retMap.putAll(RetInfo.RETFAIL);
						params.put("retmsg","添加表信息失败！");
					}
				}
				if("0".equals(cloumnNum.toString())){//添加字段信息
					params.put("attrName", oraginColumnMap.get("attrName"));
					params.put("attrComment", oraginColumnMap.get("attrComment")) ;
					Object effectNum = session.createDySQLQuery("TaskReady.insertColumn", params).executeUpdate() ;
					if("0".equals(effectNum.toString())){
						retMap.putAll(RetInfo.RETFAIL);
						params.put("retmsg","添加字段信息失败！");
					}
				}
			}else{
				retMap.putAll(RetInfo.RETSUCCESS);
				retMap.put("retmsg", "添加失败，原字段不存在！") ;
			}
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: daleteColumnAndTable
	* @Description: 删除选择的字段
	* @author: FengTao
	* @date 2020年7月21日 下午6:06:00
	* @param retMap
	* @param params void
	* @version
	 */
	public void daleteColumnAndTable(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			Object tableNum = session.createDySQLQuery("TaskReady.queryTableCountById", params).uniqueResult() ;
			Object cloumnNum = session.createDySQLQuery("TaskReady.queryColumnCountByTableId", params).uniqueResult() ;//查询该表选择的字段格式
			Integer tableCount = Integer.parseInt((String)tableNum) ;
			Integer columnCount = Integer.parseInt((String)cloumnNum) ;
			if(tableCount >0 && columnCount > 0 && tableCount==1 && columnCount==1){ //当前表信息只有最后一个字段，则删除字段信息和该表信息
				//deleteTableById，deleteCoumnById
				Object effectNum = session.createDySQLQuery("TaskReady.deleteCoumnById", params).executeUpdate() ;
//				Object effectTableNum = session.createDySQLQuery("TaskReady.deleteTableById", params).executeUpdate() ;
				if("0".equals(effectNum.toString())){
					retMap.putAll(RetInfo.RETFAIL);
					params.put("retmsg","取消失败！");
				}
				
			}else if(tableCount >0 && columnCount > 0 && tableCount==1 && columnCount > 1) {//字段信息大于等于两个，则不需要删除
				Object effectNum = session.createDySQLQuery("TaskReady.deleteCoumnById", params).executeUpdate() ;
				if("0".equals(effectNum.toString())){
					retMap.putAll(RetInfo.RETFAIL);
					params.put("retmsg","取消失败！");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 
	* @Title: getTables
	* @Description: 获取表信息
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:26
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getTables(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		try{
			data = session.createDySQLQuery("TaskReady.selectTablesOrigin", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", data) ;
			for(Map<String,Object> temp : data){
				temp.put("isEdit", false) ;
			}
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
		}catch(Exception e){
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: getcolumns
	* @Description: 获取字段信息
	* @author: FengTao
	* @date 2020年7月21日 上午11:18:15
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getColumns(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>() ;
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("TaskReady.ColumnsTitle")));
		try{
			data = session.createDySQLQuery("TaskReady.selectCloumnsOrigin", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", data) ;
			int sequence = 0 ;
			for(Map<String,Object> temp : data){
				temp.put("sequence", ++sequence) ;
				temp.put("rowKey", temp.get("attrId")) ;
			}
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
		}catch(Exception e){
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: copyTableAndColumnsFromSource
	* @Description:	备份数据信息
	* @author: FengTao
	* @date 2020年7月21日 上午11:15:23
	* @param retMap
	* @param params void
	* @version
	 */
	public void copyTableAndColumnsFromSource(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		session.getSession().beginTransaction() ;
		session.beginTransaction();
		retMap.putAll(RetInfo.RETSUCCESS);
		try{
			//查询是否该数据源已经做了准备工作，有则不需要再次准备
			Object num = session.createDySQLQuery("TaskReady.checkBackUp", params).uniqueResult() ;
			if("0".equals((String)num)){//数据准备
				Object id = session.createDySQLQuery("TaskReady.queryConnectionId", params).uniqueResult() ;
				params.put("connectionId", id) ;
				tablesList = databaseUtilService.getTables(retMap, params);
				session.getSession().doWork(new Work(){
					@Override
					public void execute(Connection arg0) throws SQLException {
						// TODO 自动生成的方法存根
						String tableSql = "insert into  TABLE_MAPPING_ORAGIN (TABLE_ID,TABLE_ORIGIN,TABLE_MEMO,CONNECTION_ID,LAST_UPDATED_USER_ID,LAST_UPDATED_USER_NAME,LAST_UPDATED_DATE)"
										+ "  values(?,?,?,?,?,?,sysdate())" ;
						String columnsSql = "insert into  ATTRIBUTE_MAPPING_ORAGIN (ATTR_ID,TABLE_ID,ATTR_ORIGIN,ATTR_MEMO,LAST_UPDATED_USER_ID,LAST_UPDATED_USER_NAME,LAST_UPDATED_DATE,ATTR_TYPE)"
								+ "  values(?,?,?,?,?,?,sysdate(),?)" ;
							//插入表信息
							PreparedStatement tablesPs = arg0.prepareStatement(tableSql) ;
							PreparedStatement columnsPs = arg0.prepareStatement(columnsSql) ;
							//int tableCount = 0 ;
							for(Map<String,Object> tablesTemp : tablesList){
								//tableCount++ ;
								String tableId = UUID.randomUUID().toString().replace("-", "") ;
								tablesPs.setString(1, tableId);
								tablesPs.setString(2, (String)tablesTemp.get("tableName"));
								tablesPs.setString(3, (String)tablesTemp.get("tableComment"));
								tablesPs.setString(4, (String)params.get("connectionId"));
								tablesPs.setString(5, (String)params.get("userId"));
								tablesPs.setString(6, (String)params.get("userName"));
								//查询表信息，插入字段信息
								params.put("tableName", (String)tablesTemp.get("tableName")) ;
								columnsList = databaseUtilService.getColumns(retMap, params);
								//int count = 0 ;
								for(Map<String,Object> columnsTemp : columnsList){
//									System.out.print(columnsTemp);
									//count++ ;
									String attrId = UUID.randomUUID().toString().replace("-", "") ;
									columnsPs.setString(1, attrId);
									columnsPs.setString(2, tableId);
									columnsPs.setString(3, (String)columnsTemp.get("columnName"));
									columnsPs.setString(4, (String)columnsTemp.get("columnComment"));
									columnsPs.setString(5, (String)params.get("userId"));
									columnsPs.setString(6, (String)params.get("userName"));
									columnsPs.setString(7, (String)columnsTemp.get("columnType"));
//									if(count % 50 == 0){
										columnsPs.addBatch();
//									}
								}
//								if(tableCount % 50 == 0){
									tablesPs.addBatch();
//								}
							}
							columnsPs.executeBatch() ;
							tablesPs.executeBatch() ;
					}
				});
				retMap.remove("data");
				retMap.put("retmsg","数据准备成功");
			}else{
				retMap.remove("data") ;
				retMap.put("retmsg","数据已预先准备");
			}
			//修改任务开始时间为当前时间
			session.createDySQLQuery("Task.updateTaskStartTime", params).executeUpdate() ;
			session.commit();
		}catch(Exception e){
			session.rollback();
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL);
		}
	}
	
	/**
	 * 
	* @Title: getStatData
	* @Description:获取表，字段选择统计数据信息
	* @author: FengTao
	* @date 2020年7月21日 下午3:08:50
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void getStatData(Map<String,Object> retMap , Map<String,Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String,Object> tableData = new HashMap<String,Object>() ;
		Map<String,Object> columnsData = new HashMap<String,Object>() ;
		Map<String,Object> data = new HashMap<String,Object>() ;
		try{
			tableData = (Map<String, Object>) session.createDySQLQuery("TaskReady.selectStatTableData", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(tableData!=null && tableData.size() > 0){
				Integer totalCount = Integer.parseInt((String) tableData.get("totalCount")) ;
				Integer selectCount = Integer.parseInt((String) tableData.get("selectCount")) ;
				data.put("tableTotalCount", totalCount) ;
				data.put("tableSelectCount", selectCount) ;
				data.put("tableNoSelectCount", totalCount-selectCount ) ;
			}
			columnsData = (Map<String, Object>) session.createDySQLQuery("TaskReady.selectStatColumnData", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult() ;
			if(columnsData!=null && columnsData.size() > 0){
				Integer totalCount = Integer.parseInt((String) columnsData.get("totalCount")) ;
				Integer selectCount = Integer.parseInt((String) columnsData.get("selectCount")) ;
				data.put("columnTotalCount", totalCount) ;
				data.put("columnSelectCount", selectCount) ;
				data.put("columnNoSelectCount", totalCount-selectCount ) ;
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", data) ;
			if(data!=null && data.size() > 0){
				retMap.put("retmsg", "查询成功") ;
			}else{
				retMap.put("retmsg", "查询成功,无数据") ;
			}
		}catch(Exception e){
			retMap.put("data", data) ;
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
}
