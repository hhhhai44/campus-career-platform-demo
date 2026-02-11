package org.hhhhai.campuscareerplatformdemo.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;

    private String errorMsg;

    private T data;

    public static Result<Void> success() {
        Result<Void> result = new Result<>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 1;
        result.data = data;
        return result;
    }

    public static Result<Void> error(String msg){
        Result<Void> result = new Result<>();
        result.code = 0;
        result.errorMsg = msg;
        return result;
    }
}
