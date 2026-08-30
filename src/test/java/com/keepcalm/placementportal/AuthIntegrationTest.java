package com.keepcalm.placementportal;

import com.keepcalm.placementportal.entity.Institution;
import com.keepcalm.placementportal.repository.InstitutionRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired InstitutionRepository institutions;

    @BeforeEach
    void institution() {
        if (institutions.findByCodeIgnoreCaseAndActiveTrue("DEFAULT").isEmpty()) {
            Institution institution = new Institution();
            institution.setCode("DEFAULT");
            institution.setName("Test College");
            institutions.save(institution);
        }
    }

    @Test
    void registerLoginRefreshAndLogout() throws Exception {
        String suffix = Long.toString(System.nanoTime());
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student" + suffix + "\",\"email\":\"student" + suffix + "@example.com\",\"password\":\"StrongPass1!\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("STUDENT"));
        var login = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student" + suffix + "\",\"password\":\"StrongPass1!\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("student" + suffix + "@example.com")).andReturn();
        String access = new com.fasterxml.jackson.databind.ObjectMapper().readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();
        Cookie refresh = login.getResponse().getCookie("refresh_token");
        mvc.perform(get("/api/v1/auth/session").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk());

        var refreshed = mvc.perform(post("/api/v1/auth/refresh").cookie(refresh))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty()).andReturn();
        String refreshedAccess = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(refreshed.getResponse().getContentAsString()).get("accessToken").asText();
        Cookie rotatedRefresh = refreshed.getResponse().getCookie("refresh_token");

        mvc.perform(post("/api/v1/auth/logout").cookie(rotatedRefresh)
                        .header("Authorization", "Bearer " + refreshedAccess))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/auth/session").header("Authorization", "Bearer " + refreshedAccess))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh").cookie(rotatedRefresh))
                .andExpect(status().isUnauthorized());
    }
}
