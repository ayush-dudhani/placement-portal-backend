package com.keepcalm.placementportal.config;

import com.keepcalm.placementportal.security.PortalPrincipal;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("institutionAwareKeyGenerator")
public class InstitutionAwareKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();
        String scope = principal instanceof PortalPrincipal portal
                ? "institution=" + portal.institutionId() + ":role=" + portal.role()
                : "anonymous";
        StringBuilder key = new StringBuilder(scope).append(':').append(method.getName());
        for (Object param : params) {
            key.append(':').append(param == null ? "null" : param.toString());
        }
        return key.toString();
    }
}
