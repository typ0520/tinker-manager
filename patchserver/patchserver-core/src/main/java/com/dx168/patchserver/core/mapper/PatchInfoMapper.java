package com.dx168.patchserver.core.mapper;

import com.dx168.patchserver.core.domain.PatchInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface PatchInfoMapper {
    Integer insert(PatchInfo patchInfo);

    List<PatchInfo> findByUidAndVersionName(@Param("appUid") String appUid, @Param("versionName") String versionName);

    PatchInfo findById(Integer id);

    PatchInfo findByUid(String uid);

    PatchInfo findByIdAndAppUid(@Param("id") Integer id,@Param("appUid") String appUid);

    void updateStatus(PatchInfo patchInfo);

    void deleteById(Integer id);
}
