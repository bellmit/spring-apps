package com.haozhuo.datag.model;

import java.io.Serializable;

public class ResponseEntity<T> implements Serializable {

    private String msg;
    private int code;
    private T data;


    public ResponseEntity() {
    }

    public ResponseEntity(int code) {
        this.setCode(code);
    }

    public ResponseEntity(int code, String message) {
        this.setCode(code);
        this.setMsg(message);
    }

    public ResponseEntity(int code, String message, T data) {
        this(code, message);
        this.setData(data);
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }


}

