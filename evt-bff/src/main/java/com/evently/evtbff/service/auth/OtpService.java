
package com.evently.evtbff.service.auth;

import com.evently.evtbff.model.auth.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final UserService userService;

    public OtpService(
            StringRedisTemplate redisTemplate,
            UserService userService) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
    }

    public void generateOtp(String mobile) {
        String otp = String.format(
                "%06d",
                new java.security.SecureRandom().nextInt(1_000_000)
        );

        redisTemplate.opsForValue().set(
                "otp:" + mobile,
                otp,
                5,
                TimeUnit.MINUTES
        );

        // Development only
        System.out.println("Generated OTP for mobile "
                + mobile + ": " + otp);
    }

    public Optional<User> verifyOtp(String mobile, String otp) {
        String key = "otp:" + mobile;

        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            return Optional.empty();
        }

        // Consume OTP after successful verification
        redisTemplate.delete(key);

        // Only registered users can log in
        return userService.findByMobile(mobile);
    }
}
