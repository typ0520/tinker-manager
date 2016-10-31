package com.dx168.tmserver.core.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.dx168.tmserver.core.domain.AppInfo;
import java.util.List;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface AppMapper {
    AppInfo findByUserIdAndName(@Param("userId") Integer userId,@Param("appname") String appname);

    Integer insert(AppInfo appInfo);

    List<AppInfo> findAllByUserId(Integer userId);

    AppInfo findByUid(String uid);
}
