/**
 * 用户认证系统前端脚本
 * 版本: 2.0
 * 优化: 模块化、异常处理、代码规范
 */

// ==================== 配置与常量 ====================
const CONFIG = {
    API_BASE_URL: 'http://localhost:8080/api',
    DEV_MODE: false,
    STORAGE_KEYS: {
        AUTH_TOKEN: 'authToken',
        CURRENT_USER: 'currentUser'
    },
    VALIDATION: {
        PHONE_LENGTH: 11,
        CODE_LENGTH: 6,
        MIN_PASSWORD_LENGTH: 6
    },
    TIMEOUTS: {
        MESSAGE_AUTO_HIDE: 3000,
        TOKEN_CHECK_INTERVAL: 5 * 60 * 1000,
        SMS_COUNTDOWN: 60
    }
};

// ==================== 全局状态管理 ====================
const AppState = {
    authToken: null,
    currentUser: null,
    reminderRecipientIndex: 0,
    contacts: [],
    tags: [],
    selectedTags: [],
    editingContactId: null,
    editingTagId: null,
    
    // 初始化状态
    init() {
        this.authToken = localStorage.getItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN);
        this.currentUser = JSON.parse(localStorage.getItem(CONFIG.STORAGE_KEYS.CURRENT_USER) || 'null');
        
        console.log('AppState初始化完成:');
        console.log('- authToken:', this.authToken);
        console.log('- currentUser:', this.currentUser);
    },
    
    // 清除状态
    clear() {
        console.log('清除AppState状态');
        this.authToken = null;
        this.currentUser = null;
        localStorage.removeItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN);
        localStorage.removeItem(CONFIG.STORAGE_KEYS.CURRENT_USER);
    },
    
    // 设置用户信息
    setUser(token, user) {
        console.log('设置用户信息:');
        console.log('- token:', token);
        console.log('- user:', user);
        
        this.authToken = token;
        this.currentUser = user;
        localStorage.setItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN, token);
        localStorage.setItem(CONFIG.STORAGE_KEYS.CURRENT_USER, JSON.stringify(user));
        
        console.log('用户信息设置完成，验证localStorage:');
        console.log('- localStorage authToken:', localStorage.getItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN));
        console.log('- localStorage currentUser:', localStorage.getItem(CONFIG.STORAGE_KEYS.CURRENT_USER));
    }
};

// ==================== 工具函数 ====================
const Utils = {
    // 格式化手机号
    formatPhone(value) {
        const cleaned = value.replace(/\D/g, '');
        const limited = cleaned.substring(0, CONFIG.VALIDATION.PHONE_LENGTH);
        
        if (limited.length >= 7) {
            return limited.replace(/(\d{3})(\d{4})(\d{4})/, '$1 $2 $3');
        } else if (limited.length >= 3) {
            return limited.replace(/(\d{3})(\d{0,4})/, '$1 $2');
        }
        return limited;
    },
    
    // 验证邮箱
    validateEmail(email) {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    },
    
    // 计算密码强度
    calculatePasswordStrength(password) {
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
    },
    
    // 格式化日期时间
    formatDateTime(dateTimeString) {
        const date = new Date(dateTimeString);
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    },
    
    // 防抖函数
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },
    
    // 安全的 DOM 操作
    safeGetElement(id) {
        const element = document.getElementById(id);
        if (!element) {
            console.warn(`Element with id '${id}' not found`);
        }
        return element;
    },
    
    // 安全的 DOM 操作 - 设置文本内容
    safeSetText(id, text) {
        const element = this.safeGetElement(id);
        if (element) {
            element.textContent = text;
        }
    },
    
    // 安全的 DOM 操作 - 设置 HTML 内容
    safeSetHTML(id, html) {
        const element = this.safeGetElement(id);
        if (element) {
            element.innerHTML = html;
        }
    }
};

// ==================== API 请求封装 ====================
const API = {
    // 通用请求方法
    async request(url, options = {}) {
        // 构建默认头部
        const defaultHeaders = {
            'Content-Type': 'application/json'
        };
        
        // 添加Authorization头部（如果token存在）
        if (AppState.authToken) {
            defaultHeaders['Authorization'] = `Bearer ${AppState.authToken}`;
            console.log('添加Authorization头部:', defaultHeaders['Authorization']);
        } else {
            console.warn('AppState.authToken为空，未设置Authorization头部');
        }
        
        const defaultOptions = {
            headers: defaultHeaders
        };
        
        // 合并选项，确保不覆盖Authorization头部
        const finalOptions = { ...defaultOptions, ...options };
        if (options.headers) {
            finalOptions.headers = { ...defaultHeaders, ...options.headers };
        }
        
        // 调试信息
        console.log('API请求URL:', `${CONFIG.API_BASE_URL}${url}`);
        console.log('最终请求头部:', finalOptions.headers);
        
        try {
            const response = await fetch(`${CONFIG.API_BASE_URL}${url}`, finalOptions);
            const data = await response.json().catch(() => ({}));
            
            return { response, data };
        } catch (error) {
            console.error('API request failed:', error);
            throw new Error('网络错误，请检查网络连接');
        }
    },
    
    // GET 请求
    async get(url) {
        return this.request(url, { method: 'GET' });
    },
    
    // POST 请求
    async post(url, data) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },
    
    // PUT 请求
    async put(url, data) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },
    
    // DELETE 请求
    async delete(url) {
        return this.request(url, { method: 'DELETE' });
    }
};

// ==================== 消息提示系统 ====================
const MessageSystem = {
    // 显示消息
    show(message, type = 'info') {
        const messageBox = Utils.safeGetElement('messageBox');
        const messageIcon = Utils.safeGetElement('messageIcon');
        const messageTitle = Utils.safeGetElement('messageTitle');
        const messageText = Utils.safeGetElement('messageText');
        
        if (!messageBox || !messageIcon || !messageTitle || !messageText) {
            console.warn('Message elements not found');
            return;
        }
        
        // 设置消息内容
        messageText.textContent = message;
        
        // 根据类型设置图标、标题和样式
        const configs = {
            success: {
                title: '成功',
                icon: 'fas fa-check-circle',
                iconClass: 'success',
                boxClass: 'success'
            },
            error: {
                title: '错误',
                icon: 'fas fa-exclamation-circle',
                iconClass: 'error',
                boxClass: 'error'
            },
            warning: {
                title: '警告',
                icon: 'fas fa-exclamation-triangle',
                iconClass: 'warning',
                boxClass: 'warning'
            },
            info: {
                title: '信息',
                icon: 'fas fa-info-circle',
                iconClass: 'info',
                boxClass: 'info'
            }
        };
        
        const config = configs[type] || configs.info;
        
        messageTitle.textContent = config.title;
        messageIcon.className = config.icon;
        messageIcon.parentElement.className = `message-icon ${config.iconClass}`;
        messageBox.className = `message-box ${config.boxClass}`;
        
        // 显示消息
        messageBox.style.display = 'flex';
        
        // 自动隐藏（除了错误消息）
        if (type !== 'error') {
            setTimeout(() => {
                this.hide();
            }, CONFIG.TIMEOUTS.MESSAGE_AUTO_HIDE);
        }
    },
    
    // 隐藏消息
    hide() {
        const messageBox = Utils.safeGetElement('messageBox');
        if (messageBox) {
            messageBox.style.display = 'none';
        }
    }
};

// ==================== 表单验证系统 ====================
const FormValidator = {
    // 显示字段错误
    showFieldError(fieldId, message) {
        const field = Utils.safeGetElement(fieldId);
        if (!field) return;
        
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
        MessageSystem.show(message, 'error');
    },
    
    // 清除验证错误
    clearValidationErrors() {
        const invalidFields = document.querySelectorAll('.is-invalid');
        invalidFields.forEach(field => {
            field.classList.remove('is-invalid');
        });
        
        const errorMessages = document.querySelectorAll('.invalid-feedback');
        errorMessages.forEach(message => {
            message.remove();
        });
    },
    
    // 验证手机号
    validatePhone(phone) {
        const cleaned = phone.replace(/\D/g, '');
        return cleaned.length === CONFIG.VALIDATION.PHONE_LENGTH;
    },
    
    // 验证验证码
    validateCode(code) {
        return code.length === CONFIG.VALIDATION.CODE_LENGTH;
    },
    
    // 验证密码
    validatePassword(password) {
        return password.length >= CONFIG.VALIDATION.MIN_PASSWORD_LENGTH;
    }
};

// ==================== UI 控制器 ====================
const UIController = {
    // 设置按钮加载状态
    setButtonLoading(button, loading) {
        if (!button) return;
        
        const btnText = button.querySelector('.btn-text');
        const btnLoading = button.querySelector('.btn-loading');
        
        if (loading) {
            button.classList.add('loading');
            button.disabled = true;
            if (btnText) btnText.style.display = 'none';
            if (btnLoading) btnLoading.style.display = 'inline-block';
        } else {
            button.classList.remove('loading');
            button.disabled = false;
            if (btnText) btnText.style.display = 'inline';
            if (btnLoading) btnLoading.style.display = 'none';
        }
    },
    
    // 切换密码显示/隐藏
    togglePassword(inputId) {
        const input = Utils.safeGetElement(inputId);
        if (!input) return;
        
        const icon = input.parentElement.querySelector('i');
        if (!icon) return;
        
        if (input.type === 'password') {
            input.type = 'text';
            icon.className = 'fas fa-eye-slash';
        } else {
            input.type = 'password';
            icon.className = 'fas fa-eye';
        }
    },
    
    // 显示/隐藏页面
    showPage(pageId) {
        const page = Utils.safeGetElement(pageId);
        if (page) {
            page.style.display = 'block';
        }
    },
    
    hidePage(pageId) {
        const page = Utils.safeGetElement(pageId);
        if (page) {
            page.style.display = 'none';
        }
    }
};

// ==================== 页面检测 ====================
const PageDetector = {
    get isLoginPage() {
        return window.location.pathname.includes('login.html');
    },
    
    get isIndexPage() {
        return window.location.pathname.includes('index.html') || window.location.pathname === '/';
    },
    
    get isRemindersPage() {
        return window.location.pathname.includes('reminders.html');
    },
    
    get isContactsPage() {
        return window.location.pathname.includes('contacts.html');
    }
};

// ==================== 认证系统 ====================
const AuthSystem = {
    // 初始化认证状态
    init() {
        AppState.init();
        
        if (PageDetector.isLoginPage) {
            if (AppState.authToken) {
                window.location.href = 'index.html';
            }
        } else if (PageDetector.isIndexPage) {
            if (!AppState.authToken) {
                window.location.href = 'login.html';
                return;
            }
            this.loadUserProfile();
        } else {
            if (!AppState.authToken) {
                window.location.href = 'login.html';
                return;
            }
        }
    },
    
    // 密码登录
    async handlePasswordLogin(event) {
        event.preventDefault();
        
        const phone = Utils.safeGetElement('loginPhone')?.value.replace(/\D/g, '') || '';
        const password = Utils.safeGetElement('loginPassword')?.value || '';
        
        if (!this.validateLoginForm(phone, password)) {
            return;
        }
        
        const submitBtn = event.target.querySelector('button[type="submit"]');
        UIController.setButtonLoading(submitBtn, true);
        
        try {
            if (CONFIG.DEV_MODE) {
                await this.mockLogin(phone);
            } else {
                await this.performLogin(phone, password);
            }
        } catch (error) {
            MessageSystem.show(error.message, 'error');
        } finally {
            UIController.setButtonLoading(submitBtn, false);
        }
    },
    
    // 验证登录表单
    validateLoginForm(phone, password) {
        if (!phone || !password) {
            const missingFields = [];
            if (!phone) missingFields.push('手机号');
            if (!password) missingFields.push('密码');
            
            MessageSystem.show(`请填写以下必填项：${missingFields.join('、')}`, 'error');
            return false;
        }
        
        if (!FormValidator.validatePhone(phone)) {
            MessageSystem.show('请输入正确的11位手机号', 'error');
            return false;
        }
        
        return true;
    },
    
    // 执行登录
    async performLogin(phone, password) {
        const { response, data } = await API.post('/auth/login', {
            username: phone,
            password: password
        });
        
        if (response.ok && data.success) {
            AppState.setUser(data.data.token, {
                username: data.data.username,
                phoneNumber: phone,
                role: data.data.role
            });
            
            MessageSystem.show('登录成功！', 'success');
            
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
        } else {
            throw new Error(data.message || '登录失败');
        }
    },
    
    // 模拟登录（开发模式）
    async mockLogin(phone) {
        return new Promise((resolve) => {
            setTimeout(() => {
                AppState.setUser('demo-token', {
                    username: '测试用户',
                    phoneNumber: phone,
                    email: 'test@example.com',
                    role: 'USER'
                });
                
                MessageSystem.show('登录成功！', 'success');
                
                setTimeout(() => {
                    window.location.href = 'index.html';
                }, 1000);
                
                resolve();
            }, 1000);
        });
    },
    
    // 短信登录
    async handleSmsLogin() {
        const phone = Utils.safeGetElement('smsLoginPhone')?.value.replace(/\D/g, '') || '';
        const code = Utils.safeGetElement('smsLoginCode')?.value || '';
        
        if (!this.validateSmsLoginForm(phone, code)) {
            return;
        }
        
        const submitBtn = document.querySelector('#smsLoginModal .btn-primary');
        UIController.setButtonLoading(submitBtn, true);
        
        try {
            const { response, data } = await API.post('/auth/sms-login', {
                phoneNumber: phone,
                verificationCode: code
            });
            
            if (response.ok && data.success) {
                AppState.setUser(data.data.token, {
                    username: data.data.username,
                    phoneNumber: phone,
                    role: data.data.role
                });
                
                MessageSystem.show('登录成功！', 'success');
                
                const modal = bootstrap.Modal.getInstance(document.getElementById('smsLoginModal'));
                if (modal) modal.hide();
                
                setTimeout(() => {
                    window.location.href = 'index.html';
                }, 1000);
            } else {
                throw new Error(data.message || '登录失败');
            }
        } catch (error) {
            MessageSystem.show(error.message, 'error');
        } finally {
            UIController.setButtonLoading(submitBtn, false);
        }
    },
    
    // 验证短信登录表单
    validateSmsLoginForm(phone, code) {
        if (!phone || !code) {
            const missingFields = [];
            if (!phone) missingFields.push('手机号');
            if (!code) missingFields.push('验证码');
            
            MessageSystem.show(`请填写以下必填项：${missingFields.join('、')}`, 'error');
            return false;
        }
        
        if (!FormValidator.validatePhone(phone)) {
            MessageSystem.show('请输入正确的11位手机号', 'error');
            return false;
        }
        
        if (!FormValidator.validateCode(code)) {
            MessageSystem.show('请输入6位验证码', 'error');
            return false;
        }
        
        return true;
    },
    
    // 用户注册
    async handleRegister() {
        const phone = Utils.safeGetElement('registerPhone')?.value.replace(/\D/g, '') || '';
        const email = Utils.safeGetElement('registerEmail')?.value.trim() || '';
        const password = Utils.safeGetElement('registerPassword')?.value || '';
        const confirmPassword = Utils.safeGetElement('confirmPassword')?.value || '';
        const agreeTerms = Utils.safeGetElement('agreeTerms')?.checked || false;
        
        FormValidator.clearValidationErrors();
        
        if (!this.validateRegisterForm(phone, email, password, confirmPassword, agreeTerms)) {
            return;
        }
        
        const registerBtn = document.querySelector('#registerModal .btn-primary');
        UIController.setButtonLoading(registerBtn, true);
        
        try {
            const { response, data } = await API.post('/auth/register', {
                username: phone,
                password: password,
                email: email,
                phoneNumber: phone
            });
            
            if (response.ok && data.success) {
                MessageSystem.show('注册成功！请登录', 'success');
                
                const modal = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
                if (modal) modal.hide();
                
                // 设置登录手机号时，需要设置value而不是textContent
                const loginPhoneInput = Utils.safeGetElement('loginPhone');
                if (loginPhoneInput) {
                    loginPhoneInput.value = phone;
                }
            } else {
                throw new Error(data.message || '注册失败');
            }
        } catch (error) {
            MessageSystem.show(error.message, 'error');
        } finally {
            UIController.setButtonLoading(registerBtn, false);
        }
    },
    
    // 验证注册表单
    validateRegisterForm(phone, email, password, confirmPassword, agreeTerms) {
        if (!phone || !email || !password || !confirmPassword) {
            const missingFields = [];
            if (!phone) missingFields.push('手机号');
            if (!email) missingFields.push('邮箱');
            if (!password) missingFields.push('密码');
            if (!confirmPassword) missingFields.push('确认密码');
            
            MessageSystem.show(`请填写以下必填项：${missingFields.join('、')}`, 'error');
            return false;
        }
        
        if (!FormValidator.validatePhone(phone)) {
            FormValidator.showFieldError('registerPhone', '请输入正确的11位手机号');
            return false;
        }
        
        if (!Utils.validateEmail(email)) {
            FormValidator.showFieldError('registerEmail', '请输入正确的邮箱格式，例如：user@example.com');
            return false;
        }
        
        if (!FormValidator.validatePassword(password)) {
            FormValidator.showFieldError('registerPassword', '密码长度至少6位，建议使用字母、数字和符号的组合');
            return false;
        }
        
        if (password !== confirmPassword) {
            FormValidator.showFieldError('confirmPassword', '两次输入的密码不一致，请重新输入');
            return false;
        }
        
        if (!agreeTerms) {
            MessageSystem.show('请先阅读并同意服务条款和隐私政策', 'warning');
            return false;
        }
        
        return true;
    },
    
    // 加载用户资料
    async loadUserProfile() {
        if (!AppState.authToken) {
            MessageSystem.show('请先登录', 'error');
            return;
        }
        
        const profileSection = Utils.safeGetElement('profileSection');
        if (profileSection) {
            profileSection.style.display = 'block';
        }
        
        try {
            const { response, data } = await API.get('/user/profile');
            
            if (response.ok && data.success) {
                this.displayUserProfile(data.data);
            } else {
                MessageSystem.show(data.message || '获取用户信息失败', 'error');
                if (response.status === 401) {
                    this.logout();
                }
            }
        } catch (error) {
            console.error('获取用户资料错误:', error);
            MessageSystem.show('网络错误', 'error');
        }
    },
    
    // 显示用户资料
    displayUserProfile(user) {
        const profileContent = Utils.safeGetElement('profileContent');
        if (!profileContent) return;
        
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
                    <span class="profile-value">${user.createdAt ? Utils.formatDateTime(user.createdAt) : '未知'}</span>
                </div>
            </div>
        `;
    },
    
    // 验证token
    async validateToken() {
        if (CONFIG.DEV_MODE) {
            return true;
        }
        
        if (!AppState.authToken) {
            return false;
        }
        
        try {
            const { response, data } = await API.get('/auth/validate');
            
            if (response.ok && data.success) {
                return true;
            } else {
                AppState.clear();
                return false;
            }
        } catch (error) {
            console.error('Token验证错误:', error);
            return false;
        }
    },
    
    // 退出登录
    async logout() {
        if (CONFIG.DEV_MODE) {
            AppState.clear();
            MessageSystem.show('已退出登录', 'info');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 1000);
            return;
        }
        
        try {
            await API.post('/auth/logout');
        } catch (error) {
            console.error('登出请求失败:', error);
        }
        
        AppState.clear();
        MessageSystem.show('已退出登录', 'info');
        
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 1000);
    }
};

// ==================== 短信系统 ====================
const SmsSystem = {
    // 发送短信验证码
    async sendSmsCode(smsType) {
        let phone, button;
        
        if (smsType === 'LOGIN') {
            phone = Utils.safeGetElement('smsLoginPhone')?.value.replace(/\D/g, '') || '';
            button = document.querySelector('#smsLoginModal .send-code-btn');
        } else if (smsType === 'RESET_PASSWORD') {
            phone = Utils.safeGetElement('resetPhone')?.value.replace(/\D/g, '') || '';
            button = document.querySelector('#forgotPasswordForm .send-code-btn');
        }
        
        if (!phone) {
            MessageSystem.show('请先输入手机号', 'error');
            return;
        }
        
        if (!FormValidator.validatePhone(phone)) {
            MessageSystem.show('请输入正确的11位手机号', 'error');
            return;
        }
        
        this.startCountdown(button);
        
        try {
            const { response, data } = await API.post('/auth/send-sms', {
                phoneNumber: phone,
                smsType: smsType
            });
            
            if (response.ok && data.success) {
                MessageSystem.show('验证码发送成功，请注意查收短信', 'success');
            } else {
                MessageSystem.show(data.message || '验证码发送失败', 'error');
                this.resetCountdown(button);
            }
        } catch (error) {
            console.error('发送验证码错误:', error);
            MessageSystem.show('网络错误，请稍后重试', 'error');
            this.resetCountdown(button);
        }
    },
    
    // 开始倒计时
    startCountdown(button) {
        if (!button) return;
        
        button.disabled = true;
        const textSpan = button.querySelector('.send-code-text');
        const countdownSpan = button.querySelector('.send-code-countdown');
        
        if (textSpan) textSpan.style.display = 'none';
        if (countdownSpan) countdownSpan.style.display = 'inline';
        
        let countdown = CONFIG.TIMEOUTS.SMS_COUNTDOWN;
        const countdownInterval = setInterval(() => {
            if (countdownSpan) {
                countdownSpan.textContent = `${countdown}s`;
            }
            countdown--;
            
            if (countdown < 0) {
                clearInterval(countdownInterval);
                this.resetCountdown(button);
            }
        }, 1000);
    },
    
    // 重置倒计时
    resetCountdown(button) {
        if (!button) return;
        
        button.disabled = false;
        const textSpan = button.querySelector('.send-code-text');
        const countdownSpan = button.querySelector('.send-code-countdown');
        
        if (textSpan) textSpan.style.display = 'inline';
        if (countdownSpan) countdownSpan.style.display = 'none';
    }
};

// ==================== 事件监听器设置 ====================
const EventListeners = {
    setup() {
        // 密码登录表单提交
        const passwordLoginForm = document.getElementById('passwordLoginForm');
        if (passwordLoginForm) {
            passwordLoginForm.addEventListener('submit', AuthSystem.handlePasswordLogin.bind(AuthSystem));
        }
        
        // 手机号输入格式化
        const phoneInputs = document.querySelectorAll('input[type="tel"]');
        phoneInputs.forEach(input => {
            input.addEventListener('input', this.formatPhoneNumber);
            input.addEventListener('keypress', this.allowOnlyNumbers);
        });
        
        // 密码强度检查（仅在注册和重置密码时显示）
        const passwordInputs = document.querySelectorAll('input[type="password"]');
        passwordInputs.forEach(input => {
            // 只对注册模态框和重置密码模态框中的密码输入框添加强度检查
            if (input.closest('#registerModal') || input.closest('#forgotPasswordModal')) {
                input.addEventListener('input', this.checkPasswordStrength);
            }
        });
        
        // 邮箱验证
        const emailInputs = document.querySelectorAll('input[type="email"]');
        emailInputs.forEach(input => {
            input.addEventListener('input', this.validateEmailField);
        });
        
        // 验证码输入限制
        const codeInputs = document.querySelectorAll('input[pattern="[0-9]{6}"]');
        codeInputs.forEach(input => {
            input.addEventListener('input', function() {
                this.value = this.value.replace(/\D/g, '').substring(0, 6);
            });
        });
        
        // 提醒时间设置默认值
        const reminderTimeInput = document.getElementById('reminderTime');
        if (reminderTimeInput) {
            const now = new Date();
            const futureTime = new Date(now.getTime() + 60 * 60 * 1000);
            reminderTimeInput.value = futureTime.toISOString().slice(0, 16);
        }
        
        // 重复类型变化监听
        const repeatTypeSelect = document.getElementById('repeatType');
        if (repeatTypeSelect) {
            repeatTypeSelect.addEventListener('change', function() {
                const repeatEndTimeGroup = document.getElementById('repeatEndTimeGroup');
                if (repeatEndTimeGroup) {
                    repeatEndTimeGroup.style.display = this.value !== 'NONE' ? 'block' : 'none';
                }
            });
        }
    },
    
    // 格式化手机号
    formatPhoneNumber(event) {
        event.target.value = Utils.formatPhone(event.target.value);
    },
    
    // 只允许数字输入
    allowOnlyNumbers(event) {
        const charCode = event.which ? event.which : event.keyCode;
        if (charCode > 31 && (charCode < 48 || charCode > 57)) {
            event.preventDefault();
        }
    },
    
    // 检查密码强度
    checkPasswordStrength(event) {
        const password = event.target.value;
        const strength = Utils.calculatePasswordStrength(password);
        
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
    },
    
    // 实时邮箱验证
    validateEmailField(event) {
        const email = event.target.value.trim();
        const field = event.target;
        
        field.classList.remove('is-valid', 'is-invalid');
        
        if (email.length === 0) {
            return;
        }
        
        if (Utils.validateEmail(email)) {
            field.classList.add('is-valid');
        } else {
            field.classList.add('is-invalid');
        }
    }
};

// ==================== 应用初始化 ====================
const App = {
    init() {
        // 开发模式设置模拟数据
        if (CONFIG.DEV_MODE && !AppState.currentUser) {
            AppState.setUser('demo-token', {
                username: '测试用户',
                phoneNumber: '13800138000',
                email: 'test@example.com',
                role: 'USER'
            });
        }
        
        // 设置事件监听器
        EventListeners.setup();
        
        // 设置动画
        this.setupAnimations();
        
        // 初始化认证系统
        AuthSystem.init();
        
        // 根据页面类型进行特定初始化
        if (PageDetector.isRemindersPage) {
            // 提醒管理页面初始化
            this.initRemindersPage();
        } else if (PageDetector.isContactsPage) {
            // 提醒人管理页面初始化
            this.initContactsPage();
        }
        
        // 定期检查token有效性
        setInterval(() => {
            if (AppState.authToken) {
                AuthSystem.validateToken();
            }
        }, CONFIG.TIMEOUTS.TOKEN_CHECK_INTERVAL);
    },
    
    // 设置动画
    setupAnimations() {
        document.body.classList.add('loaded');
        
        const shapes = document.querySelectorAll('.shape');
        shapes.forEach((shape, index) => {
            shape.style.animationDelay = `${index * 1}s`;
        });
    },
    
    // 初始化提醒页面
    initRemindersPage() {
        // 延迟执行，确保DOM已完全加载
        setTimeout(() => {
            if (typeof loadReminders === 'function') {
                loadReminders();
            }
        }, 100);
    },
    
    // 初始化联系人页面
    initContactsPage() {
        // 延迟执行，确保DOM已完全加载
        setTimeout(() => {
            if (typeof loadContacts === 'function') {
                loadContacts();
            }
            if (typeof loadTags === 'function') {
                loadTags();
            }
        }, 100);
    }
};

// ==================== 全局方法导出 ====================
// 导出给 HTML 页面调用的方法
window.showMessage = MessageSystem.show.bind(MessageSystem);
window.hideMessage = MessageSystem.hide.bind(MessageSystem);
window.togglePassword = UIController.togglePassword.bind(UIController);
window.logout = AuthSystem.logout.bind(AuthSystem);
window.handlePasswordLogin = AuthSystem.handlePasswordLogin.bind(AuthSystem);
window.handleSmsLogin = AuthSystem.handleSmsLogin.bind(AuthSystem);
window.handleRegister = AuthSystem.handleRegister.bind(AuthSystem);
window.sendSmsCode = SmsSystem.sendSmsCode.bind(SmsSystem);
window.loadUserProfile = AuthSystem.loadUserProfile.bind(AuthSystem);

// 模态框相关方法
window.showRegisterModal = function() {
    const modal = new bootstrap.Modal(document.getElementById('registerModal'));
    modal.show();
    
    const form = Utils.safeGetElement('registerForm');
    if (form) form.reset();
    FormValidator.clearValidationErrors();
};

window.showSmsLoginModal = function() {
    const modal = new bootstrap.Modal(document.getElementById('smsLoginModal'));
    modal.show();
    
    const form = Utils.safeGetElement('smsLoginForm');
    if (form) form.reset();
    FormValidator.clearValidationErrors();
};

window.showForgotPasswordModal = function() {
    const modal = new bootstrap.Modal(document.getElementById('forgotPasswordModal'));
    modal.show();
    
    const form = Utils.safeGetElement('forgotPasswordForm');
    if (form) form.reset();
    FormValidator.clearValidationErrors();
};

// 页面跳转方法
window.goToRemindersPage = function() {
    window.location.href = 'reminders.html';
};

window.goToContactsPage = function() {
    window.location.href = 'contacts.html';
};

// 用户信息加载
window.loadUserInfo = function() {
    const user = AppState.currentUser;
    if (user) {
        Utils.safeSetText('userName', user.username || user.phoneNumber || '用户');
        Utils.safeSetText('welcomeUserName', user.username || user.phoneNumber || '用户');
    } else if (CONFIG.DEV_MODE) {
        const mockUser = {
            username: '测试用户',
            phoneNumber: '13800138000',
            email: 'test@example.com',
            role: 'USER'
        };
        AppState.setUser('demo-token', mockUser);
        Utils.safeSetText('userName', mockUser.username);
        Utils.safeSetText('welcomeUserName', mockUser.username);
    }
};

// 微信登录相关方法
window.wechatLogin = async function() {
    try {
        const redirectUri = encodeURIComponent(window.location.origin + '/wechat-callback.html');
        const state = 'wechat_login_' + Date.now();
        
        const { response, data } = await API.get(`/auth/wechat/auth-url?redirectUri=${redirectUri}&state=${state}`);
        
        if (response.ok && data.success) {
            const authWindow = window.open(data.data, '_blank', 'width=600,height=600');
            
            window.addEventListener('message', function(event) {
                if (event.origin !== window.location.origin) return;
                
                if (event.data.type === 'wechat_login_success') {
                    handleWechatLoginSuccess(event.data.code);
                    if (authWindow) authWindow.close();
                }
            });
        } else {
            MessageSystem.show(data.message || '获取微信授权URL失败', 'error');
        }
    } catch (error) {
        console.error('微信登录错误:', error);
        MessageSystem.show('微信登录失败，请稍后重试', 'error');
    }
};

// 处理微信登录成功
async function handleWechatLoginSuccess(code) {
    try {
        const { response, data } = await API.post('/auth/wechat-login', { code });
        
        if (response.ok && data.success) {
            AppState.setUser(data.data.token, {
                username: data.data.username,
                role: data.data.role
            });
            
            MessageSystem.show('微信登录成功！', 'success');
            
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
        } else {
            MessageSystem.show(data.message || '微信登录失败', 'error');
        }
    } catch (error) {
        console.error('微信登录错误:', error);
        MessageSystem.show('微信登录失败，请稍后重试', 'error');
    }
}

// 处理重置密码
window.handleResetPassword = async function() {
    const phone = Utils.safeGetElement('resetPhone')?.value.replace(/\D/g, '') || '';
    const code = Utils.safeGetElement('resetCode')?.value || '';
    const newPassword = Utils.safeGetElement('newPassword')?.value || '';
    const confirmNewPassword = Utils.safeGetElement('confirmNewPassword')?.value || '';
    
    FormValidator.clearValidationErrors();
    
    if (!phone || !code || !newPassword || !confirmNewPassword) {
        const missingFields = [];
        if (!phone) missingFields.push('手机号');
        if (!code) missingFields.push('验证码');
        if (!newPassword) missingFields.push('新密码');
        if (!confirmNewPassword) missingFields.push('确认密码');
        
        MessageSystem.show(`请填写以下必填项：${missingFields.join('、')}`, 'error');
        return;
    }
    
    if (!FormValidator.validatePhone(phone)) {
        MessageSystem.show('请输入正确的11位手机号', 'error');
        return;
    }
    
    if (!FormValidator.validateCode(code)) {
        MessageSystem.show('请输入6位验证码', 'error');
        return;
    }
    
    if (!FormValidator.validatePassword(newPassword)) {
        MessageSystem.show('密码长度至少6位', 'error');
        return;
    }
    
    if (newPassword !== confirmNewPassword) {
        MessageSystem.show('两次输入的密码不一致', 'error');
        return;
    }
    
    const submitBtn = document.querySelector('#forgotPasswordModal .btn-primary');
    UIController.setButtonLoading(submitBtn, true);
    
    try {
        const { response, data } = await API.post('/auth/reset-password', {
            phoneNumber: phone,
            verificationCode: code,
            newPassword: newPassword,
            confirmPassword: confirmNewPassword
        });
        
        if (response.ok && data.success) {
            MessageSystem.show('密码重置成功！', 'success');
            
            const modal = bootstrap.Modal.getInstance(document.getElementById('forgotPasswordModal'));
            if (modal) modal.hide();
        } else {
            MessageSystem.show(data.message || '密码重置失败', 'error');
        }
    } catch (error) {
        console.error('重置密码错误:', error);
        MessageSystem.show('网络错误，请稍后重试', 'error');
    } finally {
        UIController.setButtonLoading(submitBtn, false);
    }
};

// ==================== 数据加载函数 ====================

// 加载提醒列表
window.loadReminders = async function() {
    const remindersList = Utils.safeGetElement('remindersList');
    const totalReminders = Utils.safeGetElement('totalReminders');
    const pendingReminders = Utils.safeGetElement('pendingReminders');
    const sentReminders = Utils.safeGetElement('sentReminders');
    const cancelledReminders = Utils.safeGetElement('cancelledReminders');
    
    if (!remindersList) {
        console.warn('Reminders list element not found');
        return;
    }
    
    try {
        // 显示加载状态
        remindersList.innerHTML = `
            <div class="text-center py-4">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">加载中...</span>
                </div>
                <p class="mt-2 text-muted">正在加载提醒列表...</p>
            </div>
        `;
        
        if (CONFIG.DEV_MODE) {
            // 开发模式：使用模拟数据
            setTimeout(() => {
                const mockReminders = [
                    {
                        id: 1,
                        title: '会议提醒',
                        content: '明天下午2点开会',
                        reminderTime: '2024-01-15T14:00:00',
                        status: 'PENDING',
                        reminderType: 'SMS',
                        recipients: [
                            { name: '张三', type: 'SMS', contact: '138****8888' }
                        ]
                    },
                    {
                        id: 2,
                        title: '生日提醒',
                        content: '今天是小明的生日',
                        reminderTime: '2024-01-14T09:00:00',
                        status: 'SENT',
                        reminderType: 'WECHAT',
                        recipients: [
                            { name: '小明', type: 'WECHAT', contact: 'wx****1234' }
                        ]
                    }
                ];
                
                displayReminders(mockReminders);
                updateReminderStats(mockReminders);
            }, 1000);
        } else {
            // 生产模式：调用后端API
            const { response, data } = await API.get('/reminders');
            
            console.log('📥 /reminders API响应:', {
                status: response.status,
                ok: response.ok,
                data: data
            });
            
            if (response.ok) {
                console.log('✅ API调用成功，开始处理数据...');
                console.log('📊 原始返回数据:', data);
                console.log('📊 数据类型:', typeof data);
                console.log('📊 是否为数组:', Array.isArray(data));
                
                // 🔥 智能数据格式处理
                let reminders = [];
                
                // 情况1：直接返回数组
                if (Array.isArray(data)) {
                    reminders = data;
                    console.log('✅ 直接使用数组数据');
                }
                // 情况2：标准格式 {success: true, data: [...]}
                else if (data && data.success && data.data) {
                    reminders = Array.isArray(data.data) ? data.data : [];
                    console.log('✅ 从标准格式获取数组数据');
                }
                // 情况3：其他对象格式
                else if (data && typeof data === 'object') {
                    // 尝试从常见字段获取数组
                    if (data.list && Array.isArray(data.list)) {
                        reminders = data.list;
                        console.log('✅ 从data.list获取数组数据');
                    } else if (data.content && Array.isArray(data.content)) {
                        reminders = data.content;
                        console.log('✅ 从data.content获取数组数据');
                    } else if (data.records && Array.isArray(data.records)) {
                        reminders = data.records;
                        console.log('✅ 从data.records获取数组数据');
                    } else {
                        // 如果是空对象{}，当作空数组处理
                        reminders = [];
                        console.log('⚠️ 返回空对象，当作空数组处理');
                    }
                }
                // 情况4：其他情况
                else {
                    reminders = [];
                    console.log('⚠️ 未知数据格式，使用空数组');
                }
                
                console.log('📊 最终处理的reminders:', reminders);
                console.log('📊 提醒数量:', reminders.length);
                displayReminders(reminders);
                updateReminderStats(reminders);
            } else {
                console.error('❌ API调用失败:', {
                    status: response.status,
                    data: data
                });
                throw new Error(data.message || `API调用失败 (状态码: ${response.status})`);
            }
        }
    } catch (error) {
        console.error('加载提醒列表错误:', error);
        remindersList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-triangle"></i>
                <h5>加载失败</h5>
                <p class="text-muted">${error.message}</p>
                <button class="btn btn-primary" onclick="loadReminders()">重试</button>
            </div>
        `;
    }
};

// 显示提醒列表
function displayReminders(reminders) {
    console.log('🎨 displayReminders被调用，参数:', reminders);
    console.log('📊 参数类型:', typeof reminders);
    console.log('📊 是否为数组:', Array.isArray(reminders));
    
    const remindersList = Utils.safeGetElement('remindersList');
    if (!remindersList) {
        console.error('❌ remindersList元素未找到');
        return;
    }
    
    // 🔥 关键：确保reminders是数组
    if (!Array.isArray(reminders)) {
        console.error('❌ displayReminders接收到非数组数据:', reminders);
        remindersList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-triangle"></i>
                <h5>数据格式错误</h5>
                <p class="text-muted">接收到的数据格式不正确，请检查后端API返回格式</p>
                <button class="btn btn-primary" onclick="loadReminders()">重试</button>
            </div>
        `;
        return;
    }
    
    if (!reminders || reminders.length === 0) {
        console.log('📝 显示空状态（无提醒数据）');
        remindersList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-bell-slash"></i>
                <h5>暂无提醒</h5>
                <p class="text-muted">您还没有创建任何提醒</p>
                <button class="btn btn-primary" onclick="showCreateReminderModal()">创建提醒</button>
            </div>
        `;
        return;
    }
    
    console.log(`🎨 开始渲染 ${reminders.length} 个提醒项目`);
    const remindersHtml = reminders.map(reminder => `
        <div class="reminder-item">
            <div class="reminder-header">
                <h5 class="reminder-title">${reminder.title}</h5>
                <span class="reminder-status status-${reminder.status.toLowerCase()}">
                    ${getStatusText(reminder.status)}
                </span>
            </div>
            <div class="reminder-info">
                <div class="info-item">
                    <i class="fas fa-clock"></i>
                    <span>${Utils.formatDateTime(reminder.reminderTime)}</span>
                </div>
                <div class="info-item">
                    <i class="fas fa-bell"></i>
                    <span>${getTypeText(reminder.reminderType)}</span>
                </div>
                <div class="info-item">
                    <i class="fas fa-users"></i>
                    <span>${reminder.recipients ? reminder.recipients.length : 0} 人</span>
                </div>
            </div>
            <div class="reminder-content">
                <p>${reminder.content}</p>
            </div>
            <div class="reminder-actions">
                <button class="btn btn-outline-primary btn-sm" onclick="editReminder(${reminder.id})">
                    <i class="fas fa-edit"></i> 编辑
                </button>
                <button class="btn btn-outline-info btn-sm" onclick="viewReminder(${reminder.id})">
                    <i class="fas fa-eye"></i> 查看
                </button>
                ${reminder.status === 'PENDING' ? `
                    <button class="btn btn-outline-warning btn-sm" onclick="cancelReminder(${reminder.id})">
                        <i class="fas fa-times"></i> 取消
                    </button>
                ` : ''}
                <button class="btn btn-outline-danger btn-sm" onclick="deleteReminder(${reminder.id})">
                    <i class="fas fa-trash"></i> 删除
                </button>
            </div>
        </div>
    `).join('');
    
    remindersList.innerHTML = remindersHtml;
}

// 更新提醒统计
function updateReminderStats(reminders) {
    console.log('📊 updateReminderStats被调用，参数:', reminders);
    
    // 🔥 关键：确保reminders是数组
    if (!Array.isArray(reminders)) {
        console.error('❌ updateReminderStats接收到非数组数据:', reminders);
        // 设置默认统计值
        Utils.safeSetText('totalReminders', 0);
        Utils.safeSetText('pendingReminders', 0);
        Utils.safeSetText('sentReminders', 0);
        Utils.safeSetText('cancelledReminders', 0);
        return;
    }
    
    const stats = {
        total: reminders.length,
        pending: reminders.filter(r => r.status === 'PENDING').length,
        sent: reminders.filter(r => r.status === 'SENT').length,
        cancelled: reminders.filter(r => r.status === 'CANCELLED').length
    };
    
    console.log('📊 计算的统计数据:', stats);
    
    Utils.safeSetText('totalReminders', stats.total);
    Utils.safeSetText('pendingReminders', stats.pending);
    Utils.safeSetText('sentReminders', stats.sent);
    Utils.safeSetText('cancelledReminders', stats.cancelled);
}

// 加载联系人列表
window.loadContacts = async function() {
    const contactsList = Utils.safeGetElement('contactsList');
    const totalContacts = Utils.safeGetElement('totalContacts');
    const phoneContacts = Utils.safeGetElement('phoneContacts');
    const wechatContacts = Utils.safeGetElement('wechatContacts');
    
    if (!contactsList) {
        console.warn('Contacts list element not found');
        return;
    }
    
    try {
        // 显示加载状态
        contactsList.innerHTML = `
            <div class="text-center py-4">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">加载中...</span>
                </div>
                <p class="mt-2 text-muted">正在加载联系人列表...</p>
            </div>
        `;
        
        if (CONFIG.DEV_MODE) {
            // 开发模式：使用模拟数据
            setTimeout(() => {
                const mockContacts = [
                    {
                        id: 1,
                        name: '张三',
                        phoneNumber: '13800138000',
                        wechatOpenId: null,
                        description: '同事',
                        tags: [
                            { id: 1, name: '工作', color: '#007bff' }
                        ]
                    },
                    {
                        id: 2,
                        name: '李四',
                        phoneNumber: '13900139000',
                        wechatOpenId: 'wx123456',
                        description: '朋友',
                        tags: [
                            { id: 2, name: '朋友', color: '#28a745' }
                        ]
                    }
                ];
                
                displayContacts(mockContacts);
                updateContactStats(mockContacts);
            }, 1000);
        } else {
            // 生产模式：调用后端API
            const { response, data } = await API.get('/contacts');
            
            console.log('📥 /contacts API响应:', {
                status: response.status,
                ok: response.ok,
                data: data
            });
            
            if (response.ok) {
                console.log('✅ API调用成功，开始处理数据...');
                console.log('📊 原始返回数据:', data);
                console.log('📊 数据类型:', typeof data);
                console.log('📊 是否为数组:', Array.isArray(data));
                
                // 🔥 智能数据格式处理
                let contacts = [];
                
                // 情况1：直接返回数组
                if (Array.isArray(data)) {
                    contacts = data;
                    console.log('✅ 直接使用数组数据');
                }
                // 情况2：标准格式 {success: true, data: [...]}
                else if (data && data.success && data.data) {
                    contacts = Array.isArray(data.data) ? data.data : [];
                    console.log('✅ 从标准格式获取数组数据');
                }
                // 情况3：其他对象格式
                else if (data && typeof data === 'object') {
                    // 尝试从常见字段获取数组
                    if (data.list && Array.isArray(data.list)) {
                        contacts = data.list;
                        console.log('✅ 从data.list获取数组数据');
                    } else if (data.content && Array.isArray(data.content)) {
                        contacts = data.content;
                        console.log('✅ 从data.content获取数组数据');
                    } else if (data.records && Array.isArray(data.records)) {
                        contacts = data.records;
                        console.log('✅ 从data.records获取数组数据');
                    } else {
                        // 如果是空对象{}，当作空数组处理
                        contacts = [];
                        console.log('⚠️ 返回空对象，当作空数组处理');
                    }
                }
                // 情况4：其他情况
                else {
                    contacts = [];
                    console.log('⚠️ 未知数据格式，使用空数组');
                }
                
                console.log('📊 最终处理的contacts:', contacts);
                console.log('📊 联系人数量:', contacts.length);
                displayContacts(contacts);
                updateContactStats(contacts);
            } else {
                console.error('❌ API调用失败:', {
                    status: response.status,
                    data: data
                });
                throw new Error(data.message || `API调用失败 (状态码: ${response.status})`);
            }
        }
    } catch (error) {
        console.error('加载联系人列表错误:', error);
        contactsList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-triangle"></i>
                <h5>加载失败</h5>
                <p class="text-muted">${error.message}</p>
                <button class="btn btn-primary" onclick="loadContacts()">重试</button>
            </div>
        `;
    }
};

// 显示联系人列表
function displayContacts(contacts) {
    console.log('🎨 displayContacts被调用，参数:', contacts);
    console.log('📊 参数类型:', typeof contacts);
    console.log('📊 是否为数组:', Array.isArray(contacts));
    
    const contactsList = Utils.safeGetElement('contactsList');
    if (!contactsList) {
        console.error('❌ contactsList元素未找到');
        return;
    }
    
    // 🔥 关键：确保contacts是数组
    if (!Array.isArray(contacts)) {
        console.error('❌ displayContacts接收到非数组数据:', contacts);
        contactsList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-triangle"></i>
                <h5>数据格式错误</h5>
                <p class="text-muted">接收到的数据格式不正确，请检查后端API返回格式</p>
                <button class="btn btn-primary" onclick="loadContacts()">重试</button>
            </div>
        `;
        return;
    }
    
    if (!contacts || contacts.length === 0) {
        console.log('📝 显示空状态（无联系人数据）');
        contactsList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-users"></i>
                <h5>暂无联系人</h5>
                <p class="text-muted">您还没有添加任何联系人</p>
                <button class="btn btn-primary" onclick="showAddContactModal()">添加联系人</button>
            </div>
        `;
        return;
    }
    
    console.log(`🎨 开始渲染 ${contacts.length} 个联系人项目`);
    const contactsHtml = contacts.map(contact => `
        <div class="contact-item">
            <div class="contact-header">
                <h5 class="contact-name">${contact.name}</h5>
            </div>
            <div class="contact-methods-display">
                ${contact.phoneNumber ? `
                    <span class="contact-method-badge phone">
                        <i class="fas fa-mobile-alt"></i> ${contact.phoneNumber}
                    </span>
                ` : ''}
                ${contact.wechatOpenId ? `
                    <span class="contact-method-badge wechat">
                        <i class="fab fa-weixin"></i> 微信
                    </span>
                ` : ''}
            </div>
            <div class="contact-tags">
                ${contact.tags ? contact.tags.map(tag => `
                    <span class="contact-tag" style="background-color: ${tag.color}">
                        ${tag.name}
                    </span>
                `).join('') : ''}
            </div>
            ${contact.description ? `
                <div class="contact-description">${contact.description}</div>
            ` : ''}
            <div class="contact-actions">
                <button class="btn btn-outline-primary btn-sm" onclick="editContact(${contact.id})">
                    <i class="fas fa-edit"></i> 编辑
                </button>
                <button class="btn btn-outline-info btn-sm" onclick="viewContact(${contact.id})">
                    <i class="fas fa-eye"></i> 查看
                </button>
                <button class="btn btn-outline-danger btn-sm" onclick="deleteContact(${contact.id})">
                    <i class="fas fa-trash"></i> 删除
                </button>
            </div>
        </div>
    `).join('');
    
    contactsList.innerHTML = contactsHtml;
}

// 更新联系人统计
function updateContactStats(contacts) {
    console.log('📊 updateContactStats被调用，参数:', contacts);
    
    // 🔥 关键：确保contacts是数组
    if (!Array.isArray(contacts)) {
        console.error('❌ updateContactStats接收到非数组数据:', contacts);
        // 设置默认统计值
        Utils.safeSetText('totalContacts', 0);
        Utils.safeSetText('phoneContacts', 0);
        Utils.safeSetText('wechatContacts', 0);
        return;
    }
    
    const stats = {
        total: contacts.length,
        phone: contacts.filter(c => c.phoneNumber).length,
        wechat: contacts.filter(c => c.wechatOpenId).length
    };
    
    console.log('📊 计算的联系人统计数据:', stats);
    
    Utils.safeSetText('totalContacts', stats.total);
    Utils.safeSetText('phoneContacts', stats.phone);
    Utils.safeSetText('wechatContacts', stats.wechat);
}

// 加载标签列表
window.loadTags = async function() {
    const tagsList = Utils.safeGetElement('tagsList');
    const totalTags = Utils.safeGetElement('totalTags');
    
    if (!tagsList) {
        console.warn('Tags list element not found');
        return;
    }
    
    try {
        // 显示加载状态
        tagsList.innerHTML = `
            <div class="text-center py-4">
                <div class="spinner-border spinner-border-sm" role="status">
                    <span class="visually-hidden">加载中...</span>
                </div>
            </div>
        `;
        
        if (CONFIG.DEV_MODE) {
            // 开发模式：使用模拟数据
            setTimeout(() => {
                const mockTags = [
                    {
                        id: 1,
                        name: '工作',
                        color: '#007bff',
                        description: '工作相关联系人',
                        contactCount: 5
                    },
                    {
                        id: 2,
                        name: '朋友',
                        color: '#28a745',
                        description: '朋友联系人',
                        contactCount: 3
                    },
                    {
                        id: 3,
                        name: '家人',
                        color: '#dc3545',
                        description: '家庭成员',
                        contactCount: 2
                    }
                ];
                
                displayTags(mockTags);
                updateTagStats(mockTags);
            }, 800);
        } else {
            // 生产模式：调用后端API
            const { response, data } = await API.get('/tags');
            
            console.log('📥 /tags API响应:', {
                status: response.status,
                ok: response.ok,
                data: data
            });
            
            if (response.ok) {
                console.log('✅ API调用成功，开始处理数据...');
                console.log('📊 原始返回数据:', data);
                console.log('📊 数据类型:', typeof data);
                console.log('📊 是否为数组:', Array.isArray(data));
                
                // 🔥 智能数据格式处理
                let tags = [];
                
                // 情况1：直接返回数组
                if (Array.isArray(data)) {
                    tags = data;
                    console.log('✅ 直接使用数组数据');
                }
                // 情况2：标准格式 {success: true, data: [...]}
                else if (data && data.success && data.data) {
                    tags = Array.isArray(data.data) ? data.data : [];
                    console.log('✅ 从标准格式获取数组数据');
                }
                // 情况3：其他对象格式
                else if (data && typeof data === 'object') {
                    // 尝试从常见字段获取数组
                    if (data.list && Array.isArray(data.list)) {
                        tags = data.list;
                        console.log('✅ 从data.list获取数组数据');
                    } else if (data.content && Array.isArray(data.content)) {
                        tags = data.content;
                        console.log('✅ 从data.content获取数组数据');
                    } else if (data.records && Array.isArray(data.records)) {
                        tags = data.records;
                        console.log('✅ 从data.records获取数组数据');
                    } else {
                        // 如果是空对象{}，当作空数组处理
                        tags = [];
                        console.log('⚠️ 返回空对象，当作空数组处理');
                    }
                }
                // 情况4：其他情况
                else {
                    tags = [];
                    console.log('⚠️ 未知数据格式，使用空数组');
                }
                
                console.log('📊 最终处理的tags:', tags);
                console.log('📊 标签数量:', tags.length);
                displayTags(tags);
                updateTagStats(tags);
            } else {
                console.error('❌ API调用失败:', {
                    status: response.status,
                    data: data
                });
                throw new Error(data.message || `API调用失败 (状态码: ${response.status})`);
            }
        }
    } catch (error) {
        console.error('加载标签列表错误:', error);
        tagsList.innerHTML = `
            <div class="text-center py-4">
                <i class="fas fa-exclamation-triangle text-warning"></i>
                <p class="text-muted small mt-2">加载失败: ${error.message}</p>
                <button class="btn btn-outline-primary btn-sm" onclick="loadTags()">重试</button>
            </div>
        `;
    }
};

// 显示标签列表
function displayTags(tags) {
    console.log('🎨 displayTags被调用，参数:', tags);
    console.log('📊 参数类型:', typeof tags);
    console.log('📊 是否为数组:', Array.isArray(tags));
    
    const tagsList = Utils.safeGetElement('tagsList');
    if (!tagsList) {
        console.error('❌ tagsList元素未找到');
        return;
    }
    
    // 🔥 关键：确保tags是数组
    if (!Array.isArray(tags)) {
        console.error('❌ displayTags接收到非数组数据:', tags);
        tagsList.innerHTML = `
            <div class="text-center py-4">
                <i class="fas fa-exclamation-triangle text-warning"></i>
                <p class="text-muted small mt-2">数据格式错误</p>
                <button class="btn btn-outline-primary btn-sm" onclick="loadTags()">重试</button>
            </div>
        `;
        return;
    }
    
    if (!tags || tags.length === 0) {
        console.log('📝 显示空状态（无标签数据）');
        tagsList.innerHTML = `
            <div class="text-center py-4">
                <i class="fas fa-tags text-muted"></i>
                <p class="text-muted small mt-2">暂无标签</p>
                <button class="btn btn-outline-primary btn-sm" onclick="showAddTagModal()">添加标签</button>
            </div>
        `;
        return;
    }
    
    const tagsHtml = tags.map(tag => `
        <div class="tag-item">
            <div class="tag-info">
                <div class="tag-color" style="background-color: ${tag.color}"></div>
                <div>
                    <div class="tag-name">${tag.name}</div>
                    <span class="tag-count">${tag.contactCount || 0}</span>
                </div>
            </div>
            <div class="tag-actions">
                <button class="btn btn-outline-primary btn-sm" onclick="editTag(${tag.id})" title="编辑">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-outline-danger btn-sm" onclick="deleteTag(${tag.id})" title="删除">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `).join('');
    
    tagsList.innerHTML = tagsHtml;
}

// 更新标签统计
function updateTagStats(tags) {
    console.log('📊 updateTagStats被调用，参数:', tags);
    
    // 🔥 关键：确保tags是数组
    if (!Array.isArray(tags)) {
        console.error('❌ updateTagStats接收到非数组数据:', tags);
        // 设置默认统计值
        Utils.safeSetText('totalTags', 0);
        return;
    }
    
    console.log('📊 计算的标签统计数据:', { total: tags.length });
    Utils.safeSetText('totalTags', tags.length);
}

// 工具函数
function getStatusText(status) {
    const statusMap = {
        'PENDING': '待发送',
        'SENT': '已发送',
        'CANCELLED': '已取消'
    };
    return statusMap[status] || status;
}

function getTypeText(type) {
    const typeMap = {
        'SMS': '短信',
        'WECHAT': '微信',
        'PHONE': '电话'
    };
    return typeMap[type] || type;
}

// ==================== 创建提醒功能 ====================
window.showCreateReminderModal = function() {
    const modal = new bootstrap.Modal(document.getElementById('createReminderModal'));
    resetCreateReminderForm();
    loadContactsForSelection();
    modal.show();
};

function resetCreateReminderForm() {
    document.getElementById('createReminderForm').reset();
    document.getElementById('recipientsList').innerHTML = '';
    
    // 设置默认提醒时间为1小时后
    const now = new Date();
    now.setHours(now.getHours() + 1);
    const formattedTime = now.toISOString().slice(0, 16);
    document.getElementById('reminderDateTime').value = formattedTime;
}

async function loadContactsForSelection() {
    try {
        const { response, data } = await API.get('/contacts/all');
        
        console.log('📥 加载联系人选择列表响应:', { response: response.status, data });
        
        if (response.ok) {
            // 🔥 智能数据格式处理 - 与loadContacts保持一致
            let contacts = [];
            
            // 情况1：直接返回数组
            if (Array.isArray(data)) {
                contacts = data;
                console.log('✅ 直接使用数组数据');
            }
            // 情况2：标准格式 {success: true, data: [...]}
            else if (data && data.success && data.data) {
                contacts = Array.isArray(data.data) ? data.data : [];
                console.log('✅ 从标准格式获取数组数据');
            }
            // 情况3：其他对象格式
            else if (data && typeof data === 'object') {
                // 尝试从常见字段获取数组
                if (data.list && Array.isArray(data.list)) {
                    contacts = data.list;
                    console.log('✅ 从data.list获取数组数据');
                } else if (data.content && Array.isArray(data.content)) {
                    contacts = data.content;
                    console.log('✅ 从data.content获取数组数据');
                } else if (data.records && Array.isArray(data.records)) {
                    contacts = data.records;
                    console.log('✅ 从data.records获取数组数据');
                } else {
                    // 如果是空对象{}，当作空数组处理
                    contacts = [];
                    console.log('⚠️ 返回空对象，当作空数组处理');
                }
            }
            // 情况4：其他情况
            else {
                contacts = [];
                console.log('⚠️ 未知数据格式，使用空数组');
            }
            
            // 🔒 安全地设置全局变量，避免严格模式错误
            if (typeof window !== 'undefined') {
                window.availableContacts = contacts.slice(); // 创建数组副本，避免引用问题
            }
            
            console.log('📊 最终可用联系人:', contacts.length, '个');
        } else {
            console.warn('⚠️ 加载联系人失败 - HTTP状态:', response.status);
            if (typeof window !== 'undefined') {
                window.availableContacts = [];
            }
        }
    } catch (error) {
        console.error('❌ 加载联系人出错:', error);
        // 🔒 安全地设置默认值
        if (typeof window !== 'undefined') {
            window.availableContacts = [];
        }
    }
}

window.createReminder = async function() {
    const form = document.getElementById('createReminderForm');
    const submitBtn = form.querySelector('button[onclick="createReminder()"]');
    
    try {
        UIController.setButtonLoading(submitBtn, true);
        
        // 收集表单数据
        const title = document.getElementById('reminderTitle').value.trim();
        const content = document.getElementById('reminderContent').value.trim();
        const reminderDateTime = document.getElementById('reminderDateTime').value;
        const repeatType = document.getElementById('reminderRepeat').value;
        
        // 收集提醒类型（多选）
        const reminderTypes = [];
        document.querySelectorAll('input[name="reminderType"]:checked').forEach(checkbox => {
            reminderTypes.push(checkbox.value);
        });
        
        // 收集接收者
        const recipients = [];
        document.querySelectorAll('.recipient-item').forEach(item => {
            const type = item.dataset.type;
            const value = item.dataset.value;
            if (type && value) {
                recipients.push({
                    recipientType: type,
                    recipientValue: value
                });
            }
        });
        
        // 验证表单
        if (!title) {
            throw new Error('请输入提醒标题');
        }
        if (!reminderDateTime) {
            throw new Error('请选择提醒时间');
        }
        if (reminderTypes.length === 0) {
            throw new Error('请至少选择一种提醒类型');
        }
        if (recipients.length === 0) {
            throw new Error('请至少添加一个接收者');
        }
        
        // 准备请求数据
        const requestData = {
            title,
            content,
            reminderTime: reminderDateTime,
            reminderTypes,
            repeatType,
            recipients
        };
        
        console.log('📤 提交提醒数据:', requestData);
        
        // 发送请求
        const { response, data } = await API.post('/reminders', requestData);
        
        if (response.ok && data.success) {
            MessageSystem.show('提醒创建成功！', 'success');
            bootstrap.Modal.getInstance(document.getElementById('createReminderModal')).hide();
            
            // 刷新提醒列表
            if (typeof loadReminders === 'function') {
                loadReminders();
            }
        } else {
            throw new Error(data.message || '创建提醒失败');
        }
        
    } catch (error) {
        console.error('❌ 创建提醒失败:', error);
        MessageSystem.show(`创建提醒失败: ${error.message}`, 'error');
    } finally {
        UIController.setButtonLoading(submitBtn, false);
    }
};

// ==================== 创建联系人功能 ====================
window.showAddContactModal = function() {
    const modal = new bootstrap.Modal(document.getElementById('addContactModal'));
    resetAddContactForm();
    loadTagsForSelection();
    modal.show();
};

function resetAddContactForm() {
    document.getElementById('addContactForm').reset();
    document.getElementById('selectedTags').innerHTML = '';
    document.getElementById('contactMethodSMS').checked = false;
    document.getElementById('contactMethodWeChat').checked = false;
    
    // 隐藏方法输入框
    document.getElementById('phoneNumberGroup').style.display = 'none';
    document.getElementById('wechatGroup').style.display = 'none';
}

async function loadTagsForSelection() {
    try {
        const { response, data } = await API.get('/tags');
        if (response.ok && data.success) {
            window.availableTags = Array.isArray(data.data) ? data.data : [];
            console.log('📊 可用标签:', window.availableTags);
        } else {
            window.availableTags = [];
            console.warn('⚠️ 加载标签失败');
        }
    } catch (error) {
        console.error('❌ 加载标签出错:', error);
        window.availableTags = [];
    }
}

window.saveContact = async function() {
    const form = document.getElementById('addContactForm');
    const submitBtn = form.querySelector('button[onclick="saveContact()"]');
    
    try {
        UIController.setButtonLoading(submitBtn, true);
        
        // 收集表单数据
        const name = document.getElementById('contactName').value.trim();
        const phoneNumber = document.getElementById('contactPhone').value.trim();
        const wechatOpenid = document.getElementById('contactWechat').value.trim();
        
        // 收集选中的标签ID
        const tagIds = [];
        document.querySelectorAll('.selected-tag').forEach(tag => {
            const tagId = tag.dataset.tagId;
            if (tagId) {
                tagIds.push(parseInt(tagId));
            }
        });
        
        // 验证表单
        if (!name) {
            throw new Error('请输入联系人姓名');
        }
        
        const isSMSSelected = document.getElementById('contactMethodSMS').checked;
        const isWeChatSelected = document.getElementById('contactMethodWeChat').checked;
        
        if (isSMSSelected && !phoneNumber) {
            throw new Error('选择短信方式时，手机号不能为空');
        }
        if (isWeChatSelected && !wechatOpenid) {
            throw new Error('选择微信方式时，微信号不能为空');
        }
        if (!isSMSSelected && !isWeChatSelected) {
            throw new Error('请至少选择一种联系方式');
        }
        
        // 准备请求数据
        const requestData = {
            name,
            phoneNumber: isSMSSelected ? phoneNumber : null,
            wechatOpenid: isWeChatSelected ? wechatOpenid : null,
            tagIds
        };
        
        console.log('📤 提交联系人数据:', requestData);
        
        // 发送请求
        const { response, data } = await API.post('/contacts', requestData);
        
        // 🔍 详细调试信息
        console.log('📥 创建联系人API响应:', {
            status: response.status,
            ok: response.ok,
            data: data,
            dataType: typeof data,
            hasSuccess: data?.hasOwnProperty('success'),
            successValue: data?.success
        });
        
        if (response.ok && data && data.success === true) {
            console.log('✅ 联系人创建成功判断通过');
            MessageSystem.show('联系人创建成功！', 'success');
            bootstrap.Modal.getInstance(document.getElementById('addContactModal')).hide();
            
            // 刷新联系人列表
            if (typeof loadContacts === 'function') {
                loadContacts();
            }
        } else {
            console.error('❌ 联系人创建失败判断:', {
                responseOk: response.ok,
                dataExists: !!data,
                dataSuccess: data?.success,
                dataMessage: data?.message
            });
            throw new Error(data?.message || '创建联系人失败');
        }
        
    } catch (error) {
        console.error('❌ 创建联系人失败:', error);
        MessageSystem.show(`创建联系人失败: ${error.message}`, 'error');
    } finally {
        UIController.setButtonLoading(submitBtn, false);
    }
};

// ==================== 创建标签功能 ====================
window.showAddTagModal = function() {
    const modal = new bootstrap.Modal(document.getElementById('addTagModal'));
    resetAddTagForm();
    modal.show();
};

function resetAddTagForm() {
    document.getElementById('addTagForm').reset();
    document.getElementById('tagColor').value = '#007bff';
}

window.saveTag = async function() {
    const form = document.getElementById('addTagForm');
    const submitBtn = form.querySelector('button[onclick="saveTag()"]');
    
    try {
        UIController.setButtonLoading(submitBtn, true);
        
        // 收集表单数据
        const name = document.getElementById('tagName').value.trim();
        const color = document.getElementById('tagColor').value;
        
        // 验证表单
        if (!name) {
            throw new Error('请输入标签名称');
        }
        
        // 准备请求数据
        const requestData = {
            name,
            color
        };
        
        console.log('📤 提交标签数据:', requestData);
        
        // 发送请求
        const { response, data } = await API.post('/tags', requestData);
        
        if (response.ok && data.success) {
            MessageSystem.show('标签创建成功！', 'success');
            bootstrap.Modal.getInstance(document.getElementById('addTagModal')).hide();
            
            // 刷新标签列表
            if (typeof loadTags === 'function') {
                loadTags();
            }
        } else {
            throw new Error(data.message || '创建标签失败');
        }
        
    } catch (error) {
        console.error('❌ 创建标签失败:', error);
        MessageSystem.show(`创建标签失败: ${error.message}`, 'error');
    } finally {
        UIController.setButtonLoading(submitBtn, false);
    }
};

window.editReminder = function(id) {
    MessageSystem.show(`编辑提醒 #${id} 功能开发中...`, 'info');
};

window.viewReminder = function(id) {
    MessageSystem.show(`查看提醒 #${id} 功能开发中...`, 'info');
};

window.cancelReminder = function(id) {
    MessageSystem.show(`取消提醒 #${id} 功能开发中...`, 'info');
};

window.deleteReminder = function(id) {
    if (confirm('确定要删除这个提醒吗？')) {
        MessageSystem.show(`删除提醒 #${id} 功能开发中...`, 'info');
    }
};

window.editContact = function(id) {
    MessageSystem.show(`编辑联系人 #${id} 功能开发中...`, 'info');
};

window.viewContact = function(id) {
    MessageSystem.show(`查看联系人 #${id} 功能开发中...`, 'info');
};

window.deleteContact = function(id) {
    if (confirm('确定要删除这个联系人吗？')) {
        MessageSystem.show(`删除联系人 #${id} 功能开发中...`, 'info');
    }
};

window.editTag = function(id) {
    MessageSystem.show(`编辑标签 #${id} 功能开发中...`, 'info');
};

window.deleteTag = function(id) {
    if (confirm('确定要删除这个标签吗？')) {
        MessageSystem.show(`删除标签 #${id} 功能开发中...`, 'info');
    }
};

window.filterReminders = function() {
    MessageSystem.show('筛选功能开发中...', 'info');
};

window.filterContacts = function() {
    MessageSystem.show('筛选功能开发中...', 'info');
};

// 加载个人资料详情（用于profile.html页面）
window.loadUserProfile = async function() {
    const loadingSpinner = Utils.safeGetElement('loadingSpinner');
    const errorMessage = Utils.safeGetElement('errorMessage');
    const profileContent = Utils.safeGetElement('profileContent');
    
    if (!loadingSpinner || !profileContent) {
        console.warn('Profile elements not found');
        return;
    }
    
    try {
        // 显示加载状态
        loadingSpinner.style.display = 'flex';
        if (errorMessage) errorMessage.style.display = 'none';
        profileContent.style.display = 'none';
        
        if (CONFIG.DEV_MODE) {
            // 开发模式：使用模拟数据
            setTimeout(() => {
                const mockProfile = {
                    username: '测试用户',
                    phoneNumber: '13800138000',
                    email: 'test@example.com',
                    fullName: '张三',
                    role: 'USER',
                    createdAt: '2024-01-01T00:00:00',
                    updatedAt: '2024-01-14T12:00:00'
                };
                
                const mockStats = {
                    totalReminders: 12,
                    totalContacts: 8,
                    totalTags: 4
                };
                
                displayUserProfile(mockProfile, mockStats);
                loadingSpinner.style.display = 'none';
                profileContent.style.display = 'block';
            }, 1000);
        } else {
            // 生产模式：调用后端API
            const [profileResponse, statsResponse] = await Promise.all([
                API.get('/user/profile'),
                API.get('/user/stats')
            ]);
            
            if (profileResponse.response.ok && profileResponse.data.success) {
                const stats = statsResponse.response.ok && statsResponse.data.success 
                    ? statsResponse.data.data 
                    : { totalReminders: 0, totalContacts: 0, totalTags: 0 };
                
                displayUserProfile(profileResponse.data.data, stats);
                loadingSpinner.style.display = 'none';
                profileContent.style.display = 'block';
            } else {
                throw new Error(profileResponse.data.message || '加载个人资料失败');
            }
        }
    } catch (error) {
        console.error('加载个人资料错误:', error);
        loadingSpinner.style.display = 'none';
        if (errorMessage) {
            errorMessage.textContent = error.message;
            errorMessage.style.display = 'block';
        }
        
        if (error.message.includes('401') || error.message.includes('未授权')) {
            setTimeout(() => {
                AuthSystem.logout();
            }, 2000);
        }
    }
};

// 显示个人资料
function displayUserProfile(profile, stats) {
    // 更新头部信息
    Utils.safeSetText('profileName', profile.fullName || profile.username || '用户');
    Utils.safeSetText('profileRole', getRoleText(profile.role));
    
    // 更新统计信息
    Utils.safeSetText('totalReminders', stats.totalReminders || 0);
    Utils.safeSetText('totalContacts', stats.totalContacts || 0);
    Utils.safeSetText('totalTags', stats.totalTags || 0);
    
    // 更新详细信息
    Utils.safeSetText('username', profile.username || '未设置');
    Utils.safeSetText('phoneNumber', profile.phoneNumber || '未设置');
    Utils.safeSetText('email', profile.email || '未设置');
    Utils.safeSetText('role', getRoleText(profile.role));
    Utils.safeSetText('createdAt', profile.createdAt ? Utils.formatDateTime(profile.createdAt) : '未知');
    Utils.safeSetText('updatedAt', profile.updatedAt ? Utils.formatDateTime(profile.updatedAt) : '未知');
}

// 获取角色文本
function getRoleText(role) {
    const roleMap = {
        'ADMIN': '管理员',
        'USER': '普通用户',
        'VIP': 'VIP用户'
    };
    return roleMap[role] || role || '普通用户';
}

// 编辑字段
window.editField = function(fieldName) {
    const editForm = Utils.safeGetElement('editForm');
    const editFormTitle = Utils.safeGetElement('editFormTitle');
    const editFieldLabel = Utils.safeGetElement('editFieldLabel');
    const editField = Utils.safeGetElement('editField');
    
    if (!editForm || !editFormTitle || !editFieldLabel || !editField) {
        MessageSystem.show('编辑表单元素未找到', 'error');
        return;
    }
    
    const fieldLabels = {
        'username': '用户名',
        'phoneNumber': '手机号',
        'email': '邮箱地址'
    };
    
    const currentValue = Utils.safeGetElement(fieldName)?.textContent || '';
    
    editFormTitle.textContent = `编辑${fieldLabels[fieldName]}`;
    editFieldLabel.textContent = fieldLabels[fieldName];
    editField.value = currentValue === '未设置' ? '' : currentValue;
    editField.dataset.fieldName = fieldName;
    
    editForm.classList.add('show');
    editField.focus();
};

// 取消编辑
window.cancelEdit = function() {
    const editForm = Utils.safeGetElement('editForm');
    if (editForm) {
        editForm.classList.remove('show');
    }
};

// 处理编辑表单提交
window.handleProfileEdit = async function(event) {
    event.preventDefault();
    
    const editField = Utils.safeGetElement('editField');
    const fieldName = editField?.dataset.fieldName;
    const newValue = editField?.value.trim();
    
    if (!fieldName || !newValue) {
        MessageSystem.show('请输入有效的值', 'error');
        return;
    }
    
    try {
        if (CONFIG.DEV_MODE) {
            // 开发模式：模拟更新
            setTimeout(() => {
                Utils.safeSetText(fieldName, newValue);
                MessageSystem.show('更新成功！', 'success');
                cancelEdit();
            }, 500);
        } else {
            // 生产模式：调用后端API
            const updateData = {};
            updateData[fieldName] = newValue;
            
            const { response, data } = await API.put('/user/profile', updateData);
            
            if (response.ok && data.success) {
                Utils.safeSetText(fieldName, newValue);
                MessageSystem.show('更新成功！', 'success');
                cancelEdit();
                
                // 如果更新的是用户名，同时更新头部显示
                if (fieldName === 'username') {
                    Utils.safeSetText('profileName', newValue);
                }
            } else {
                throw new Error(data.message || '更新失败');
            }
        }
    } catch (error) {
        console.error('更新个人资料错误:', error);
        MessageSystem.show(error.message, 'error');
    }
};

// ==================== 界面交互辅助函数 ====================
window.toggleContactMethod = function(method) {
    if (method === 'phone') {
        const checkbox = document.getElementById('contactMethodSMS');
        const group = document.getElementById('phoneNumberGroup');
        group.style.display = checkbox.checked ? 'block' : 'none';
    } else if (method === 'wechat') {
        const checkbox = document.getElementById('contactMethodWeChat');
        const group = document.getElementById('wechatGroup');
        group.style.display = checkbox.checked ? 'block' : 'none';
    }
};

// 添加接收者到提醒
window.addRecipient = function() {
    const recipientsList = document.getElementById('recipientsList');
    const recipientItem = document.createElement('div');
    recipientItem.className = 'recipient-item';
    recipientItem.innerHTML = `
        <div class="input-group mb-2">
            <select class="form-select" onchange="updateRecipientType(this)">
                <option value="">选择类型</option>
                <option value="PHONE">手机号</option>
                <option value="WECHAT">微信</option>
            </select>
            <input type="text" class="form-control" placeholder="请输入接收者信息">
            <button type="button" class="btn btn-outline-danger" onclick="removeRecipient(this)">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;
    recipientsList.appendChild(recipientItem);
};

window.removeRecipient = function(button) {
    const recipientItem = button.closest('.recipient-item');
    recipientItem.remove();
};

window.updateRecipientType = function(select) {
    const recipientItem = select.closest('.recipient-item');
    const input = recipientItem.querySelector('input');
    const type = select.value;
    
    recipientItem.dataset.type = type;
    
    if (type === 'PHONE') {
        input.placeholder = '请输入手机号';
        input.type = 'tel';
    } else if (type === 'WECHAT') {
        input.placeholder = '请输入微信OpenID';
        input.type = 'text';
    } else {
        input.placeholder = '请输入接收者信息';
        input.type = 'text';
    }
    
    // 当用户输入时，更新dataset
    input.oninput = function() {
        recipientItem.dataset.value = this.value;
    };
};

// 显示联系人选择器
window.showContactSelector = function() {
    const modal = new bootstrap.Modal(document.getElementById('contactSelectorModal'));
    loadContactsForSelector();
    modal.show();
};

async function loadContactsForSelector() {
    const contactList = document.getElementById('contactSelectorList');
    
    try {
        contactList.innerHTML = '<p class="text-center">加载中...</p>';
        
        const { response, data } = await API.get('/contacts/all');
        if (response.ok && data.success && Array.isArray(data.data)) {
            const contacts = data.data;
            
            if (contacts.length === 0) {
                contactList.innerHTML = '<p class="text-center text-muted">暂无联系人</p>';
                return;
            }
            
            const contactsHtml = contacts.map(contact => `
                <div class="contact-selector-item" data-contact-id="${contact.id}">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" id="contact-${contact.id}" 
                               onchange="toggleContactSelection(${contact.id}, '${contact.name}', '${contact.phoneNumber || ''}', '${contact.wechatOpenid || ''}')">
                        <label class="form-check-label" for="contact-${contact.id}">
                            <strong>${contact.name}</strong>
                            <div class="contact-methods-display">
                                ${contact.phoneNumber ? `<span class="badge bg-primary"><i class="fas fa-mobile-alt"></i> ${contact.phoneNumber}</span>` : ''}
                                ${contact.wechatOpenid ? `<span class="badge bg-success"><i class="fab fa-weixin"></i> 微信</span>` : ''}
                            </div>
                        </label>
                    </div>
                </div>
            `).join('');
            
            contactList.innerHTML = contactsHtml;
        } else {
            contactList.innerHTML = '<p class="text-center text-danger">加载联系人失败</p>';
        }
    } catch (error) {
        console.error('❌ 加载联系人失败:', error);
        contactList.innerHTML = '<p class="text-center text-danger">加载联系人失败</p>';
    }
}

window.toggleContactSelection = function(contactId, name, phoneNumber, wechatOpenid) {
    // 这个函数将在用户确认选择时使用，暂时存储选中状态
    const checkbox = document.getElementById(`contact-${contactId}`);
    checkbox.dataset.name = name;
    checkbox.dataset.phone = phoneNumber;
    checkbox.dataset.wechat = wechatOpenid;
};

window.confirmContactSelection = function() {
    const selectedContacts = document.querySelectorAll('#contactSelectorList input[type="checkbox"]:checked');
    const recipientsList = document.getElementById('recipientsList');
    
    selectedContacts.forEach(checkbox => {
        const name = checkbox.dataset.name;
        const phone = checkbox.dataset.phone;
        const wechat = checkbox.dataset.wechat;
        
        // 添加手机号接收者
        if (phone) {
            addRecipientFromData('PHONE', phone, name);
        }
        
        // 添加微信接收者
        if (wechat) {
            addRecipientFromData('WECHAT', wechat, name);
        }
    });
    
    // 关闭模态框
    bootstrap.Modal.getInstance(document.getElementById('contactSelectorModal')).hide();
};

function addRecipientFromData(type, value, displayName) {
    const recipientsList = document.getElementById('recipientsList');
    const recipientItem = document.createElement('div');
    recipientItem.className = 'recipient-item';
    recipientItem.dataset.type = type;
    recipientItem.dataset.value = value;
    
    const typeText = type === 'PHONE' ? '手机号' : '微信';
    const icon = type === 'PHONE' ? 'fas fa-mobile-alt' : 'fab fa-weixin';
    
    recipientItem.innerHTML = `
        <div class="recipient-display">
            <div class="recipient-info">
                <i class="${icon}"></i>
                <span class="recipient-name">${displayName}</span>
                <span class="recipient-type">(${typeText}: ${value})</span>
            </div>
            <button type="button" class="btn btn-outline-danger btn-sm" onclick="removeRecipient(this)">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;
    
    recipientsList.appendChild(recipientItem);
}

// 标签选择相关函数
window.showTagSelector = function() {
    const modal = new bootstrap.Modal(document.getElementById('tagSelectorModal'));
    loadTagsForSelector();
    modal.show();
};

async function loadTagsForSelector() {
    const tagList = document.getElementById('tagSelectorList');
    
    try {
        tagList.innerHTML = '<p class="text-center">加载中...</p>';
        
        const { response, data } = await API.get('/tags');
        if (response.ok && data.success && Array.isArray(data.data)) {
            const tags = data.data;
            
            if (tags.length === 0) {
                tagList.innerHTML = '<p class="text-center text-muted">暂无标签</p>';
                return;
            }
            
            const tagsHtml = tags.map(tag => `
                <div class="tag-selector-item" data-tag-id="${tag.id}">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" id="tag-${tag.id}" 
                               data-tag-name="${tag.name}" data-tag-color="${tag.color}">
                        <label class="form-check-label" for="tag-${tag.id}">
                            <span class="tag-preview" style="background-color: ${tag.color}">
                                ${tag.name}
                            </span>
                        </label>
                    </div>
                </div>
            `).join('');
            
            tagList.innerHTML = tagsHtml;
        } else {
            tagList.innerHTML = '<p class="text-center text-danger">加载标签失败</p>';
        }
    } catch (error) {
        console.error('❌ 加载标签失败:', error);
        tagList.innerHTML = '<p class="text-center text-danger">加载标签失败</p>';
    }
}

window.confirmTagSelection = function() {
    const selectedTags = document.querySelectorAll('#tagSelectorList input[type="checkbox"]:checked');
    const selectedTagsContainer = document.getElementById('selectedTags');
    
    // 清除之前的选择
    selectedTagsContainer.innerHTML = '';
    
    selectedTags.forEach(checkbox => {
        const tagId = checkbox.id.replace('tag-', '');
        const tagName = checkbox.dataset.tagName;
        const tagColor = checkbox.dataset.tagColor;
        
        const tagElement = document.createElement('span');
        tagElement.className = 'selected-tag';
        tagElement.dataset.tagId = tagId;
        tagElement.style.backgroundColor = tagColor;
        tagElement.innerHTML = `
            ${tagName}
            <button type="button" class="tag-remove" onclick="removeSelectedTag(this)">
                <i class="fas fa-times"></i>
            </button>
        `;
        
        selectedTagsContainer.appendChild(tagElement);
    });
    
    // 关闭模态框
    bootstrap.Modal.getInstance(document.getElementById('tagSelectorModal')).hide();
};

window.removeSelectedTag = function(button) {
    const tagElement = button.closest('.selected-tag');
    tagElement.remove();
};

// ==================== 应用启动 ====================
document.addEventListener('DOMContentLoaded', App.init);