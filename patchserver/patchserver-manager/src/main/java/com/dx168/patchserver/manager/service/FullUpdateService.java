package com.dx168.patchserver.manager.service;

import com.dx168.patchserver.core.domain.FullUpdateInfo;
import com.dx168.patchserver.core.mapper.FullUpdateInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * Created by tong on 17/6/29.
 */
@Service
public class FullUpdateService {
    @Autowired
    private FullUpdateInfoMapper fullUpdateInfoMapper;

    @Autowired
    private FacadeService facadeService;

    public FullUpdateInfo findByAppUid(String appUid) {
        return fullUpdateInfoMapper.findByAppUid(appUid);
    }

    public void saveOrUpdate(FullUpdateInfo fullUpdateInfo) {
        FullUpdateInfo dbFullUpdateInfo = findByAppUid(fullUpdateInfo.getAppUid());
        if (dbFullUpdateInfo == null) {
            save(fullUpdateInfo);
        }
        else {
            fullUpdateInfo.setId(dbFullUpdateInfo.getId());
            update(fullUpdateInfo);
        }

        facadeService.clearCache();
    }

    private void save(FullUpdateInfo fullUpdateInfo) {
        fullUpdateInfo.setCreatedAt(new Date());
        fullUpdateInfo.setUpdatedAt(fullUpdateInfo.getCreatedAt());

        Integer id = fullUpdateInfoMapper.insert(fullUpdateInfo);
        fullUpdateInfo.setId(id);
    }

    private void update(FullUpdateInfo fullUpdateInfo) {
        fullUpdateInfo.setUpdatedAt(new Date());
        fullUpdateInfoMapper.update(fullUpdateInfo);
    }
}
