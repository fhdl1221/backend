package com.example.backend.controller;

import com.example.backend.dto.UserProfileResponse;
import com.example.backend.dto.UserProfileUpdateRequest;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 인증된 사용자 본인("me")의 프로필 정보를 반환
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        String email = authentication.getName(); // 토큰에서 이메일(username)을 가져옵니다.
        UserProfileResponse userProfile = userService.getUserProfile(email);
        return ResponseEntity.ok(userProfile);
    }

    // TODO: 프로필 업데이트(PATCH /api/user/me) API도 여기에 추가할 수 있습니다.
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @RequestBody UserProfileUpdateRequest request, // 5. 요청 바디
            Authentication authentication) {

        String email = authentication.getName();
        UserProfileResponse updatedProfile = userService.updateUserProfile(email, request);

        // 6. 업데이트된 최신 프로필 정보를 반환
        return ResponseEntity.ok(updatedProfile);
    }
}