package com.dx168.patchserver.core.mapper;

import com.dx168.patchserver.core.domain.AppInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface AppMapper {
    AppInfo findByUserIdAndName(@Param("userId") Integer userId, @Param("appname") String appname);

    Integer insert(AppInfo appInfo);

    List<AppInfo> findAllByUserId(Integer userId);

    AppInfo findByUid(String uid);
}
