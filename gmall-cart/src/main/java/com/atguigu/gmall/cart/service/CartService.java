package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.CartInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.DivException;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartInterceptor cartInterceptor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private ThreadPoolExecutor threadPool;

    @Autowired
    private Asynservice asynservice;

    private static final String KEY_PRE = "cart:info:";

    /**
     * 添加到购物车
     * @param cart
     */
    public void addCart(Cart cart) {
        String userId = getUserId();
        cart.setUserId(userId);
        String key = KEY_PRE + userId;
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if(hashOps.hasKey(skuId)) {
            //1.如果redis中存在这条商品的信息，那么就改变这个商品的数目
            String data = (String) hashOps.get(skuId);
            Cart newCart = JSON.parseObject(data, cart.getClass());
            newCart.setCount(newCart.getCount().add(count));
            hashOps.put(skuId,JSON.toJSONString(newCart));
            //2.异步保存到数据库
            this.asynservice.updateCart(newCart,Long.valueOf(skuId));
        } else {
            //1.如果redis中没有这条商品的信息，那么开始根据skuid查询sku信息
            CompletableFuture<Void> sku =
                    CompletableFuture.runAsync(() -> {
                        ResponseVo<SkuEntity> skuEntity = this.pmsClient.querySkuById(Long.valueOf(skuId));
                        SkuEntity data = skuEntity.getData();
                        if(data != null) {
                            cart.setCheck(true);
                            cart.setDefaultImage(data.getDefaultImage());
                            cart.setTitle(data.getTitle());
                            cart.setPrice(data.getPrice());
                        }
                    }, threadPool);
            //2.设置营销信息
            CompletableFuture<Void> sale = CompletableFuture.runAsync(() -> {
                ResponseVo<List<ItemSaleVo>> saleStrategy = this.smsClient.queryAllSaleStrategy(Long.valueOf(skuId));
                List<ItemSaleVo> data = saleStrategy.getData();
                if (CollectionUtils.isNotEmpty(data)) {
                    cart.setSales(JSON.toJSONString(data));
                }
            }, threadPool);
            //3.设置是否有货
            CompletableFuture<Void> store = CompletableFuture.runAsync(() -> {
                ResponseVo<List<WareSkuEntity>> wareSkuByItId = this.wmsClient.queryWareSkuByItId(Long.valueOf(skuId));
                List<WareSkuEntity> data = wareSkuByItId.getData();
                if(CollectionUtils.isNotEmpty(data)) {
                    boolean haveStore = data.stream().anyMatch
                            ((wareSkuEntity) -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0);
                    cart.setStore(haveStore);
                }
            }, threadPool);
            //4.设置销售属性
            CompletableFuture<Void> saleAttr = CompletableFuture.runAsync(() -> {
                ResponseVo<List<SkuAttrValueEntity>> list = this.pmsClient.queryAllSalesBySkuId(Long.valueOf(skuId));
                List<SkuAttrValueEntity> skuAttrValueEntityList = list.getData();
                if(CollectionUtils.isNotEmpty(skuAttrValueEntityList)) {
                    cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntityList));
                }
            }, threadPool);
            CompletableFuture.allOf(sku,store,sale,saleAttr).join();
            //5.存入redis
            hashOps.put(skuId,JSON.toJSONString(cart));
            //6.异步存入数据库中
            this.asynservice.insertCart(cart);
            //this.cartMapper.insert(cart);

        }
    }

    private String getUserId() {
        UserInfo userInfo = cartInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        Long userId = userInfo.getUserId();
        if(userId == null) {
            return userKey;
        } else {
            return userId.toString();
        }
    }

    public Cart querySkuBySkuId(Long skuId) {
        String key = KEY_PRE + getUserId();
        BoundHashOperations<String, Object, Object> cartMap = redisTemplate.boundHashOps(key);
        //从redis中获得该条数据的记录
        if(cartMap.hasKey(skuId.toString())) {
            String cartJson = (String) cartMap.get(skuId.toString());
            Cart cart = JSON.parseObject(cartJson, Cart.class);
            return cart;
        }
        throw new DivException("您的购物车里并没有此条记录");
    }



    public List<Cart> queryCarts() {
        UserInfo userInfo = cartInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        String userKeyKey = KEY_PRE + userKey;
        //首先得到未登录状态的购物车信息
        BoundHashOperations<String, Object, Object> unLoginHashOps = redisTemplate.boundHashOps(userKeyKey);
        List<Object> values = unLoginHashOps.values();
        List<Cart> unLockCarts = null;
        if(CollectionUtils.isNotEmpty(values)) {
             unLockCarts = values.stream().
                    map(value -> JSON.parseObject(value.toString(), Cart.class)).
                    collect(Collectors.toList());
        }
        //判断登录状态，如果为空的话，返回未登录状态的购物车
        if(userInfo.getUserId() == null){
            return unLockCarts;
        }
        //如果登录状态为未登录，先获得登录后的购物车信息
        String userIdKey = KEY_PRE + userInfo.getUserId();
        BoundHashOperations<String, Object, Object> loginHashOps = redisTemplate.boundHashOps(userIdKey);
        List<Object> loginCartsJson = loginHashOps.values();
        //进行判断，如果在登录状态的购物车中有未登录状态的商品那么要进行合并
        if(CollectionUtils.isNotEmpty(unLockCarts)) {
            if(CollectionUtils.isNotEmpty(loginCartsJson)) {
                //如果登录状态的购物车不为空，那么就要对其进行遍历判断，如果存在那么就要修改数量，如果不存在的话，那么就要删除添加
                unLockCarts.forEach((unLockCart) -> {
                    //如果存在对应的商品信息，那么就修改其数目
                    if(loginHashOps.hasKey(unLockCart.getSkuId())) {
                        BigDecimal count = unLockCart.getCount();
                        Cart loginCart = JSON.parseObject(loginHashOps.get(unLockCart.getSkuId()).toString(), Cart.class);
                        BigDecimal loginCount = loginCart.getCount();
                        loginCart.setCount(loginCount.add(count));
                        loginHashOps.put(unLockCart.getSkuId(),JSON.toJSONString(loginCart));
                        //异步修改数据库
                        this.asynservice.updataCart(loginCart);
                    } else{
                        //如果不存在那么就添加到现有的数据中
                        unLockCart.setUserId(this.cartInterceptor.getUserInfo().getUserId().toString());
                        loginHashOps.put(unLockCart.getSkuId().toString(),JSON.toJSONString(unLockCart));
                        //异步修改数据库
                        this.asynservice.insertCart(unLockCart);
                    }
                });
                //删除redis中的user-key旧数据
                this.redisTemplate.delete(userKeyKey);
                //返回修改后的值
                return loginHashOps.values().stream().
                        map(value -> JSON.parseObject(value.toString(),Cart.class)).
                        collect(Collectors.toList());
            } else {
                //如果登录状态的购物车为空，那么将要使用未登录的购物车去替换已经存在的购物车并返回
                //替换所有的user-key为userid
                List<Cart> loginCarts = unLockCarts.stream().map(unLockCart -> {
                    unLockCart.setUserId(cartInterceptor.getUserInfo().getUserId().toString());
                    return unLockCart;
                }).collect(Collectors.toList());
                //将redis中的旧数据删除
                redisTemplate.delete(userKeyKey);
                //在redis中用新的数据替代旧的数据
                loginCarts.forEach((loginCart) -> {
                    loginHashOps.put(loginCart.getUserId(),JSON.toJSONString(loginCart));
                });
                //异步删除数据库中的数据
                this.asynservice.deleteCartByUserId(userKeyKey);
                //异步添加到数据库
                this.asynservice.insertCarts(loginCarts);
                return loginCarts;
            }

        } else {
            //如果未登录状态的购物车为空，那么直接返回登录状态的购物车
            if(CollectionUtils.isNotEmpty(loginCartsJson)) {
                List<Cart> loginCarts = loginCartsJson.stream().map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class)).collect(Collectors.toList());
                return loginCarts;
            }
            return null;
        }
    }

    private List<Cart> getUserKeyCarts(String userKeyIndex) {
        String key = KEY_PRE + userKeyIndex;
        BoundHashOperations<String, Object, Object> keyCarts = redisTemplate.boundHashOps(key);
        List<Object> values =keyCarts.values();
        return values.stream().map((value) -> JSON.parseObject(value.toString(), Cart.class)).collect(Collectors.toList());
    }

    public void updateNum(Cart cart) {
        //第一步，到redis中查询对应的数据的信息，查询完成之后进行信息的修改
        String key = KEY_PRE + cart.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        String cartStr  = hashOps.get(cart.getSkuId().toString()).toString();
        Cart queryCart = JSON.parseObject(cartStr, Cart.class);
        queryCart.setCount(cart.getCount());
        hashOps.put(cart.getSkuId().toString(),queryCart);
        //第二步，异步添加修改到mysql数据库中
        this.asynservice.updataCart(queryCart);
    }

    public void deleteCartBySkuId(Long skuId) {
        String key = KEY_PRE + getUserId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //第一步：删除redis中对应的购物车数据
        hashOps.delete(skuId);
        //第二步，异步删除mysql中的数据
        this.asynservice.deleteCartByUserIdAndSkuId(getUserId(),skuId);

    }

    public List<Cart> queryCartsSelected(Long userId) {
        String userIdKey = KEY_PRE + userId;
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(userIdKey);
        List<Object> values = hashOps.values();
        if(CollectionUtils.isNotEmpty(values)) {
            List<Cart> cartsChecked = values.stream().map((value -> JSON.parseObject(value.toString(), Cart.class))).filter(value -> value.getCheck()).collect(Collectors.toList());
            return cartsChecked;
        }
        return null;
    }
}
