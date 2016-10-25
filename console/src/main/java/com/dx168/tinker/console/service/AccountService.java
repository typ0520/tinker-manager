package com.dx168.tinker.console.service;

import com.dx168.tinker.console.bean.UserInfo;
import org.springframework.stereotype.Service;

/**
 * Created by tong on 16/10/25.
 */
@Service
public class AccountService {
    public boolean authenticate(String username,String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            return true;
        }
        return false;
    }

    public UserInfo getUserInfo(String username) {
        return new UserInfo(username);
    }
}
