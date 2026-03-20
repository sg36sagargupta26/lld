package com.lld.practice.modal;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;


public class UserRateLimitId implements Serializable {
    private String userId;
    private LocalDateTime requestAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getRequestAt() {
        return requestAt;
    }

    public void setRequestAt(LocalDateTime requestAt) {
        this.requestAt = requestAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserRateLimitId that = (UserRateLimitId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(requestAt, that.requestAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, requestAt);
    }
}
