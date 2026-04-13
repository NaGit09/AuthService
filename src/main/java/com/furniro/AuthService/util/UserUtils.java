package com.furniro.AuthService.util;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UserUtils {

    public static String generateUniqueUsername() {
        String uuid = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);
        return "user_" + uuid;
    }

    public static String generateUUIDToken() {
        return UUID.randomUUID().toString();
    }

}
