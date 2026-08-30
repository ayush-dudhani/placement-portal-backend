package com.keepcalm.placementportal.security;

import com.keepcalm.placementportal.enums.Role;

public record PortalPrincipal(Long userId, Long institutionId, String username, Role role) {
}
