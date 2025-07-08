try {
    Write-Host "🔍 检查数据库连接和用户数据..." -ForegroundColor Yellow
    
    # 等待服务器启动
    Write-Host "等待服务器启动..." -ForegroundColor Gray
    Start-Sleep -Seconds 10
    
    # 测试获取所有用户
    Write-Host "`n📋 获取所有用户..." -ForegroundColor Yellow
    $usersResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/users" -Method GET
    
    Write-Host "用户列表响应:" -ForegroundColor Green
    $usersResponse | ConvertTo-Json -Depth 10
    
    if ($usersResponse.success -and $usersResponse.data.Count -gt 0) {
        Write-Host "✅ 找到 $($usersResponse.data.Count) 个用户" -ForegroundColor Green
        
        # 测试查找特定用户
        $testUsernames = @("admin", "zhangsan", "testuser")
        foreach ($username in $testUsernames) {
            Write-Host "`n🔍 检查用户: $username" -ForegroundColor Yellow
            try {
                $userResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/user/$username" -Method GET
                if ($userResponse.success) {
                    Write-Host "✅ 用户 $username 存在" -ForegroundColor Green
                    
                    # 测试密码验证
                    Write-Host "🔐 验证密码..." -ForegroundColor Yellow
                    $passwordResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/verify-password" -Method POST -Headers @{"Content-Type"="application/x-www-form-urlencoded"} -Body "username=$username`&password=123456"
                    
                    if ($passwordResponse.success) {
                        Write-Host "✅ 用户 $username 密码验证成功" -ForegroundColor Green
                    } else {
                        Write-Host "❌ 用户 $username 密码验证失败: $($passwordResponse.message)" -ForegroundColor Red
                    }
                } else {
                    Write-Host "❌ 用户 $username 不存在" -ForegroundColor Red
                }
            } catch {
                Write-Host "❌ 检查用户 $username 失败: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    } else {
        Write-Host "❌ 没有找到用户，尝试创建测试用户..." -ForegroundColor Red
        
        # 创建测试用户
        Write-Host "`n👤 创建测试用户..." -ForegroundColor Yellow
        $createResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/create-test-user" -Method POST
        
        Write-Host "创建用户响应:" -ForegroundColor Green
        $createResponse | ConvertTo-Json -Depth 10
        
        if ($createResponse.success) {
            Write-Host "✅ 测试用户创建成功" -ForegroundColor Green
        } else {
            Write-Host "❌ 测试用户创建失败: $($createResponse.message)" -ForegroundColor Red
        }
    }
    
} catch {
    Write-Host "❌ 错误: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "详细信息: $($_.Exception)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host "响应状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        Write-Host "响应状态描述: $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
    }
} 