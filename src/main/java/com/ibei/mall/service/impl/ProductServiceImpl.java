package com.ibei.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ibei.mall.common.ApiRestResponse;
import com.ibei.mall.exception.MallException;
import com.ibei.mall.exception.MallExceptionEnum;
import com.ibei.mall.model.dao.ProductMapper;
import com.ibei.mall.model.pojo.Product;
import com.ibei.mall.model.query.ProductListQuery;
import com.ibei.mall.model.request.AddProductRequest;
import com.ibei.mall.model.request.ProductListRequest;
import com.ibei.mall.model.vo.CategoryVO;
import com.ibei.mall.service.CategoryService;
import com.ibei.mall.service.ProductService;
import com.ibei.mall.util.Constant;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductMapper productMapper;
    @Autowired
    CategoryService categoryService;

    @Override
    public void add(AddProductRequest addProductRequest){
        Product product = new Product();
        BeanUtils.copyProperties(addProductRequest,product);
        Product productOld = productMapper.selectByName(addProductRequest.getName());
        if(productOld != null)throw new MallException(MallExceptionEnum.NAME_EXISTED);
        int count = productMapper.insertSelective(product);
        if (count==0)throw new MallException(MallExceptionEnum.CREATE_FAILED);
    }

    @Override
    public void update(Product updateProduct){
        Product productOld = productMapper.selectByName(updateProduct.getName());
        //如果已经存在了同名且不同id的产品，抛出异常
        if(productOld != null && !Objects.equals(productOld.getId(), updateProduct.getId()))throw new MallException(MallExceptionEnum.NAME_EXISTED);
        int count = productMapper.updateByPrimaryKeySelective(updateProduct);
        if(count == 0)throw new MallException(MallExceptionEnum.UPLOAD_FAILED);//更新失败，抛出异常
    }

    @Override
    public void delete(Integer id){
        Product productOld = productMapper.selectByPrimaryKey(id);
        //如果不存在该产品，抛出异常
        if(productOld == null)throw new MallException(MallExceptionEnum.DELETE_FAILED);
        int count = productMapper.deleteByPrimaryKey(id);
        if(count == 0)throw new MallException(MallExceptionEnum.DELETE_FAILED);//更新失败，抛出异常
    }

    @Override
    public void batchUpdateSellStatus(Integer[] ids, Integer sellStatus){
        productMapper.batchUpdateSellStatus(ids,sellStatus);
    }

    @Override
    public PageInfo listForAdmin(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Product> list = productMapper.selectListForAdmin();
        PageInfo pageInfo = new PageInfo(list);
        return pageInfo;
    }

    @Override
    public Product detail(Integer id){
        Product product = productMapper.selectByPrimaryKey(id);
        return product;
    }

    @Override
    public PageInfo list(ProductListRequest productListRequest){
        //构建query对象
        ProductListQuery productListQuery = new ProductListQuery();
        //搜索功能
        //productListRequest传递过来的搜索关键字(keyword)不为空时，才能进行搜索
        if(!StringUtils.isEmpty(productListRequest.getKeyWord())){
            //关键字左右各添加一个%是为了进行sql自带的模糊字搜索
            String keyWord = new StringBuffer().append("%").append(productListRequest.getKeyWord()).append("%").toString();
            productListQuery.setKeyWord(keyWord);
        }
        //目录处理
        //当查询某个目录下的商品时，也意味着需要搜索该目录下所有的子目录的商品，所以此时需要得到目录的子目录list
        if(productListRequest.getCategoryId() != null){
            List<CategoryVO> categoryVOList = categoryService.listForCustomer(productListRequest.getCategoryId());
            ArrayList<Integer> categoryIds = new ArrayList<>();
            categoryIds.add(productListRequest.getCategoryId());
            getCategoryIds(categoryVOList,categoryIds);
            productListQuery.setCategoryIds(categoryIds);
        }

        //排序处理
        String orderBy = productListRequest.getOrderBy();
        if(Constant.ProductListOrderBy.PRICE_AES_DESC.contains(orderBy)){//检验前台传入的排序顺序是否准确
            PageHelper.startPage(productListRequest.getPageNum(),productListRequest.getPageSize(),orderBy);
        }else{
            PageHelper.startPage(productListRequest.getPageNum(),productListRequest.getPageSize());
        }

        //去sql中查询该商品
        List<Product> productList = productMapper.selectList(productListQuery);
        PageInfo pageInfo = new PageInfo(productList);
        return pageInfo;
    }

    private void getCategoryIds(List<CategoryVO> categoryVOList,ArrayList<Integer> categoryIds){
        for (CategoryVO category:categoryVOList
             ) {
            categoryIds.add(category.getId());
            getCategoryIds(category.getChildCategories(),categoryIds);
        }
    }


}
