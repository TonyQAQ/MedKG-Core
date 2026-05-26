package com.sunsheen.hkks.common.util;

import java.util.List;
 

/**
 * 
 * @Title: Node
 * @Description: 树节点
 * @author: FengTao
 * @date 2020年9月9日 下午12:11:45
 */
public class FileNode {
    String title ;
    String key ;
    List<FileNode> children ;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<FileNode> getChildren() {
        return children;
    }

    public void setChildren(List<FileNode> children) {
        this.children = children;
    }


}
