package com.keepcalm.placementportal;
import com.keepcalm.placementportal.entity.*;
import com.keepcalm.placementportal.enums.EligibilityRuleType;
import com.keepcalm.placementportal.service.EligibilityEngine;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
class EligibilityEngineTest {
 @Test void evaluatesRulesAndBuildsImmutableProfileSnapshot(){
  Student student=new Student();student.setGender("FEMALE");
  AcademicRecord academic=new AcademicRecord();academic.setCgpa(new BigDecimal("8.20"));academic.setActiveBacklogs(0);academic.setBranch("CSE");academic.setGraduationYear(2027);academic.setTenthPercentage(new BigDecimal("91"));
  EligibilityRule cgpa=rule(EligibilityRuleType.MIN_CGPA,"{\"value\":8.0}");EligibilityRule branches=rule(EligibilityRuleType.ALLOWED_BRANCHES,"{\"values\":[\"CSE\",\"IT\"]}");
  var result=new EligibilityEngine().evaluate(student,academic,List.of(cgpa,branches));
  assertThat(result.eligible()).isTrue();assertThat(result.snapshot()).containsKeys("evaluatedAt","profile","rules");
  academic.setCgpa(new BigDecimal("7.5"));
  assertThat(((java.util.Map<?,?>)result.snapshot().get("profile")).get("cgpa").toString()).isEqualTo("8.20");
 }
 private EligibilityRule rule(EligibilityRuleType type,String config){EligibilityRule r=new EligibilityRule();r.setRuleType(type);try{r.setConfiguration(new com.fasterxml.jackson.databind.ObjectMapper().readValue(config,java.util.Map.class));}catch(Exception e){throw new RuntimeException(e);}r.setMandatory(true);r.setActive(true);return r;}
}
