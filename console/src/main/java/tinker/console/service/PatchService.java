package tinker.console.service;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import tinker.console.common.BizException;
import tinker.console.domain.AppInfo;
import tinker.console.domain.PatchInfo;
import tinker.console.domain.VersionInfo;
import tinker.console.mapper.PatchInfoMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by tong on 16/10/25.
 */
@Service
public class PatchService {
    @Autowired
    private PatchInfoMapper patchInfoMapper;

    @Value("${file_storage_path}")
    private String fileStoragePath;

    public void save(PatchInfo patchInfo) {
        Integer id = patchInfoMapper.insert(patchInfo);
        patchInfo.setId(id);
    }

    public List<PatchInfo> findByUidAndVersionName(@Param("appUid") String appUid, @Param("versionName") String versionName) {
        return patchInfoMapper.findByUidAndVersionName(appUid,versionName);
    }

    public PatchInfo savePatch(AppInfo appInfo, VersionInfo versionInfo, String description, CommonsMultipartFile multipartFile) {
        List<PatchInfo> patchInfoList = patchInfoMapper.findByUidAndVersionName(appInfo.getUid(),versionInfo.getVersionName());

        int maxPatchVersion = getMaxPatchVersion(patchInfoList) + 1;
        File path = new File(new File(fileStoragePath), appInfo.getUid() + File.separator + versionInfo.getVersionName() + File.separator + maxPatchVersion + File.separator + "patch.apk");
        try {
            multipartFile.transferTo(path);
        } catch (IOException e) {
            throw new BizException("文件保存失败");
        }
        return null;
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
}
