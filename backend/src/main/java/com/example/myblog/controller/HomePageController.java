package com.example.myblog.controller;

import com.example.myblog.domain.view.HomePageView;
import com.example.myblog.service.HomePageService;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class HomePageController {

    private final HomePageService homePageService;

    public HomePageController(HomePageService homePageService) {
        this.homePageService = homePageService;
    }

    @GetMapping("/me/home")
    public HomePageView myHome(Principal principal) {
        return homePageService.getOwnHomePage(principal.getName());
    }

    @GetMapping("/{account}/home")
    public HomePageView userHome(@PathVariable String account, Principal principal) {
        return homePageService.getHomePage(principal.getName(), account);
    }
}

