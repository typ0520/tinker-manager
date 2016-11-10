package com.dx168.patchserver.core.mapper;

import com.dx168.patchserver.core.domain.Channel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface ChannelMapper {
    Integer insert(Channel Channel);

    void deleteById(Integer id);

    Channel findById(Integer id);

    List<Channel> findAllByUserId(Integer userId);

    Channel findByUserIdAndName(@Param("userId") Integer userId, @Param("channelName") String regularExp);
}
