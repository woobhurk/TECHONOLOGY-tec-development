/*
 Navicat Premium Data Transfer

 Source Server         : local-dev-localhost
 Source Server Type    : MySQL
 Source Server Version : 100602
 Source Host           : localhost:3306
 Source Schema         : stu-sql-demo01

 Target Server Type    : MySQL
 Target Server Version : 100602
 File Encoding         : 65001

 Date: 29/06/2021 14:41:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '部门编码',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '部门名',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES (1, '1001', '苍空府');
INSERT INTO `department` VALUES (2, '1002', '碎尸堂');
INSERT INTO `department` VALUES (3, '2001', '金童殿');
INSERT INTO `department` VALUES (4, '2002', '冰蚕观');
INSERT INTO `department` VALUES (5, '2003', '坎水盟');
INSERT INTO `department` VALUES (6, '3001', '天元刹');
INSERT INTO `department` VALUES (7, '3002', '天元岛');

-- ----------------------------
-- Table structure for salary_level
-- ----------------------------
DROP TABLE IF EXISTS `salary_level`;
CREATE TABLE `salary_level`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `level` int NOT NULL COMMENT '薪资等级',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '等级名称',
  `salary` int NOT NULL COMMENT '薪水',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '薪水等级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of salary_level
-- ----------------------------
INSERT INTO `salary_level` VALUES (1, 1, '穷逼', 2000);
INSERT INTO `salary_level` VALUES (2, 2, '温饱', 4000);
INSERT INTO `salary_level` VALUES (3, 3, '小康', 6000);
INSERT INTO `salary_level` VALUES (4, 4, '富足', 8000);

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `student_no` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '学号',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '姓名',
  `sex` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '1' COMMENT '性别（1 男，2 女）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `u_school_no`(`student_no`) USING BTREE COMMENT '学号唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '学生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (1, '210001', '高兴昌', '1');
INSERT INTO `student` VALUES (2, '210002', '敖熙熙', '2');
INSERT INTO `student` VALUES (3, '210003', '公西新语', '2');
INSERT INTO `student` VALUES (4, '210004', '闵阳飙', '1');
INSERT INTO `student` VALUES (5, '210005', '卢孤萍', '2');
INSERT INTO `student` VALUES (6, '210006', '韩白亦', '2');
INSERT INTO `student` VALUES (7, '210007', '禄嘉瑞', '1');
INSERT INTO `student` VALUES (8, '210008', '钭绍辉', '1');
INSERT INTO `student` VALUES (9, '210009', '屈平良', '2');
INSERT INTO `student` VALUES (10, '210010', '郗咏志', '1');
INSERT INTO `student` VALUES (11, '210011', '燕思雁', '2');
INSERT INTO `student` VALUES (12, '210012', '马良吉', '1');
INSERT INTO `student` VALUES (13, '210013', '焦悠婉', '2');
INSERT INTO `student` VALUES (14, '210014', '越尔蓝', '2');
INSERT INTO `student` VALUES (15, '210015', '秋修诚', '1');

-- ----------------------------
-- Table structure for student_score
-- ----------------------------
DROP TABLE IF EXISTS `student_score`;
CREATE TABLE `student_score`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `student_id` int NOT NULL COMMENT '学生ID，关联student.id',
  `subject_id` int NOT NULL COMMENT '学科ID，关联subject.id',
  `term` datetime NOT NULL COMMENT '时间',
  `score` int NOT NULL COMMENT '分数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '学生成绩表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_score
-- ----------------------------
INSERT INTO `student_score` VALUES (1, 1, 2, '2021-06-29 11:27:47', 47);
INSERT INTO `student_score` VALUES (2, 2, 2, '2021-06-29 11:27:47', 67);
INSERT INTO `student_score` VALUES (3, 3, 1, '2021-06-29 11:27:47', 36);
INSERT INTO `student_score` VALUES (4, 4, 2, '2021-06-29 11:27:47', 90);
INSERT INTO `student_score` VALUES (5, 5, 3, '2021-06-29 11:27:47', 25);
INSERT INTO `student_score` VALUES (6, 6, 4, '2021-06-29 11:27:47', 72);
INSERT INTO `student_score` VALUES (7, 7, 4, '2021-06-29 11:27:47', 60);
INSERT INTO `student_score` VALUES (8, 8, 3, '2021-06-29 11:27:47', 88);
INSERT INTO `student_score` VALUES (9, 9, 1, '2021-06-29 11:27:47', 99);
INSERT INTO `student_score` VALUES (10, 10, 2, '2021-06-29 11:27:47', 56);
INSERT INTO `student_score` VALUES (11, 11, 3, '2021-06-29 11:27:47', 61);
INSERT INTO `student_score` VALUES (12, 12, 3, '2021-06-29 11:27:47', 83);
INSERT INTO `student_score` VALUES (13, 13, 4, '2021-06-29 11:27:47', 28);
INSERT INTO `student_score` VALUES (14, 14, 1, '2021-06-29 11:27:47', 66);
INSERT INTO `student_score` VALUES (15, 15, 1, '2021-06-29 11:27:47', 85);

-- ----------------------------
-- Table structure for subject
-- ----------------------------
DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '学科编码',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '学科名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '学科表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of subject
-- ----------------------------
INSERT INTO `subject` VALUES (1, 'C-100', '辟心步');
INSERT INTO `subject` VALUES (2, 'M-100', '醉爆');
INSERT INTO `subject` VALUES (3, 'E-100', '旋风解');
INSERT INTO `subject` VALUES (4, 'T-100', '神蛇佛腿');

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `teacher_no` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教工号',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教师姓名',
  `sex` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '性别（1 男，2 女）',
  `department_id` int NOT NULL COMMENT '部门ID，关联department.id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '教师表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (1, '440001', '从泽宇', '1', 6);
INSERT INTO `teacher` VALUES (2, '362001', '竺玉堂', '1', 3);
INSERT INTO `teacher` VALUES (3, '362002', '红珍瑞', '2', 6);
INSERT INTO `teacher` VALUES (4, '362003', '管雅志', '1', 3);
INSERT INTO `teacher` VALUES (5, '362004', '陈瑶岑', '2', 1);
INSERT INTO `teacher` VALUES (6, '362005', '管沛凝', '2', 6);
INSERT INTO `teacher` VALUES (7, '362006', '宣天逸', '1', 4);
INSERT INTO `teacher` VALUES (8, '362007', '华凌春', '2', 5);
INSERT INTO `teacher` VALUES (9, '362008', '皇甫博超', '1', 7);
INSERT INTO `teacher` VALUES (10, '362009', '羿鹏涛', '1', 2);

-- ----------------------------
-- Table structure for teacher_salary
-- ----------------------------
DROP TABLE IF EXISTS `teacher_salary`;
CREATE TABLE `teacher_salary`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `teacher_id` int NOT NULL COMMENT '教师ID，关联teacher.id',
  `salary` int NOT NULL COMMENT '工资',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '教师工资表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher_salary
-- ----------------------------
INSERT INTO `teacher_salary` VALUES (1, 1, 3000);
INSERT INTO `teacher_salary` VALUES (2, 2, 900);
INSERT INTO `teacher_salary` VALUES (3, 3, 5000);
INSERT INTO `teacher_salary` VALUES (4, 4, 3000);
INSERT INTO `teacher_salary` VALUES (5, 5, 7000);
INSERT INTO `teacher_salary` VALUES (6, 6, 6000);
INSERT INTO `teacher_salary` VALUES (7, 7, 8000);
INSERT INTO `teacher_salary` VALUES (8, 8, 9000);
INSERT INTO `teacher_salary` VALUES (9, 9, 4000);
INSERT INTO `teacher_salary` VALUES (10, 10, 10000);

SET FOREIGN_KEY_CHECKS = 1;
