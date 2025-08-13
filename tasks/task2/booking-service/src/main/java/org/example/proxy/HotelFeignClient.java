package org.example.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hotelio-monolith-Hotel", url = "http://hotelio-monolith:8080", path = "/api/hotels")
public interface HotelFeignClient {
    @GetMapping("/{id}/operational")
    public boolean isOperational(@PathVariable String id);

    @GetMapping("/{id}/fully-booked")
    public boolean isFullyBooked(@PathVariable String id);
}
