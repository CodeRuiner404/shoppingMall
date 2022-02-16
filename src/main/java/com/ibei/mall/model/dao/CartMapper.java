package com.ibei.mall.model.dao;

import com.ibei.mall.model.pojo.Cart;
import com.ibei.mall.model.vo.CartVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCartByUserIdAndProductId(@Param("productId") Integer productId, @Param("userId")Integer userId);

    List<CartVO> selectList(@Param("userId")Integer userId);

    Integer selectOrNot(@Param("productId") Integer productId, @Param("userId")Integer userId,@Param("selected")Integer selected);
}