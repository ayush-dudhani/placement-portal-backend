package com.keepcalm.placementportal.service.drive;
import com.keepcalm.placementportal.repository.audit.*;
import com.keepcalm.placementportal.repository.communication.*;
import com.keepcalm.placementportal.repository.offer.*;
import com.keepcalm.placementportal.repository.selection.*;
import com.keepcalm.placementportal.repository.application.*;
import com.keepcalm.placementportal.repository.drive.*;
import com.keepcalm.placementportal.repository.company.*;
import com.keepcalm.placementportal.repository.profile.*;
import com.keepcalm.placementportal.repository.student.*;
import com.keepcalm.placementportal.repository.auth.*;
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
import com.keepcalm.placementportal.enums.EligibilityRuleType;
import com.keepcalm.placementportal.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
@Service
public class EligibilityEngine {
 public Evaluation evaluate(Student student,AcademicRecord academic,List<EligibilityRule> rules){
  List<String> explanations=new ArrayList<>(); List<Map<String,Object>> results=new ArrayList<>(); boolean eligible=true;
  for(EligibilityRule rule:rules){ Map<String,Object> config=rule.getConfiguration(); Result result=evaluateRule(rule.getRuleType(),config,student,academic); if(!result.passed()&&rule.isMandatory())eligible=false; explanations.add(result.explanation()); results.add(Map.of("type",rule.getRuleType().name(),"passed",result.passed(),"mandatory",rule.isMandatory(),"explanation",result.explanation(),"configuration",config)); }
  Map<String,Object> profile=new LinkedHashMap<>(); profile.put("cgpa",academic.getCgpa());profile.put("activeBacklogs",academic.getActiveBacklogs());profile.put("branch",academic.getBranch());profile.put("graduationYear",academic.getGraduationYear());profile.put("tenthPercentage",academic.getTenthPercentage());profile.put("twelfthPercentage",academic.getTwelfthPercentage());profile.put("diplomaPercentage",academic.getDiplomaPercentage());
  Map<String,Object> snapshot=new LinkedHashMap<>();snapshot.put("evaluatedAt",Instant.now().toString());snapshot.put("studentProfileVersion",student.getVersion());snapshot.put("academicRecordVersion",academic.getVersion());snapshot.put("profile",profile);snapshot.put("rules",results);snapshot.put("eligible",eligible);
  return new Evaluation(eligible,List.copyOf(explanations),snapshot);
 }
 private Result evaluateRule(EligibilityRuleType type,Map<String,Object> c,Student s,AcademicRecord a){return switch(type){
  case MIN_CGPA -> compareMinimum("CGPA",a.getCgpa(),decimal(c,"value"));
  case MAX_ACTIVE_BACKLOGS -> {int max=integer(c,"value");yield new Result(a.getActiveBacklogs()<=max,"Active backlogs "+a.getActiveBacklogs()+"; maximum "+max);}
  case ALLOWED_BRANCHES -> contains("Branch",a.getBranch(),strings(c,"values"));
  case GRADUATION_YEARS -> contains("Graduation year",Integer.toString(a.getGraduationYear()),strings(c,"values"));
  case MIN_TENTH_PERCENTAGE -> compareMinimum("10th percentage",a.getTenthPercentage(),decimal(c,"value"));
  case MIN_TWELFTH_PERCENTAGE -> compareMinimum("12th percentage",a.getTwelfthPercentage(),decimal(c,"value"));
  case MIN_DIPLOMA_PERCENTAGE -> compareMinimum("Diploma percentage",a.getDiplomaPercentage(),decimal(c,"value"));
  case GENDER_RULE -> {if(!Boolean.TRUE.equals(c.get("institutionallyApproved")))throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,"UNAPPROVED_GENDER_RULE","Gender rule is not institutionally approved");yield contains("Gender",s.getGender(),strings(c,"values"));}
  case CUSTOM_RULE -> new Result(false,"Custom rule requires a configured evaluator");
 };}
 private Result compareMinimum(String label,BigDecimal actual,BigDecimal min){boolean pass=actual!=null&&actual.compareTo(min)>=0;return new Result(pass,label+" is "+(actual==null?"missing":actual)+"; minimum "+min);}
 private Result contains(String label,String actual,List<String> allowed){boolean pass=actual!=null&&allowed.stream().anyMatch(v->v.equalsIgnoreCase(actual));return new Result(pass,label+" is "+(actual==null?"missing":actual)+"; allowed "+allowed);}
 private BigDecimal decimal(Map<String,Object>c,String key){Object v=required(c,key);return new BigDecimal(v.toString());}
 private int integer(Map<String,Object>c,String key){return Integer.parseInt(required(c,key).toString());}
 private List<String> strings(Map<String,Object>c,String key){Object v=required(c,key);if(!(v instanceof Collection<?> values))throw invalid(key);return values.stream().map(Object::toString).toList();}
 private Object required(Map<String,Object>c,String key){Object v=c.get(key);if(v==null)throw invalid(key);return v;}
 private DomainException invalid(String key){return new DomainException(HttpStatus.UNPROCESSABLE_ENTITY,"INVALID_ELIGIBILITY_RULE","Eligibility configuration is missing or invalid: "+key);}
 public record Evaluation(boolean eligible,List<String> explanations,Map<String,Object> snapshot){}
 private record Result(boolean passed,String explanation){}
}
