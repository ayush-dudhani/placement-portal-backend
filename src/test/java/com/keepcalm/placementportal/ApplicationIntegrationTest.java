package com.keepcalm.placementportal;
import com.keepcalm.placementportal.service.storage.*;
import com.keepcalm.placementportal.service.audit.*;
import com.keepcalm.placementportal.service.event.*;
import com.keepcalm.placementportal.service.analytics.*;
import com.keepcalm.placementportal.service.communication.*;
import com.keepcalm.placementportal.service.offer.*;
import com.keepcalm.placementportal.service.selection.*;
import com.keepcalm.placementportal.service.application.*;
import com.keepcalm.placementportal.service.drive.*;
import com.keepcalm.placementportal.service.company.*;
import com.keepcalm.placementportal.service.student.*;
import com.keepcalm.placementportal.service.profile.*;
import com.keepcalm.placementportal.service.auth.*;
import com.keepcalm.placementportal.controller.event.*;
import com.keepcalm.placementportal.controller.analytics.*;
import com.keepcalm.placementportal.controller.communication.*;
import com.keepcalm.placementportal.controller.offer.*;
import com.keepcalm.placementportal.controller.selection.*;
import com.keepcalm.placementportal.controller.application.*;
import com.keepcalm.placementportal.controller.drive.*;
import com.keepcalm.placementportal.controller.company.*;
import com.keepcalm.placementportal.controller.student.*;
import com.keepcalm.placementportal.controller.profile.*;
import com.keepcalm.placementportal.controller.auth.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepcalm.placementportal.entity.auth.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.enums.*;
import com.keepcalm.placementportal.repository.auth.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.audit.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
class ApplicationIntegrationTest {
 @Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired InstitutionRepository institutions;@Autowired UserRepository users;
 @Autowired CompanyRepository companies;@Autowired CampusDriveRepository drives;@Autowired DriveRoleRepository roles;@Autowired EligibilityRuleRepository rules;@Autowired JobApplicationRepository applications;@Autowired StudentRepository studentRepository;
 private String token;private Long studentId;private CampusDrive drive;private DriveRole eligibleRole;private DriveRole lateRole;private DriveRole ineligibleRole;
 @BeforeEach void setup() throws Exception{
  Institution institution=institutions.findByCodeIgnoreCaseAndActiveTrue("DEFAULT").orElseGet(()->{Institution i=new Institution();i.setCode("DEFAULT");i.setName("Test College");return institutions.save(i);});
  String suffix=Long.toString(System.nanoTime());mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"apply"+suffix+"\",\"email\":\"apply"+suffix+"@example.com\",\"password\":\"StrongPass1!\"}"));
  String login=mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"apply"+suffix+"\",\"password\":\"StrongPass1!\"}")).andReturn().getResponse().getContentAsString();token=json.readTree(login).get("accessToken").asText();
  studentId=studentRepository.findByUser(users.findByUsername("apply"+suffix).orElseThrow()).orElseThrow().getId();
  mvc.perform(put("/api/v1/students/me/academics").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"cgpa\":8.5,\"tenthPercentage\":90,\"twelfthPercentage\":88,\"activeBacklogs\":0,\"branch\":\"CSE\",\"graduationYear\":2027}")).andExpect(status().isOk());
  Company company=new Company();company.setInstitution(institution);company.setName("Company "+suffix);companies.save(company);
  drive=new CampusDrive();drive.setInstitution(institution);drive.setCompany(company);drive.setOwner(users.findByUsername("apply"+suffix).orElseThrow());drive.setTitle("Graduate Drive");drive.setStatus(DriveStatus.APPLICATIONS_OPEN);drive.setApplicationsOpenAt(Instant.now().minusSeconds(60));drive.setApplicationDeadline(Instant.now().plusSeconds(3600));drives.save(drive);
  eligibleRole=role("Engineer",Instant.now().plusSeconds(3600));lateRole=role("Late Role",Instant.now().minusSeconds(10));ineligibleRole=role("Senior Engineer",Instant.now().plusSeconds(3600));
  rule(eligibleRole,"{\"value\":8.0}");rule(lateRole,"{\"value\":8.0}");rule(ineligibleRole,"{\"value\":9.0}");
 }
 @Test void eligibleDuplicateLateAndIneligibleApplications(){
  try{
   mvc.perform(post("/api/v1/drives/"+drive.getId()+"/roles/"+eligibleRole.getId()+"/applications").header("Authorization","Bearer "+token).header("Idempotency-Key","apply-1")).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ELIGIBLE"));
   mvc.perform(post("/api/v1/drives/"+drive.getId()+"/roles/"+eligibleRole.getId()+"/applications").header("Authorization","Bearer "+token).header("Idempotency-Key","different")).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_APPLICATION"));
   mvc.perform(post("/api/v1/drives/"+drive.getId()+"/roles/"+lateRole.getId()+"/applications").header("Authorization","Bearer "+token)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("APPLICATION_DEADLINE_PASSED"));
   mvc.perform(post("/api/v1/drives/"+drive.getId()+"/roles/"+ineligibleRole.getId()+"/applications").header("Authorization","Bearer "+token)).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("ELIGIBILITY_FAILED"));
   Assertions.assertTrue(applications.existsByDriveRoleIdAndStudentId(ineligibleRole.getId(),studentId));
  }catch(Exception e){throw new RuntimeException(e);}
 }
 private DriveRole role(String title,Instant deadline){DriveRole r=new DriveRole();r.setDrive(drive);r.setTitle(title);r.setPositions(1);r.setCurrency("INR");r.setApplicationDeadline(deadline);return roles.save(r);}
 private void rule(DriveRole role,String config){EligibilityRule r=new EligibilityRule();r.setDrive(drive);r.setDriveRole(role);r.setRuleType(EligibilityRuleType.MIN_CGPA);try{r.setConfiguration(json.readValue(config,java.util.Map.class));}catch(Exception e){throw new RuntimeException(e);}r.setMandatory(true);r.setActive(true);rules.save(r);}
}
