package com.keepcalm.placementportal.controller;

import com.keepcalm.placementportal.models.student.StudentProfileRequest;
import com.keepcalm.placementportal.models.student.StudentProfileResponse;
import com.keepcalm.placementportal.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/profile")
    public ResponseEntity<StudentProfileResponse> saveProfile(
            @RequestHeader("Authorization")
            String authHeader,

            @RequestBody
            StudentProfileRequest request) {

        return ResponseEntity.ok(
                studentService.saveProfile(
                        authHeader,
                        request));
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentProfileResponse> getProfile(
            @RequestHeader("Authorization")
            String authHeader) {

        return ResponseEntity.ok(
                studentService.getProfile(authHeader));
    }
}