package project.coca.auth.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JwtRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    public String getUsername(String refreshToken) {
        return (String) getValue(refreshToken);
    }

    public UserSession getSession(String accessToken) {
        return (UserSession) getValue(accessToken);
    }

    private Object getValue(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis에서 조회 수행 중 오류가 발생함 : {}", e.getMessage());
            throw new RedisOperationException("Redis에서 조회 수행 중 오류가 발생함", e);
        }
    }

    public void deleteValue(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis 데이터 삭제 실패 : {}", e.getMessage());
            throw new RedisOperationException("Redis에서 삭제 수행 중 오류가 발생함", e);
        }
    }

    public void setValue(String key, Object value, long time) {
        try {
            if (getValue(key) != null) {
                deleteValue(key);
            }
            Duration expiredDuration = Duration.ofMillis(time);
            redisTemplate.opsForValue().set(key, value, expiredDuration);
            Object o = redisTemplate.opsForValue().get(key);
            log.info("Success to set token in Redis{}", o);
        } catch (Exception e) {
            log.error("Failed to set token in Redis: {}", e.getMessage());
            throw new RedisOperationException("Error setting token in Redis", e);
        }
    }
}