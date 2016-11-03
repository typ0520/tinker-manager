package com.dx168.tmserver.manager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import com.dx168.tmserver.core.domain.BasicUser;
import com.dx168.tmserver.core.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * Created by tong on 16/10/25.
 */
@Service
public class AccountService {
    @Autowired
    private UserMapper userMapper;

    @Value("${global_secret_key}")
    private String globalSecretKey;

    public boolean authenticate(String username,String password) {
        BasicUser basicUser = findByUsername(username);
        if (basicUser == null) {
            return false;
        }
        String correctPassword = DigestUtils.md5DigestAsHex((globalSecretKey + "_" + password).getBytes());
        return basicUser.getPassword().equals(correctPassword);
    }

    public BasicUser findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public void save(BasicUser basicUser) {
        basicUser.setPassword(DigestUtils.md5DigestAsHex((globalSecretKey + "_" + basicUser.getPassword()).getBytes()));
        basicUser.setCreatedAt(new Date());
        basicUser.setUpdatedAt(new Date());
        Integer id = userMapper.insert(basicUser);
        basicUser.setId(id);
    }

    public BasicUser findById(Integer id) {
        return userMapper.findById(id);
    }

    public Integer getRootUserId(BasicUser basicUser) {
        if (basicUser.isChildAccount()) {
            return basicUser.getParentId();
        }
        return basicUser.getId();
    }
}
