package com.keepcalm.placementportal.service;

import com.keepcalm.placementportal.entity.User;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.models.auth.CurrentUserDto;
import com.keepcalm.placementportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CurrentUserDto me() { return dto(currentUserService.getCurrentUser()); }

    @Transactional
    public CurrentUserDto update(CurrentUserDto.Update request) {
        User user = currentUserService.getCurrentUser();
        if (request.username() != null && !request.username().equalsIgnoreCase(user.getUsername())) {
            if (userRepository.existsByInstitutionIdAndUsernameIgnoreCase(user.getInstitution().getId(), request.username())) {
                throw DomainException.conflict("USERNAME_EXISTS", "Username is already in use");
            }
            user.setUsername(request.username().trim());
        }
        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByInstitutionIdAndEmailIgnoreCase(user.getInstitution().getId(), request.email())) {
                throw DomainException.conflict("EMAIL_EXISTS", "Email is already in use");
            }
            user.setEmail(request.email().trim().toLowerCase());
            user.setEmailVerified(false);
        }
        return dto(user);
    }

    private CurrentUserDto dto(User user) {
        return new CurrentUserDto(user.getId(), user.getInstitution().getId(), user.getInstitution().getCode(),
                user.getUsername(), user.getEmail(), user.getRole().name(), user.isEmailVerified());
    }
}
