Write-Host "🔐 测试密码验证..." -ForegroundColor Yellow

try {
    # 测试zhangsan用户的密码
    Write-Host "`n🔍 验证zhangsan用户密码..." -ForegroundColor Yellow
    $passwordResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/verify-password?username=zhangsan&password=123456" -Method POST
    
    Write-Host "密码验证响应:" -ForegroundColor Green
    $passwordResponse | ConvertTo-Json -Depth 10
    
    # 测试admin用户的密码
    Write-Host "`n🔍 验证admin用户密码..." -ForegroundColor Yellow
    $adminPasswordResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/test/verify-password?username=admin&password=123456" -Method POST
    
    Write-Host "admin密码验证响应:" -ForegroundColor Green
    $adminPasswordResponse | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "❌ 错误: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "响应状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
} 