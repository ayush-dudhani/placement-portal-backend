package com.keepcalm.placementportal.security;

import com.keepcalm.placementportal.entity.User;

public interface AccessSessionStore {
    void register(User user, String accessToken);
    boolean isActive(long institutionId, long userId, String jti);
    void revoke(String accessToken);
    void revokeAll(long institutionId, long userId);
}
