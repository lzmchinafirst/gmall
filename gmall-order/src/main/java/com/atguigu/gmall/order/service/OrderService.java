package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.DivException;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.Vo.OrderConfirmVo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.OrderInterceptor;
import com.atguigu.gmall.order.pojo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private GmallCartClient gmallCartClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private GmallOmsClient gmallOmsClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private ThreadPoolExecutor threadPool;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String ORDER_TOKEN_PRE = "order:token:";
    private static final String STOCK_LOCK_PRE = "stock:lock:";
    private static final String KEY_PRE = "cart:info:";

    public OrderConfirmVo confirm() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        UserInfo userInfo = OrderInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        if(userId == null) {
            throw new DivException("用户为空！");
        }
        //1.根据当前用户的id查询收货地址
        CompletableFuture<Void> queryAddress = CompletableFuture.runAsync(() -> {
            ResponseVo<List<UserAddressEntity>> listResponseVo = this.umsClient.queryUserAddressByUserId(userId);
            List<UserAddressEntity> userAddressEntityList = listResponseVo.getData();
            if(CollectionUtils.isNotEmpty(userAddressEntityList)) {
                orderConfirmVo.setAddresses(userAddressEntityList);
            }
        }, threadPool);
        //2.查询用户选中的购物车记录
        CompletableFuture<List<Cart>> queryCarts = CompletableFuture.supplyAsync(() -> {
            ResponseVo<List<Cart>> cartsList = this.gmallCartClient.queryCartsSelected(userId);
            List<Cart> carts = cartsList.getData();
            return carts;
        }, threadPool);
        //3.将用户查询的购物车记录转换为OrderItemVo的list集合
        CompletableFuture<Void> queryOrderItemVo = queryCarts.thenAcceptAsync(carts -> {
            if (CollectionUtils.isEmpty(carts)) {
                throw new DivException("您没有选中的购物车信息！");
            }
            List<OrderItemVo> orderItemVoList = carts.stream().map(cart -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(cart.getSkuId());
                orderItemVo.setCount(cart.getCount().intValue());
                CompletableFuture<Void> sku = CompletableFuture.runAsync(() -> {
                    ResponseVo<SkuEntity> skuVo = this.pmsClient.querySkuById(cart.getSkuId());
                    SkuEntity skuEntity = skuVo.getData();
                    if (skuEntity != null) {
                        orderItemVo.setWeight(new BigDecimal(skuEntity.getWeight()));
                        orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
                        orderItemVo.setPrice(skuEntity.getPrice());
                        orderItemVo.setTitle(skuEntity.getTitle());
                    }
                }, threadPool);
                //设置库存信息
                CompletableFuture<Void> store = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<WareSkuEntity>> wareSkuByItId = this.wmsClient.queryWareSkuByItId(cart.getSkuId());
                    List<WareSkuEntity> wareSkuEntities = wareSkuByItId.getData();
                    if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
                        boolean have = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0);
                        orderItemVo.setStore(have);
                    }
                }, threadPool);
                //设置营销信息
                CompletableFuture<Void> sale = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<ItemSaleVo>> saleStrategy = this.smsClient.queryAllSaleStrategy(cart.getSkuId());
                    List<ItemSaleVo> sales = saleStrategy.getData();
                    if(sales != null) {
                        orderItemVo.setSales(sales);
                    }
                }, threadPool);

                //设置销售属性
                CompletableFuture<Void> skuAttr = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<SkuAttrValueEntity>> queryAllSalesBySkuId = this.pmsClient.queryAllSalesBySkuId(cart.getSkuId());
                    List<SkuAttrValueEntity> skuAttrValueEntities = queryAllSalesBySkuId.getData();
                    if(CollectionUtils.isNotEmpty(skuAttrValueEntities)) {
                        orderItemVo.setSaleAttrs(skuAttrValueEntities);
                    }
                }, threadPool);
                CompletableFuture.allOf(sku,store,sale,skuAttr).join();
                return orderItemVo;
            }).collect(Collectors.toList());
            orderConfirmVo.setItems(orderItemVoList);
        }, threadPool);
        CompletableFuture<Void> bounds = CompletableFuture.runAsync(() -> {
            ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUserById(userId);
            UserEntity userEntity = userEntityResponseVo.getData();
            if(userEntity != null) {
                orderConfirmVo.setBounds(userEntity.getIntegration());
            }
        }, threadPool);
        CompletableFuture<Void> token = CompletableFuture.runAsync(() -> {
            String orderToken = IdWorker.getTimeId();
            orderConfirmVo.setOrderToken(orderToken);
            this.redisTemplate.opsForValue().set(ORDER_TOKEN_PRE + orderToken,orderToken);
        }, threadPool);
        CompletableFuture.allOf(queryAddress,queryCarts,queryOrderItemVo,bounds,token).join();
        return orderConfirmVo;
    }

    @Transactional
    public OrderEntity submitOrder(OrderSubmitVo orderSubmitVo) {
        //第一步：防止重复提交
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Boolean execute = this.redisTemplate.execute(new DefaultRedisScript<Boolean>(script),
                Arrays.asList(ORDER_TOKEN_PRE + orderSubmitVo.getOrderToken()),
                orderSubmitVo.getOrderToken());
        if(!execute) {
            throw new OrderException("请勿重复提交！");
        }
        //第二步：验证价格
        BigDecimal submitTotalPrice = orderSubmitVo.getTotalPrice();
        List<OrderItemVo> items = orderSubmitVo.getItems();
        if(CollectionUtils.isEmpty(items)) {
            throw new OrderException("您的购物车为空！");
        }
        BigDecimal trueTotalPrice = new BigDecimal(0);
        items.forEach(item -> {
            Long skuId = item.getSkuId();
            Integer count = item.getCount();
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            BigDecimal price = skuEntity.getPrice();
            BigDecimal singleTotlePrice = price.multiply(new BigDecimal(count));
            trueTotalPrice.add(singleTotlePrice);
        });
        int i = submitTotalPrice.compareTo(trueTotalPrice);
        if(i != 0) {
            throw new OrderException("请刷新页面！");
        }
        //第三步：骤验证库存与锁库存
        List<SkuLockVo> skuLockVoList = items.stream().map(item -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setLock(false);
            skuLockVo.setCount(item.getCount());
            skuLockVo.setOrderToken(orderSubmitVo.getOrderToken());
            skuLockVo.setSkuId(item.getSkuId());
            return skuLockVo;
        }).collect(Collectors.toList());
        ResponseVo<List<SkuLockVo>> listResponseVo = this.wmsClient.checkAndLock(skuLockVoList);
        List<SkuLockVo> skuLockVos = listResponseVo.getData();
        if(CollectionUtils.isNotEmpty(skuLockVoList)) {
            throw new OrderException("您选中的某一商品已经无货！");
        }


        //保存订单的锁定信息，方便后续的解锁（此步骤已经在对应的远程调用方法中实现）
//        this.redisTemplate.opsForValue().set(STOCK_LOCK_PRE + orderSubmitVo.getOrderToken(),
//                JSON.toJSONString(skuLockVoList));
        //第四步：创建订单
        OrderEntity orderEntity = null;
        try {
            OrderInterceptor orderInterceptor = new OrderInterceptor();
            UserInfo userInfo = orderInterceptor.getUserInfo();
            Long userId = userInfo.getUserId();
            ResponseVo<OrderEntity> orderEntityResponseVo = this.gmallOmsClient.saveOrder(orderSubmitVo, userId);
            orderEntity = orderEntityResponseVo.getData();
        } catch (Exception e) {
            e.printStackTrace();
            //如果订单补货到了异常的话，那么我们要同时干两件事
            //1.更新订单为无效订单
            this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE","ORDER.DISABLE",orderSubmitVo.getOrderToken());
            //2.对已锁定的库存进行解锁处理
            this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE","SEND.DEAD",orderSubmitVo.getOrderToken());

            throw new OrderException("创建订单失败！");
        }
        //第五步：异步删除购物车
        Map<String ,Object> map = new HashMap<>();
        List<Long> skuIds = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
        map.put("userId",KEY_PRE + skuIds);
        map.put("skuIds",JSON.toJSONString(skuIds));
        this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE","cart.delete",map);
        return orderEntity;
    }
}
