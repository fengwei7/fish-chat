package com.fish.chat.interfaces.user.feign;


/**
 * 用户服务 Feign 接口
 * 
 * 职责：
 * - 供其他微服务调用用户相关接口
 * - 基于 HTTP 协议的远程调用
 * 
 * 使用说明：
 * - 当前为占位接口，暂未启用
 * - 启用后需要配置 Spring Cloud OpenFeign 依赖
 * - 其他服务通过 @Autowired 注入此接口即可调用
 * 
 * 示例：
 * <pre>
 * {@code
 * @Autowired
 * private UserFeignClient userFeignClient;
 * 
 * public void getUserInfo() {
 *     Result<UserDTO> result = userFeignClient.getUserByCode("user-code-xxx");
 *     UserDTO user = result.getData();
 * }
 * }
 * </pre>
 * 
 * TODO: 启用 Feign 需要：
 * 1. 添加依赖：spring-cloud-starter-openfeign
 * 2. 启动类添加 @EnableFeignClients 注解
 * 3. 配置 Feign 相关参数（超时、重试等）
 */
// @FeignClient(name = "fish-chat-user", path = "/api/user")
public interface UserFeignClient {
    
    /**
     * 根据用户 Code 获取用户信息
     * 
     * @param code 用户编码
     * @return 用户信息
     */
    // @GetMapping("/{code}")
    // Result<UserDTO> getUserByCode(@PathVariable("code") String code);
    
    /**
     * 搜索用户
     * 
     * @param keyword 搜索关键字
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    // @GetMapping("/search")
    // Result<?> searchUsers(
    //         @RequestParam("keyword") String keyword,
    //         @RequestParam("pageNum") int pageNum,
    //         @RequestParam("pageSize") int pageSize
    // );
}
