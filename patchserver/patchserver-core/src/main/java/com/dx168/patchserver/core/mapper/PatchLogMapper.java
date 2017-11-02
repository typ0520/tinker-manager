package com.dx168.patchserver.core.mapper;

import com.dx168.patchserver.core.domain.PatchLog;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * Created by liangyuanqi on 17/9/20.
 */
@Mapper
public interface PatchLogMapper {
    Integer insert(PatchLog patchLog);

    Page<PatchLog> findByPage(Map<String, Object> param);

}
