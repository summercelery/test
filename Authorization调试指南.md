# Authorization 头部调试指南

## 🔍 问题描述

当使用 `python -m http.server 8000` 启动前端，debug模式启动后端时，`request.getHeader("Authorization")` 一直返回 `null`。

## 📋 环境信息

- **前端**: http://localhost:8000 (Python HTTP Server)
- **后端**: http://localhost:8080 (Spring Boot Debug模式)
- **问题接口**: `/api/reminders`

## 🛠️ 调试步骤

### 步骤1: 启动调试工具

1. 确保前端和后端都已启动
2. 在浏览器中打开: `http://localhost:8000/debug-cors-auth.html`
3. 这个工具会帮助你逐步排查问题

### 步骤2: 检查localStorage状态

```javascript
// 打开浏览器控制台，运行以下代码
console.log('authToken:', localStorage.getItem('authToken'));
console.log('currentUser:', localStorage.getItem('currentUser'));
```

**如果都是 `null`**：
- 说明用户没有登录或登录状态丢失
- 需要先完成登录流程

### 步骤3: 测试登录流程

在调试页面中：
1. 点击 "🔑 先测试登录" 按钮
2. 检查登录接口是否正常响应
3. 如果登录失败，说明CORS或后端配置有问题

### 步骤4: 模拟登录状态

如果登录接口有问题，可以：
1. 点击 "✨ 模拟登录" 按钮
2. 这会在localStorage中设置测试token
3. 然后继续后续测试

### 步骤5: 测试OPTIONS预检请求

1. 点击 "🔍 测试OPTIONS预检" 按钮
2. 检查CORS配置是否正确
3. 预检请求必须成功，浏览器才会发送实际请求

**预检请求失败的解决方案**：
- 检查后端 `CorsConfig.java` 配置
- 确保允许 `Authorization` 头部
- 确保允许来源 `http://localhost:8000`

### 步骤6: 测试调试接口

1. 点击 "🔧 测试调试接口" 按钮
2. 这会调用 `/api/debug/headers` 接口
3. 查看后端是否收到Authorization头部

### 步骤7: 检查网络请求

打开浏览器开发者工具的 Network 标签：

1. **查看是否有两个请求**：
   - 第一个: `OPTIONS /api/reminders` (预检请求)
   - 第二个: `GET /api/reminders` (实际请求)

2. **检查实际请求的Headers**：
   ```
   Request Headers:
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   Content-Type: application/json
   ```

3. **如果只看到OPTIONS请求**：
   - 说明预检请求失败，实际请求没有发送
   - 需要修复CORS配置

## 🐛 常见问题排查

### 问题1: 前端没有设置token

**症状**: 调试日志显示 "AppState.authToken为空"

**解决方案**:
```javascript
// 检查是否成功登录
if (!localStorage.getItem('authToken')) {
    console.log('需要先登录');
    // 重新登录或使用调试页面的模拟登录
}
```

### 问题2: CORS预检请求失败

**症状**: 网络请求被浏览器阻止，控制台出现CORS错误

**解决方案**:
1. 检查 `CorsConfig.java` 中的配置
2. 确保包含 `http://localhost:8000`
3. 确保 `allowCredentials(true)`

### 问题3: 后端收到OPTIONS而不是GET请求

**症状**: 后端日志显示收到OPTIONS请求，Authorization为null

**解决方案**:
- 这是正常的预检请求
- 检查后端是否正确响应了OPTIONS请求
- 确保CORS头部设置正确

### 问题4: 前端头部合并问题

**症状**: 前端日志显示设置了Authorization，但后端收不到

**解决方案**:
检查前端API请求代码中的头部合并逻辑：
```javascript
// 确保Authorization头部不被覆盖
const finalOptions = { ...defaultOptions, ...options };
if (options.headers) {
    finalOptions.headers = { ...defaultHeaders, ...options.headers };
}
```

## 🔧 后端调试代码

在你的Controller中添加调试代码：

```java
@GetMapping("/reminders")
public ResponseEntity<?> getReminders(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    String method = request.getMethod();
    String origin = request.getHeader("Origin");
    
    // 添加调试日志
    System.out.println("=== /reminders 接口调试 ===");
    System.out.println("请求方法: " + method);
    System.out.println("请求来源: " + origin);
    System.out.println("Authorization头部: " + authHeader);
    System.out.println("所有头部:");
    request.getHeaderNames().asIterator().forEachRemaining(name -> {
        System.out.println("  " + name + ": " + request.getHeader(name));
    });
    System.out.println("===========================");
    
    if (authHeader == null) {
        return ResponseEntity.status(401).body("Authorization头部为null");
    }
    
    // 你的正常业务逻辑...
}
```

## 📊 调试检查清单

- [ ] localStorage中有authToken
- [ ] localStorage中有currentUser  
- [ ] OPTIONS预检请求成功 (200状态)
- [ ] 实际GET请求被发送
- [ ] 前端正确设置Authorization头部
- [ ] 后端CORS配置正确
- [ ] 后端收到Authorization头部

## 🎯 预期结果

修复后，你应该在后端看到：
```
=== /reminders 接口调试 ===
请求方法: GET
请求来源: http://localhost:8000
Authorization头部: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
===========================
```

而不是:
```
Authorization头部: null
```

## 📞 如果问题仍然存在

1. 使用调试工具 `debug-cors-auth.html` 进行详细测试
2. 检查浏览器控制台的网络请求
3. 查看后端控制台的调试日志
4. 确认前后端的端口和URL配置
5. 考虑暂时禁用其他浏览器扩展（可能干扰请求）

记住：跨域请求中的Authorization头部传递比同源请求更复杂，需要正确的CORS配置和浏览器预检请求处理。 