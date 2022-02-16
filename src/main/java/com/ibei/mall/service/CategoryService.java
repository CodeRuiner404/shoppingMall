package com.ibei.mall.service;

import com.github.pagehelper.PageInfo;
import com.ibei.mall.model.dao.CategoryMapper;
import com.ibei.mall.model.pojo.Category;
import com.ibei.mall.model.request.AddCategoryRequest;
import com.ibei.mall.model.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface CategoryService {

    void add(AddCategoryRequest addCategoryRequest);

    void update(Category category);

    void delete(Integer id);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    List<CategoryVO> listForCustomer(Integer parentId);
}
