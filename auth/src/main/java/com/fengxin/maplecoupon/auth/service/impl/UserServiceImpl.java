package com.fengxin.maplecoupon.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengxin.exception.ClientException;
import com.fengxin.maplecoupon.auth.common.context.UserContext;
import com.fengxin.maplecoupon.auth.common.enums.UserErrorCodeEnum;
import com.fengxin.maplecoupon.auth.dao.entity.UserDO;
import com.fengxin.maplecoupon.auth.dao.mapper.UserMapper;
import com.fengxin.maplecoupon.auth.dto.req.UserLoginReqDTO;
import com.fengxin.maplecoupon.auth.dto.req.UserRegisterReqDTO;
import com.fengxin.maplecoupon.auth.dto.req.UserUpdateReqDTO;
import com.fengxin.maplecoupon.auth.dto.resp.UserLoginRespDTO;
import com.fengxin.maplecoupon.auth.dto.resp.UserRespDTO;
import com.fengxin.maplecoupon.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.fengxin.maplecoupon.auth.common.constant.EngineRedisConstant.COUPON_USER_LOGIN_KEY;
import static com.fengxin.maplecoupon.auth.common.constant.EngineRedisConstant.LOCK_COUPON_USER_REGISTER_KEY;
import static com.fengxin.maplecoupon.auth.common.enums.UserErrorCodeEnum.USER_LOGOUT_ERROR;


/**
 * @author FENGXIN
 * @date 2024/9/26
 * @project feng-shortlink
 * @description 用户管理实现
 **/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    
    @Override
    public UserRespDTO getUserByUserName (String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        // 捕获空异常
        if (userDO == null) {
            throw new ClientException (UserErrorCodeEnum.USER_NULL.message ());
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties (userDO, result);
        return result;
    }
    
    @Override
    public Boolean hasUserName (String username) {
        return userRegisterCachePenetrationBloomFilter.contains (username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerUser (UserRegisterReqDTO requestParams) {
        if (hasUserName (requestParams.getUsername ())){
            throw new ClientException (UserErrorCodeEnum.USER_NAME_EXISTS.message ());
        }
        // 给register username上分布式锁 防止恶意注册
        RLock lock = redissonClient.getLock ( StrUtil.format (LOCK_COUPON_USER_REGISTER_KEY, requestParams.getUsername ()));
        boolean tryLock = lock.tryLock ();
        if (!tryLock){
            throw new ClientException (UserErrorCodeEnum.USER_NAME_EXISTS.message ());
        }
        try {
            UserDO userDO = BeanUtil.toBean (requestParams , UserDO.class);
            // 生成店铺id
            userDO.setShopNumber (UUID.fastUUID ().toString ());
            // 新增用户
            int insert = baseMapper.insert (userDO);
            if (insert < 1) {
                throw new ClientException (UserErrorCodeEnum.USER_SAVE_ERROR.message ());
            }
            userRegisterCachePenetrationBloomFilter.add (requestParams.getUsername ());
        } catch (DuplicateKeyException e){
            throw new ClientException (UserErrorCodeEnum.USER_SAVE_ERROR.message ());
        }finally {
            // 释放锁
            lock.unlock ();
        }
        
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser (UserUpdateReqDTO requestParams,String token) {
        if (StrUtil.equals (requestParams.getUsername () , UserContext.getUsername ())) {
            throw new ClientException ("用户更新信息异常");
        }
        LambdaUpdateWrapper<UserDO> updateWrapper = new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getUsername, requestParams.getUsername ());
        UserDO userDO = BeanUtil.toBean (requestParams , UserDO.class);
        baseMapper.update (userDO, updateWrapper);
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, requestParams.getUsername ());
        UserDO selectOne = baseMapper.selectOne (queryWrapper);
        stringRedisTemplate.opsForHash ().delete (COUPON_USER_LOGIN_KEY + userDO.getUsername (), token);
        stringRedisTemplate.opsForHash ().put (COUPON_USER_LOGIN_KEY + userDO.getUsername () , token , JSON.toJSONString (selectOne));
        // 设置有效时间
        stringRedisTemplate.expire (COUPON_USER_LOGIN_KEY + userDO.getUsername () , 30L , TimeUnit.DAYS);
    }
    
    @Override
    public UserLoginRespDTO login (UserLoginReqDTO requestParams) {
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<UserDO> ()
                .eq (UserDO::getUsername , requestParams.getUsername ())
                .eq (UserDO::getPassword , requestParams.getPassword ())
                .eq (UserDO::getDelFlag , 0);
        UserDO userDO = baseMapper.selectOne (queryWrapper);
        if (userDO == null) {
            throw new ClientException (UserErrorCodeEnum.USER_NULL.message ());
        }
        // 防止用户恶意刷登录token 使redis崩溃
        Boolean hasKey = stringRedisTemplate.hasKey (COUPON_USER_LOGIN_KEY + userDO.getUsername ());
        if (Boolean.TRUE.equals (hasKey)) {
            throw new ClientException (UserErrorCodeEnum.USER_LOGIN_ERROR.message ());
        }
        // 存在 登录成功 存入redis
        String token = UUID.randomUUID ().toString ();
        String jsonString = JSON.toJSONString (userDO);
        stringRedisTemplate.opsForHash ().put (COUPON_USER_LOGIN_KEY + userDO.getUsername () , token , jsonString);
        // 设置有效时间
        stringRedisTemplate.expire (COUPON_USER_LOGIN_KEY + userDO.getUsername () , 30L , TimeUnit.DAYS);
        return new UserLoginRespDTO (token);
    }
    
    @Override
    public Boolean checkLogin (String username , String token) {
        return stringRedisTemplate.opsForHash ().hasKey (COUPON_USER_LOGIN_KEY + username , token);
    }
    
    @Override
    public void logout (String username , String token) {
        if (Boolean.TRUE.equals (checkLogin (username , token))) {
            stringRedisTemplate.opsForHash ().delete (COUPON_USER_LOGIN_KEY + username , token);
            return;
        }
        throw new ClientException (USER_LOGOUT_ERROR.message ());
    }
}
