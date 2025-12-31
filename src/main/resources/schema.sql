-- 用户表 (防止关键字冲突)
CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER'
);

-- 商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2),
    stock INT,
    image_url VARCHAR(255)
);

-- 订单表 (一定要有 product_id 和 create_time)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT,
    total_amount DECIMAL(10, 2),
    status VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始管理员
INSERT INTO `user` (username, password, role) SELECT 'admin', '123', 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'admin');

-- 初始商品 (MJ 5大精选)
INSERT INTO product (name, description, price, stock) SELECT 'Persona 3 Reload', 'PS5游戏光盘 - 重新装填你的命运', 399.00, 10 WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Persona 3 Reload');
INSERT INTO product (name, description, price, stock) SELECT 'MJ Fedora Hat', '经典的黑色软呢帽 - 顺滑犯罪', 999.00, 5 WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'MJ Fedora Hat');
INSERT INTO product (name, description, price, stock) SELECT 'Diamond Glove', '水钻手套 - 舞台的焦点', 5000.00, 3 WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Diamond Glove');
INSERT INTO product (name, description, price, stock) SELECT 'Moonwalker Shoes', '反重力舞鞋 - 只有你能驾驭', 1200.00, 8 WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Moonwalker Shoes');
INSERT INTO product (name, description, price, stock) SELECT 'Evoker', '召唤器 - 面对恐惧', 299.00, 20 WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Evoker');