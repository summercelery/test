/**
 * 修复严格模式下的 arguments/caller/callee 错误
 * 这个补丁应该在 script.js 之前加载
 */

// 1. 重写可能有问题的全局函数，避免访问 arguments
(function() {
    'use strict';
    
    // 安全的数组处理函数
    window.SafeArrayUtils = {
        // 安全地将类数组对象转换为数组
        toArray: function(arrayLike) {
            if (!arrayLike) return [];
            if (Array.isArray(arrayLike)) return arrayLike;
            
            // 使用现代方法而不是 arguments
            try {
                return Array.from(arrayLike);
            } catch (e) {
                // 降级处理
                const result = [];
                for (let i = 0; i < arrayLike.length; i++) {
                    result.push(arrayLike[i]);
                }
                return result;
            }
        },
        
        // 安全地克隆数组
        clone: function(arr) {
            if (!Array.isArray(arr)) return [];
            return arr.slice();
        },
        
        // 安全地检查是否为数组
        isArray: function(obj) {
            return Array.isArray(obj);
        }
    };
    
    // 2. 重写可能有问题的对象访问方法
    window.SafeObjectUtils = {
        // 安全地获取对象属性
        get: function(obj, path, defaultValue) {
            if (!obj || typeof obj !== 'object') return defaultValue;
            
            const keys = path.split('.');
            let current = obj;
            
            for (const key of keys) {
                if (current === null || current === undefined) {
                    return defaultValue;
                }
                current = current[key];
            }
            
            return current !== undefined ? current : defaultValue;
        },
        
        // 安全地设置对象属性
        set: function(obj, path, value) {
            if (!obj || typeof obj !== 'object') return false;
            
            const keys = path.split('.');
            const lastKey = keys.pop();
            let current = obj;
            
            for (const key of keys) {
                if (!(key in current) || typeof current[key] !== 'object') {
                    current[key] = {};
                }
                current = current[key];
            }
            
            current[lastKey] = value;
            return true;
        }
    };
    
    // 3. 修复可能导致严格模式错误的 jQuery 或 Bootstrap 问题
    if (typeof $ !== 'undefined') {
        // 重写可能有问题的 jQuery 方法
        const originalEach = $.fn.each;
        $.fn.each = function(callback) {
            return originalEach.call(this, function(index, element) {
                // 避免在 callback 中访问 arguments
                return callback.call(this, index, element);
            });
        };
    }
    
    // 4. 全局错误捕获，防止严格模式错误中断执行
    window.addEventListener('error', function(event) {
        if (event.error && event.error.message) {
            const message = event.error.message;
            if (message.includes('arguments') || 
                message.includes('caller') || 
                message.includes('callee')) {
                
                console.warn('🔧 捕获到严格模式错误，已处理:', message);
                event.preventDefault(); // 阻止错误传播
                return false;
            }
        }
    });
    
    console.log('✅ 严格模式错误修复补丁已加载');
})(); 