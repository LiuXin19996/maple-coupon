package com.fengxin.maplecoupon.engine.controller;

import cn.hutool.core.bean.BeanUtil;

import com.fengxin.maplecoupon.engine.dto.req.UserLoginReqDTO;
import com.fengxin.maplecoupon.engine.dto.req.UserRegisterReqDTO;
import com.fengxin.maplecoupon.engine.dto.req.UserUpdateReqDTO;
import com.fengxin.maplecoupon.engine.dto.resp.UserActualRespDTO;
import com.fengxin.maplecoupon.engine.dto.resp.UserLoginRespDTO;
import com.fengxin.maplecoupon.engine.dto.resp.UserRespDTO;
import com.fengxin.maplecoupon.engine.service.UserService;
import com.fengxin.web.Result;
import com.fengxin.web.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author FENGXIN
 * @date 2024/9/25
 * @project feng-shortlink
 * @description 用户名管理控制层
 **/
@RestController
@Tag(name = "用户管理")
public class UserController {
    @Resource
    private UserService userService;
    
    @Operation(summary = "根据用户名检索用户信息", description = "脱敏展示")
    @GetMapping("/api/engine/admin/user/{username}")
    public Result<UserRespDTO> getUserByUserName (@PathVariable String username) {
        return Results.success (userService.getUserByUserName (username));
    }
    
    @Operation(summary = "根据用户名检索实际用户信息", description = "不脱敏")
    @GetMapping("/api/engine/v1/admin/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUserName (@PathVariable String username) {
        return Results.success (BeanUtil.toBean (userService.getUserByUserName (username) , UserActualRespDTO.class));
    }
    
    @Operation(summary = "检查是否存在指定用户名的用户")
    @GetMapping("/api/engine/v1/admin/user/has-username")
    public Result<Boolean> hasUserName (@RequestParam("username") String username) {
        return Results.success (userService.hasUserName(username));
    }
    
    @Operation(summary = "注册新用户")
    @PostMapping("/api/engine/v1/admin/user")
    public Result<Void> registerUser (@RequestBody UserRegisterReqDTO requestParams) {
        userService.registerUser (requestParams);
        return Results.success ();
    }
    
    @Operation(summary = "更新用户信息")
    @PutMapping("/api/engine/v1/admin/user")
    public Result<Void> updateUser (@RequestBody UserUpdateReqDTO requestParams) {
        userService.updateUser (requestParams);
        return Results.success ();
    }
    
    @Operation(summary = "用户登录")
    @PostMapping("/api/engine/v1/admin/user/login")
    public Result<UserLoginRespDTO> login (@RequestBody UserLoginReqDTO requestParams) {
        return Results.success (userService.login(requestParams));
    }
    
    @Operation(summary = "检查用户登录状态")
    @GetMapping("/api/engine/v1/admin/user/check-login")
    public Result<Boolean> checkLogin (@RequestParam("username") String username,@RequestParam("token") String token){
        return Results.success (userService.checkLogin (username,token));
    }
    
    @Operation(summary = "用户登出")
    @DeleteMapping("/api/engine/v1/admin/user/logout")
    public Result<Void> logout (@RequestParam("username") String username,@RequestParam("token") String token){
        userService.logout(username,token);
        return Results.success ();
    }
}
