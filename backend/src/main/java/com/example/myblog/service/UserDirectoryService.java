package com.example.myblog.service;

import com.example.myblog.domain.view.SearchUserResponse;
import org.springframework.stereotype.Service;
import com.example.myblog.mapper.UserAccountMapper;

@Service
public class UserDirectoryService {

    private final UserAccountMapper userAccountMapper;

    public UserDirectoryService(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    public SearchUserResponse search(String currentAccount, String query, int limit) {
        int boundedLimit = Math.clamp(limit, 1, 20);
        return new SearchUserResponse(userAccountMapper.searchByAccount(query.trim(), currentAccount, boundedLimit));
    }
}
