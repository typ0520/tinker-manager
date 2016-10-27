package tinker.console.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tinker.console.common.BizException;
import tinker.console.domain.AppInfo;
import tinker.console.domain.BasicUser;
import tinker.console.domain.VersionInfo;
import tinker.console.mapper.AppMapper;
import tinker.console.mapper.VersionInfoMapper;
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

    public AppInfo addApp(BasicUser basicUser,String appname,String description,String platform) {
        Integer rootUserId = accountService.getRootUserId(basicUser);
        AppInfo appInfo = appMapper.findByUserIdAndName(rootUserId,appname);
        if (appInfo != null) {
            throw new BizException("名字为: " + appname + "的应用已存在");
        }

        appInfo = new AppInfo();
        appInfo.setUserId(rootUserId);
        appInfo.setCreatedAt(new Date());
        appInfo.setUpdatedAt(new Date());
        appInfo.setAppname(appname);
        appInfo.setDescription(description);
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
}
