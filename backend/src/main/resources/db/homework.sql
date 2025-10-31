-- 作业表
CREATE TABLE IF NOT EXISTS `assignment` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `title` varchar(255) NOT NULL COMMENT '作业标题',
    `description` text COMMENT '作业描述',
    `course_id` bigint(20) NOT NULL COMMENT '所属课程ID',
    `teacher_id` bigint(20) NOT NULL COMMENT '教师ID',
    `deadline` datetime NOT NULL COMMENT '截止日期',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待完成，SUBMITTED-已提交，OVERDUE-已逾期',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_assignment_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_assignment_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业表';
