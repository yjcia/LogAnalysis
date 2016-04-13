package com.kewill.model;

/**
 * Created by YanJun on 2016/3/25.
 */
public class LogModel {
    private int id;
    private String execSelectSql;
    private String execTime;
    private String error;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExecSelectSql() {
        return execSelectSql;
    }

    public void setExecSelectSql(String execSelectSql) {
        this.execSelectSql = execSelectSql;
    }

    public String getExecTime() {
        return execTime;
    }

    public void setExecTime(String execTime) {
        this.execTime = execTime;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "LogModel{" +
                "id=" + id +
                ", execSelectSql='" + execSelectSql + '\'' +
                ", execTime='" + execTime + '\'' +
                '}';
    }
}
