package com.jeonlog.exhibition_recommender.auth.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "user") //자루 부투랑ㄴㅁ
public @interface CurrentUser {
}