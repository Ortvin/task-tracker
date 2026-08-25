package com.tracker.task;

import com.tracker.task.controller.TaskControllerTest;
import com.tracker.task.mapper.TaskMapperTest;
import com.tracker.task.service.TaskServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        TaskServiceTest.class,
        TaskMapperTest.class,
        TaskControllerTest.class
})
public class TaskServiceUnitTests {
}