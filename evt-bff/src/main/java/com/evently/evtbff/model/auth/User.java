package com.evently.evtbff.model.auth;

public record User(
        String userId,
        String mobile,
        String role
) {
}