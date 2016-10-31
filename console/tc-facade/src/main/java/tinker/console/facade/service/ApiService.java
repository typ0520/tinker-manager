package tinker.console.facade.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tinker.console.core.domain.AppInfo;
import tinker.console.core.domain.PatchInfo;
import tinker.console.core.domain.VersionInfo;
import tinker.console.core.mapper.AppMapper;
import tinker.console.core.mapper.PatchInfoMapper;
import tinker.console.core.mapper.VersionInfoMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by tong on 16/10/31.
 */
@Service
public class ApiService {
    @Autowired
    private VersionInfoMapper versionInfoMapper;

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private PatchInfoMapper patchInfoMapper;

    public AppInfo findAppByUid(String uid) {
        return appMapper.findByUid(uid);
    }

    public VersionInfo findVersionByUidAndVersionName(AppInfo appInfo, String versionName) {
        return versionInfoMapper.findByUidAndVersionName(appInfo.getUid(),versionName);
    }

    public List<PatchInfo> findByUidAndVersionName(String appUid,String versionName) {
        return patchInfoMapper.findByUidAndVersionName(appUid,versionName);
    }

    public PatchInfo findPatchInfoByUid(String uid) {
        return patchInfoMapper.findByUid(uid);
    }

    public PatchInfo getLatestNormalPatchInfo(List<PatchInfo> patchInfoList) {
        if (patchInfoList == null) {
            return null;
        }
        PatchInfo result = null;
        for (PatchInfo patchInfo : patchInfoList) {
            if (patchInfo.getStatus() == PatchInfo.STATUS_PUBLISHED
                    && patchInfo.getPublishType() == PatchInfo.PUBLISH_TYPE_NORMAL) {
                if ((result == null || patchInfo.getPatchVersion() > result.getPatchVersion()) && new File(patchInfo.getStoragePath()).exists()) {
                    result = patchInfo;
                }
            }
        }
        return result;
    }

    public PatchInfo getLatestGrayPatchInfo(List<PatchInfo> patchInfoList, String tag) {
        if (patchInfoList == null) {
            return null;
        }
        PatchInfo result = null;
        for (PatchInfo patchInfo : patchInfoList) {
            if (patchInfo.getStatus() == PatchInfo.STATUS_PUBLISHED
                    && patchInfo.getPublishType() == PatchInfo.PUBLISH_TYPE_GRAY) {
                if (((result == null || patchInfo.getPatchVersion() > result.getPatchVersion()) && patchInfo.getTags().contains(tag)) && new File(patchInfo.getStoragePath()).exists()) {
                    result = patchInfo;
                }
            }
        }
        return result;
    }

    public InputStream getDownloadStream(PatchInfo patchInfo) throws FileNotFoundException {
        return new FileInputStream(patchInfo.getStoragePath());
    }
}
