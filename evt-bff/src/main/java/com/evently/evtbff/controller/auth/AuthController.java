
package com.evently.evtbff.controller.auth;

import com.evently.evtbff.dto.auth.GenerateOtpRequest;
import com.evently.evtbff.dto.auth.VerifyOtpRequest;
import com.evently.evtbff.model.auth.User;
import com.evently.evtbff.service.auth.JwtService;
import com.evently.evtbff.service.auth.OtpService;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final OtpService otpService;
    private final JwtService jwtService;

    public AuthController(
            OtpService otpService,
            JwtService jwtService) {
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    @PostMapping("/generate-otp")
    public ResponseEntity<Map<String, Object>> generateOtp(
            @Valid @RequestBody GenerateOtpRequest request) {

        otpService.generateOtp(request.getMobile());

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "data", Map.of(
                                "message",
                                "OTP generated successfully"
                        )
                )
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        var userOptional = otpService.verifyOtp(
                request.getMobile(),
                request.getOtp()
        );

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "Invalid or expired OTP, or user not registered"
                    ));
        }

        User user = userOptional.get();

        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken =
                jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "data", Map.of(
                                "userId", user.userId(),
                                "role", user.role(),
                                "accessToken", accessToken,
                                "refreshToken", refreshToken,
                                "tokenType", "Bearer",
                                "expiresIn", 900
                        )
                )
        );
    }
}
