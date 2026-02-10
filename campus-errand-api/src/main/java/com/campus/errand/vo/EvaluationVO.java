package com.campus.errand.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EvaluationVO {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
    private Long toUserId;
    private String toUserName;
    private String toUserAvatar;
    private Integer rating;
    private String content;
    private List<String> tags;
    private Integer isAnonymous;
    private LocalDateTime createTime;
    
    // 扩展字段
    private String userTypeName;
    
    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }
    
    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }
    
    public void setFromUserAvatar(String fromUserAvatar) {
        this.fromUserAvatar = fromUserAvatar;
    }
    
    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }
    
    public void setToUserAvatar(String toUserAvatar) {
        this.toUserAvatar = toUserAvatar;
    }
    
    public void setUserTypeName(String userTypeName) {
        this.userTypeName = userTypeName;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
