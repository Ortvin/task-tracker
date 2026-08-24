package com.tracker.task.service;

import com.tracker.task.model.UserContact;
import com.tracker.task.repository.UserContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserContactService {

    private final UserContactRepository userContactRepository;

    @Transactional(readOnly = true)
    public String getPrimaryEmail(Long userId) {
        return userContactRepository.findByUserIdAndIsPrimaryTrue(userId)
                .map(UserContact::getValue)
                .orElseThrow(() -> new RuntimeException("Primary email not found for user: " + userId));
    }
}