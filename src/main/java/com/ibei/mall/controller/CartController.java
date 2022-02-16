package com.ibei.mall.controller;

import com.ibei.mall.common.ApiRestResponse;
import com.ibei.mall.filter.UserFilter;
import com.ibei.mall.model.vo.CartVO;
import com.ibei.mall.service.CartService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    CartService cartService;


    @ApiOperation("把商品添加到购物车")
    @PostMapping("/add")
    public ApiRestResponse add(@RequestParam Integer productId, @RequestParam Integer count){
        List<CartVO> list = cartService.add(UserFilter.currentUser.getId(),productId,count);
        return ApiRestResponse.success(list);
    }

    @ApiOperation("删除购物车")
    @PostMapping("/delete")
    public ApiRestResponse delete(@RequestParam Integer productId){//不能传入userid和cartid，否则可以删除别人的购物车
        List<CartVO> list = cartService.delete(UserFilter.currentUser.getId(),productId);
        return ApiRestResponse.success(list);
    }

    @ApiOperation("更新购物车")
    @PostMapping("/update")
    public ApiRestResponse update(@RequestParam Integer productId, @RequestParam Integer count){
        List<CartVO> list = cartService.update(UserFilter.currentUser.getId(),productId,count);
        return ApiRestResponse.success(list);
    }

    @ApiOperation("购物车列表")
    @PostMapping("/list")
    public ApiRestResponse list(){
        List<CartVO> list = cartService.list(UserFilter.currentUser.getId());
        return ApiRestResponse.success(list);
    }

    @ApiOperation("选择/不选择购物车的某个商品")
    @PostMapping("/select")
    public ApiRestResponse select(@RequestParam Integer productId,@RequestParam Integer selected){
        List<CartVO> list = cartService.selectOrNot(UserFilter.currentUser.getId(),productId,selected);
        return ApiRestResponse.success(list);
    }

    @ApiOperation("选择/不选择购物车的某个商品")
    @PostMapping("/selectAll")
    public ApiRestResponse selectAll(@RequestParam Integer selected){
        List<CartVO> list = cartService.selectAllOrNot(UserFilter.currentUser.getId(),selected);
        return ApiRestResponse.success(list);
    }

}
