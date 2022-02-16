package com.ibei.mall.controller;

import com.github.pagehelper.PageInfo;
import com.ibei.mall.common.ApiRestResponse;
import com.ibei.mall.model.request.CreateOrderRequest;
import com.ibei.mall.model.vo.OrderVo;
import com.ibei.mall.service.OrderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("order/create")
    @ApiOperation("创建订单")
    public ApiRestResponse create(@RequestBody CreateOrderRequest createOrderRequest){
        String orderNo = orderService.create(createOrderRequest);
        return ApiRestResponse.success(orderNo);
    }

    @GetMapping("order/detail")
    @ApiOperation("前台订单详情")
    public ApiRestResponse detail(@RequestParam String orderNo){
        OrderVo orderVo = orderService.detail(orderNo);
        return ApiRestResponse.success(orderVo);
    }

    @GetMapping("order/list")
    @ApiOperation("前台订单列表")
    public ApiRestResponse list(@RequestParam Integer pageNum, @RequestParam Integer pageSize){
       PageInfo pageInfo = orderService.listForCustomer(pageNum,pageSize);
       return ApiRestResponse.success(pageInfo);
    }

    @PostMapping("order/cancel")
    @ApiOperation("取消订单")
    public ApiRestResponse cancel(@RequestParam String orderNo){
        orderService.cancel(orderNo);
        return ApiRestResponse.success();
    }

    @PostMapping("order/qrcode")
    @ApiOperation("生成二维码")
    public ApiRestResponse qrCode(@RequestParam String orderNo){
        String qrCode = orderService.qrCode(orderNo);
        return ApiRestResponse.success(qrCode);
    }

    @GetMapping("pay")
    @ApiOperation("支付接口")
    public ApiRestResponse pay(@RequestParam String orderNo){
        orderService.pay(orderNo);
        return ApiRestResponse.success();
    }


}
