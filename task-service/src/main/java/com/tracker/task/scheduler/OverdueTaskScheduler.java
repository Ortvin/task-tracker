package com.tracker.task.scheduler;

import com.tracker.task.event.TaskOverdueEvent;
import com.tracker.task.kafka.TaskOverdueProducer;
import com.tracker.task.model.Person;
import com.tracker.task.model.Task;
import com.tracker.task.model.TaskParticipant;
import com.tracker.task.model.TaskStatus;
import com.tracker.task.repository.PersonRepository;
import com.tracker.task.repository.TaskParticipantRepository;
import com.tracker.task.repository.TaskRepository;
import com.tracker.task.service.UserContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueTaskScheduler {

    private final TaskRepository taskRepository;
    private final TaskOverdueProducer producer;
    private final UserContactService userContactService;
    private final TaskParticipantRepository taskParticipantRepository;
    private final PersonRepository personRepository;


    @Scheduled(cron = "0 0 8 * * ?") // каждый день в 8:00
  //  @Scheduled(fixedDelay = 30000)
    @Transactional(readOnly = true)
    public void checkOverdueTasks() {
        log.info("Checking overdue tasks...");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<Task> overdue = taskRepository.findByDatePlanFinalAndStatusNot(yesterday, "COMPLETED");

        for (Task task : overdue) {
            TaskParticipant author = taskParticipantRepository
                    .findByTaskIdAndRoleId(task.getId(), 1L)
                    .orElseThrow(() -> new RuntimeException("Author not found"));
            Long userId = author.getParticipant().getId();
            Person person = personRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Person not found"));
            String email = userContactService.getPrimaryEmail(person.getUserId());

            TaskOverdueEvent event = TaskOverdueEvent.builder()
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .deadline(task.getDatePlanFinal())
                    .userEmail(email)
                    .build();

            producer.sendOverdueEvent(event);
        }
    }
}