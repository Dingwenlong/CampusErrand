package com.campus.errand.validator;

import com.campus.errand.dto.TaskPublishDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ValidTaskTimeValidator implements ConstraintValidator<ValidTaskTime, TaskPublishDTO> {

    @Value("${task.time.buffer-minutes:15}")
    private int bufferMinutes;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean isValid(TaskPublishDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectTime = dto.getExpectTime();
        LocalDateTime deadlineTime = dto.getDeadlineTime();

        if (expectTime != null) {
            if (!expectTime.isAfter(now)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("期望送达时间必须晚于当前时间")
                        .addPropertyNode("expectTime")
                        .addConstraintViolation();
                return false;
            }

            if (deadlineTime != null) {
                if (!deadlineTime.isAfter(now)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("截止时间必须晚于当前时间")
                            .addPropertyNode("deadlineTime")
                            .addConstraintViolation();
                    return false;
                }

                if (!deadlineTime.isBefore(expectTime)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("截止时间必须早于期望送达时间")
                            .addPropertyNode("deadlineTime")
                            .addConstraintViolation();
                    return false;
                }

                long diffMinutes = java.time.Duration.between(deadlineTime, expectTime).toMinutes();
                if (diffMinutes < bufferMinutes) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("截止时间与期望送达时间至少间隔" + bufferMinutes + "分钟")
                            .addPropertyNode("deadlineTime")
                            .addConstraintViolation();
                    return false;
                }
            }
        }

        return true;
    }
}
