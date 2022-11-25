
-- 代理抽点默认配置
DROP TABLE IF EXISTS `extract_points_config` ;
CREATE TABLE `extract_points_config` (
 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
 `game_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '游戏id',
 `game_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '游戏名称',
 `game_en_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '游戏名称(英文)',
 `platform` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '平台:wm,PG,CQ9',
 `rate` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '比例: 比例限制范围 0%~5%',
 `state` int NOT NULL DEFAULT 1 COMMENT '开关 1:启用 0:禁用',
 `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '创建人',
 `create_time` timestamp NULL default CURRENT_TIMESTAMP COMMENT '创建时间',
 `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '修改人',
 `update_time` timestamp NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 UNIQUE INDEX `uq_game`(`game_id`) USING BTREE COMMENT '游戏抽点配置唯一索引',
 PRIMARY KEY (`id`) USING BTREE
)ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic COMMENT '代理抽点默认配置';

INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('101','百家乐','Baccarat','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('102','龙虎','Dragon Tiger','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('103','轮盘','Roulette','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('104','骰宝','SicBo','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('105','牛牛','Niu Niu','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('106','三公','Three Face','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('107','番摊','Fantan','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('108','色碟','Se Die','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('110','鱼虾蟹','Fish-Prawn-Crab','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('111','炸金花','Golden Flower','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('112','温州牌九','Wenzhou Pai Gow','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('113','二八杠','Mahjong tiles','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('128','安達巴哈','AndarBahar','wm');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('PG','PG','PG','PG');
INSERT INTO extract_points_config(game_id, game_name, game_en_name, platform) VALUES('CQ9','CQ9','CQ9','CQ9');

-- 代理抽点配置表
DROP TABLE IF EXISTS `poxy_extract_points_config` ;
CREATE TABLE `poxy_extract_points_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `game_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '游戏id',
  `game_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '游戏名称',
  `game_en_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '游戏名称(英文)',
  `platform` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '平台:wm,PG,CQ9',
  `rate` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '比例: 比例限制范围 0%~5%',
  `state` int NOT NULL DEFAULT 0 COMMENT '开关 1:启用 0:禁用',
  `poxy_id` bigint NOT NULL COMMENT '基层代代理Id',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  UNIQUE INDEX `uq_game`(`game_id`, `poxy_id`) USING BTREE COMMENT '游戏抽点配置唯一索引',
  PRIMARY KEY (`id`) USING BTREE
)ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic COMMENT '代理抽点配置表';

-- 会员代理抽点配置表
DROP TABLE IF EXISTS `user_extract_points_config` ;
CREATE TABLE `user_extract_points_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `game_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '游戏id',
  `game_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '游戏名称',
  `game_en_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '游戏名称(英文)',
  `platform` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '平台:wm,PG,CQ9',
  `rate` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '比例: 比例限制范围 0%~5%',
  `state` int NOT NULL DEFAULT 0 COMMENT '开关 1:启用 0:禁用',
  `poxy_id` bigint NOT NULL COMMENT '基层代代理Id',
  `user_id` bigint NOT NULL COMMENT '玩家Id',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  UNIQUE INDEX `uq_game`(`game_id`, `poxy_id`, `user_id`) USING BTREE COMMENT '游戏抽点配置唯一索引',
  PRIMARY KEY (`id`) USING BTREE
)ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic COMMENT '会员代理抽点配置表';

-- 代理抽点记录表
DROP TABLE IF EXISTS `extract_points_change`;
CREATE TABLE `extract_points_change` (
 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
 `amount` decimal(19, 6) NOT NULL COMMENT '抽点金额',
 `game_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '游戏id',
 `game_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '游戏名称',
 `game_record_id` bigint NOT NULL COMMENT '游戏注单id',
 `platform` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '平台:wm,PG,CQ9',
 `rate` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '比例: 比例限制范围 0%~5%',
 `poxy_id` bigint NOT NULL COMMENT '代理Id',
 `user_id` bigint NOT NULL COMMENT '玩家Id',
 `valid_bet` decimal(19, 2) NULL DEFAULT NULL COMMENT '有效投注',
 `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '创建人',
 `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
 `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '修改人',
 `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
 UNIQUE INDEX `uq_game_record`(`game_record_id`, `platform`) USING BTREE COMMENT '注单id唯一索引',
 PRIMARY KEY (`id`) USING BTREE
)ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic COMMENT '代理抽点记录表';

-- 游戏记录表新增字段
ALTER TABLE `casino`.`game_record`
    ADD COLUMN `extract_status` int NOT NULL DEFAULT 0 COMMENT '抽点状态: 0: 否 1: 是' AFTER `wash_code_status`;

ALTER TABLE `casino`.`game_record`
    MODIFY COLUMN `extract_status` int NULL DEFAULT 0 COMMENT '抽点状态: 0: 否 1: 是' AFTER `wash_code_status`;

ALTER TABLE `casino`.`game_record_goldenf`
    ADD COLUMN `extract_status` int NOT NULL DEFAULT 0 COMMENT '抽点状态: 0: 否 1: 是' AFTER `wash_code_status`;

-- 首页报表新增字段
ALTER TABLE `casino`.`home_page_report`
    ADD COLUMN `extract_points_amount` decimal(15, 6) NOT NULL DEFAULT 0.00 COMMENT '抽点金额' AFTER `wash_code_amount`;

-- 佣金月结报表新增字段
ALTER TABLE `casino`.`company_proxy_month`
    ADD COLUMN `extract_points_amount` decimal(19, 6) NULL COMMENT '代理抽点金额' AFTER `user_id_temp`;

