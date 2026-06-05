package com.bloghub.api.service;

import java.time.Duration;

public interface IdempotencyService {

    boolean claim(String key, Duration ttl);
}
