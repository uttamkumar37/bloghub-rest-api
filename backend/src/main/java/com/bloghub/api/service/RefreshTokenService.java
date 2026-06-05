package com.bloghub.api.service;

import com.bloghub.api.entity.RefreshToken;
import com.bloghub.api.entity.User;

public interface RefreshTokenService {

    IssuedRefreshToken issueToken(User user, String clientIp);

    IssuedRefreshToken rotate(String rawRefreshToken, String clientIp);

    void revoke(String rawRefreshToken);

    record IssuedRefreshToken(String rawToken, RefreshToken entity) {
    }
}
