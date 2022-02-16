package com.ibei.mall.service;

import com.ibei.mall.exception.MallException;
import com.ibei.mall.model.pojo.User;



public interface UserService {


    User getUser();
    void register(String username,String password) throws MallException;
    User login(String username, String password) throws MallException;

    void updateInformation(User user) throws MallException;

    boolean checkAdminRole(User user);
}
