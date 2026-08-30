package com.keepcalm.placementportal;
import com.keepcalm.placementportal.enums.ApplicationStatus;
import com.keepcalm.placementportal.exception.DomainException;
import com.keepcalm.placementportal.service.ApplicationStatusPolicy;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class ApplicationStatusPolicyTest {
 private final ApplicationStatusPolicy policy=new ApplicationStatusPolicy();
 @Test void allowsDefinedProgression(){assertThatCode(()->policy.require(ApplicationStatus.ELIGIBLE,ApplicationStatus.SHORTLISTED)).doesNotThrowAnyException();}
 @Test void rejectsTerminalOrBackwardProgression(){assertThatThrownBy(()->policy.require(ApplicationStatus.WITHDRAWN,ApplicationStatus.ELIGIBLE)).isInstanceOf(DomainException.class);}
}
