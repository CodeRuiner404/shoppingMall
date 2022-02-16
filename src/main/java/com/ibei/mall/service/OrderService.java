package com.ibei.mall.service;

import com.github.pagehelper.PageInfo;
import com.ibei.mall.model.request.CreateOrderRequest;
import com.ibei.mall.model.vo.CartVO;
import com.ibei.mall.model.vo.OrderVo;

import java.util.List;


public interface OrderService {


    String create(CreateOrderRequest createOrderRequest);

    OrderVo detail(String orderNo);

    PageInfo listForCustomer(Integer pageNum, Integer pageSize);

    void cancel(String orderNo);

    String qrCode(String orderNo);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    void pay(String orderNo);

    void deliver(String orderNo);

    void finish(String orderNo);
}
