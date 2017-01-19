package com.dx168.patchserver.manager.service;

import com.dx168.patchserver.core.domain.Tester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import com.dx168.patchserver.core.domain.AppInfo;
import com.dx168.patchserver.core.domain.PatchInfo;
import com.dx168.patchserver.core.domain.VersionInfo;
import com.dx168.patchserver.core.mapper.PatchInfoMapper;
import com.dx168.patchserver.core.utils.BizException;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tong on 16/10/25.
 */
@Service
public class PatchService {
    private static final Logger LOG = LoggerFactory.getLogger(FacadeService.class);

    @Value("${file_storage_path}")
    private String fileStoragePath;

    @Value("${patch-static-url}")
    private String patchStaticUrl;

    @Value("${tm-manager-url}")
    private String managerUrl;

    @Autowired
    private PatchInfoMapper patchInfoMapper;

    @Autowired
    private AppService appService;

    @Autowired
    private FacadeService facadeService;

    @Autowired
    private TesterService testerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private JavaMailSender javaMailSender;

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    public List<PatchInfo> findByUidAndVersionName(String appUid,String versionName) {
        return patchInfoMapper.findByUidAndVersionName(appUid,versionName);
    }

    public PatchInfo savePatch(AppInfo appInfo, VersionInfo versionInfo, String description, MultipartFile multipartFile) {
        List<PatchInfo> patchInfoList = patchInfoMapper.findByUidAndVersionName(appInfo.getUid(),versionInfo.getVersionName());
        int maxPatchVersion = getMaxPatchVersion(patchInfoList) + 1;
        String childPath = appInfo.getUid() + "/" + versionInfo.getVersionName() + "/" + maxPatchVersion + "/";
        PatchInfo patchInfo = new PatchInfo();
        try {
            String fileHash = DigestUtils.md5DigestAsHex(multipartFile.getBytes());
            String fileName = fileHash + "_patch.zip";
            File path = new File(new File(fileStoragePath), childPath);
            File patchFile = new File(path,fileName);

            if (!path.exists() && !path.mkdirs()) {
                throw new BizException("文件目录创建失败");
            }
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
            patchInfo.setDownloadUrl(getDownloadUrl(patchStaticUrl,childPath + fileName));
            patchInfo.setCreatedAt(new Date());
            patchInfo.setUpdatedAt(new Date());

            Integer id = patchInfoMapper.insert(patchInfo);
            patchInfo.setId(id);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BizException("文件保存失败");
        }

        facadeService.clearCache();
        return patchInfo;
    }

    private String getDownloadUrl(String patchStaticUrl, String rel) {
        if (!patchStaticUrl.endsWith("/") && !rel.startsWith("/")) {
            patchStaticUrl = patchStaticUrl + "/";
        }

        return patchStaticUrl + rel;
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

    public void updateStatus(final PatchInfo patchInfo) {
        final PatchInfo oldInfo = patchInfoMapper.findById(patchInfo.getId());
        patchInfo.setUpdatedAt(new Date());
        patchInfoMapper.updateStatus(patchInfo);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyPatchStatusChanged(oldInfo,patchInfo);
                } catch (Throwable e) {
                    LOG.error("通知补丁状态变化失败: " + e.getMessage());
                }
            }
        });
    }

    private void notifyPatchStatusChanged(PatchInfo oldInfo, PatchInfo patchInfo) {
        //通知facade清空缓存
        facadeService.clearCache();

        if (patchInfo.getTags() == null) {
            return;
        }

        List<Tester> testerList = null;
        try {
            testerList = testerService.findAllByAppUid(patchInfo.getAppUid());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (testerList == null || testerList.isEmpty()) {
            return;
        }

        List<String> emailList = new ArrayList<>();
        for (Tester tester : testerList) {
            if (patchInfo.getTags().contains(tester.getTag())) {
                emailList.add(tester.getEmail());
            }
        }

        if (emailList.isEmpty()) {
            return;
        }

        AppInfo appInfo = appService.findByUid(patchInfo.getAppUid());

        String[] sendTo = new String[emailList.size()];
        for (int i = 0; i < emailList.size(); i++) {
            sendTo[i] = emailList.get(i);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("应用名称: " + appInfo.getAppname());
        sb.append("\n");
        sb.append("应用版本: " + patchInfo.getVersionName());
        sb.append("\n");
        sb.append("补丁版本: " + patchInfo.getPatchVersion());
        sb.append("\n");
        sb.append("补丁描述: " + patchInfo.getDescription());
        sb.append("\n");
        sb.append("补丁状态: " + oldInfo.getStatusDesc() + " => " + patchInfo.getStatusDesc());
        sb.append("\n");
        if (patchInfo.getStatus() != PatchInfo.STATUS_UNPUBLISHED) {
            sb.append("发布类型: " + patchInfo.getPublishTypeDesc());
            sb.append("\n");
        }
        sb.append("操作账号: " + accountService.findById(patchInfo.getUserId()).getUsername());
        sb.append("\n");
        sb.append("操作时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        sb.append("\n");
        sb.append(managerUrl);
        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(mail, true);
                helper.setTo(sendTo);
                helper.setSubject("[Tinker-热修复管理平台]-补丁状态变化了");
                helper.setText(sb.toString());
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            javaMailSender.send(mail);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        //TODO 如果是已发布状态 通过推送告诉客户端来获取新的patch
    }

    public void deletePatch(PatchInfo patchInfo) {
        patchInfoMapper.deleteById(patchInfo.getId());
//        File file = new File(patchInfo.getStoragePath());
//        try {
//            file.delete();
//        } catch (Exception e) {
//
//        }

        facadeService.clearCache();
    }
}
