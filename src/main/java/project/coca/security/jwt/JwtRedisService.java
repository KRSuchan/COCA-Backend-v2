package project.coca.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JwtRedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public String getToken(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Failed to retrieve token from Redis: {}", e.getMessage());
            throw new RedisOperationException("Error retrieving token from Redis", e);
        }
    }

    public void deleteToken(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete token from Redis: {}", e.getMessage());
            throw new RedisOperationException("Error deleting token from Redis", e);
        }
    }

    public void setToken(String key, String value, long time) {
        try {
            if (getToken(key) != null) {
                deleteToken(key);
            }
            Duration expiredDuration = Duration.ofMillis(time);
            redisTemplate.opsForValue().set(key, value, expiredDuration);
            String s = redisTemplate.opsForValue().get(key);
            log.info("Success to set token in Redis{}", s);
        } catch (Exception e) {
            log.error("Failed to set token in Redis: {}", e.getMessage());
            throw new RedisOperationException("Error setting token in Redis", e);
        }
    }
}