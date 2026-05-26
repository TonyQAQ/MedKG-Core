package com.sunsheen.hkks.common.util;

import java.util.HashMap;
import java.util.Map;

public class RetInfo {
	public static final String SUCCESSCODE = "01";
	public static final String SUCCESSMSG = "操作成功";
	
	public static final String FAILCODE = "00";
	public static final String FAILMSG = "操作失败";
	
	public final static Map<String, Object> RETFAIL = new HashMap<>();    
	public final static Map<String,Object> RETSUCCESS = new HashMap<>();
	static {    
		RETFAIL.put("retcode", RetInfo.FAILCODE);
		RETFAIL.put("retmsg", RetInfo.FAILMSG);
		
		RETSUCCESS.put("retcode", RetInfo.SUCCESSCODE);
		RETSUCCESS.put("retmsg", RetInfo.SUCCESSMSG);
	}
}
