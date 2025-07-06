// 全局变量
let currentUser = null;
let authToken = localStorage.getItem('authToken');

// API 基础URL - 根据您的后端配置修改
const API_BASE_URL = 'http://localhost:8080/api';

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
    setupAnimations();
});

// 初始化应用
function initializeApp() {
    // 检查是否有保存的认证token
    if (authToken) {
        // 验证token有效性
        validateToken();
    } else {
        showLoginPage();
    }
}

// 设置事件监听器
function setupEventListeners() {
    // 密码登录表单提交
    document.getElementById('passwordLoginForm').addEventListener('submit', handlePasswordLogin);
    
    // 手机号输入格式化
    const phoneInputs = document.querySelectorAll('input[type="tel"]');
    phoneInputs.forEach(input => {
        input.addEventListener('input', formatPhoneNumber);
        input.addEventListener('keypress', allowOnlyNumbers);
    });
    
    // 密码强度检查
    const passwordInputs = document.querySelectorAll('input[type="password"]');
    passwordInputs.forEach(input => {
        input.addEventListener('input', checkPasswordStrength);
    });
    
    // 邮箱验证
    const emailInputs = document.querySelectorAll('input[type="email"]');
    emailInputs.forEach(input => {
        input.addEventListener('input', validateEmailField);
    });
    
    // 验证码输入限制
    const codeInputs = document.querySelectorAll('input[pattern="[0-9]{6}"]');
    codeInputs.forEach(input => {
        input.addEventListener('input', function() {
            this.value = this.value.replace(/\D/g, '').substring(0, 6);
        });
    });
}

// 设置动画
function setupAnimations() {
    // 添加页面加载动画
    document.body.classList.add('loaded');
    
    // 设置浮动形状动画
    const shapes = document.querySelectorAll('.shape');
    shapes.forEach((shape, index) => {
        shape.style.animationDelay = `${index * 1}s`;
    });
}

// 显示登录页面
function showLoginPage() {
    document.getElementById('login-page').classList.add('active');
    document.getElementById('dashboard-page').style.display = 'none';
}

// 显示仪表板
function showDashboard() {
    document.getElementById('login-page').classList.remove('active');
    document.getElementById('dashboard-page').style.display = 'block';
    
    // 更新用户信息
    if (currentUser) {
        document.getElementById('userName').textContent = currentUser.username || currentUser.phoneNumber;
        document.getElementById('welcomeUserName').textContent = currentUser.username || currentUser.phoneNumber;
    }
}

// 显示注册模态框
function showRegisterModal() {
    const modal = new bootstrap.Modal(document.getElementById('registerModal'));
    modal.show();
    
    // 清空表单
    document.getElementById('registerForm').reset();
    clearValidationErrors();
}

// 显示短信验证码登录模态框
function showSmsLoginModal() {
    const modal = new bootstrap.Modal(document.getElementById('smsLoginModal'));
    modal.show();
    
    // 清空表单
    document.getElementById('smsLoginForm').reset();
    clearValidationErrors();
}

// 微信登录
async function wechatLogin() {
    try {
        // 获取微信授权URL
        const redirectUri = encodeURIComponent(window.location.origin + '/wechat-callback.html');
        const state = 'wechat_login_' + Date.now();
        
        const response = await fetch(`${API_BASE_URL}/auth/wechat/auth-url?redirectUri=${redirectUri}&state=${state}`);
        const data = await response.json();
        
        if (response.ok && data.success) {
            // 打开微信授权页面
            const authWindow = window.open(data.data, '_blank', 'width=600,height=600');
            
            // 监听微信回调
            window.addEventListener('message', function(event) {
                if (event.origin !== window.location.origin) return;
                
                if (event.data.type === 'wechat_login_success') {
                    handleWechatLoginSuccess(event.data.code);
                    if (authWindow) authWindow.close();
                }
            });
            
        } else {
            showMessage(data.message || '获取微信授权URL失败', 'error');
        }
    } catch (error) {
        console.error('微信登录错误:', error);
        showMessage('微信登录失败，请稍后重试', 'error');
    }
}

// 处理微信登录成功
async function handleWechatLoginSuccess(code) {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/wechat-login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                code: code
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            // 保存token和用户信息
            authToken = data.data.token;
            currentUser = {
                username: data.data.username,
                role: data.data.role
            };
            
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMessage('微信登录成功！', 'success');
            
            // 显示仪表板
            setTimeout(() => {
                showDashboard();
            }, 1000);
            
        } else {
            showMessage(data.message || '微信登录失败', 'error');
        }
    } catch (error) {
        console.error('微信登录错误:', error);
        showMessage('微信登录失败，请稍后重试', 'error');
    }
}

// 处理密码登录
async function handlePasswordLogin(event) {
    event.preventDefault();
    
    const phone = document.getElementById('loginPhone').value.replace(/\D/g, '');
    const password = document.getElementById('loginPassword').value;
    
    if (!phone || !password) {
        const missingFields = [];
        if (!phone) missingFields.push('手机号');
        if (!password) missingFields.push('密码');
        
        showMessage(`请填写以下必填项：${missingFields.join('、')}`, 'error');
        return;
    }
    
    if (phone.length !== 11) {
        showMessage('请输入正确的11位手机号', 'error');
        return;
    }
    
    // 显示加载状态
    const submitBtn = event.target.querySelector('button[type="submit"]');
    setButtonLoading(submitBtn, true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: phone, // 使用手机号作为用户名
                password: password
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            // 保存token和用户信息
            authToken = data.data.token;
            currentUser = {
                username: data.data.username,
                phoneNumber: phone,
                role: data.data.role
            };
            
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMessage('登录成功！', 'success');
            
            // 显示仪表板
            setTimeout(() => {
                showDashboard();
            }, 1000);
            
        } else {
            showMessage(data.message || '登录失败', 'error');
        }
    } catch (error) {
        console.error('登录错误:', error);
        showMessage('网络错误，请检查后端服务是否运行', 'error');
    } finally {
        setButtonLoading(submitBtn, false);
    }
}

// 处理短信验证码登录
async function handleSmsLogin() {
    const phone = document.getElementById('smsLoginPhone').value.replace(/\D/g, '');
    const code = document.getElementById('smsLoginCode').value;
    
    if (!phone || !code) {
        const missingFields = [];
        if (!phone) missingFields.push('手机号');
        if (!code) missingFields.push('验证码');
        
        showMessage(`请填写以下必填项：${missingFields.join('、')}`, 'error');
        return;
    }
    
    if (phone.length !== 11) {
        showMessage('请输入正确的11位手机号', 'error');
        return;
    }
    
    if (code.length !== 6) {
        showMessage('请输入6位验证码', 'error');
        return;
    }
    
    // 显示加载状态
    const submitBtn = document.querySelector('#smsLoginModal .btn-primary');
    setButtonLoading(submitBtn, true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/sms-login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                phoneNumber: phone,
                verificationCode: code
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            // 保存token和用户信息
            authToken = data.data.token;
            currentUser = {
                username: data.data.username,
                phoneNumber: phone,
                role: data.data.role
            };
            
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMessage('登录成功！', 'success');
            
            // 关闭模态框
            const modal = bootstrap.Modal.getInstance(document.getElementById('smsLoginModal'));
            modal.hide();
            
            // 显示仪表板
            setTimeout(() => {
                showDashboard();
            }, 1000);
            
        } else {
            showMessage(data.message || '登录失败', 'error');
        }
    } catch (error) {
        console.error('登录错误:', error);
        showMessage('网络错误，请检查后端服务是否运行', 'error');
    } finally {
        setButtonLoading(submitBtn, false);
    }
}

// 处理注册
async function handleRegister() {
    const phone = document.getElementById('registerPhone').value.replace(/\D/g, '');
    const email = document.getElementById('registerEmail').value.trim();
    const password = document.getElementById('registerPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const agreeTerms = document.getElementById('agreeTerms').checked;
    
    // 清除之前的验证错误
    clearValidationErrors();
    
    // 验证表单
    if (!phone || !email || !password || !confirmPassword) {
        const missingFields = [];
        if (!phone) missingFields.push('手机号');
        if (!email) missingFields.push('邮箱');
        if (!password) missingFields.push('密码');
        if (!confirmPassword) missingFields.push('确认密码');
        
        showMessage(`请填写以下必填项：${missingFields.join('、')}`, 'error');
        return;
    }
    
    if (phone.length !== 11) {
        showFieldError('registerPhone', '请输入正确的11位手机号');
        return;
    }
    
    // 验证邮箱格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showFieldError('registerEmail', '请输入正确的邮箱格式，例如：user@example.com');
        return;
    }
    
    if (password.length < 6) {
        showFieldError('registerPassword', '密码长度至少6位，建议使用字母、数字和符号的组合');
        return;
    }
    
    if (password !== confirmPassword) {
        showFieldError('confirmPassword', '两次输入的密码不一致，请重新输入');
        return;
    }
    
    if (!agreeTerms) {
        showMessage('请先阅读并同意服务条款和隐私政策', 'warning');
        return;
    }
    
    // 显示加载状态
    const registerBtn = document.querySelector('#registerModal .btn-primary');
    setButtonLoading(registerBtn, true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: phone, // 使用手机号作为用户名
                password: password,
                email: email,
                phoneNumber: phone
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            showMessage('注册成功！请登录', 'success');
            
            // 关闭模态框
            const modal = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
            modal.hide();
            
            // 清空登录表单的手机号，填入注册的手机号
            document.getElementById('loginPhone').value = phone;
            
        } else {
            showMessage(data.message || '注册失败', 'error');
        }
    } catch (error) {
        console.error('注册错误:', error);
        showMessage('网络错误，请检查后端服务是否运行', 'error');
    } finally {
        setButtonLoading(registerBtn, false);
    }
}

// 加载用户资料
async function loadUserProfile() {
    if (!authToken) {
        showMessage('请先登录', 'error');
        return;
    }
    
    const profileSection = document.getElementById('profileSection');
    profileSection.style.display = 'block';
    
    try {
        const response = await fetch(`${API_BASE_URL}/user/profile`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
            }
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            displayUserProfile(data.data);
        } else {
            showMessage(data.message || '获取用户信息失败', 'error');
            // token可能已过期，清除本地存储
            if (response.status === 401) {
                logout();
            }
        }
    } catch (error) {
        console.error('获取用户资料错误:', error);
        showMessage('网络错误', 'error');
    }
}

// 显示用户资料
function displayUserProfile(user) {
    const profileContent = document.getElementById('profileContent');
    
    profileContent.innerHTML = `
        <div class="profile-info">
            <h5><i class="fas fa-user-circle"></i> 基本信息</h5>
            <div class="profile-field">
                <span class="profile-label">用户名:</span>
                <span class="profile-value">${user.username || '未设置'}</span>
            </div>
            <div class="profile-field">
                <span class="profile-label">手机号:</span>
                <span class="profile-value">${user.phoneNumber || '未设置'}</span>
            </div>
            <div class="profile-field">
                <span class="profile-label">邮箱:</span>
                <span class="profile-value">${user.email || '未设置'}</span>
            </div>
            <div class="profile-field">
                <span class="profile-label">姓名:</span>
                <span class="profile-value">${user.fullName || '未设置'}</span>
            </div>
            <div class="profile-field">
                <span class="profile-label">角色:</span>
                <span class="profile-value">
                    <span class="badge bg-primary">${user.role || 'USER'}</span>
                </span>
            </div>
            <div class="profile-field">
                <span class="profile-label">注册时间:</span>
                <span class="profile-value">${user.createdAt ? new Date(user.createdAt).toLocaleString() : '未知'}</span>
            </div>
        </div>
        
        <div class="text-center mt-4">
            <button class="btn btn-info me-2" onclick="testAuth()">
                <i class="fas fa-check-circle"></i> 测试认证
            </button>
            <button class="btn btn-warning me-2" onclick="loadSessionStats()">
                <i class="fas fa-chart-line"></i> 会话统计
            </button>
            <button class="btn btn-success me-2" onclick="extendSession()">
                <i class="fas fa-clock"></i> 延长会话
            </button>
            <button class="btn btn-danger" onclick="logoutOtherDevices()">
                <i class="fas fa-sign-out-alt"></i> 下线其他设备
            </button>
        </div>
        
        <div id="sessionStats" class="mt-4" style="display: none;"></div>
    `;
}

// 测试认证
async function testAuth() {
    if (!authToken) {
        showMessage('请先登录', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/user/hello`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
            }
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            showMessage(data.data, 'success');
        } else {
            showMessage(data.message || '认证测试失败', 'error');
        }
    } catch (error) {
        console.error('认证测试错误:', error);
        showMessage('网络错误', 'error');
    }
}

// 验证token
async function validateToken() {
    if (!authToken) {
        return false;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/user/profile`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            if (data.success) {
                currentUser = data.data;
                showDashboard();
                return true;
            }
        }
        
        // token无效，清除本地存储
        logout();
        return false;
    } catch (error) {
        console.error('Token验证错误:', error);
        logout();
        return false;
    }
}

// 退出登录
async function logout() {
    if (authToken) {
        try {
            // 调用后端登出接口，清除Redis中的会话
            await fetch(`${API_BASE_URL}/auth/logout`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${authToken}`,
                    'Content-Type': 'application/json',
                }
            });
        } catch (error) {
            console.error('登出请求失败:', error);
        }
    }
    
    authToken = null;
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    
    showMessage('已退出登录', 'info');
    showLoginPage();
    
    // 清空表单
    document.getElementById('loginForm').reset();
    
    // 隐藏个人资料区域
    document.getElementById('profileSection').style.display = 'none';
}

// 显示消息
function showMessage(message, type = 'info') {
    const messageBox = document.getElementById('messageBox');
    const messageIcon = document.getElementById('messageIcon');
    const messageTitle = document.getElementById('messageTitle');
    const messageText = document.getElementById('messageText');
    
    // 设置消息内容
    messageText.textContent = message;
    
    // 根据类型设置图标、标题和样式
    switch (type) {
        case 'success':
            messageTitle.textContent = '成功';
            messageIcon.className = 'fas fa-check-circle';
            messageIcon.parentElement.className = 'message-icon success';
            messageBox.className = 'message-box success';
            break;
        case 'error':
            messageTitle.textContent = '错误';
            messageIcon.className = 'fas fa-exclamation-circle';
            messageIcon.parentElement.className = 'message-icon error';
            messageBox.className = 'message-box error';
            break;
        case 'warning':
            messageTitle.textContent = '警告';
            messageIcon.className = 'fas fa-exclamation-triangle';
            messageIcon.parentElement.className = 'message-icon warning';
            messageBox.className = 'message-box warning';
            break;
        default:
            messageTitle.textContent = '信息';
            messageIcon.className = 'fas fa-info-circle';
            messageIcon.parentElement.className = 'message-icon info';
            messageBox.className = 'message-box info';
            break;
    }
    
    // 显示消息
    messageBox.style.display = 'flex';
    
    // 自动隐藏（除了错误消息）
    if (type !== 'error') {
        setTimeout(() => {
            hideMessage();
        }, 3000);
    }
}

// 隐藏消息
function hideMessage() {
    const messageBox = document.getElementById('messageBox');
    messageBox.style.display = 'none';
}

// 切换密码显示/隐藏
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const icon = input.parentElement.querySelector('i');
    
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fas fa-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'fas fa-eye';
    }
}

// 格式化手机号
function formatPhoneNumber(event) {
    let value = event.target.value.replace(/\D/g, '');
    
    if (value.length > 11) {
        value = value.substring(0, 11);
    }
    
    // 格式化显示：138 1234 5678
    if (value.length >= 7) {
        value = value.replace(/(\d{3})(\d{4})(\d{4})/, '$1 $2 $3');
    } else if (value.length >= 3) {
        value = value.replace(/(\d{3})(\d{0,4})/, '$1 $2');
    }
    
    event.target.value = value;
}

// 只允许数字输入
function allowOnlyNumbers(event) {
    const charCode = event.which ? event.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
        event.preventDefault();
    }
}

// 验证邮箱格式
function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// 实时邮箱验证
function validateEmailField(event) {
    const email = event.target.value.trim();
    const field = event.target;
    
    // 移除之前的验证状态
    field.classList.remove('is-valid', 'is-invalid');
    
    if (email.length === 0) {
        return; // 空值时不显示验证状态
    }
    
    if (validateEmail(email)) {
        field.classList.add('is-valid');
    } else {
        field.classList.add('is-invalid');
    }
}

// 检查密码强度
function checkPasswordStrength(event) {
    const password = event.target.value;
    const strength = calculatePasswordStrength(password);
    
    // 移除之前的强度指示器
    const existingIndicator = event.target.parentElement.querySelector('.password-strength');
    if (existingIndicator) {
        existingIndicator.remove();
    }
    
    if (password.length > 0) {
        const indicator = document.createElement('div');
        indicator.className = 'password-strength';
        indicator.innerHTML = `
            <div class="strength-bar">
                <div class="strength-fill strength-${strength.level}"></div>
            </div>
            <span class="strength-text">${strength.text}</span>
        `;
        
        event.target.parentElement.appendChild(indicator);
    }
}

// 计算密码强度
function calculatePasswordStrength(password) {
    let score = 0;
    
    if (password.length >= 6) score++;
    if (password.length >= 8) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    
    if (score <= 2) return { level: 'weak', text: '弱' };
    if (score <= 4) return { level: 'medium', text: '中等' };
    return { level: 'strong', text: '强' };
}

// 设置按钮加载状态
function setButtonLoading(button, loading) {
    const btnText = button.querySelector('.btn-text');
    const btnLoading = button.querySelector('.btn-loading');
    
    if (loading) {
        button.classList.add('loading');
        button.disabled = true;
    } else {
        button.classList.remove('loading');
        button.disabled = false;
    }
}

// 显示字段错误
function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    field.classList.add('is-invalid');
    
    // 移除之前的错误消息
    const existingError = field.parentElement.querySelector('.invalid-feedback');
    if (existingError) {
        existingError.remove();
    }
    
    // 添加新的错误消息
    const errorDiv = document.createElement('div');
    errorDiv.className = 'invalid-feedback';
    errorDiv.innerHTML = `<i class="fas fa-exclamation-circle"></i> ${message}`;
    field.parentElement.appendChild(errorDiv);
    
    // 同时显示居中消息提示
    showMessage(message, 'error');
}

// 清除验证错误
function clearValidationErrors() {
    const invalidFields = document.querySelectorAll('.is-invalid');
    invalidFields.forEach(field => {
        field.classList.remove('is-invalid');
    });
    
    const errorMessages = document.querySelectorAll('.invalid-feedback');
    errorMessages.forEach(message => {
        message.remove();
    });
}

// 定期检查token有效性
setInterval(() => {
    if (authToken) {
        validateToken();
    }
}, 5 * 60 * 1000); // 每5分钟检查一次

// 加载会话统计信息
async function loadSessionStats() {
    if (!authToken) {
        showMessage('请先登录', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/session/stats`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
            }
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            displaySessionStats(data.data);
        } else {
            showMessage(data.message || '获取会话统计失败', 'error');
        }
    } catch (error) {
        console.error('获取会话统计错误:', error);
        showMessage('网络错误', 'error');
    }
}

// 显示会话统计信息
function displaySessionStats(stats) {
    const sessionStats = document.getElementById('sessionStats');
    
    const loginTime = new Date(stats.loginTime).toLocaleString();
    const lastAccessTime = new Date(stats.lastAccessTime).toLocaleString();
    const expireTimeHours = Math.floor(stats.expireTimeSeconds / 3600);
    const expireTimeMinutes = Math.floor((stats.expireTimeSeconds % 3600) / 60);
    
    sessionStats.innerHTML = `
        <div class="profile-info">
            <h5><i class="fas fa-chart-line"></i> 会话统计信息</h5>
            <div class="profile-field">
                <span class="profile-label">登录时间:</span>
                <span class="profile-value">${loginTime}</span>
            </div>
            <div class="profile-field">
                <span class="profile-label">最后访问:</span>
                <span class="profile-value">${lastAccessTime}</span>
            </div>
            <div class="profile-field">
                <span class="profile-label">剩余时间:</span>
                <span class="profile-value">
                    <span class="badge bg-${expireTimeHours > 1 ? 'success' : expireTimeHours > 0 ? 'warning' : 'danger'}">
                        ${expireTimeHours}小时${expireTimeMinutes}分钟
                    </span>
                </span>
            </div>
            <div class="profile-field">
                <span class="profile-label">IP地址:</span>
                <span class="profile-value">${stats.ipAddress || '未知'}</span>
            </div>
            <div class="profile-field">
                <span class="profile-label">设备信息:</span>
                <span class="profile-value">${stats.userAgent ? stats.userAgent.substring(0, 50) + '...' : '未知'}</span>
            </div>
        </div>
    `;
    
    sessionStats.style.display = 'block';
}

// 延长会话时间
async function extendSession() {
    if (!authToken) {
        showMessage('请先登录', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/session/extend`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
            }
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            showMessage('会话时间已延长', 'success');
            // 刷新会话统计
            setTimeout(() => {
                loadSessionStats();
            }, 1000);
        } else {
            showMessage(data.message || '延长会话失败', 'error');
        }
    } catch (error) {
        console.error('延长会话错误:', error);
        showMessage('网络错误', 'error');
    }
}

// 强制下线其他设备
async function logoutOtherDevices() {
    if (!authToken) {
        showMessage('请先登录', 'error');
        return;
    }
    
    if (!confirm('确定要强制下线其他设备吗？这将使其他设备上的登录会话失效。')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/session/logout-others`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
            }
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            showMessage('其他设备已成功下线', 'success');
        } else {
            showMessage(data.message || '下线其他设备失败', 'error');
        }
    } catch (error) {
        console.error('下线其他设备错误:', error);
        showMessage('网络错误', 'error');
    }
}

// 定期检查会话状态
setInterval(() => {
    if (authToken) {
        checkSessionStatus();
    }
}, 5 * 60 * 1000); // 每5分钟检查一次

// 检查会话状态
async function checkSessionStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/session/stats`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            if (data.success) {
                const expireTimeHours = Math.floor(data.data.expireTimeSeconds / 3600);
                
                // 如果会话即将过期（少于1小时），显示提醒
                if (expireTimeHours < 1) {
                    showMessage(`会话即将过期，剩余时间不足1小时`, 'warning');
                }
            }
        }
    } catch (error) {
        console.error('检查会话状态错误:', error);
    }
}

// 发送短信验证码
async function sendSmsCode(smsType) {
    let phone;
    let button;
    
    if (smsType === 'LOGIN') {
        phone = document.getElementById('smsLoginPhone').value.replace(/\D/g, '');
        button = document.querySelector('#smsLoginModal .send-code-btn');
    } else if (smsType === 'RESET_PASSWORD') {
        phone = document.getElementById('resetPhone').value.replace(/\D/g, '');
        button = document.querySelector('#forgotPasswordForm .send-code-btn');
    }
    
    if (!phone) {
        showMessage('请先输入手机号', 'error');
        return;
    }
    
    if (phone.length !== 11) {
        showMessage('请输入正确的11位手机号', 'error');
        return;
    }
    
    // 禁用按钮并开始倒计时
    button.disabled = true;
    const textSpan = button.querySelector('.send-code-text');
    const countdownSpan = button.querySelector('.send-code-countdown');
    
    textSpan.style.display = 'none';
    countdownSpan.style.display = 'inline';
    
    let countdown = 60;
    const countdownInterval = setInterval(() => {
        countdownSpan.textContent = `${countdown}s`;
        countdown--;
        
        if (countdown < 0) {
            clearInterval(countdownInterval);
            button.disabled = false;
            textSpan.style.display = 'inline';
            countdownSpan.style.display = 'none';
        }
    }, 1000);
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/send-sms`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                phoneNumber: phone,
                smsType: smsType
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            showMessage('验证码发送成功，请注意查收短信', 'success');
        } else {
            showMessage(data.message || '验证码发送失败', 'error');
            // 发送失败时重置按钮状态
            clearInterval(countdownInterval);
            button.disabled = false;
            textSpan.style.display = 'inline';
            countdownSpan.style.display = 'none';
        }
    } catch (error) {
        console.error('发送验证码错误:', error);
        showMessage('网络错误，请稍后重试', 'error');
        // 发送失败时重置按钮状态
        clearInterval(countdownInterval);
        button.disabled = false;
        textSpan.style.display = 'inline';
        countdownSpan.style.display = 'none';
    }
}

// 显示忘记密码模态框
function showForgotPasswordModal() {
    const modal = new bootstrap.Modal(document.getElementById('forgotPasswordModal'));
    modal.show();
    
    // 清空表单
    document.getElementById('forgotPasswordForm').reset();
    clearValidationErrors();
}

// 处理重置密码
async function handleResetPassword() {
    const phone = document.getElementById('resetPhone').value.replace(/\D/g, '');
    const code = document.getElementById('resetCode').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmNewPassword = document.getElementById('confirmNewPassword').value;
    
    // 清除之前的验证错误
    clearValidationErrors();
    
    // 验证表单
    if (!phone || !code || !newPassword || !confirmNewPassword) {
        const missingFields = [];
        if (!phone) missingFields.push('手机号');
        if (!code) missingFields.push('验证码');
        if (!newPassword) missingFields.push('新密码');
        if (!confirmNewPassword) missingFields.push('确认密码');
        
        showMessage(`请填写以下必填项：${missingFields.join('、')}`, 'error');
        return;
    }
    
    if (phone.length !== 11) {
        showMessage('请输入正确的11位手机号', 'error');
        return;
    }
    
    if (code.length !== 6) {
        showMessage('请输入6位验证码', 'error');
        return;
    }
    
    if (newPassword.length < 6) {
        showMessage('密码长度至少6位', 'error');
        return;
    }
    
    if (newPassword !== confirmNewPassword) {
        showMessage('两次输入的密码不一致', 'error');
        return;
    }
    
    // 显示加载状态
    const submitBtn = document.querySelector('#forgotPasswordModal .btn-primary');
    setButtonLoading(submitBtn, true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/reset-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                phoneNumber: phone,
                verificationCode: code,
                newPassword: newPassword,
                confirmPassword: confirmNewPassword
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            showMessage('密码重置成功！', 'success');
            
            // 关闭模态框
            const modal = bootstrap.Modal.getInstance(document.getElementById('forgotPasswordModal'));
            modal.hide();
            
        } else {
            showMessage(data.message || '密码重置失败', 'error');
        }
    } catch (error) {
        console.error('重置密码错误:', error);
        showMessage('网络错误，请稍后重试', 'error');
    } finally {
        setButtonLoading(submitBtn, false);
    }
}

// 导出函数供HTML调用
window.showRegisterModal = showRegisterModal;
window.handleRegister = handleRegister;
window.logout = logout;
window.testAuth = testAuth;
window.loadUserProfile = loadUserProfile;
window.togglePassword = togglePassword;
window.loadSessionStats = loadSessionStats;
window.extendSession = extendSession;
window.logoutOtherDevices = logoutOtherDevices;
window.sendSmsCode = sendSmsCode;
window.showForgotPasswordModal = showForgotPasswordModal;
window.handleResetPassword = handleResetPassword;
window.showSmsLoginModal = showSmsLoginModal;
window.handleSmsLogin = handleSmsLogin;
window.wechatLogin = wechatLogin; 