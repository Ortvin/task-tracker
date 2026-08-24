package com.tracker.task.config;

import com.tracker.task.model.ContactType;
import com.tracker.task.model.ParticipantRole;
import com.tracker.task.model.TaskStatus;
import com.tracker.task.model.TaskType;
import com.tracker.task.repository.ContactTypeRepository;
import com.tracker.task.repository.ParticipantRoleRepository;
import com.tracker.task.repository.TaskStatusRepository;
import com.tracker.task.repository.TaskTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TaskTypeRepository taskTypeRepository;
    private final ParticipantRoleRepository participantRoleRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final ContactTypeRepository contactTypeRepository;

    @Override
    public void run(String... args) {
        if (taskTypeRepository.count() == 0) {
            List<TaskType> taskTypes = List.of(
                    TaskType.builder().name("Epic").build(),
                    TaskType.builder().name("Feature").build(),
                    TaskType.builder().name("UserStory").build(),
                    TaskType.builder().name("Task").build()
            );
            taskTypeRepository.saveAll(taskTypes);
            System.out.println("✅ Task types initialized");
        }

        if (participantRoleRepository.count() == 0) {
            List<ParticipantRole> roles = List.of(
                    ParticipantRole.builder().name("Создатель").build(),
                    ParticipantRole.builder().name("Исполнитель").build(),
                    ParticipantRole.builder().name("Наблюдатель").build(),
                    ParticipantRole.builder().name("Заказчик").build(),
                    ParticipantRole.builder().name("Куратор").build()
            );
            participantRoleRepository.saveAll(roles);
            System.out.println("✅ Participant roles initialized");
        }

        if (taskStatusRepository.count() == 0) {
            List<TaskStatus> statuses = List.of(
                    TaskStatus.builder().code("PENDING").name("Ожидает").sortOrder(1).build(),
                    TaskStatus.builder().code("IN_PROGRESS").name("В работе").sortOrder(2).build(),
                    TaskStatus.builder().code("COMPLETED").name("Выполнена").sortOrder(3).build(),
                    TaskStatus.builder().code("OVERDUE").name("Просрочена").sortOrder(4).build()
            );
            taskStatusRepository.saveAll(statuses);
            System.out.println("✅ Task statuses initialized");
        }

        if (contactTypeRepository.count() == 0) {
            contactTypeRepository.saveAll(List.of(
                    ContactType.builder().code("EMAIL").name("Email").build(),
                    ContactType.builder().code("PHONE").name("Телефон").build(),
                    ContactType.builder().code("TELEGRAM").name("Telegram").build()
            ));
        }
    }
}