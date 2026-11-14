CREATE DATABASE IF NOT EXISTS auction_db;
USE auction_db;

CREATE TABLE IF NOT EXISTS items (
  item_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  start_price INT NOT NULL,
  min_increment INT NOT NULL,
  winner VARCHAR(50),
  final_price INT
);

CREATE TABLE IF NOT EXISTS bids (
  bid_id INT AUTO_INCREMENT PRIMARY KEY,
  item_id INT NOT NULL,
  bidder_name VARCHAR(50) NOT NULL,
  amount INT NOT NULL,
  placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (item_id) REFERENCES items(item_id)
);
