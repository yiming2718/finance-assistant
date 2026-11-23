package com.example.finance.security;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}