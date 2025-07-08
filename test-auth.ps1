try {
    Write-Host "🔐 测试登录..." -ForegroundColor Yellow
    
    $loginData = @{
        username = "zhangsan"
        password = "123456"
    }
    
    $loginJson = $loginData | ConvertTo-Json
    Write-Host "发送登录请求: $loginJson" -ForegroundColor Gray
    
    # 使用Invoke-WebRequest来获取更详细的响应信息
    $webResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body $loginJson
    
    Write-Host "HTTP状态码: $($webResponse.StatusCode)" -ForegroundColor Gray
    Write-Host "响应内容: $($webResponse.Content)" -ForegroundColor Gray
    
    $response = $webResponse.Content | ConvertFrom-Json
    
    Write-Host "登录响应:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 10
    
    if ($response.success) {
        Write-Host "✅ 登录成功！" -ForegroundColor Green
        $token = $response.data.token
        Write-Host "Token: $($token.Substring(0, 50))..." -ForegroundColor Gray
        
        Write-Host "`n📋 测试认证接口..." -ForegroundColor Yellow
        
        $authHeaders = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        $authResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/reminders" -Method GET -Headers $authHeaders
        
        Write-Host "认证接口响应:" -ForegroundColor Green
        $authResponse | ConvertTo-Json -Depth 10
        
        Write-Host "✅ 认证接口调用成功！" -ForegroundColor Green
    } else {
        Write-Host "❌ 登录失败" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 错误: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "详细信息: $($_.Exception)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host "响应状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        Write-Host "响应状态描述: $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
        
        # 尝试读取错误响应内容
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorContent = $reader.ReadToEnd()
            Write-Host "错误响应内容: $errorContent" -ForegroundColor Red
        } catch {
            Write-Host "无法读取错误响应内容" -ForegroundColor Red
        }
    }
} 