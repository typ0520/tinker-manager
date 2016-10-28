package tinker.console.web;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import tinker.console.common.BizAssert;
import tinker.console.common.BizException;
import tinker.console.common.RestResponse;
import tinker.console.domain.AppInfo;
import tinker.console.domain.PatchInfo;
import tinker.console.domain.VersionInfo;
import tinker.console.dto.PatchInfoDto;
import tinker.console.service.AppService;
import tinker.console.service.PatchService;
import tinker.console.utils.BeanMapConvertUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by tong on 16/10/27.
 */
@Controller
public class ApiController {
    @Autowired
    private AppService appService;

    @Autowired
    private PatchService patchService;

    @Value("${server_path}")
    private String serverPath;

    /**
     * 获取最新的补丁包信息
     * @param appUid        app唯一标示
     * @param token         app的秘钥
     * @param versionName   应用版本号
     * @param tag           标记(用于灰度发布)
     * @param platform      平台(Android|iOS)
     * @param osVersion     系统的版本号
     * @param model         手机型号
     * @param sdkVersion    sdk版本号
     * @return
     */
    @RequestMapping(value = "/api/patch",method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody RestResponse gray_publish(String appUid, String token,String versionName,String tag,String platform,String osVersion,String model,String sdkVersion) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notNull(appUid,"应用唯一id不能为空");
            BizAssert.notEpmty(token,"令牌不能为空");
            BizAssert.notEpmty(versionName,"应用版本号不能为空");

            AppInfo appInfo = appService.findByUid(appUid);
            if (appInfo == null) {
                throw new BizException("应用不存在");
            }

            VersionInfo versionInfo = appService.findVersionByUidAndVersionName(appInfo,versionName);
            if (versionInfo == null) {
                throw new BizException("版本信息不正确");
            }

            List<PatchInfo> patchInfoList = patchService.findByUidAndVersionName(appUid,versionName);
            //查询最新的正常发布的补丁信息
            PatchInfo normalPatchInfo = patchService.getLatestNormalPatchInfo(patchInfoList);
            PatchInfo grayPatchInfo = null;
            if (tag != null && tag.trim().length() > 0) {
                //查询最新的灰度发布信息
                grayPatchInfo = patchService.getLatestGrayPatchInfo(patchInfoList,tag);
            }

            PatchInfo resultInfo = normalPatchInfo;
            if (grayPatchInfo != null) {
                resultInfo = grayPatchInfo;
            }

            if (resultInfo != null) {
                PatchInfoDto patchInfoDto = new PatchInfoDto();
                BeanUtils.copyProperties(resultInfo,patchInfoDto);
                patchInfoDto.setHash(DigestUtils.md5DigestAsHex((appUid + "_" + token + "_" + resultInfo.getFileHash()).getBytes()));
                patchInfoDto.setCreatedTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(resultInfo.getCreatedAt()));
                if (serverPath.endsWith("/")) {
                    patchInfoDto.setDownloadUrl(serverPath + "api/patch/" + resultInfo.getId());
                }
                else {
                    patchInfoDto.setDownloadUrl(serverPath + "/api/patch/" + resultInfo.getId());
                }

                restR.getData().putAll(BeanMapConvertUtil.convertBean2Map(patchInfoDto));
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

    @RequestMapping(value = "/api/patch/{patchId}", method = RequestMethod.GET)
    public void getFile(@PathVariable("patchId") Integer patchId, HttpServletRequest req, HttpServletResponse response) {
        try {
            PatchInfo patchInfo = patchService.findById(patchId);
            InputStream is = patchService.getDownloadStream(patchInfo);
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }
}
