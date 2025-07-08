package com.example.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * API日志记录切面
 * 记录所有Controller方法的请求和响应信息
 */
@Aspect
@Component
@Slf4j
public class ApiLoggingAspect {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切点：拦截所有Controller中的方法
     */
    @Pointcut("execution(public * com.example.controller..*(..))")
    public void controllerMethods() {}

    /**
     * 环绕通知：记录完整的请求响应信息
     */
    @Around("controllerMethods()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // 记录请求开始
        logRequest(request, className, methodName, args, joinPoint);
        
        Object result = null;
        Exception exception = null;
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 记录响应结束
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            logResponse(request, className, methodName, result, exception, executionTime);
        }
    }

    /**
     * 记录请求信息
     */
    private void logRequest(HttpServletRequest request, String className, String methodName, Object[] args, ProceedingJoinPoint joinPoint) {
        if (request == null) {
            log.info("=== API调用开始 ===");
            log.info("类名: {}", className);
            log.info("方法: {}", methodName);
            log.info("参数: {}", formatArgs(args));
            log.info("===============");
            return;
        }

        String requestId = generateRequestId();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String queryString = request.getQueryString();
        String clientIp = getClientIp(request);
        
        log.info("=== API请求开始 [{}] ===", requestId);
        log.info("请求URI: {} {}", method, uri);
        if (queryString != null) {
            log.info("查询参数: {}", queryString);
        }
        log.info("客户端IP: {}", clientIp);
        log.info("Controller: {}.{}", className, methodName);
        
        // 记录请求体（如果有@RequestBody参数）
        Object requestBody = getRequestBody(joinPoint, args);
        if (requestBody != null) {
            log.info("请求体: {}", formatRequestBody(requestBody));
        }
        
        log.info("请求参数: {}", formatArgs(args));
        log.info("请求头: {}", getRequestHeaders(request));
        log.info("================================");
    }

    /**
     * 获取请求体数据（通过@RequestBody注解的参数）
     */
    private Object getRequestBody(ProceedingJoinPoint joinPoint, Object[] args) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();
            
            for (int i = 0; i < parameters.length; i++) {
                // 检查参数是否有@RequestBody注解
                if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                    return args[i];
                }
            }
        } catch (Exception e) {
            log.warn("获取请求体失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 格式化请求体为JSON字符串
     */
    private String formatRequestBody(Object requestBody) {
        if (requestBody == null) {
            return "null";
        }
        
        try {
            return "\n======= 前端上送数据 =======\n" +
                   objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody) +
                   "\n=============================";
        } catch (Exception e) {
            log.warn("格式化请求体失败: {}", e.getMessage());
            return requestBody.toString();
        }
    }

    /**
     * 记录响应信息
     */
    private void logResponse(HttpServletRequest request, String className, String methodName, 
                           Object result, Exception exception, long executionTime) {
        String requestId = generateRequestId();
        
        if (exception != null) {
            log.error("=== API请求异常 [{}] ===", requestId);
            log.error("Controller: {}.{}", className, methodName);
            log.error("执行时间: {}ms", executionTime);
            log.error("异常信息: {}", exception.getMessage());
            log.error("异常类型: {}", exception.getClass().getSimpleName());
            log.error("=========================");
        } else {
            log.info("=== API响应完成 [{}] ===", requestId);
            log.info("Controller: {}.{}", className, methodName);
            log.info("执行时间: {}ms", executionTime);
            log.info("响应数据: {}", formatResponse(result));
            log.info("响应状态: {}", getResponseStatus(result));
            log.info("==========================");
        }
    }

    /**
     * 格式化参数输出
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "无";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(formatObject(args[i]));
        }
        return sb.toString();
    }

    /**
     * 格式化响应数据
     */
    private String formatResponse(Object result) {
        if (result == null) {
            return "null";
        }
        
        try {
            // 对于ResponseEntity，我们既要记录完整信息，也要突出显示body内容
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
                Object body = responseEntity.getBody();
                
                StringBuilder sb = new StringBuilder();
                sb.append("\n======= 响应详情 =======\n");
                sb.append("状态码: ").append(responseEntity.getStatusCode().value())
                  .append(" ").append(responseEntity.getStatusCode().getReasonPhrase()).append("\n");
                
                if (!responseEntity.getHeaders().isEmpty()) {
                    sb.append("响应头: ").append(responseEntity.getHeaders().toSingleValueMap()).append("\n");
                }
                
                sb.append("响应体: ");
                if (body != null) {
                    sb.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
                } else {
                    sb.append("null");
                }
                sb.append("\n========================");
                
                return sb.toString();
            }
            
            return formatObject(result);
        } catch (Exception e) {
            log.warn("格式化响应数据失败: {}", e.getMessage());
            return formatObject(result);
        }
    }

    /**
     * 格式化对象为JSON字符串
     */
    private String formatObject(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        try {
            // 对于简单类型直接返回
            if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
                return obj.toString();
            }
            
            // 对于HttpServletRequest及其相关对象，不进行JSON序列化
            if (obj instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) obj;
                return String.format("HttpServletRequest[%s %s]", request.getMethod(), request.getRequestURI());
            }
            
            // 对于Spring Security相关的对象，使用toString()
            String className = obj.getClass().getName();
            if (className.contains("org.springframework.security") || 
                className.contains("org.springframework.web.context") ||
                className.contains("javax.servlet") ||
                className.contains("org.apache.catalina")) {
                return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
            }
            
            // 对于ResponseEntity类型，需要特殊处理
            if (obj instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) obj;
                Object body = responseEntity.getBody();
                
                // 创建一个包含状态码和body的对象来序列化
                Map<String, Object> responseInfo = new HashMap<>();
                responseInfo.put("statusCode", responseEntity.getStatusCode().value());
                responseInfo.put("statusText", responseEntity.getStatusCode().getReasonPhrase());
                responseInfo.put("headers", responseEntity.getHeaders().toSingleValueMap());
                responseInfo.put("body", body);
                
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseInfo);
            }
            
            // 对于集合类型，检查是否可序列化
            if (obj instanceof java.util.Collection) {
                java.util.Collection<?> collection = (java.util.Collection<?>) obj;
                if (collection.isEmpty()) {
                    return "[]";
                }
                // 检查第一个元素是否可序列化
                Object firstElement = collection.iterator().next();
                if (firstElement != null && isNotSerializable(firstElement)) {
                    return String.format("Collection[size=%d, type=%s]", collection.size(), firstElement.getClass().getSimpleName());
                }
            }
            
            // 对于Map类型，检查是否可序列化
            if (obj instanceof java.util.Map) {
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) obj;
                if (map.isEmpty()) {
                    return "{}";
                }
                // 检查是否包含不可序列化的键或值
                for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
                    if ((entry.getKey() != null && isNotSerializable(entry.getKey())) ||
                        (entry.getValue() != null && isNotSerializable(entry.getValue()))) {
                        return String.format("Map[size=%d, type=%s]", map.size(), obj.getClass().getSimpleName());
                    }
                }
            }
            
            // 对于复杂对象，转换为格式化的JSON（美化输出）
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("JSON序列化失败，使用toString()方法: {}", e.getMessage());
            return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
        }
    }
    
    /**
     * 检查对象是否不可序列化
     */
    private boolean isNotSerializable(Object obj) {
        if (obj == null) {
            return false;
        }
        
        String className = obj.getClass().getName();
        return className.contains("org.springframework.security") || 
               className.contains("org.springframework.web.context") ||
               className.contains("javax.servlet") ||
               className.contains("org.apache.catalina") ||
               className.contains("java.util.Collections$") ||
               obj instanceof HttpServletRequest;
    }

    /**
     * 获取响应状态
     */
    private String getResponseStatus(Object result) {
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            return response.getStatusCode().toString();
        }
        return "200 OK";
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取请求头信息
     */
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // 过滤敏感信息
            if ("authorization".equalsIgnoreCase(headerName) && headerValue != null) {
                headerValue = "Bearer ***";
            }
            headers.put(headerName, headerValue);
        }
        return headers;
    }

    /**
     * 生成请求ID（简单实现）
     */
    private String generateRequestId() {
        return String.valueOf(System.currentTimeMillis() % 100000);
    }
} 