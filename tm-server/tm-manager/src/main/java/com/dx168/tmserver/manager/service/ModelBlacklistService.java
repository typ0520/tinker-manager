package com.dx168.tmserver.manager.service;

import com.dx168.tmserver.core.domain.Model;
import com.dx168.tmserver.core.mapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

/**
 * Created by tong on 16/11/1.
 */
@Service
public class ModelBlacklistService {
    @Autowired
    private ModelMapper modelMapper;

    public void save(Model model) {
        model.setCreatedAt(new Date());
        model.setUpdatedAt(new Date());
        int id = modelMapper.insert(model);
        model.setId(id);
    }

    public List<Model> findAllByUserId(Integer userId) {
        return modelMapper.findAllByUserId(userId);
    }

    public Model findById(Integer id) {
        return modelMapper.findById(id);
    }

    public Model findByRegexp(Integer userId, String regexp) {
        return modelMapper.findByUserIdAndRegexp(userId,regexp);
    }

    public void delete(Model model) {
        modelMapper.deleteById(model.getId());
    }
}
