package org.example.proxy;

import org.example.proxy.dto.PromoCode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hotelio-monolith-PromoCode", url = "http://hotelio-monolith:8080", path = "/api/promos")
public interface PromoCodeFeignClient {
    @PostMapping("/validate")
    public PromoCode validatePromo(@RequestParam String code,
                                   @RequestParam String userId);
}
