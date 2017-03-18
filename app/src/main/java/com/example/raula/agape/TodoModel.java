package com.example.raula.agape;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by raula on 2/21/2017.
 */

public class TodoModel {
    private String message;

    public TodoModel() {
        message = "";
    }

    public TodoModel(String mess) {
        message = mess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public HashMap<String,String> toFirebaseObject() {
        HashMap<String,String> todo =  new HashMap<String,String>();
        todo.put("message", message);

        return todo;
    }
}
