const https = require('https');
const http = require('http');

// 忽略SSL证书错误（仅用于测试）
process.env["NODE_TLS_REJECT_UNAUTHORIZED"] = 0;

const API_BASE = 'http://localhost:8080';

function makeRequest(options, data = null) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', (chunk) => {
                body += chunk;
            });
            res.on('end', () => {
                try {
                    const result = {
                        statusCode: res.statusCode,
                        headers: res.headers,
                        body: body,
                        data: body ? JSON.parse(body) : null
                    };
                    resolve(result);
                } catch (e) {
                    resolve({
                        statusCode: res.statusCode,
                        headers: res.headers,
                        body: body,
                        data: null
                    });
                }
            });
        });
        
        req.on('error', (err) => {
            reject(err);
        });
        
        if (data) {
            req.write(JSON.stringify(data));
        }
        
        req.end();
    });
}

async function testLogin() {
    console.log('🔐 测试登录...');
    
    const options = {
        hostname: 'localhost',
        port: 8080,
        path: '/api/auth/login',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    };
    
    const loginData = {
        username: 'admin',
        password: '123456'
    };
    
    try {
        const response = await makeRequest(options, loginData);
        console.log(`状态码: ${response.statusCode}`);
        console.log(`响应头: ${JSON.stringify(response.headers, null, 2)}`);
        console.log(`响应体: ${response.body}`);
        
        if (response.statusCode === 200 && response.data && response.data.success) {
            console.log('✅ 登录成功！');
            const token = response.data.data.token;
            console.log(`Token: ${token.substring(0, 50)}...`);
            
            // 测试认证接口
            await testAuthenticatedRequest(token);
        } else {
            console.log('❌ 登录失败');
        }
    } catch (error) {
        console.error('❌ 登录请求失败:', error.message);
    }
}

async function testAuthenticatedRequest(token) {
    console.log('\n📋 测试认证接口...');
    
    const options = {
        hostname: 'localhost',
        port: 8080,
        path: '/api/reminders',
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        }
    };
    
    try {
        const response = await makeRequest(options);
        console.log(`状态码: ${response.statusCode}`);
        console.log(`响应头: ${JSON.stringify(response.headers, null, 2)}`);
        console.log(`响应体: ${response.body}`);
        
        if (response.statusCode === 200) {
            console.log('✅ 认证接口调用成功！');
        } else {
            console.log('❌ 认证接口调用失败');
        }
    } catch (error) {
        console.error('❌ 认证请求失败:', error.message);
    }
}

// 运行测试
console.log('🚀 开始认证测试...\n');
testLogin().then(() => {
    console.log('\n✨ 测试完成');
}).catch((error) => {
    console.error('❌ 测试失败:', error);
}); 