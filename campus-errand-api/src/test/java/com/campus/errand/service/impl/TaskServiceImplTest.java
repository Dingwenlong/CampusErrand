package com.campus.errand.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskServiceImplTest {

    @Test
    void resolveTaskListStatusShouldDefaultToPendingWhenNull() {
        assertThat(TaskServiceImpl.resolveTaskListStatus(null)).isEqualTo(0);
    }

    @Test
    void resolveTaskListStatusShouldUseProvidedStatus() {
        assertThat(TaskServiceImpl.resolveTaskListStatus(3)).isEqualTo(3);
    }
}
