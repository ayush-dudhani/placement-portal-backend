package com.keepcalm.placementportal.service.application;
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
import com.keepcalm.placementportal.entity.audit.*;
import com.keepcalm.placementportal.entity.communication.*;
import com.keepcalm.placementportal.entity.offer.*;
import com.keepcalm.placementportal.entity.selection.*;
import com.keepcalm.placementportal.entity.application.*;
import com.keepcalm.placementportal.entity.drive.*;
import com.keepcalm.placementportal.entity.company.*;
import com.keepcalm.placementportal.entity.profile.*;
import com.keepcalm.placementportal.entity.student.*;
import com.keepcalm.placementportal.entity.auth.*;
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
import com.keepcalm.placementportal.enums.ApplicationStatus;
import com.keepcalm.placementportal.exception.DomainException;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class ApplicationStatusPolicy {
 private static final Map<ApplicationStatus,Set<ApplicationStatus>> ALLOWED=Map.ofEntries(
  Map.entry(ApplicationStatus.APPLIED,Set.of(ApplicationStatus.UNDER_REVIEW,ApplicationStatus.ELIGIBLE,ApplicationStatus.INELIGIBLE,ApplicationStatus.WITHDRAWN)),
  Map.entry(ApplicationStatus.UNDER_REVIEW,Set.of(ApplicationStatus.ELIGIBLE,ApplicationStatus.INELIGIBLE,ApplicationStatus.SHORTLISTED,ApplicationStatus.WITHDRAWN)),
  Map.entry(ApplicationStatus.ELIGIBLE,Set.of(ApplicationStatus.SHORTLISTED,ApplicationStatus.NOT_SELECTED,ApplicationStatus.WITHDRAWN)),
  Map.entry(ApplicationStatus.SHORTLISTED,Set.of(ApplicationStatus.IN_PROCESS,ApplicationStatus.SELECTED,ApplicationStatus.NOT_SELECTED,ApplicationStatus.WITHDRAWN)),
  Map.entry(ApplicationStatus.IN_PROCESS,Set.of(ApplicationStatus.SELECTED,ApplicationStatus.NOT_SELECTED,ApplicationStatus.WITHDRAWN)),
  Map.entry(ApplicationStatus.SELECTED,Set.of(ApplicationStatus.OFFER_ACCEPTED,ApplicationStatus.OFFER_DECLINED)),
  Map.entry(ApplicationStatus.OFFER_ACCEPTED,Set.of()),Map.entry(ApplicationStatus.OFFER_DECLINED,Set.of()),Map.entry(ApplicationStatus.NOT_SELECTED,Set.of()),Map.entry(ApplicationStatus.INELIGIBLE,Set.of()),Map.entry(ApplicationStatus.WITHDRAWN,Set.of()));
 public void require(ApplicationStatus from,ApplicationStatus to){if(from==to||!ALLOWED.getOrDefault(from,Set.of()).contains(to))throw DomainException.conflict("INVALID_STATUS_TRANSITION","Cannot change application status from "+from+" to "+to);}
 public void requireWithdrawable(ApplicationStatus status){require(status,ApplicationStatus.WITHDRAWN);}
}
