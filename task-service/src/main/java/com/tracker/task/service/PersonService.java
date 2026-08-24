package com.tracker.task.service;

import com.tracker.task.model.ContactType;
import com.tracker.task.model.Person;
import com.tracker.task.model.UserContact;
import com.tracker.task.repository.ContactTypeRepository;
import com.tracker.task.repository.PersonRepository;
import com.tracker.task.repository.UserContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final UserContactRepository userContactRepository;
    private final ContactTypeRepository contactTypeRepository;

    @Transactional
    public Person createPersonFromRegistration(Long userId, String firstName, String middleName, String lastName, String email) {
        // Создаём Person
        Person person = new Person();
        person.setUserId(userId);
        person.setName(firstName);
        person.setMiddleName(middleName);
        person.setLastName(lastName);
        person.setIsJuridical(false);
        person = personRepository.save(person);

        // Находим тип контакта "EMAIL"
        ContactType emailType = contactTypeRepository.findByCode("EMAIL")
                .orElseThrow(() -> new RuntimeException("Contact type EMAIL not found"));

        // Создаём UserContact
        UserContact contact = UserContact.builder()
                .userId(userId)
                .contactType(emailType)
                .value(email)
                .isPrimary(true)
                .build();
        userContactRepository.save(contact);

        return person;
    }
}