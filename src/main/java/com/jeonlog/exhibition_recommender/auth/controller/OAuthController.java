package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {

    private final UserRepository userRepository;

    @GetMapping("/add-info")
    public String addInfoForm(@RequestParam(value = "error", required = false) String error,
                              Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "oauth-add-info"; // Thymeleaf 폼 뷰
    }

    @PostMapping("/add-info")
    public String addInfoSubmit(@RequestParam("gender") Gender gender,
                                @RequestParam("birthYear") Integer birthYear,
                                @RequestParam("nickname") String nickname,
                                HttpSession session) {

        OAuthAttributes tempAttr = (OAuthAttributes) session.getAttribute("tempOAuthAttributes");
        if (tempAttr == null) return "redirect:/";

        //  닉네임 중복 검사
        if (userRepository.existsByNickname(nickname)) {
            return "redirect:/oauth/add-info?error=이미 사용 중인 닉네임입니다.";
        }

        User user = User.builder()
                .name(tempAttr.getName())
                .email(tempAttr.getEmail())
                .oauthProvider(tempAttr.getOauthProvider())
                .oauthId(tempAttr.getOauthId())
                .gender(gender)
                .birthYear(birthYear)
                .nickname(nickname)                      // ✅ 닉네임 저장
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        session.setAttribute("user", user);
        session.removeAttribute("tempOAuthAttributes");

        return "redirect:/"; // 홈으로 리디렉트
    }
}