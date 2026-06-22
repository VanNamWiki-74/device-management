package com.devicemgmt.common.dto;

import com.google.gson.JsonObject;

import java.io.Serializable;

public class Request implements Serializable {
    private String action;
    private String token;
    private JsonObject data;
    private int page = 1;
    private int pageSize = 20;
    private String keyword;
    private String filter;

    public Request() {}

    public Request(String action) {
        this.action = action;
    }

    public Request(String action, JsonObject data) {
        this.action = action;
        this.data = data;
    }

    public String getAction()            { return action; }
    public void setAction(String action) { this.action = action; }

    public String getToken()             { return token; }
    public void setToken(String token)   { this.token = token; }

    public JsonObject getData()          { return data; }
    public void setData(JsonObject data) { this.data = data; }

    public int getPage()                 { return page; }
    public void setPage(int page)        { this.page = page; }

    public int getPageSize()             { return pageSize; }
    public void setPageSize(int pageSize){ this.pageSize = pageSize; }

    public String getKeyword()           { return keyword; }
    public void setKeyword(String kw)    { this.keyword = kw; }

    public String getFilter()            { return filter; }
    public void setFilter(String filter) { this.filter = filter; }
}
