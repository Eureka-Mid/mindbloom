package com.eureka.mindbloom.auth.filter;

import com.eureka.mindbloom.auth.dto.MemberLoginRequest;
import com.eureka.mindbloom.auth.utils.JwtProvider;
import com.eureka.mindbloom.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    private final JwtProvider jwtProvider;

    public JwtLoginFilter(JwtProvider jwtProvider,
                          AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.jwtProvider = jwtProvider;
        setFilterProcessesUrl("/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        try {
            MemberLoginRequest requestDto = new ObjectMapper()
                    .readValue(request.getInputStream(), MemberLoginRequest.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.email(),
                            requestDto.password()
                    )
            );
        } catch (IOException e) {
            throw new AuthenticationServiceException("로그인 시도 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        String username = userDetails.getUsername();

        String token = jwtProvider.createToken(username, ACCESS_TOKEN_EXPIRATION_TIME);

        response.addHeader(HttpHeaders.AUTHORIZATION, token);

        ApiResponse<?> successResponse = ApiResponse.success("로그인 성공");
        servletResponse(response, successResponse, HttpStatus.OK);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        ApiResponse<?> errorResponse = ApiResponse.failure("인증 과정 중 오류 발생");

        servletResponse(response, errorResponse, HttpStatus.UNAUTHORIZED);
    }

    private void servletResponse(HttpServletResponse response,
                                 ApiResponse<?> responseDto,
                                 HttpStatus status) throws IOException {
        String jsonResponse = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(responseDto);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.getWriter().write(jsonResponse);
    }
}