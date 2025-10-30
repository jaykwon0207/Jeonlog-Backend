package com.jeonlog.exhibition_recommender.system;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class HealthController {


    @GetMapping("/api/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("OK");
    }
}


