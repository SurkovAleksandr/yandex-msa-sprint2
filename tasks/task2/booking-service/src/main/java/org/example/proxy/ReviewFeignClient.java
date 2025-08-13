package org.example.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hotelio-monolith-Review", url = "http://hotelio-monolith:8080", path = "/api/reviews")
public interface ReviewFeignClient {
    @GetMapping("/hotel/{hotelId}/trusted")
    public boolean isHotelTrusted(@PathVariable String hotelId);
}
