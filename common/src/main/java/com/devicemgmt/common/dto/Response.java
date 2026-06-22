package com.devicemgmt.common.dto;

import com.google.gson.JsonElement;

import java.io.Serializable;

public class Response implements Serializable {
    private boolean success;
    private String message;
    private JsonElement data;
    private int totalCount;
    private int currentPage;
    private int totalPages;

    public Response() {}

    public static Response ok(String message) {
        Response r = new Response();
        r.success = true;
        r.message = message;
        return r;
    }

    public static Response ok(String message, JsonElement data) {
        Response r = ok(message);
        r.data = data;
        return r;
    }

    public static Response ok(String message, JsonElement data, int total, int page, int pageSize) {
        Response r = ok(message, data);
        r.totalCount = total;
        r.currentPage = page;
        r.totalPages = (int) Math.ceil((double) total / pageSize);
        return r;
    }

    public static Response error(String message) {
        Response r = new Response();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess()               { return success; }
    public void setSuccess(boolean success)  { this.success = success; }

    public String getMessage()               { return message; }
    public void setMessage(String message)   { this.message = message; }

    public JsonElement getData()             { return data; }
    public void setData(JsonElement data)    { this.data = data; }

    public int getTotalCount()               { return totalCount; }
    public void setTotalCount(int n)         { this.totalCount = n; }

    public int getCurrentPage()              { return currentPage; }
    public void setCurrentPage(int p)        { this.currentPage = p; }

    public int getTotalPages()               { return totalPages; }
    public void setTotalPages(int p)         { this.totalPages = p; }
}
