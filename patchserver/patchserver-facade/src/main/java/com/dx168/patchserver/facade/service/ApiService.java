package com.dx168.patchserver.facade.service;

import com.dx168.patchserver.core.domain.*;
import com.dx168.patchserver.core.mapper.*;
import com.dx168.patchserver.facade.dto.PatchCounter;
import com.dx168.patchserver.facade.web.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dx168.patchserver.core.utils.CacheEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

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

    @Autowired
    private PatchLogMapper patchLogMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private FullUpdateInfoMapper fullUpdateInfoMapper;

    private final Map<String,CacheEntry<AppInfo>> appInfoCache = new ConcurrentHashMap<>();
    private final Map<String,CacheEntry<VersionInfo>> versionInfoCache = new ConcurrentHashMap<>();
    private final Map<String,CacheEntry<PatchInfo>> patchInfoCache = new ConcurrentHashMap<>();
    private final Map<String,CacheEntry<List<PatchInfo>>> patchInfoListCache = new ConcurrentHashMap<>();
    private final Map<Integer,CacheEntry<List<Pattern>>> modelBlackListPatternCache = new ConcurrentHashMap<>();
    private final Map<Integer,CacheEntry<List<Channel>>> channelListCache = new ConcurrentHashMap<>();
    private final Map<String,CacheEntry<FullUpdateInfo>> fullUpdateInfoCache = new ConcurrentHashMap<>();
    private final Map<Integer,PatchCounter> patchCounterCache = new ConcurrentHashMap<>();

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
                if ((result == null || patchInfo.getPatchVersion() > result.getPatchVersion())) {
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
                if (((result == null || patchInfo.getPatchVersion() > result.getPatchVersion()) && patchInfo.getTags().contains(tag))) {
                    result = patchInfo;
                }
            }
        }
        return result;
    }

    public List<Pattern> getAllModelBlackListPattern(Integer userId) {
        CacheEntry<List<Pattern>> cacheEntry = modelBlackListPatternCache.get(userId);
        List<Pattern> patterns = null;
        if (cacheEntry != null) {
            patterns = cacheEntry.getEntry();
        }
        if (patterns == null) {
            List<Model> modelList = modelMapper.findAllByUserId(userId);
            patterns = new ArrayList<>();

            if (modelList != null) {
                for (Model model : modelList) {
                    try {
                        patterns.add(Pattern.compile(model.getRegularExp()));
                    } catch (Throwable e) {

                    }
                }
            }

            LOG.info("new model blacklist list cache: " + patterns);
            modelBlackListPatternCache.put(userId,new CacheEntry<List<Pattern>>(patterns,TimeUnit.MINUTES,10));
        }
        return patterns;
    }

    public List<Channel> getAllChannel(Integer userId) {
        CacheEntry<List<Channel>> cacheEntry = channelListCache.get(userId);
        List<Channel> channelList = null;
        if (cacheEntry != null) {
            channelList = cacheEntry.getEntry();
        }
        if (channelList == null) {
            channelList = channelMapper.findAllByUserId(userId);

            if (channelList == null) {
                channelList = new ArrayList<>();
            }

            LOG.info("new channel blacklist list cache: " + channelList);
            channelListCache.put(userId,new CacheEntry<List<Channel>>(channelList,TimeUnit.MINUTES,10));
        }
        return channelList;
    }

    public FullUpdateInfo findFullUpdateInfoByAppUid(String appUid) {
        CacheEntry<FullUpdateInfo> cacheEntry = fullUpdateInfoCache.get(appUid);
        FullUpdateInfo fullUpdateInfo = null;
        if (cacheEntry != null) {
            fullUpdateInfo = cacheEntry.getEntry();
        }
        if (fullUpdateInfo == null) {
            fullUpdateInfo = fullUpdateInfoMapper.findByAppUid(appUid);
            if (fullUpdateInfo != null) {
                LOG.info("new full update info cache: " + fullUpdateInfo.toString());
                fullUpdateInfoCache.put(appUid,new CacheEntry<>(fullUpdateInfo, TimeUnit.MINUTES,10));
            }
        }
        return fullUpdateInfo;
    }

    public void clearCache() {
        appInfoCache.clear();
        versionInfoCache.clear();
        //fileCache.clear();

        patchInfoCache.clear();
        patchInfoListCache.clear();
        modelBlackListPatternCache.clear();
        fullUpdateInfoCache.clear();
    }

    public void report(PatchInfo patchInfo, boolean applyResult) {
        PatchCounter patchCounter = patchCounterCache.get(patchInfo.getId());

        if (patchCounter == null) {
            patchCounter = new PatchCounter();
            patchCounter.setId(patchInfo.getId());
            patchCounter.setAtomicApplySuccessSize(new AtomicInteger(patchInfo.getApplySuccessSize()));
            patchCounter.setAtomicApplySize(new AtomicInteger(patchInfo.getApplySize()));

            patchCounterCache.put(patchCounter.getId(),patchCounter);
            LOG.info("new patch counter cache: " + patchCounter);
        }
        patchCounter.getAtomicApplySize().getAndIncrement();
        if (applyResult) {
            patchCounter.getAtomicApplySuccessSize().getAndIncrement();
        }
    }

    @Scheduled(cron="0 0/1 8-20 * * ?")
    public void syncPatch() {
        LOG.info("start sync： " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        Collection<PatchCounter> patchCounterList = patchCounterCache.values();

        for (PatchCounter patchCounter : patchCounterList) {
            if (patchCounter.getAtomicApplySize() != null && patchCounter.getAtomicApplySuccessSize() != null) {
                LOG.info("update count： " + patchCounter);
                patchInfoMapper.updateCount(patchCounter.getId(),patchCounter.getAtomicApplySuccessSize().get(),patchCounter.getAtomicApplySize().get(),new Date());
            }
        }
    }

    public void patchLog(PatchLog patchLog) {
        patchLogMapper.insert(patchLog);
    }

}
