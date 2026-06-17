CREATE DATABASE coldchain_db;
CREATE USER coldchain_user WITH PASSWORD 'coldchain_password';
GRANT ALL PRIVILEGES ON DATABASE coldchain_db TO coldchain_user;
\c coldchain_db
GRANT ALL ON SCHEMA public TO coldchain_user;
