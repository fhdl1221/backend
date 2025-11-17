package com.example.backend.dto;

import lombok.Data;

@Data
public class WebPushSubscriptionDto {
    private String endpoint;
    private Keys keys;

    @Data
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}