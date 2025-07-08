Write-Host "🔍 检查数据库连接和用户数据..." -ForegroundColor Yellow

# 等待服务器启动
Write-Host "等待服务器启动..." -ForegroundColor Gray
Start-Sleep -Seconds 5

try {
    # 测试获取所有用户
    Write-Host "`n📋 获取所有用户..." -ForegroundColor Yellow
    $usersResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/users" -Method GET
    
    Write-Host "用户列表响应:" -ForegroundColor Green
    $usersResponse | ConvertTo-Json -Depth 10
    
    if ($usersResponse.success) {
        Write-Host "✅ 找到用户数据" -ForegroundColor Green
    } else {
        Write-Host "❌ 没有找到用户数据" -ForegroundColor Red
    }
    
    # 创建测试用户
    Write-Host "`n👤 创建测试用户..." -ForegroundColor Yellow
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/create-test-user" -Method POST
    
    Write-Host "创建用户响应:" -ForegroundColor Green
    $createResponse | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "❌ 错误: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "响应状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
} 