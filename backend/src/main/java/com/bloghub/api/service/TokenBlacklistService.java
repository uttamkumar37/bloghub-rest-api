package com.bloghub.api.service;

import java.time.Duration;

public interface TokenBlacklistService {

    void blacklist(String accessToken, Duration ttl);

    boolean isBlacklisted(String accessToken);
}
