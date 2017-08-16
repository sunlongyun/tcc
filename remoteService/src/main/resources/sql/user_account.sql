/*
Navicat MySQL Data Transfer

Source Server         : n1
Source Server Version : 50718
Source Host           : n1:3306
Source Database       : user_account

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-08-16 09:18:45
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userName` varchar(255) NOT NULL COMMENT '用户名',
  `amount` double(10,2) DEFAULT NULL COMMENT '可用金额',
  `freezeAmount` double(10,2) DEFAULT NULL COMMENT '冻结金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='个人账户';

-- ----------------------------
-- Records of account
-- ----------------------------
INSERT INTO `account` VALUES ('4', 'zhangsan', '199000.00', '0.00');

-- ----------------------------
-- Table structure for order_detail
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `orderNo` varchar(255) DEFAULT NULL COMMENT '订单号',
  `amount` double(10,2) DEFAULT NULL COMMENT '交易金额',
  `status` tinyint(1) DEFAULT '1' COMMENT '1-预处理；2-已处理；0-已撤销',
  `direction` tinyint(1) DEFAULT NULL COMMENT '1-流入；2-流出',
  `date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of order_detail
-- ----------------------------
INSERT INTO `order_detail` VALUES ('4', '1502788297452', '1000.00', '2', '2', '2017-08-15 02:11:20');
INSERT INTO `order_detail` VALUES ('12', '1502788621129', '1000.00', '0', '2', '2017-08-15 02:16:43');
INSERT INTO `order_detail` VALUES ('14', '1502790779908', '1000.00', '0', '2', '2017-08-15 02:52:42');
