package com.jeonlog.exhibition_recommender.web;

import com.jeonlog.exhibition_recommender.domain.user.Gender;
import com.jeonlog.exhibition_recommender.domain.user.User;
import com.jeonlog.exhibition_recommender.domain.user.UserRepository;
import com.jeonlog.exhibition_recommender.oauth.OAuthAttributes;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class GoogleOAuthController {

    private final UserRepository userRepository;

    @GetMapping("/add-info")
    public String addInfoForm() {
        return "oauth-add-info";
    }

    @PostMapping("/add-info")
    public String addInfoSubmit(@RequestParam("gender") Gender gender,
                                @RequestParam("birthYear") Integer birthYear,
                                HttpSession session) {

        OAuthAttributes tempAttr = (OAuthAttributes) session.getAttribute("tempOAuthAttributes");
        if (tempAttr == null)
            return "redirect:/";

        User user = User.builder()
                .name(tempAttr.getName())
                .email(tempAttr.getEmail())
                .oauthProvider(tempAttr.getOauthProvider())
                .oauthId(tempAttr.getOauthId())
                .gender(gender)
                .birthYear(birthYear)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        session.setAttribute("user", user);
        session.removeAttribute("tempOAuthAttributes");

        return "redirect:/";
    }

}
