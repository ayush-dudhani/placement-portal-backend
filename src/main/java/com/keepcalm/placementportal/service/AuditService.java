package com.keepcalm.placementportal.service;
import com.keepcalm.placementportal.entity.*;
import com.keepcalm.placementportal.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import java.util.Map;
@Service @RequiredArgsConstructor
public class AuditService {
 private final AuditLogRepository logs;private final CurrentUserService current;private final CacheManager caches;
 public void record(String action,String type,Object id,Map<String,Object> details){User actor=current.getCurrentUser();AuditLog log=new AuditLog();log.setInstitution(actor.getInstitution());log.setActor(actor);log.setAction(action);log.setResourceType(type);log.setResourceId(id==null?null:id.toString());log.setDetails(details==null?Map.of():details);logs.save(log);var analytics=caches.getCache("analytics");if(analytics!=null)analytics.clear();}
}
