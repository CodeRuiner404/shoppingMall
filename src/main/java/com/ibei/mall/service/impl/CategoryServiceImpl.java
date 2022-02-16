package com.ibei.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ibei.mall.exception.MallException;
import com.ibei.mall.exception.MallExceptionEnum;
import com.ibei.mall.model.dao.CategoryMapper;
import com.ibei.mall.model.pojo.Category;
import com.ibei.mall.model.request.AddCategoryRequest;
import com.ibei.mall.model.request.UpdateCategoryRequest;
import com.ibei.mall.model.vo.CategoryVO;
import com.ibei.mall.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public void add(AddCategoryRequest addCategoryRequest){
        Category category = new Category();
        //把addCategoryRequest中和category同名的字段的值拷贝到category中
        BeanUtils.copyProperties(addCategoryRequest,category);
        //查找该命名的category是否存在
        Category categoryOld = categoryMapper.selectByName(addCategoryRequest.getName());
        if (categoryOld != null) {//存在
            throw new MallException(MallExceptionEnum.NAME_EXISTED);
        }
        int count = categoryMapper.insertSelective(category);
        if (count==0) {
            throw new MallException(MallExceptionEnum.CREATE_FAILED);
        }
    }

    @Override
    public void update(Category category){
        if(category.getName() != null){
            Category categoryOld = categoryMapper.selectByName(category.getName());
            if (categoryOld.getName()!=null && !categoryOld.getId().equals(category.getId())) {
                throw new MallException(MallExceptionEnum.NAME_EXISTED);
            }
        }
        int count = categoryMapper.updateByPrimaryKeySelective(category);
        if(count==0) throw new MallException(MallExceptionEnum.UPDATE_FAILED);
    }

    @Override
    public void delete(Integer id){
        Category oldCategory = categoryMapper.selectByPrimaryKey(id);
        if(oldCategory == null)throw new MallException(MallExceptionEnum.DELETE_FAILED);
        int count = categoryMapper.deleteByPrimaryKey(id);
        if (count==0)throw new MallException(MallExceptionEnum.DELETE_FAILED);
    }

    @Override
    public PageInfo listForAdmin(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize,"type,order_num");
        List<Category> categoryList = categoryMapper.selectList();
        PageInfo pageInfo = new PageInfo(categoryList);
        return pageInfo;
    }

    @Override
    @Cacheable(value = "listForCustomer")
    public List<CategoryVO> listForCustomer(Integer parentId){
        ArrayList<CategoryVO> categoryVOList = new ArrayList<>();
        recursivelyFindCategories(categoryVOList,parentId);
        return categoryVOList;
    }

    private void recursivelyFindCategories(List<CategoryVO> categoryVOList,Integer parentId){
        List<Category> categoryList = categoryMapper.selectCategoriesByParentId(parentId);
        if(!CollectionUtils.isEmpty(categoryList)){
            for (Category category:categoryList
                 ) {
                CategoryVO categoryVO = new CategoryVO();
                //把每一个category转换为CategoryVO
                BeanUtils.copyProperties(category,categoryVO);
                categoryVOList.add(categoryVO);//把CategoryVO添加到返回的list里
                //递归，查找当前的categoryVO的子分类并添加到私有属性中去
                recursivelyFindCategories(categoryVO.getChildCategories(),categoryVO.getId());
            }
        }

    }
}
