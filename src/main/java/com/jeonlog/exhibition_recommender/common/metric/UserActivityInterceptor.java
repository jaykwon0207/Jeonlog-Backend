package com.jeonlog.exhibition_recommender.common.metric;

import com.jeonlog.exhibition_recommender.auth.model.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class UserActivityInterceptor implements HandlerInterceptor {

    private final MetricRecorder recorder;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        Long userId = currentUserId();
        if (userId != null) {
            recorder.recordActiveUser(userId);
            recorder.recordHeartbeat(userId);
        }
        return true;
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUser().getId();
        }
        return null;
    }
}
