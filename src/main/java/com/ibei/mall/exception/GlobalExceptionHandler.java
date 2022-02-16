package com.ibei.mall.exception;

import com.ibei.mall.common.ApiRestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice//这个注解拦截所有异常
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handlerException(Exception e){
        log.error("Default Exception: " + e);
        return ApiRestResponse.fail(MallExceptionEnum.SYSTEM_ERROR);
    }

    @ExceptionHandler(MallException.class)
    @ResponseBody
    public Object handlerMallException(MallException e){
        log.error("MallException: " + e);
        return ApiRestResponse.fail(e.getCode(),e.getMsg());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ApiRestResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error("MethodArgumentNotValidException: ",e);
        return handlerBindingResult(e.getBindingResult());
    }

    private ApiRestResponse handlerBindingResult(BindingResult result){
        //把异常处理为对外暴露的提示
        List<String> list = new ArrayList<>();
        if(result.hasErrors()){
            List<ObjectError> allErrors = result.getAllErrors();
            for (ObjectError oe:allErrors
                 ) {
                String message = oe.getDefaultMessage();
                list.add(message);
            }
        }
        if (list.size()==0) {
            return ApiRestResponse.fail(MallExceptionEnum.REQUEST_PARAM_ERROR);
        }
        return ApiRestResponse.fail(MallExceptionEnum.REQUEST_PARAM_ERROR.getCode(),list.toString());
    }
}
