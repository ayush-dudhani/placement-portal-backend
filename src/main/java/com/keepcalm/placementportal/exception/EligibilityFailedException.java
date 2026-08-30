package com.keepcalm.placementportal.exception;
import org.springframework.http.HttpStatus;
public class EligibilityFailedException extends DomainException {
 public EligibilityFailedException(String detail){ super(HttpStatus.UNPROCESSABLE_ENTITY,"ELIGIBILITY_FAILED",detail); }
}
