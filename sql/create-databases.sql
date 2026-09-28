-- Chạy bằng tài khoản có quyền CREATE DATABASE trên SQL Server.
IF DB_ID(N'security_vd1') IS NULL EXEC(N'CREATE DATABASE security_vd1');
GO
IF DB_ID(N'security_vd2') IS NULL EXEC(N'CREATE DATABASE security_vd2');
GO
IF DB_ID(N'security_vd3') IS NULL EXEC(N'CREATE DATABASE security_vd3');
GO
-- JPA tự tạo bảng khi khởi động. DataInitializer tạo ROLE_USER/ROLE_ADMIN.
