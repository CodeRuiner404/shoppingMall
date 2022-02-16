package com.ibei.mall.controller;

import com.ibei.mall.common.ApiRestResponse;
import com.ibei.mall.exception.MallException;
import com.ibei.mall.exception.MallExceptionEnum;
import com.ibei.mall.model.pojo.User;
import com.ibei.mall.service.UserService;
import com.ibei.mall.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/test")
    @ResponseBody
    public User personalPage(){
        return userService.getUser();
    }

    @PostMapping("/register")
    @ResponseBody
    public ApiRestResponse register(@RequestParam("username") String username,@RequestParam("password")String password) throws MallException {
        //防止空用户名
        if(StringUtils.isEmpty(username)){//isEmpty方法相比较于if(username==null)，它既可以判断字符串本事是否存在（==null）也可以判断字符串的内容是否为空
            return ApiRestResponse.fail(MallExceptionEnum.NEED_USER_NAME);
        }
        //防止空字符串
        if(StringUtils.isEmpty(password)){
            return ApiRestResponse.fail(MallExceptionEnum.NEED_PASSWORD);
        }
        //密码长度不得少于8位
        if(password.length()<8){
            return ApiRestResponse.fail(MallExceptionEnum.PASSWORD_TOO_SHORT);
        }
        userService.register(username,password);
        return ApiRestResponse.success();
    }

    @PostMapping("/login")
    @ResponseBody
    public ApiRestResponse login(@RequestParam("username") String username, @RequestParam("password")String password, HttpSession session) throws MallException {
        //防止空用户名
        if(StringUtils.isEmpty(username)){//isEmpty方法相比较于if(username==null)，它既可以判断字符串本事是否存在（==null）也可以判断字符串的内容是否为空
            return ApiRestResponse.fail(MallExceptionEnum.NEED_USER_NAME);
        }
        //防止空字符串
        if(StringUtils.isEmpty(password)){
            return ApiRestResponse.fail(MallExceptionEnum.NEED_PASSWORD);
        }
        User user = userService.login(username,password);
        System.out.println(user.getPersonalizedSignature());
        user.setPassword(null);//把返回的用户信息的密码设置为空，避免别人得到加密后的密码，从而破解加密方式
        session.setAttribute(Constant.MALL_USER,user);
        return ApiRestResponse.success(user);
    }

    @PostMapping("/user/update")
    @ResponseBody
    public ApiRestResponse updateUserInfo(HttpSession session,@RequestParam String signature) throws MallException {
        User curUser = (User) session.getAttribute(Constant.MALL_USER);
        if (curUser==null)throw new MallException(MallExceptionEnum.NEED_LOGIN);
        User user = new User();
        user.setId(curUser.getId());
        user.setPersonalizedSignature(signature);
        userService.updateInformation(user);
        return ApiRestResponse.success();
    }

    @PostMapping("/user/logout")
    @ResponseBody
    public ApiRestResponse logout(HttpSession session){
        session.removeAttribute(Constant.MALL_USER);
        return ApiRestResponse.success();
    }

    @PostMapping("/adminlogin")
    @ResponseBody
    public ApiRestResponse adminlogin(@RequestParam("username") String username, @RequestParam("password")String password, HttpSession session) throws MallException {
        //防止空用户名
        if(StringUtils.isEmpty(username)){//isEmpty方法相比较于if(username==null)，它既可以判断字符串本事是否存在（==null）也可以判断字符串的内容是否为空
            return ApiRestResponse.fail(MallExceptionEnum.NEED_USER_NAME);
        }
        //防止空字符串
        if(StringUtils.isEmpty(password)){
            return ApiRestResponse.fail(MallExceptionEnum.NEED_PASSWORD);
        }
        User user = userService.login(username,password);
        if (userService.checkAdminRole(user)) {
            user.setPassword(null);//把返回的用户信息的密码设置为空，避免别人得到加密后的密码，从而破解加密方式
            session.setAttribute(Constant.MALL_USER,user);
            return ApiRestResponse.success(user);
        }else{
            return ApiRestResponse.fail(MallExceptionEnum.NEED_ADMIN);
        }

    }
}
