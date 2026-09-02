package com.tracker.task;

import com.tracker.task.controller.TaskControllerIntegrationTest;
import com.tracker.task.repository.TaskRepositoryTest;
import com.tracker.task.service.TaskServiceIntegrationTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        TaskRepositoryTest.class,
        TaskServiceIntegrationTest.class,
        TaskControllerIntegrationTest.class
})
public class TaskServiceIntegrationTests {
}