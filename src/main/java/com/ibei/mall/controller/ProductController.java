package com.ibei.mall.controller;

import com.github.pagehelper.PageInfo;
import com.ibei.mall.common.ApiRestResponse;
import com.ibei.mall.model.pojo.Product;
import com.ibei.mall.model.request.ProductListRequest;
import com.ibei.mall.service.ProductService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {
    @Autowired
    ProductService productService;

    @ApiOperation("商品详情")
    @GetMapping("product/detail")
    public ApiRestResponse detail(@RequestParam Integer id){
        Product product = productService.detail(id);
        return ApiRestResponse.success(product);
    }

    @ApiOperation("前台商品列表")
    @GetMapping("product/list")
    public ApiRestResponse list(ProductListRequest productListRequest){
        PageInfo pageInfo = productService.list(productListRequest);
        return ApiRestResponse.success(pageInfo);
    }


}
