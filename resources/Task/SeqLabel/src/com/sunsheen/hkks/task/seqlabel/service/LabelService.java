package com.sunsheen.hkks.task.seqlabel.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.Transformers;

import com.sunsheen.hkks.common.uitl.mq.RabbitmqUtil;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

public class LabelService {
	@SuppressWarnings("unchecked")
	public Map<String, Object> getModelInfo() {
		Map<String, Object> retMap = new HashMap<String, Object>();
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		retMap.putAll(RetInfo.RETSUCCESS);
		try {
			List<Map<String, Object>> list = session
					.createDySQLQuery("Model.queryInfo", new HashMap<>())
					.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
					.list();
			retMap.put("data", list);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL);
		} finally {
			session.close();
		}
		return retMap;
	}

	public void saveTaskModel(Map<String, Object> param,
			Map<String, Object> retMap) {
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try {
			RabbitmqUtil mq = new RabbitmqUtil("label");
			mq.sendMessage(param);
			if (mq.getSuccess()) {
				session.createDySQLQuery("Model.saveTaskModel", param)
						.executeUpdate();
			} else {
				retMap.putAll(RetInfo.RETFAIL);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL);
		} finally {
			session.close();
		}
	}

	public void saveLabelResult(Map<String, Object> param,
			Map<String, Object> retMap) {
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try {
			session.createDySQLQuery("Model.savaLabelResult", param)
			.executeUpdate();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL);
		} finally {
			session.close();
		}
	}
	
	public void startTask(Map<String, Object> param,
			Map<String, Object> retMap) {
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try {
			BigDecimal count = (BigDecimal)session.createDySQLQuery("Model.queryTask", param).uniqueResult();
			if(count.intValue() > 0){
				session.createDySQLQuery("Model.updateTask", param).executeUpdate();
			}else{
				session.createDySQLQuery("Model.insertTask", param).executeUpdate();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL);
		} finally {
			session.close();
		}
	}
}
