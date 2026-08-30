package com.keepcalm.placementportal.controller;

import com.keepcalm.placementportal.models.auth.CurrentUserDto;
import com.keepcalm.placementportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {
    private final UserService userService;
    @GetMapping public CurrentUserDto get() { return userService.me(); }
    @PatchMapping public CurrentUserDto update(@Valid @RequestBody CurrentUserDto.Update request) { return userService.update(request); }
}
