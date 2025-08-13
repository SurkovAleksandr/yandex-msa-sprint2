package org.example.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hotelio-monolith-User", url = "http://hotelio-monolith:8080", path = "/api/users")
public interface UserFeignClient {

    @GetMapping("/{userId}/active")
    boolean isUserActive(@PathVariable String userId);

    @GetMapping("/{userId}/blacklisted")
    boolean isUserBlacklisted(@PathVariable String userId);

    @GetMapping("/{userId}/status")
    public String getUserStatus(@PathVariable String userId);
}
