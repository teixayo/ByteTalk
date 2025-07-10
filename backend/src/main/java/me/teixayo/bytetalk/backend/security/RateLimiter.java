package me.teixayo.bytetalk.backend.security;

public class RateLimiter {
    private final int maxTokens;
    private final long refillIntervalMillis;
    private int tokens;
    private long lastRefillTimestamp;

    public RateLimiter(int maxTokens, long refillIntervalMillis) {
        this.maxTokens = maxTokens;
        this.refillIntervalMillis = refillIntervalMillis;
        this.tokens = maxTokens;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean allowRequest() {
        refill();
        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTimestamp;
        if (elapsed > refillIntervalMillis) {
            tokens = maxTokens;
            lastRefillTimestamp = now;
        }
    }

    public RateLimiter copy() {
        return new RateLimiter(maxTokens,refillIntervalMillis);
    }
}
