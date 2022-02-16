package com.ibei.mall.controller;

import com.github.pagehelper.PageInfo;
import com.ibei.mall.common.ApiRestResponse;
import com.ibei.mall.exception.MallExceptionEnum;
import com.ibei.mall.model.pojo.Category;
import com.ibei.mall.model.pojo.User;
import com.ibei.mall.model.request.AddCategoryRequest;
import com.ibei.mall.model.request.UpdateCategoryRequest;
import com.ibei.mall.model.vo.CategoryVO;
import com.ibei.mall.service.CategoryService;
import com.ibei.mall.service.UserService;
import com.ibei.mall.util.Constant;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
public class CategoryController {
    @Autowired
    UserService userService;
    @Autowired
    CategoryService categoryService;

    /*
    * 后台的添加商品分类
    * */
    @ApiOperation("后台添加目录")
    @PostMapping("admin/category/add")
    @ResponseBody
    public ApiRestResponse addCategory(HttpSession session,@Valid @RequestBody AddCategoryRequest addCategoryRequest){
        User cUser = (User) session.getAttribute(Constant.MALL_USER);
        if(cUser==null)return ApiRestResponse.fail(MallExceptionEnum.NEED_LOGIN);
        boolean adminRole = userService.checkAdminRole(cUser);
        if(adminRole){//是管理员
            //执行插入操作
            categoryService.add(addCategoryRequest);
            return ApiRestResponse.success();
        }else{//不是管理员
            return ApiRestResponse.fail(MallExceptionEnum.NEED_ADMIN);
        }
    }

    /*
     * 后台的更新商品分类
     * */
    @ApiOperation("后台更新目录")
    @PostMapping("admin/category/update")
    @ResponseBody
    public ApiRestResponse updateCategory(HttpSession session,@Valid @RequestBody UpdateCategoryRequest updateCategoryRequest){
        User cUser = (User) session.getAttribute(Constant.MALL_USER);
        if(cUser==null)return ApiRestResponse.fail(MallExceptionEnum.NEED_LOGIN);
        boolean adminRole = userService.checkAdminRole(cUser);
        if(adminRole){//是管理员
            //执行更新操作
            Category category = new Category();
            BeanUtils.copyProperties(updateCategoryRequest,category);
            categoryService.update(category);
            return ApiRestResponse.success();
        }else{//不是管理员
            return ApiRestResponse.fail(MallExceptionEnum.NEED_ADMIN);
        }
    }

    /*
     * 后台的删除商品分类
     * */
    @ApiOperation("后台删除目录")
    @PostMapping("admin/category/delete")
    @ResponseBody
    public ApiRestResponse deleteCategory(@RequestParam Integer id){
        categoryService.delete(id);
        return ApiRestResponse.success();
    }

    /*
     * 后台目录列表（后台管理员）
     * */
    @ApiOperation("后台目录列表")
    @PostMapping("admin/category/list")
    @ResponseBody
    public ApiRestResponse listCategoryForAdmin(@RequestParam Integer pageNum,@RequestParam Integer pageSize){
        PageInfo pageInfo = categoryService.listForAdmin(pageNum,pageSize);
        return ApiRestResponse.success(pageInfo);
    }

    /*
     * 前台目录列表（前端的用户看）
     * */
    @ApiOperation("前台目录列表")
    @PostMapping("/list")
    @ResponseBody
    public ApiRestResponse listCategoryForCustomer(){
        List<CategoryVO> categoryVOList = categoryService.listForCustomer(0);
        return ApiRestResponse.success(categoryVOList);
    }
}
