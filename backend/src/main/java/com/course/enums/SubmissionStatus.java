package com.course.enums;

/**
 * 作业提交状态枚举
 */
public enum SubmissionStatus {
    /**
     * 已提交
     */
    SUBMITTED("submitted", "已提交"),
    
    /**
     * 已批改
     */
    GRADED("graded", "已批改"),
    
    /**
     * 已退回
     */
    REJECTED("rejected", "已退回"),
    
    /**
     * 草稿
     */
    DRAFT("draft", "草稿");

    private final String code;
    private final String description;

    SubmissionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
