package com.sunsheen.jfids.dsi.sysman.security.login;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sunsheen.hkks.common.util.RetInfo;

public class SystemFilter implements Filter{
	
	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
		//类型转换
		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;
		HttpSession session = request.getSession();
		String url = request.getRequestURI();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=utf-8");
		Map<String, Object> retMap = new HashMap<String,Object>() ;
		/*
		 * 
		 
		if(url.toString().contains(".css") 
				|| url.toString().contains(".js") 
					|| url.toString().contains(".png")
						||url.toString().contains(".jpg")
						||url.toString().contains(".pdf")
							||url.toString().contains(".ico")
								||url.toString().endsWith("html")
									||url.toString().endsWith("data/Login.svt")
										||url.toString().endsWith("/session/isEmpty")){
				arg2.doFilter(arg0, arg1);
				
		}else{
			if(session != null && session.getAttribute("currUser")!=null){ // 用户已经登录
				arg2.doFilter(arg0, arg1);
			}else{ //用户未登录
				//System.out.print("重定向----"+url);
				//response.sendRedirect("/HKKnowledgeStudio/build/index.html#/login") ;
				retMap.putAll(RetInfo.RETFAIL);
				retMap.put("retmsg", "用户未登陆！");
				PrintWriter pw=response.getWriter();
				pw.write(retMap.toString());
				pw.flush();
				pw.close();
				return ;
			}
		}
		
		*/
		arg2.doFilter(arg0, arg1);
	}
	
	@Override
	public void destroy() {
		// TODO 自动生成的方法存根
		
	}
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO 自动生成的方法存根
		
	}
}
