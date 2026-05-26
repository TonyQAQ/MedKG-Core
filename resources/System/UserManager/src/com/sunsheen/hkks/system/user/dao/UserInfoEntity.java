package com.sunsheen.hkks.system.user.dao;

/**
 * 
 * @Title: UserInfoEntity
 * @Description: 人员信息的添加
 * @author: FengTao
 * @date 2020年9月22日 下午4:53:37
 */
public class UserInfoEntity {
	
	private String userId ;
	private String userName ;
	private String account ;
	private String organizationId ;
	private String roleId ;
	private String password ;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getOrganizationId() {
		return organizationId;
	}
	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
