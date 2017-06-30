package com.dx168.patchserver.facade.web;

import com.dx168.patchserver.core.domain.*;
import com.dx168.patchserver.facade.service.RequestStatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.dx168.patchserver.facade.dto.PatchInfoDto;
import com.dx168.patchserver.core.utils.BizAssert;
import com.dx168.patchserver.core.utils.BizException;
import com.dx168.patchserver.facade.common.RestResponse;
import com.dx168.patchserver.facade.service.ApiService;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by tong on 16/10/27.
 */
@Controller
public class ApiController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ApiService apiService;

    @Autowired
    private RequestStatService requestStatService;

    /**
     * 获取最新的补丁包信息
     * @param appUid                app唯一标示
     * @param token                 app的秘钥
     * @param versionName           应用版本号
     * @param tag                   标记(用于灰度发布)
     * @param platform              平台(Android|iOS)
     * @param osVersion             系统的版本号
     * @param model                 手机型号
     * @param channel               渠道号
     * @param sdkVersion            sdk版本号
     * @param deviceId              设备id
     * @param withFullUpdateInfo    是否带回全量更新信息(patchsdk 1.2.0才支持这个字段)
     * @return
     */
    @RequestMapping(value = "/api/patch",method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody RestResponse patch_info(HttpServletRequest req, String appUid, String token, String versionName, String tag,
                                                 String platform, String osVersion, String model,String channel, String sdkVersion, boolean debugMode,String deviceId,boolean withFullUpdateInfo) {
        requestStatService.increment();
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notNull(appUid,"应用唯一id不能为空");
            BizAssert.notEpmty(token,"令牌不能为空");
            BizAssert.notEpmty(versionName,"应用版本号不能为空");

            AppInfo appInfo = apiService.findAppInfo(appUid);
            if (appInfo == null) {
                throw new BizException("应用不存在");
            }

            if (!debugMode && !token.equals(DigestUtils.md5DigestAsHex((appUid + "_" + appInfo.getSecret()).getBytes()))) {
                throw new BizException("校验失败");
            }

            VersionInfo versionInfo = apiService.findVersionInfo(appUid,versionName);
            if (versionInfo == null) {
                throw new BizException("版本信息不正确");
            }

            if (withFullUpdateInfo) {
                //查询全量补丁信息
                FullUpdateInfo fullUpdateInfo = apiService.findFullUpdateInfoByAppUid(appUid);
                if (fullUpdateInfo != null) {
                    Map<String,Object> extra = new HashMap<>();
                    extra.put("fullUpdateInfo",fullUpdateInfo);
                    extra.put("needUpdate",versionName.compareTo(fullUpdateInfo.getLatestVersion()) < 0);
                    extra.put("forceUpdate",versionName.compareTo(fullUpdateInfo.getLowestSupportVersion()) < 0);

                    if (StringUtils.isEmpty(channel)) {
                        extra.put("downloadUrl",FullUpdateInfo.formatDownloadUrl(fullUpdateInfo.getDefaultUrl(),channel,fullUpdateInfo.getLatestVersion()));
                    }
                    else {
                        extra.put("downloadUrl",FullUpdateInfo.formatDownloadUrl(fullUpdateInfo.getChannelUrl(),channel,fullUpdateInfo.getLatestVersion()));
                    }

                    restR.setExtra(extra);
                }
            }

            if (!StringUtils.isEmpty(model)) {
                List<Pattern> patterns = apiService.getAllModelBlackListPattern(appInfo.getUserId());
                if (patterns != null && patterns.size() > 0) {
                    for (Pattern pattern : patterns) {
                        if (pattern.matcher(model).matches()) {
                            //这个机型在黑名单中
                            restR.setData(null);
                            return restR;
                        }
                    }
                }
            }

            if (!StringUtils.isEmpty(channel)) {
                List<Channel> channelList = apiService.getAllChannel(appInfo.getUserId());
                if (channelList != null && channelList.size() > 0) {
                    for (Channel channelInfo : channelList) {
                        if (channel.equals(channelInfo.getChannelName())) {
                            //这个渠道在黑名单中
                            restR.setData(null);
                            return restR;
                        }
                    }
                }
            }

            List<PatchInfo> patchInfoList = apiService.findPatchInfos(appUid,versionName);
            //查询最新的正常发布的补丁信息
            PatchInfo normalPatchInfo = apiService.getLatestNormalPatchInfo(patchInfoList);
            PatchInfo grayPatchInfo = null;
            if (tag != null && tag.trim().length() > 0) {
                //查询最新的灰度发布信息
                grayPatchInfo = apiService.getLatestGrayPatchInfo(patchInfoList,tag);
            }

            PatchInfo resultInfo = normalPatchInfo;
            if (grayPatchInfo != null) {
                resultInfo = grayPatchInfo;
            }

            if (resultInfo != null) {
                PatchInfoDto patchInfoDto = new PatchInfoDto();
                BeanUtils.copyProperties(resultInfo,patchInfoDto);
                patchInfoDto.setHash(DigestUtils.md5DigestAsHex((appUid + "_" + appInfo.getSecret() + "_" + resultInfo.getFileHash()).getBytes()));
                patchInfoDto.setCreatedTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(resultInfo.getCreatedAt()));

                restR.setData(patchInfoDto);
            }
            else {
                restR.setData(null);
            }
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }
        return restR;
    }

    /**
     * 报告补丁加载结果
     * @param appUid        app唯一标示
     * @param token         app的秘钥
     * @param versionName   应用版本号
     * @param tag           标记(用于灰度发布)
     * @param platform      平台(Android|iOS)
     * @param osVersion     系统的版本号
     * @param model         手机型号
     * @param channel       渠道号
     * @param sdkVersion    sdk版本号
     * @param deviceId      设备id
     * @param patchUid      补丁唯一标示
     * @param applyResult   是否加载成功
     * @return
     */
    @RequestMapping(value = "/api/report",method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody RestResponse report(HttpServletRequest req, String appUid, String token, String versionName, String tag, String platform, String osVersion,
                                             String model,String channel, String sdkVersion, boolean debugMode,String deviceId,String patchUid,boolean applyResult) throws Exception {
        requestStatService.increment();
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notNull(appUid,"应用唯一id不能为空");
            BizAssert.notEpmty(token,"令牌不能为空");
            BizAssert.notEpmty(versionName,"应用版本号不能为空");
            BizAssert.notEpmty(patchUid,"patchUid不能为空");

            AppInfo appInfo = apiService.findAppInfo(appUid);
            if (appInfo == null) {
                throw new BizException("应用不存在");
            }

            if (!debugMode && !token.equals(DigestUtils.md5DigestAsHex((appUid + "_" + appInfo.getSecret()).getBytes()))) {
                throw new BizException("校验失败");
            }
            VersionInfo versionInfo = apiService.findVersionInfo(appUid,versionName);
            if (versionInfo == null) {
                throw new BizException("版本信息不正确");
            }

            PatchInfo patchInfo = apiService.findPatchInfo(patchUid);
            if (patchInfo == null) {
                throw new BizException("补丁信息不存在");
            }

            if (!versionName.equals(patchInfo.getVersionName())) {
                throw new BizException("补丁信息不存在");
            }
            if (!appUid.equals(patchInfo.getAppUid())) {
                throw new BizException("补丁信息不存在");
            }

            apiService.report(patchInfo,applyResult);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }
        return restR;
    }

    @RequestMapping(value = "/api/clearCache", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody RestResponse clearCache() throws Exception {
        apiService.clearCache();
        LOG.info("clear cache.......");
        return new RestResponse();
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public @ResponseBody RestResponse health() throws Exception {
        RestResponse restR = new RestResponse();
        restR.setMessage("I am still alive");
        restR.setData(requestStatService.getStatInfo());
        return restR;
    }
}
