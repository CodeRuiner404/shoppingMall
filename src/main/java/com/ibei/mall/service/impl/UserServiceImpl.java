package com.ibei.mall.service.impl;

import com.ibei.mall.exception.MallException;
import com.ibei.mall.exception.MallExceptionEnum;
import com.ibei.mall.model.dao.UserMapper;
import com.ibei.mall.model.pojo.User;
import com.ibei.mall.service.UserService;
import com.ibei.mall.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Override
    public User getUser() {
        return userMapper.selectByPrimaryKey(1);
    }

    @Override
    public void register(String username, String password) throws MallException {
        //查询用户名是否存在，不允许重名
        User result = userMapper.selectByName(username);
        if(result != null){
            throw new MallException(MallExceptionEnum.NAME_EXISTED);
        }
        //用户名通过，把用户信息写入数据库
        User user = new User();
        user.setUsername(username);
        try {
            user.setPassword(MD5Utils.getMD5Str(password));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        int count = userMapper.insertSelective(user);
        //插入失败
        if(count == 0){
            throw new MallException(MallExceptionEnum.INSERT_FAILED);
        }
    }

    @Override
    public User login(String username, String password) throws MallException {
        String md5Password = null;
        try {
            md5Password = MD5Utils.getMD5Str(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        User user = userMapper.selectLogin(username,md5Password);
        if(user ==null) throw new MallException(MallExceptionEnum.WRONG_PASSWORD);
        return user;
    }

    @Override
    public void updateInformation(User user) throws MallException {
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount>1)throw new MallException(MallExceptionEnum.UPDATE_FAILED);
    }

    @Override
    public boolean checkAdminRole(User user){
        //role=2,代表是管理员
        return user.getRole().equals(2);
    }

}
