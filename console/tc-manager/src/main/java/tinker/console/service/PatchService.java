package tinker.console.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import tinker.console.core.domain.AppInfo;
import tinker.console.core.domain.PatchInfo;
import tinker.console.core.domain.VersionInfo;
import tinker.console.core.mapper.PatchInfoMapper;
import tinker.console.core.utils.BizException;
import java.io.*;
import java.util.*;

/**
 * Created by tong on 16/10/25.
 */
@Service
public class PatchService {
    @Autowired
    private PatchInfoMapper patchInfoMapper;

    @Value("${file_storage_path}")
    private String fileStoragePath;

    public List<PatchInfo> findByUidAndVersionName(String appUid,String versionName) {
        return patchInfoMapper.findByUidAndVersionName(appUid,versionName);
    }

    public PatchInfo savePatch(AppInfo appInfo, VersionInfo versionInfo, String description, MultipartFile multipartFile) {
        List<PatchInfo> patchInfoList = patchInfoMapper.findByUidAndVersionName(appInfo.getUid(),versionInfo.getVersionName());

        int maxPatchVersion = getMaxPatchVersion(patchInfoList) + 1;
        File path = new File(new File(fileStoragePath), appInfo.getUid() + File.separator + versionInfo.getVersionName() + File.separator + maxPatchVersion + File.separator);
        File patchFile = new File(path,"patch.apk");
        PatchInfo patchInfo = new PatchInfo();
        try {
            if (!path.exists() && !path.mkdirs()) {
                throw new BizException("文件目录创建失败");
            }
            String fileHash = DigestUtils.md5DigestAsHex(multipartFile.getBytes());
            multipartFile.transferTo(patchFile);

            patchInfo.setUserId(appInfo.getUserId());
            patchInfo.setAppUid(appInfo.getUid());
            patchInfo.setUid(UUID.randomUUID().toString().replaceAll("-", ""));
            patchInfo.setVersionName(versionInfo.getVersionName());
            patchInfo.setPatchVersion(maxPatchVersion);
            patchInfo.setPatchSize(patchFile.length());
            patchInfo.setFileHash(fileHash);
            patchInfo.setDescription(description);
            patchInfo.setStoragePath(patchFile.getAbsolutePath());
            patchInfo.setCreatedAt(new Date());
            patchInfo.setUpdatedAt(new Date());

            Integer id = patchInfoMapper.insert(patchInfo);
            patchInfo.setId(id);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BizException("文件保存失败");
        }
        return patchInfo;
    }

    private int getMaxPatchVersion(List<PatchInfo> patchInfoList) {
        int result = 0;
        if (patchInfoList != null) {
            for (PatchInfo patchInfo : patchInfoList) {
                if (patchInfo.getPatchVersion() > result) {
                    result = patchInfo.getPatchVersion();
                }
            }
        }
        return result;
    }

    public PatchInfo findByIdAndAppUid(Integer id, String appUid) {
        return patchInfoMapper.findByIdAndAppUid(id,appUid);
    }

    public void updateStatus(PatchInfo patchInfo) {
        patchInfo.setUpdatedAt(new Date());
        patchInfoMapper.updateStatus(patchInfo);
    }

    public void deletePatch(PatchInfo patchInfo) {
        patchInfoMapper.deleteById(patchInfo.getId());
//        File file = new File(patchInfo.getStoragePath());
//        try {
//            file.delete();
//        } catch (Exception e) {
//
//        }
    }
}
