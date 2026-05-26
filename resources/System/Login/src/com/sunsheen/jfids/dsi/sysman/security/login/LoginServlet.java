package com.sunsheen.jfids.dsi.sysman.security.login;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

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
import com.sunsheen.jfids.system.sysman.IRole;

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
@Servlet(value = "/data/Login.svt", anonymous = true)
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		Identity identity = Identity.instance();
		identity.getCredentials().setPassword(request.getParameter("password"));
		identity.getCredentials().setUsername(request.getParameter("username"));
		identity.login();
		// 如果要放参数则
		// Session.getCurrUser().getInfo().put("d1", "ds");
		//通过角色判断跳转界面
		JSONObject map=new JSONObject();
		if(Session.getCurrUser()!=null){
			
			Set<IRole> roleSet = Session.getCurrUser().getRole();
			for(IRole role : roleSet) {
				map.element("roleId",  role.getId());
				map.element("roleName", role.getText());
			}
			map.element("userId", Session.getCurrUser().getId());
			map.element("userName", Session.getCurrUser().getUsername());
			map.element("retcode", RetInfo.SUCCESSCODE);
			map.element("retmsg", "登录成功!");
			Cookie cookie = new Cookie("username",Session.getCurrUser().getAccount());
			 cookie.setDomain("sunsheen");
			 cookie.setPath("/");
			 cookie.setMaxAge(60); //默认60分钟
			 response.addCookie(cookie);
		}else{
			map.element("retcode", RetInfo.FAILCODE);
			map.element("retmsg", "登录失败，请检查用户名密码!");
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
