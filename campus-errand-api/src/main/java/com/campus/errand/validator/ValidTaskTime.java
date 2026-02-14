package com.campus.errand.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTaskTimeValidator.class)
@Documented
public @interface ValidTaskTime {
    String message() default "时间验证失败";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
