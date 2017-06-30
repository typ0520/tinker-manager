package com.dx168.patchserver.core.mapper;

import com.dx168.patchserver.core.domain.ChildUserApp;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface ChildUserAppMapper {
    Integer insert(ChildUserApp childUserApp);

    List<ChildUserApp> findAllByUserId(Integer userId);

    void deleteByAppUid(String appUid);

    void deleteByUserId(Integer userId);
}
