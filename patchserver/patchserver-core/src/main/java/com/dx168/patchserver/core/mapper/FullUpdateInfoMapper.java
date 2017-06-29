package com.dx168.patchserver.core.mapper;

import com.dx168.patchserver.core.domain.FullUpdateInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by tong on 17/6/29.
 */
@Mapper
public interface FullUpdateInfoMapper {
    FullUpdateInfo findByAppUid(String appUid);

    Integer insert(FullUpdateInfo fullUpdateInfo);

    void update(FullUpdateInfo fullUpdateInfo);
}
