package com.dx168.patchserver.manager.service;

import com.dx168.patchserver.core.domain.Channel;
import com.dx168.patchserver.core.mapper.ChannelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

/**
 * Created by tong on 16/11/10.
 */
@Service
public class ChannelService {
    @Autowired
    private ChannelMapper channelMapper;

    public void save(Channel Channel) {
        Channel.setCreatedAt(new Date());
        Channel.setUpdatedAt(new Date());
        int id = channelMapper.insert(Channel);
        Channel.setId(id);
    }

    public List<Channel> findAllByUserId(Integer userId) {
        return channelMapper.findAllByUserId(userId);
    }

    public Channel findById(Integer id) {
        return channelMapper.findById(id);
    }

    public Channel findByUserIdAndName(Integer userId, String channelName) {
        return channelMapper.findByUserIdAndName(userId,channelName);
    }

    public void delete(Channel Channel) {
        channelMapper.deleteById(Channel.getId());
    }
}
