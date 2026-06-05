package com.bloghub.api.service;

import java.time.Duration;

public interface RateLimiterService {

    void checkAllowed(String key, int limit, Duration window);
}
