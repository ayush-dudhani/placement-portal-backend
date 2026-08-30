package com.keepcalm.placementportal.service;
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
