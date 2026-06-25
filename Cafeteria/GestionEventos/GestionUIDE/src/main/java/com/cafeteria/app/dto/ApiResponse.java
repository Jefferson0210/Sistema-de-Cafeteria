package com.cafeteria.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object meta;   // metadatos opcionales (paginación); omitido si null

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message, T data, Object meta) {
        this(success, message, data);
        this.meta = meta;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Object getMeta() { return meta; }
    public void setMeta(Object meta) { this.meta = meta; }
}
