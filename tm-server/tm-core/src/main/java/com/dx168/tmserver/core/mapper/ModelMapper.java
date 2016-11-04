package com.dx168.tmserver.core.mapper;

import com.dx168.tmserver.core.domain.Model;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface ModelMapper {
    Integer insert(Model model);

    void deleteById(Integer id);

    Model findById(Integer id);

    List<Model> findAllByUserId(Integer userId);

    Model findByUserIdAndRegexp(@Param("userId") Integer userId,@Param("regularExp") String regularExp);
}
