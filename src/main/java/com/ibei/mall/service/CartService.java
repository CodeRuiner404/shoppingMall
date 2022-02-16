package com.ibei.mall.service;

import com.ibei.mall.exception.MallException;
import com.ibei.mall.model.pojo.User;
import com.ibei.mall.model.vo.CartVO;

import java.util.List;


public interface CartService {

    List<CartVO> add(Integer userId, Integer produceId, Integer count);

    List<CartVO> delete(Integer userId, Integer produceId);

    List<CartVO> update(Integer userId, Integer produceId, Integer count);

    List<CartVO> list(Integer userId);

    List<CartVO> selectOrNot(Integer userId, Integer produceId, Integer selected);

    List<CartVO> selectAllOrNot(Integer userId, Integer selected);
}
