package com.tracker.notification;

import com.tracker.notification.consumer.TaskEventConsumerTest;
import com.tracker.notification.consumer.TaskOverdueEventConsumerTest;
import com.tracker.notification.service.EmailServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        EmailServiceTest.class,
        TaskEventConsumerTest.class,
        TaskOverdueEventConsumerTest.class
})
public class NotificationServiceUnitTests {
}