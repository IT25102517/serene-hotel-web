-- Run once in SQL Server Management Studio as a database administrator.
IF DB_ID(N'SereneWeddings') IS NULL CREATE DATABASE SereneWeddings;
GO
-- Configure a dedicated SQL login using your own password in SSMS.
-- Map it to SereneWeddings and grant db_ddladmin + db_datareader + db_datawriter
-- for this development milestone. Hibernate creates the tables on first startup.
-- Never commit passwords. Production should use versioned migrations and fewer permissions.
