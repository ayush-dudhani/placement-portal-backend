package com.keepcalm.placementportal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepcalm.placementportal.entity.Institution;
import com.keepcalm.placementportal.repository.InstitutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StudentProfileIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired InstitutionRepository institutions;
    private String token;

    @BeforeEach
    void login() throws Exception {
        if (institutions.findByCodeIgnoreCaseAndActiveTrue("DEFAULT").isEmpty()) {
            Institution institution = new Institution(); institution.setCode("DEFAULT"); institution.setName("Test College"); institutions.save(institution);
        }
        String suffix = Long.toString(System.nanoTime());
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"profile" + suffix + "\",\"email\":\"profile" + suffix + "@example.com\",\"password\":\"StrongPass1!\"}"));
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"profile" + suffix + "\",\"password\":\"StrongPass1!\"}"))
                .andReturn().getResponse().getContentAsString();
        token = json.readTree(body).get("accessToken").asText();
    }

    @Test
    void updateProfileAcademicsAndUploadResume() throws Exception {
        mvc.perform(put("/api/v1/students/me/profile").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Asha\",\"lastName\":\"Rao\",\"mobileNo\":\"9876543210\",\"rollNumber\":\"R" + System.nanoTime() + "\",\"dateOfBirth\":\"2003-04-12\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("Asha"));
        mvc.perform(put("/api/v1/students/me/academics").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cgpa\":8.5,\"tenthPercentage\":90,\"twelfthPercentage\":88,\"activeBacklogs\":0,\"branch\":\"CSE\",\"graduationYear\":2027}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cgpa").value(8.5));
        MockMultipartFile resume = new MockMultipartFile("file", "resume.pdf", "application/pdf", "%PDF-1.4 test".getBytes());
        mvc.perform(multipart("/api/v1/students/me/documents").file(resume).param("documentType", "RESUME")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.primary").value(true));
    }
}
