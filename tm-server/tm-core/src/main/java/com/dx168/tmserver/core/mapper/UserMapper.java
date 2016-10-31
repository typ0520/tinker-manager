package com.dx168.tmserver.core.mapper;

import com.dx168.tmserver.core.domain.BasicUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface UserMapper {
    BasicUser findByUsername(String username);

    Integer insert(BasicUser basicUser);
}
