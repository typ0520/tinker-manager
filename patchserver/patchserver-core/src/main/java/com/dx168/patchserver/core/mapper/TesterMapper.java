package com.dx168.patchserver.core.mapper;

import com.dx168.patchserver.core.domain.Tester;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface TesterMapper {
    Integer insert(Tester tester);

    Tester findByTagAndUid(@Param("tag") String tag, @Param("appUid") String appUid);

    List<Tester> findAllByAppUid(String appUid);

    void deleteById(Integer id);

    Tester findById(Integer id);
}
