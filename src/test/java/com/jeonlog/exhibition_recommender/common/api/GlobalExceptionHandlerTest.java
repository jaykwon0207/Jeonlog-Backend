package com.jeonlog.exhibition_recommender.common.api;

import com.jeonlog.exhibition_recommender.common.logging.TraceIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    void handleGeneric_hidesInternalErrorDetails() throws Exception {
        mockMvc.perform(get("/test/boom")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.message", not(containsString("NullPointerException"))))
                .andExpect(jsonPath("$.message", not(containsString("internal-db"))));
    }

    @Test
    void handleIllegalArgument_returnsSanitizedMessage() throws Exception {
        mockMvc.perform(get("/test/invalid-argument")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.message", not(containsString("drop_table"))));
    }

    @RestController
    static class ExceptionThrowingController {

        @GetMapping("/test/boom")
        String boom() {
            throw new NullPointerException("jdbc:mysql://internal-db:3306/drop_table");
        }

        @GetMapping("/test/invalid-argument")
        String invalidArgument() {
            throw new IllegalArgumentException("유효하지 않은 filter 값입니다: drop_table");
        }
    }
}
