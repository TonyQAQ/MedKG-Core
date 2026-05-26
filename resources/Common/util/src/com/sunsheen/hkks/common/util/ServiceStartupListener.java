package com.sunsheen.hkks.common.util;

import javax.servlet.ServletContext;

import com.sunsheen.jfids.system.servlet.Listener;
import com.sunsheen.jfids.system.servlet.SystemStartupListener;

@Listener
public class ServiceStartupListener implements SystemStartupListener{
	@Override
	public void init(ServletContext arg0) {
		System.out.println("☆☆☆☆☆☆☆☆☆是时候展现真正的监听了☆☆☆☆☆☆☆☆☆☆☆");
	}
}
