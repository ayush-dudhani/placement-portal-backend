package com.keepcalm.placementportal.service;

import com.keepcalm.placementportal.entity.Student;
import com.keepcalm.placementportal.entity.User;
import com.keepcalm.placementportal.enums.PlacementStatus;
import com.keepcalm.placementportal.models.student.StudentProfileRequest;
import com.keepcalm.placementportal.models.student.StudentProfileResponse;
import com.keepcalm.placementportal.repository.StudentRepository;
import com.keepcalm.placementportal.repository.UserRepository;
import com.keepcalm.placementportal.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CurrentUserService currentUserService;

    public StudentProfileResponse saveProfile(
            String authHeader,
            StudentProfileRequest request) {

        User user = currentUserService.getCurrentUser(authHeader);

        Student student = studentRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        student.setUser(user);

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());

        student.setMobileNo(request.getMobileNo());

        student.setRollNumber(request.getRollNumber());

        student.setBranch(request.getBranch());
        student.setYearOfPassing(
                request.getYearOfPassing());

        student.setCgpa(request.getCgpa());

        student.setTenthPercentage(
                request.getTenthPercentage());

        student.setTwelfthPercentage(
                request.getTwelfthPercentage());

        student.setDiplomaPercentage(
                request.getDiplomaPercentage());

        student.setActiveBacklogs(
                request.getActiveBacklogs());

        student.setResumeUrl(
                request.getResumeUrl());

        student.setLinkedinUrl(
                request.getLinkedinUrl());

        student.setGithubUrl(
                request.getGithubUrl());

        student.setDateOfBirth(
                request.getDateOfBirth());

        student.setGender(
                request.getGender());

        student.setSkills(
                request.getSkills());

        student.setCollegeName(
                request.getCollegeName());

        student.setPlacementStatus(
                PlacementStatus.NOT_PLACED);

        studentRepository.save(student);

        return buildResponse(student);
    }

    public StudentProfileResponse getProfile(
            String authHeader) {

        User user = currentUserService.getCurrentUser(authHeader);

        Student student = studentRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Profile not found"));

        return buildResponse(student);
    }

    private StudentProfileResponse buildResponse(
            Student student) {

        return StudentProfileResponse.builder()
                .collegeName(student.getCollegeName())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .mobileNo(student.getMobileNo())
                .rollNumber(student.getRollNumber())
                .branch(student.getBranch())
                .yearOfPassing(student.getYearOfPassing())
                .cgpa(student.getCgpa())
                .tenthPercentage(
                        student.getTenthPercentage())
                .twelfthPercentage(
                        student.getTwelfthPercentage())
                .diplomaPercentage(
                        student.getDiplomaPercentage())
                .activeBacklogs(
                        student.getActiveBacklogs())
                .resumeUrl(
                        student.getResumeUrl())
                .linkedinUrl(
                        student.getLinkedinUrl())
                .githubUrl(
                        student.getGithubUrl())
                .gender(student.getGender())
                .dateOfBirth(
                        student.getDateOfBirth())
                .skills(student.getSkills())
                .placementStatus(
                        student.getPlacementStatus())
                .profileCompletion(
                        calculateProfileCompletion(
                                student))
                .build();
    }

    private int calculateProfileCompletion(
            Student student) {

        int totalFields = 12;
        int completed = 0;

        if (student.getFirstName() != null) completed++;
        if (student.getLastName() != null) completed++;
        if (student.getMobileNo() != null) completed++;
        if (student.getRollNumber() != null) completed++;
        if (student.getBranch() != null) completed++;
        if (student.getYearOfPassing() != null) completed++;
        if (student.getCgpa() != null) completed++;
        if (student.getResumeUrl() != null) completed++;
        if (student.getLinkedinUrl() != null) completed++;
        if (student.getGithubUrl() != null) completed++;
        if (student.getGender() != null) completed++;
        if (student.getDateOfBirth() != null) completed++;

        return completed * 100 / totalFields;
    }
}