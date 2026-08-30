package com.keepcalm.placementportal.service;

import com.keepcalm.placementportal.entity.User;
import com.keepcalm.placementportal.repository.UserRepository;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.security.PortalPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PortalPrincipal principal)) {
            throw new DomainException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required");
        }
        return userRepository.findById(principal.userId()).orElseThrow(() -> DomainException.notFound("User not found"));
    }

    /** Legacy controller compatibility. Authentication is still taken from the validated security context. */
    public User getCurrentUser(String ignoredAuthorizationHeader) {
        return getCurrentUser();
    }

    public PortalPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PortalPrincipal principal)) {
            throw new DomainException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required");
        }
        return principal;
    }
}
