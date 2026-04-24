package com.vibeflow.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketEvent<T> {
    private String type;
    private T data;
    
    public WebSocketEvent() {}
    
    public WebSocketEvent(String type, T data) {
        this.type = type;
        this.data = data;
    }
    
    public static <T> WebSocketEvent<T> of(String type, T data) {
        return new WebSocketEvent<>(type, data);
    }
}