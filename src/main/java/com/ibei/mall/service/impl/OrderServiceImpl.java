package com.ibei.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.zxing.WriterException;
import com.ibei.mall.exception.MallException;
import com.ibei.mall.exception.MallExceptionEnum;
import com.ibei.mall.filter.UserFilter;
import com.ibei.mall.model.dao.CartMapper;
import com.ibei.mall.model.dao.OrderItemMapper;
import com.ibei.mall.model.dao.OrderMapper;
import com.ibei.mall.model.dao.ProductMapper;
import com.ibei.mall.model.pojo.Order;
import com.ibei.mall.model.pojo.OrderItem;
import com.ibei.mall.model.pojo.Product;
import com.ibei.mall.model.request.CreateOrderRequest;
import com.ibei.mall.model.vo.CartVO;
import com.ibei.mall.model.vo.OrderItemVo;
import com.ibei.mall.model.vo.OrderVo;
import com.ibei.mall.service.CartService;
import com.ibei.mall.service.OrderService;
import com.ibei.mall.service.UserService;
import com.ibei.mall.util.Constant;
import com.ibei.mall.util.OrderCodeFactory;
import com.ibei.mall.util.QRCodeGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    CartService cartService;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    CartMapper cartMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    UserService userService;

    @Value("${file.upload.ip}")
    String ip;

    @Override
    @Transactional(rollbackFor = Exception.class)//??????????????????????????????
    public String create(CreateOrderRequest createOrderRequest){
        //????????????id
        Integer userId = UserFilter.currentUser.getId();
        //????????????????????????????????????
        List<CartVO> cartVOList = cartService.list(userId);
        List<CartVO> cartVOListTemp = new ArrayList<>();
        for (CartVO cart:cartVOList
             ) {
            if(cart.getSelected().equals(Constant.Cart.CHECKED))cartVOListTemp.add(cart);
        }
        cartVOList = cartVOListTemp;
        //?????????????????????????????????????????????
        if(CollectionUtils.isEmpty(cartVOList))throw new MallException(MallExceptionEnum.CART_EMPTY);
        //???????????????????????????????????????????????????
        validSellStatusAndStock(cartVOList);
        //?????????????????????????????????item??????
        List<OrderItem> orderItemList = cartVOListToOrderItemList(cartVOList);
        //?????????
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            int stock = product.getStock() - orderItem.getQuantity();
            if (stock < 0) {
                throw new MallException(MallExceptionEnum.NOT_ENOUGH);
            }
            product.setStock(stock);
            productMapper.updateByPrimaryKeySelective(product);
        }
        //???????????????????????????????????????
        cleanCart(cartVOList);
        //????????????
        Order order = new Order();
        //???????????????????????????????????????
        String orderNo = OrderCodeFactory.getOrderCode(Long.valueOf(userId));
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalPrice(totalPrice(orderItemList));
        order.setReceiverName(createOrderRequest.getReceiverName());
        order.setReceiverMobile(createOrderRequest.getReceiverMobile());
        order.setReceiverAddress(createOrderRequest.getReceiverAddress());
        order.setOrderStatus(Constant.OrderStatusEnum.NOT_PAID.getCode());
        order.setPostage(0);
        order.setPaymentType(1);
        orderMapper.insertSelective(order);
        //???????????????????????????order??????item???
        for (OrderItem orderItem:orderItemList
             ) {
            orderItem.setOrderNo(order.getOrderNo());
            orderItemMapper.insertSelective(orderItem);
        }
        return orderNo;
    }

    private Integer totalPrice(List<OrderItem> orderItemList) {
        Integer total = 0;
        for (OrderItem orderItem: orderItemList
             ) {
            total += orderItem.getTotalPrice();
        }
        return total;
    }

    private void cleanCart(List<CartVO> cartVOList) {
        for (CartVO cart:cartVOList
             ) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private List<OrderItem> cartVOListToOrderItemList(List<CartVO> cartVOList) {
        List<OrderItem> orderItemList = new ArrayList<>();
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartVO.getProductId());
            //????????????????????????
            orderItem.setProductName(cartVO.getProductName());
            orderItem.setProductImg(cartVO.getProductImage());
            orderItem.setUnitPrice(cartVO.getPrice());
            orderItem.setQuantity(cartVO.getQuantity());
            orderItem.setTotalPrice(cartVO.getTotalPrice());
            orderItemList.add(orderItem);
        }
        return orderItemList;
    }

    private void validSellStatusAndStock(List<CartVO> cartVOList) {
        for (CartVO cart:cartVOList
             ) {
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            //????????????????????????????????????????????????
            if(product == null||product.getStatus().equals(Constant.SaleStatus.NOT_SALE)){
                throw new MallException(MallExceptionEnum.NOT_SALE);
            }
            //?????????????????????????????????
            if(product.getStock()<cart.getQuantity()){
                throw new MallException(MallExceptionEnum.NOT_ENOUGH);
            }
        }
    }

    @Override
    public OrderVo detail(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        //?????????????????????
        if (order == null)throw new MallException(MallExceptionEnum.NO_ORDER);
        Integer userId = UserFilter.currentUser.getId();
        //???????????????????????????????????????
        if(!order.getUserId().equals(userId)){
            throw new MallException(MallExceptionEnum.NO_YOUR_ORDER);
        }
        OrderVo ordervo = getOrderVo(order);
        return ordervo;
    }

    private OrderVo getOrderVo(Order order) {
        OrderVo ordervo = new OrderVo();
        BeanUtils.copyProperties(order,ordervo);
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
        List<OrderItemVo> orderItemVOList = new ArrayList<>();
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            OrderItemVo orderItemVO = new OrderItemVo();
            BeanUtils.copyProperties(orderItem, orderItemVO);
            orderItemVOList.add(orderItemVO);
        }
        ordervo.setOrderItemVoList(orderItemVOList);
        ordervo.setOrderStatusName(Constant.OrderStatusEnum.codeOf(ordervo.getOrderStatus()).getValue());
        return ordervo;
    }

    @Override
    public PageInfo listForCustomer(Integer pageNum, Integer pageSize){
        Integer userId = UserFilter.currentUser.getId();
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectForCustomer(userId);
        List<OrderVo> orderVOList = orderListToOrderVoList(orderList);
        PageInfo pageInfo = new PageInfo<>(orderList);
        pageInfo.setList(orderVOList);
        return pageInfo;
    }

    private List<OrderVo> orderListToOrderVoList(List<Order> orderList){
        List<OrderVo> orderVOList = new ArrayList<>();
        for (int i = 0; i < orderList.size(); i++) {
            Order order = orderList.get(i);
            OrderVo orderVO = getOrderVo(order);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    @Override
    public void cancel(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        //?????????????????????
        if (order == null) {
            throw new MallException(MallExceptionEnum.NO_ORDER);
        }
        //??????????????????
        //?????????????????????????????????
        Integer userId = UserFilter.currentUser.getId();
        if (!order.getUserId().equals(userId)) {
            throw new MallException(MallExceptionEnum.NO_YOUR_ORDER);
        }
        //?????????????????????????????????????????????
        //????????????????????????????????????????????????
        if(order.getOrderStatus().equals(Constant.OrderStatusEnum.NOT_PAID.getCode())){
            order.setOrderStatus(Constant.OrderStatusEnum.CANCELED.getCode());
            order.setEndTime(new Date());
            orderMapper.updateByPrimaryKey(order);
        }else {
            throw new MallException(MallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }

    @Override
    public String qrCode(String orderNo){
        ServletRequestAttributes attributes =(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String address = ip+request.getLocalPort();
        String payUrl = "http://"+address+"/pay?orderNo="+orderNo;
        try {
            //???????????????????????????????????????
            QRCodeGenerator.generateQRCodeImage(payUrl,350,350,Constant.FILE_UPLOAD_DIR+orderNo+".png");
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pngAddress = "http://"+address+"/images/"+orderNo+".png";
        return pngAddress;
    }

    @Override
    public PageInfo listForAdmin(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAllForAdmin();
        List<OrderVo> orderVOList = orderListToOrderVoList(orderList);
        PageInfo pageInfo = new PageInfo<>(orderList);
        pageInfo.setList(orderVOList);
        return pageInfo;
    }

    @Override
    public void pay(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        //?????????????????????
        if (order == null) {
            throw new MallException(MallExceptionEnum.NO_ORDER);
        }
        if(order.getOrderStatus()== Constant.OrderStatusEnum.NOT_PAID.getCode()){
            order.setOrderStatus(Constant.OrderStatusEnum.PAID.getCode());
            order.setPayTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }else{
            throw  new MallException(MallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }

    @Override
    public void deliver(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        //?????????????????????
        if (order == null) {
            throw new MallException(MallExceptionEnum.NO_ORDER);
        }
        if(order.getOrderStatus()== Constant.OrderStatusEnum.PAID.getCode()){
            order.setOrderStatus(Constant.OrderStatusEnum.DELIVERED.getCode());
            order.setDeliveryTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }else{
            throw  new MallException(MallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }

    @Override
    public void finish (String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        //?????????????????????
        if (order == null) {
            throw new MallException(MallExceptionEnum.NO_ORDER);
        }
        //?????????????????????????????????????????????????????????????????????????????????
        if(!userService.checkAdminRole(UserFilter.currentUser)&&!order.getUserId().equals(UserFilter.currentUser.getId())){
            throw  new MallException(MallExceptionEnum.NO_YOUR_ORDER);
        }
        //??????????????????????????????
        if(order.getOrderStatus()== Constant.OrderStatusEnum.DELIVERED.getCode()){
            order.setOrderStatus(Constant.OrderStatusEnum.FINISHED.getCode());
            order.setEndTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }else{
            throw  new MallException(MallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }
}
