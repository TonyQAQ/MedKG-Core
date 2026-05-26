package com.sunsheen.hkks.task.iaa.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.Transformers;

import com.alibaba.fastjson.JSON;
import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

@SuppressWarnings("unchecked")
public class IAAService {
	private final String POST_URL = Configs.get("IAA.serverIP") + "/calculate/IAA";

	public Map<String, Object> getIAA(String taskId, String path, String type) {
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String, Object> retMap = new HashMap<>();
		retMap.putAll(RetInfo.RETSUCCESS);
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("TASK_ID", taskId);
			param.put("TYPE_CODE", type);
			Map<String, String> value = (Map<String, String>) session
					.createDySQLQuery("IAA.queryIAA", param)
					.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
					.uniqueResult();
			param.put("EVAL_ID", value.get("EVAL_ID"));
			if (value.get("IAA") == null || "01".equals(type)) {
				List<Map<String, String>> labels = session
						.createDySQLQuery("IAA.queryLables", param)
						.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
						.list();
				Object number = session.createDySQLQuery("IAA.queryCount",
						param).uniqueResult();
				List<Map<String, String>> items = session
						.createDySQLQuery("IAA.queryItem", param)
						.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
						.list();
				// String labels = JSON.toJSONString(list);
				String IAA = sendPost(
						path + File.separator + value.get("EVAL_ID"),
						number.toString(), labels, items);
				if (StringUtils.isEmptyOrWhitespaceOnly(IAA)) {
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "IAA计算失败！");
				} else {
					param.put("IAA", IAA);
					session.createDySQLQuery("IAA.saveIAA", param)
							.executeUpdate();
				}
				retMap.put("IAA", IAA);
			} else {
				retMap.put("IAA", value.get("IAA"));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL);
		}
		return retMap;
	}

	// public String calculate(String path, String labels) {
	// PythonInterpreter interpreter = new PythonInterpreter();
	// interpreter.execfile("E:\\PythonWorkSpace\\IAA\\Kappa\\analysis.py");
	// // 第一个参数为期望获得的函数（变量）的名字，第二个参数为期望返回的对象类型
	// PyFunction pyFunction = interpreter.get("fleiss_kappa",
	// PyFunction.class);
	// // 调用函数，如果函数需要参数，在Java中必须先将参数转化为对应的“Python类型”
	// PyObject pyobj = pyFunction
	// .__call__(new PyString(path), new PyString(labels));
	// System.out.println("fleiss_kappa is: " + pyobj);
	// return "";
	// }

	public String sendPost(String path, String number,
			List<Map<String, String>> labels, List<Map<String, String>> items) {
		// String IAAUrl = GET_URL + "?filePath=" + URLEncoder.encode(path)
		// + "&labels=" + URLEncoder.encode(labels) + "&number=" + number;
		Map<String, Object> param = new HashMap<>();
		String content = null;
		String str = null;
		PrintWriter out = null;
		InputStream in = null;
		HttpURLConnection conn = null;
		try {
			param.put("filePath", path);
			param.put("number", number);
			param.put("labels", labels);
			param.put("items", items);
			content = JSON.toJSONString(param);
			URL url = new URL(POST_URL);
			// 打开和url之间的连接
			conn = (HttpURLConnection) url.openConnection();

			/** 设置URLConnection的参数和普通的请求属性****start ***/

			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");// GET和POST必须全大写
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.write(content);
			// flush输出流的缓冲
			out.flush();
			conn.connect();
			// 获取URLConnection对象对应的输入流
			in = conn.getInputStream();
			// 构造一个字符流缓存
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			str = br.readLine();
			// while ((str = br.readLine()) != null) {
			// str = new String(str.getBytes(), "UTF-8");// 解决中文乱码问题
			// }
		
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {  
                if (out != null) { out.close();}  
                if (in != null) {in.close();}  
                if (conn != null) {	conn.disconnect();} 
            } catch (Exception ex) {  
                ex.printStackTrace();  
            } 
		}
		return str;
	}

}
