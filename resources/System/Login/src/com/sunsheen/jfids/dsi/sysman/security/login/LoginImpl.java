package com.sunsheen.jfids.dsi.sysman.security.login;

import com.sunsheen.hkks.common.util.MD5;
import com.sunsheen.jfids.dsi.sysman.department.Ts_department;
import com.sunsheen.jfids.dsi.sysman.security.login.SysUser;
import com.sunsheen.jfids.dsi.sysman.security.login.authgroup.Role;
import com.sunsheen.jfids.system.base.composite.dao.IDao;
import com.sunsheen.jfids.system.base.composite.data.query.QueryParameterImpl;
import com.sunsheen.jfids.system.security.login.ILogin;
import com.sunsheen.jfids.system.sysman.ISysUser;
import com.sunsheen.jfids.system.sysman.LoginMessage;
import com.sunsheen.jfids.util.CommonUtil;

import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.transform.Transformers;
import org.jboss.seam.security.Identity;
import org.osoa.sca.annotations.Reference;

public class LoginImpl
  implements ILogin
{
  IDao dao;
  
  @Reference
  public void setDao(IDao dao)
  {
    this.dao = dao;
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public ISysUser doLogin(Identity identity, Map data)
  {
    ISysUser currUser = new SysUser();
    MD5 m = new MD5();
    Query query = this.dao.getSession().createSQLQuery("select * from SV_USERDEPART where account=:account and password=:password");
    data.put("account", identity.getCredentials().getUsername());
    data.put("password", m.getMD5ofStr(identity.getCredentials().getPassword()));
    QueryParameterImpl qp = new QueryParameterImpl();
    query = qp.initParameter(query, data);
    query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
    List userDataList = query.list();
    if (userDataList.size() > 0)
    {
    try{
      for (int i = 0; i < userDataList.size(); i++)
      {
        Map userData = (Map)userDataList.get(i);
        if (i == 0)
        {
          currUser.setAccount((String)data.get("USERNAME"));
          currUser.setId((String)userData.get("ID"));
          currUser.setMemo((String)userData.get("MEMO"));
          currUser.setEmpid((String)userData.get("EMPID"));
          currUser.setUsername((String)userData.get("USERNAME"));
          currUser.setSexname((String)userData.get("SEXNAME"));
          currUser.setAccount((String)userData.get("ACCOUNT"));
          currUser.getInfo().put("SKIN", (String)userData.get("SKIN"));
          data.clear();
          data.put("id", currUser.getId());
          Query roleQuery = this.dao.getSession().createSQLQuery("select * from SV_USERROLE where userid=:id");
          roleQuery = qp.initParameter(roleQuery, data);
          roleQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
          List<Map<String, String>> roleList = roleQuery.list();
          if (roleList != null) {
            for (int j = 0; j < roleList.size(); j++)
            {
              String roleid = (String)((Map)roleList.get(j)).get("ROLEID");
              String text = (String)((Map)roleList.get(j)).get("TEXT");
              identity.addRole(roleid);
              currUser.getRole().add(new Role(roleid,text));
            }
          }
        }
        Ts_department depart = new Ts_department();
        depart.setId((String)userData.get("DEPARTID"));
        depart.setText((String)userData.get("DEPARTNAME"));
        currUser.getDepart().add(depart);
      }
    }catch(Exception e){
    	e.printStackTrace();
    }
      return currUser;
    }
    LoginMessage message = (LoginMessage)CommonUtil.getComponent("LoginMessage");
    message.setMessage("用户名密码错误");
    
    return null;
  }
}
