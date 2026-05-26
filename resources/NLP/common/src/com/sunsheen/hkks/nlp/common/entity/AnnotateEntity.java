 package com.sunsheen.hkks.nlp.common.entity;

import java.util.List;

public class AnnotateEntity {
    private String content ;
    private List<LabelCategoriesEntity> labelCategories ;
    private List<LabelEntity> labels ;
    private List<ConnCategoriesEntity> connectionCategories ;
    private List<ConnectionEntity> connections ;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<LabelEntity> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelEntity> labels) {
        this.labels = labels;
    }

    public List<LabelCategoriesEntity> getLabelCategories() {
        return labelCategories;
    }

    public void setLabelCategories(List<LabelCategoriesEntity> labelCategories) {
        this.labelCategories = labelCategories;
    }

    public List<ConnCategoriesEntity> getConnectionCategories() {
        return connectionCategories;
    }

    public void setConnectionCategories(List<ConnCategoriesEntity> connectionCategories) {
        this.connectionCategories = connectionCategories;
    }

    public List<ConnectionEntity> getConnections() {
        return connections;
    }

    public void setConnections(List<ConnectionEntity> connections) {
        this.connections = connections;
    }

    @Override
    public String toString() {
        return "AnnotateEntity{" +
                "content='" + content + '\'' +
                ", labelCategories=" + labelCategories +
                ", connectionCategories=" + connectionCategories +
                ", connections=" + connections +
                '}';
    }
}
