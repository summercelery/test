Write-Host "🔧 修复密码问题..." -ForegroundColor Yellow

try {
    # 测试BCrypt
    Write-Host "`n🧪 测试BCrypt..." -ForegroundColor Yellow
    $bcryptResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/test-bcrypt" -Method POST
    
    Write-Host "BCrypt测试响应:" -ForegroundColor Green
    $bcryptResponse | ConvertTo-Json -Depth 10
    
    if ($bcryptResponse.success) {
        Write-Host "✅ BCrypt工作正常" -ForegroundColor Green
        
        # 重置admin用户密码
        Write-Host "`n🔄 重置admin用户密码..." -ForegroundColor Yellow
        $resetResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/reset-user-password?username=admin" -Method POST
        
        Write-Host "admin密码重置响应:" -ForegroundColor Green
        $resetResponse | ConvertTo-Json -Depth 10
        
        if ($resetResponse.success) {
            Write-Host "✅ admin密码重置成功" -ForegroundColor Green
            
            # 验证重置后的密码
            Write-Host "`n🔍 验证重置后的admin密码..." -ForegroundColor Yellow
            $verifyResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/verify-password?username=admin&password=123456" -Method POST
            
            Write-Host "密码验证响应:" -ForegroundColor Green
            $verifyResponse | ConvertTo-Json -Depth 10
            
            if ($verifyResponse.success) {
                Write-Host "✅ admin密码验证成功！现在尝试登录..." -ForegroundColor Green
                
                # 尝试登录
                $loginData = @{
                    username = "admin"
                    password = "123456"
                }
                
                $loginJson = $loginData | ConvertTo-Json
                $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body $loginJson
                
                Write-Host "登录响应:" -ForegroundColor Green
                $loginResponse | ConvertTo-Json -Depth 10
                
                if ($loginResponse.success) {
                    Write-Host "🎉 登录成功！问题已解决！" -ForegroundColor Green
                } else {
                    Write-Host "❌ 登录仍然失败" -ForegroundColor Red
                }
            } else {
                Write-Host "❌ 密码验证仍然失败" -ForegroundColor Red
            }
        } else {
            Write-Host "❌ 密码重置失败" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ BCrypt测试失败" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ 错误: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "响应状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
} 