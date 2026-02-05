package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.common.Result;
import com.campus.errand.service.MessageService;
import com.campus.errand.util.UserContext;
import com.campus.errand.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "消息管理", description = "消息通知相关接口")
@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "获取消息列表")
    @GetMapping("/list")
    public Result<IPage<MessageVO>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getUserId();
        IPage<MessageVO> page = messageService.getUserMessages(userId, current, size);
        return Result.success(page);
    }

    @Operation(summary = "标记消息已读")
    @PostMapping("/read/{id}")
    public Result<Boolean> markAsRead(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        boolean success = messageService.markAsRead(id, userId);
        if (!success) {
            return Result.error("标记失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "标记所有消息已读")
    @PostMapping("/read-all")
    public Result<Integer> markAllAsRead() {
        Long userId = UserContext.getUserId();
        int count = messageService.markAllAsRead(userId);
        return Result.success(count);
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = UserContext.getUserId();
        int count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }
}
