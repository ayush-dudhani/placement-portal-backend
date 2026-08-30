package com.keepcalm.placementportal.models.student;

import java.util.List;

public record ProfileCompletionDto(int percentage, List<String> completedSections, List<String> missingSections) {}
