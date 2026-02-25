package com.campus.errand.task;

import com.campus.errand.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskExpireScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TaskExpireScheduler.class);

    private final TaskService taskService;

    @Autowired
    public TaskExpireScheduler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkExpiredTasks() {
        logger.info("开始检查超时任务...");
        try {
            int count = taskService.processExpiredTasks();
            if (count > 0) {
                logger.info("处理了 {} 个超时任务", count);
            }
        } catch (Exception e) {
            logger.error("检查超时任务时发生异常", e);
        }
    }
}
