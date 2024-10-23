package com.eureka.mindbloom.member.controller;

import com.eureka.mindbloom.common.dto.ApiResponse;
import com.eureka.mindbloom.member.domain.Member;
import com.eureka.mindbloom.member.dto.ChildProfileResponse;
import com.eureka.mindbloom.member.dto.ChildRegisterRequest;
import com.eureka.mindbloom.member.dto.ChildRegisterResponse;
import com.eureka.mindbloom.member.service.ChildService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;

    @PostMapping("/members/children")
    public ResponseEntity<ApiResponse<?>> registerChild(
            @AuthenticationPrincipal(expression = "member") Member member,
            @RequestBody ChildRegisterRequest request) {

        ChildRegisterResponse response = childService.registerChild(member, request);

        return ResponseEntity.ok().body(ApiResponse.success("자녀 프로필이 생성되었습니다.", response));
    }

    @GetMapping("/members/children")
    public ResponseEntity<ApiResponse<?>> getChildrenProfile(
            @AuthenticationPrincipal(expression = "member") Member member) {

        List<ChildProfileResponse> response = childService.getChildProfile(member);

        return ResponseEntity.ok().body(ApiResponse.success("OK", response));
    }

    @PostMapping("/members/children/{childId}")
    public ResponseEntity<ApiResponse<?>> choiceChildProfile(
            @PathVariable Long childId,
            HttpServletResponse response) {

        Cookie cookie = new Cookie("childId", String.valueOf(childId));
        cookie.setPath("/");

        response.addCookie(cookie);
        return ResponseEntity.ok().body(ApiResponse.success("OK"));
    }
}