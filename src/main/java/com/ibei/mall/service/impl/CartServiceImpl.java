package com.ibei.mall.service.impl;

import com.ibei.mall.exception.MallException;
import com.ibei.mall.exception.MallExceptionEnum;
import com.ibei.mall.model.dao.CartMapper;
import com.ibei.mall.model.dao.ProductMapper;
import com.ibei.mall.model.pojo.Cart;
import com.ibei.mall.model.pojo.Product;
import com.ibei.mall.model.vo.CartVO;
import com.ibei.mall.service.CartService;
import com.ibei.mall.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    ProductMapper productMapper;

    @Autowired
    CartMapper cartMapper;

    @Override
    public List<CartVO> add(Integer userId, Integer produceId, Integer count){
        //验证加入购物车的商品和数量是否合理
        validProduct(produceId,count);
        //判断商品是否以及被加入购物车了
        Cart cart = cartMapper.selectCartByUserIdAndProductId(produceId,userId);
        if(cart==null){//商品以前未被加入购物车
            cart = new Cart();
            cart.setProductId(produceId);
            cart.setUserId(userId);
            cart.setQuantity(count);
            cart.setSelected(Constant.Cart.CHECKED);
            cartMapper.insertSelective(cart);
        }else{//商品以前被加入过购物车
            count += cart.getQuantity();
            Cart newCart = new Cart();
            newCart.setProductId(produceId);
            newCart.setUserId(userId);
            newCart.setSelected(Constant.Cart.CHECKED);
            newCart.setId(cart.getId());
            newCart.setQuantity(count);
            newCart.setSelected(Constant.Cart.CHECKED);
            cartMapper.updateByPrimaryKeySelective(newCart);
        }
        return this.list(userId);
    }


    /**
     * @param produceId 想要被加入购物车的商品id
     * @param count 该商品数量
     *  判断商品是否有资格被加入购物车
     */
    private void validProduct(Integer produceId,Integer count){
        Product product = productMapper.selectByPrimaryKey(produceId);
        //排除商品下架或者商品不存在的情况
        if(product == null||product.getStatus().equals(Constant.SaleStatus.NOT_SALE)){
            throw new MallException(MallExceptionEnum.NOT_SALE);
        }
        //排除商品库存不足的情况
        if(product.getStock()<count){
            throw new MallException(MallExceptionEnum.NOT_ENOUGH);
        }
    }

    @Override
    public List<CartVO> delete(Integer userId, Integer produceId){
        //判断商品是否已经被加入购物车了
        Cart cart = cartMapper.selectCartByUserIdAndProductId(produceId,userId);
        if(cart==null){//商品以前未被加入购物车，商品不存在，报错
            throw new MallException(MallExceptionEnum.DELETE_FAILED);
        }else{//商品以前被加入过购物车,可以删除了
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
        return this.list(userId);
    }

    @Override
    public List<CartVO> update(Integer userId, Integer produceId, Integer count){
        //验证加入购物车的商品和数量是否合理
        validProduct(produceId,count);
        //判断商品是否已经被加入购物车了
        Cart cart = cartMapper.selectCartByUserIdAndProductId(produceId,userId);
        if(cart==null){//商品以前未被加入购物车，报错
            throw new MallException(MallExceptionEnum.UPDATE_FAILED);
        }else{//商品以前被加入过购物车,更新
            Cart newCart = new Cart();
            newCart.setProductId(produceId);
            newCart.setUserId(userId);
            newCart.setSelected(Constant.Cart.CHECKED);
            newCart.setId(cart.getId());
            newCart.setQuantity(count);
            newCart.setSelected(Constant.Cart.CHECKED);
            cartMapper.updateByPrimaryKeySelective(newCart);
        }
        return this.list(userId);
    }

    @Override
    public List<CartVO> list(Integer userId){
        List<CartVO> list = cartMapper.selectList(userId);
        for (CartVO cart:list
             ) {
            cart.setTotalPrice(cart.getPrice()*cart.getQuantity());
        }
        return list;
    }

    @Override
    public List<CartVO> selectOrNot(Integer userId, Integer produceId, Integer selected){
        //判断商品是否已经被加入购物车了
        Cart cart = cartMapper.selectCartByUserIdAndProductId(produceId,userId);
        if(cart==null){//商品以前未被加入购物车，无法被选中/不选，报错
            throw new MallException(MallExceptionEnum.UPDATE_FAILED);
        }else{//商品以前被加入过购物车,可以被选中/不选
            cartMapper.selectOrNot(produceId,userId,selected);
        }
        return this.list(userId);
    }

    @Override
    public List<CartVO> selectAllOrNot(Integer userId,Integer selected){
        cartMapper.selectOrNot(null,userId,selected);
        return this.list(userId);
    }
}
