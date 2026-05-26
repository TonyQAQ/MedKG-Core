package com.sunsheen.jfids.dsi.sysman.security.login;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.jboss.seam.security.Identity;

import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.jfids.system.security.login.Session;
import com.sunsheen.jfids.system.servlet.Servlet;
import com.sunsheen.jfids.system.sysman.ISysUser;

/**
 * 
 * <p>
 * VerifyCodeServlet
 * </p>
 * <p>
 * 说明:生成验证码
 * </p>
 * <p>
 * 创建时间:Jul 22, 2011
 * </p>
 * <p>
 * 描述:
 * </p>
 * <p>
 * Copyright (c) 2011 SunSheen Technology. All rights reserved.
 * </p>
 * 
 * @version 1.0
 * @author LCK
 */
@Servlet(value = "/data/LoginOut.svt", anonymous = true)
public class LoginOutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		Identity identity = Identity.instance();
		JSONObject map=new JSONObject();
		ISysUser user=Session.getCurrUser();
		if(user!=null){
			identity.getCredentials().setUsername(user.getUsername());
		}
		Cookie[] cookies = request.getCookies();  
		try{
	        if (null==cookies) {  
	            System.out.println("没有cookie==============");  
	        } else {  
	            for(Cookie cookie : cookies){  
	                if(cookie.getValue().equals(user.getAccount())){  
	                	System.out.println("被删除的cookie名字为:"+cookie.getValue());  
	                    cookie=new Cookie("username",null);  
	                    cookie.setMaxAge(0);// 立即销毁cookie  
	                    cookie.setDomain(".sunsheen.cn");
	                    cookie.setPath("/");  
	                    response.addCookie(cookie);  
	                    break;  
	                }  
	            }  
	        }  
			identity.logout();
			//response.sendRedirect("../index.html");
			map.element("retcode", RetInfo.SUCCESSCODE);
			map.element("retmsg", "退出成功!");
		}catch(Exception e){
			System.out.print("登出异常 ： "+e.getMessage());
			map.element("retcode", RetInfo.FAILCODE);
			map.element("retmsg", "退出失败!");
		}
		PrintWriter pw=response.getWriter();
		pw.write(map.toString());
		pw.flush();
		pw.close();
	}

	public void doPost(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(req, response);
	}
}
