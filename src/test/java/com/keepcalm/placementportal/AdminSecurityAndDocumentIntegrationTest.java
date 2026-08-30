package com.keepcalm.placementportal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepcalm.placementportal.entity.*;
import com.keepcalm.placementportal.enums.Role;
import com.keepcalm.placementportal.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
class AdminSecurityAndDocumentIntegrationTest {
 @Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired InstitutionRepository institutions;@Autowired UserRepository users;@Autowired StudentRepository students;@Autowired PasswordEncoder passwords;@Autowired AuditLogRepository audits;
 private String studentToken,adminToken;private Long studentId,documentId;
 @BeforeEach void setup() throws Exception{
  Institution institution=institutions.findByCodeIgnoreCaseAndActiveTrue("DEFAULT").orElseGet(()->{Institution i=new Institution();i.setCode("DEFAULT");i.setName("Test College");return institutions.save(i);});String suffix=Long.toString(System.nanoTime());String studentName="secure"+suffix;
  mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\""+studentName+"\",\"email\":\""+studentName+"@example.com\",\"password\":\"StrongPass1!\"}"));studentToken=login(studentName);
  studentId=students.findByUser(users.findByUsername(studentName).orElseThrow()).orElseThrow().getId();MockMultipartFile resume=new MockMultipartFile("file","resume.pdf","application/pdf","%PDF-1.4 test".getBytes());String upload=mvc.perform(multipart("/api/v1/students/me/documents").file(resume).param("documentType","RESUME").header("Authorization","Bearer "+studentToken)).andReturn().getResponse().getContentAsString();documentId=json.readTree(upload).get("id").asLong();
  User admin=new User();admin.setInstitution(institution);admin.setUsername("admin"+suffix);admin.setEmail("admin"+suffix+"@example.com");admin.setPasswordHash(passwords.encode("StrongPass1!"));admin.setRole(Role.ADMIN);admin.setIsActive(true);users.save(admin);adminToken=login(admin.getUsername());
 }
 @Test void studentCannotUseAdminWorkspaceAndAdminCanVerifyOwnedInstitutionDocument() throws Exception{
  mvc.perform(get("/api/v1/admin/students").header("Authorization","Bearer "+studentToken)).andExpect(status().isForbidden());
  mvc.perform(patch("/api/v1/admin/students/"+studentId+"/documents/"+documentId+"/verification").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"VERIFIED\",\"note\":\"Checked\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
  Assertions.assertTrue(audits.count()>0);
 }
 private String login(String username)throws Exception{String response=mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\""+username+"\",\"password\":\"StrongPass1!\"}")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();return json.readTree(response).get("accessToken").asText();}
}
