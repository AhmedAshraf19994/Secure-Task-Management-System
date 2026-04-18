package com.ahmed.Secure.Task.Management.System.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class Response <T>{

    private boolean flag;

    private Integer code;

    private T data;

    private String message;
}
