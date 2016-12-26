package com.example.wasswa.testtest5.model;

import java.io.Serializable;

/**
 * Created by wasswa on 25.11.2016.
 */

public class SendMessageEvent implements Serializable {
    private String message;
    public SendMessageEvent(String s){
        message = s;
    }
    public String getMessage(){
        return message;
    }
}
