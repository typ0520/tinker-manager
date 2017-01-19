package com.dx168.patchserver.manager.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dx168.patchserver.core.domain.AppInfo;
import com.dx168.patchserver.core.domain.BasicUser;
import com.dx168.patchserver.core.domain.VersionInfo;
import com.dx168.patchserver.core.mapper.AppMapper;
import com.dx168.patchserver.core.mapper.VersionInfoMapper;
import com.dx168.patchserver.core.utils.BizException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by tong on 16/10/26.
 */
@Service
public class AppService {
    @Autowired
    private AppMapper appMapper;

    @Autowired
    private VersionInfoMapper versionInfoMapper;

    @Autowired
    private AccountService accountService;

    public AppInfo addApp(BasicUser basicUser,String appname,String description,String packageName,String platform) {
        Integer rootUserId = accountService.getRootUserId(basicUser);
        AppInfo appInfo = appMapper.findByUserIdAndName(rootUserId,appname);
        if (appInfo != null) {
            throw new BizException("名字为: " + appname + "的应用已存在");
        }

        if (appMapper.findByUserIdAndPackageName(rootUserId,packageName) != null) {
            throw new BizException("包名为: " + packageName + "的应用已存在");
        }

        appInfo = new AppInfo();
        appInfo.setUserId(rootUserId);
        appInfo.setCreatedAt(new Date());
        appInfo.setUpdatedAt(new Date());
        appInfo.setAppname(appname);
        appInfo.setDescription(description);
        appInfo.setPackageName(packageName);
        appInfo.setPlatform(platform);
        appInfo.setUid(generateAppUid(rootUserId));
        appInfo.setSecret(UUID.randomUUID().toString().replaceAll("-", ""));
        Integer id = appMapper.insert(appInfo);
        appInfo.setId(id);
        return appInfo;
    }

    public String generateAppUid(Integer rootUserId) {
        int x = (int)(Math.random() * 9000) + 1000;
        String nowStr = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        return nowStr + "-" + (rootUserId + x);
    }

    public List<AppInfo> findAllAppInfoByUser(BasicUser basicUser) {
        return appMapper.findAllByUserId(accountService.getRootUserId(basicUser));
    }

    public AppInfo findByUid(String uid) {
        return appMapper.findByUid(uid);
    }

    public List<VersionInfo> findAllVersion(AppInfo appInfo) {
        return versionInfoMapper.findAllByAppUid(appInfo.getUid());
    }

    public VersionInfo findVersionByUidAndVersionName(AppInfo appInfo, String versionName) {
        return versionInfoMapper.findByUidAndVersionName(appInfo.getUid(),versionName);
    }

    public void saveVersionInfo(VersionInfo versionInfo) {
        versionInfo.setCreatedAt(new Date());
        versionInfo.setUpdatedAt(new Date());
        versionInfoMapper.insert(versionInfo);
    }

    //补全包名
    public void fillPackageName(BasicUser basicUser, String appUid, String packageName) {
        AppInfo appInfo = findByUid(appUid);
        if (StringUtils.isNotBlank(appInfo.getPackageName())) {
            throw new BizException("此app的包名已经补全");
        }
//        Integer rootUserId = accountService.getRootUserId(basicUser);
//        if (appMapper.findByUserIdAndPackageName(rootUserId,packageName) != null) {
//            throw new BizException("包名为: " + packageName + "的应用已存在");
//        }
        appInfo.setPackageName(packageName);
        appMapper.updatePackageName(appInfo);
    }
}
