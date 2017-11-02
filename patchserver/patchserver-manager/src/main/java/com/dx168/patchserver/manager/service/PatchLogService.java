package com.dx168.patchserver.manager.service;

import com.dx168.patchserver.core.domain.PatchLog;
import com.dx168.patchserver.core.mapper.PatchLogMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by guoyongping on 17/10/12.
 */
@Service
public class PatchLogService {
    private static final Logger LOG = LoggerFactory.getLogger(PatchLogService.class);

    @Autowired
    private PatchLogMapper patchLogMapper;

    public Page<PatchLog> findByPage(Map param,int pageNo, int pageSize){
        PageHelper.startPage(pageNo, pageSize);
        return patchLogMapper.findByPage(param);
    }
}
