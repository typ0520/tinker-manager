package com.dx168.patchserver.core.mapper;

import com.dx168.patchserver.core.domain.AppInfo;
import com.dx168.patchserver.core.domain.PatchInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface AppMapper {
    AppInfo findByUserIdAndName(@Param("userId") Integer userId, @Param("appname") String appname);

    AppInfo findByUserIdAndPackageName(@Param("userId") Integer userId, @Param("packageName") String packageName);

    Integer insert(AppInfo appInfo);

    List<AppInfo> findAllByUserId(Integer userId);

    AppInfo findByUid(String uid);

    void updatePackageName(AppInfo appInfo);
}
