package com.dx168.tmserver.facade.service;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dx168.tmserver.core.utils.CacheEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dx168.tmserver.core.domain.AppInfo;
import com.dx168.tmserver.core.domain.PatchInfo;
import com.dx168.tmserver.core.domain.VersionInfo;
import com.dx168.tmserver.core.mapper.AppMapper;
import com.dx168.tmserver.core.mapper.PatchInfoMapper;
import com.dx168.tmserver.core.mapper.VersionInfoMapper;
import com.dx168.tmserver.facade.web.ApiController;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by tong on 16/10/31.
 */
@Service
public class ApiService {
    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private VersionInfoMapper versionInfoMapper;

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private PatchInfoMapper patchInfoMapper;

    private final Map<String,CacheEntry<AppInfo>> appInfoCache = new ConcurrentHashMap<>();
    private final Map<String,CacheEntry<VersionInfo>> versionInfoCache = new ConcurrentHashMap<>();
    private final Map<String,CacheEntry<PatchInfo>> patchInfoCache = new ConcurrentHashMap<>();
    private final Map<String,CacheEntry<List<PatchInfo>>> patchInfoListCache = new ConcurrentHashMap<>();
    private final Map<Integer,byte[]> fileCache = new ConcurrentHashMap<>();

    public AppInfo findAppInfo(String uid) {
        CacheEntry<AppInfo> cacheEntry = appInfoCache.get(uid);
        AppInfo appInfo = null;
        if (cacheEntry != null) {
            appInfo = cacheEntry.getEntry();
        }
        if (appInfo == null) {
            appInfo = appMapper.findByUid(uid);
            if (appInfo != null) {
                LOG.info("new app cache: " + appInfo.toString());
                appInfoCache.put(uid,new CacheEntry<>(appInfo, TimeUnit.MINUTES,10));
            }
        }
        return appInfo;
    }

    public VersionInfo findVersionInfo(String appUid, String versionName) {
        CacheEntry<VersionInfo> cacheEntry = versionInfoCache.get(appUid + "-" + versionName);
        VersionInfo versionInfo = null;
        if (cacheEntry != null) {
            versionInfo = cacheEntry.getEntry();
        }
        if (versionInfo == null) {
            versionInfo = versionInfoMapper.findByUidAndVersionName(appUid,versionName);
            if (versionInfo != null) {
                LOG.info("new version cache: " + versionInfo.toString());
                versionInfoCache.put(appUid + "-" + versionName,new CacheEntry<>(versionInfo, TimeUnit.MINUTES,10));
            }
        }
        return versionInfo;
    }

    public PatchInfo findPatchInfo(String uid) {
        CacheEntry<PatchInfo> cacheEntry = patchInfoCache.get(uid);
        PatchInfo patchInfo = null;
        if (cacheEntry != null) {
            patchInfo = cacheEntry.getEntry();
        }
        if (patchInfo == null) {
            patchInfo = patchInfoMapper.findByUid(uid);
            if (patchInfo != null) {
                LOG.info("new patch cache: " + patchInfo.toString());
                patchInfoCache.put(uid,new CacheEntry<>(patchInfo, TimeUnit.MINUTES,10));
            }
        }
        return patchInfo;
    }

    public List<PatchInfo> findPatchInfos(String appUid, String versionName) {
        CacheEntry<List<PatchInfo>> cacheEntry = patchInfoListCache.get(appUid + "-" + versionName);
        List<PatchInfo> patchInfoList = null;
        if (cacheEntry != null) {
            patchInfoList = cacheEntry.getEntry();
        }
        if (patchInfoList == null) {
            patchInfoList = patchInfoMapper.findByUidAndVersionName(appUid,versionName);
            if (patchInfoList != null) {
                LOG.info("new patch list cache: " + patchInfoList.toString());
                patchInfoListCache.put(appUid + "-" + versionName,new CacheEntry<>(patchInfoList, TimeUnit.MINUTES,10));
            }
        }

        return patchInfoList;
    }

    public PatchInfo getLatestNormalPatchInfo(List<PatchInfo> patchInfoList) {
        if (patchInfoList == null || patchInfoList.isEmpty()) {
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
        if (patchInfoList == null || patchInfoList.isEmpty()) {
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

    public InputStream getDownloadStream(PatchInfo patchInfo) throws IOException {
        byte[] fileContent = fileCache.get(patchInfo.getId());
        if (fileContent == null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(new FileInputStream(patchInfo.getStoragePath()), bos);

            LOG.info("new file cache: " + patchInfo.getId());
            fileCache.put(patchInfo.getId(),bos.toByteArray());
        }
        return new ByteArrayInputStream(fileContent);
    }

    public void clearCache() {
//        appInfoCache.clear();
//        versionInfoCache.clear();
        patchInfoCache.clear();
        patchInfoListCache.clear();
        //fileCache.clear();
    }
}
