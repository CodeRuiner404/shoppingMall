package com.ibei.mall.service;

import com.github.pagehelper.PageInfo;
import com.ibei.mall.model.pojo.Category;
import com.ibei.mall.model.pojo.Product;
import com.ibei.mall.model.request.AddCategoryRequest;
import com.ibei.mall.model.request.AddProductRequest;
import com.ibei.mall.model.request.ProductListRequest;
import com.ibei.mall.model.vo.CategoryVO;

import java.util.List;

public interface ProductService {

    void add(AddProductRequest addProductRequest);

    void update(Product updateProduct);

    void delete(Integer id);

    void batchUpdateSellStatus(Integer[] ids, Integer sellStatus);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    Product detail(Integer id);

    PageInfo list(ProductListRequest productListRequest);
}
