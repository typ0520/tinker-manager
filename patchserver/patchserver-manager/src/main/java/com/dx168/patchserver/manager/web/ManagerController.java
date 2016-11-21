package com.dx168.patchserver.manager.web;

import com.dx168.patchserver.core.domain.*;
import com.dx168.patchserver.manager.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.dx168.patchserver.manager.common.Constants;
import com.dx168.patchserver.manager.common.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import com.dx168.patchserver.core.utils.BizAssert;
import com.dx168.patchserver.core.utils.BizException;
import com.dx168.patchserver.core.utils.HttpRequestUtils;
import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by tong on 15/10/24.
 */
@Controller
public class ManagerController {
    @Autowired
    private AppService appService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PatchService patchService;

    @Autowired
    private TesterService testerService;

    @Autowired
    private ModelBlacklistService modelBlacklistService;

    @Autowired
    private ChannelService channelService;

    @Value("${spring.http.multipart.max-file-size}")
    private String maxPatchSize;

    @RequestMapping("/404")
    public String pageNotFound() {
        return "404";
    }

    @RequestMapping(value = "/",method = RequestMethod.GET)
    public ModelAndView index() {
        return new ModelAndView("redirect:/app/list");
    }

    @RequestMapping(value = "/app/list",method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest req) {
        RestResponse restR = new RestResponse();

        BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
        List<AppInfo> appInfoList = appService.findAllAppInfoByUser(basicUser);
        restR.getData().put("user",basicUser);
        restR.getData().put("appInfoList",appInfoList);
        return new ModelAndView("app_list","restR",restR);
    }

    @RequestMapping(value = "/app/create",method = RequestMethod.POST)
    public @ResponseBody RestResponse app_create(HttpServletRequest req, String appname, String description) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notEpmty(appname,"应用名不能为空");
            BizAssert.notEpmty(description,"描述信息不能为空");
            BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            appService.addApp(basicUser,appname,description,"Android");
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }
        return restR;
    }

    @RequestMapping(value = "/app",method = RequestMethod.GET)
    public ModelAndView app(HttpServletRequest req, String appUid) {
        RestResponse restR = new RestResponse();
        BizAssert.notEpmty(appUid,"应用编号不能为空");
        AppInfo appInfo = appService.findByUid(appUid);
        BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
        List<AppInfo> appInfoList = appService.findAllAppInfoByUser(basicUser);
        restR.getData().put("user",basicUser);
        restR.getData().put("appInfo",appInfo);
        restR.getData().put("appInfoList",appInfoList);
        restR.getData().put("versionList",appService.findAllVersion(appInfo));
        return new ModelAndView("app","restR",restR);
    }

    @RequestMapping(value = "/tester/list",method = RequestMethod.GET)
    public ModelAndView tester_list(HttpServletRequest req,String appUid) {
        RestResponse restR = new RestResponse();
        BizAssert.notEpmty(appUid,"应用编号不能为空");
        AppInfo appInfo = appService.findByUid(appUid);
        BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
        restR.getData().put("user",basicUser);
        restR.getData().put("appInfo",appInfo);
        restR.getData().put("testerList",testerService.findAllByAppUid(appUid));
        restR.getData().put("appInfoList",appService.findAllAppInfoByUser(basicUser));
        return new ModelAndView("tester_list","restR",restR);
    }

    @RequestMapping(value = "/tester/add",method = RequestMethod.POST)
    public @ResponseBody RestResponse addTester(HttpServletRequest req,String appUid,String tag,String email,String description) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notEpmty(appUid,"应用号不能为空");
            BizAssert.notEpmty(tag,"tag不能为空");
            BizAssert.notEpmty(email,"email不能为空");
            BizAssert.notEpmty(tag,"版本号不能为空");

            Tester tester = testerService.findByTagAndUid(tag,appUid);
            if (tester != null) {
                throw new BizException("测试tag已存在");
            }
            BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            tester = new Tester();
            tester.setUserId(accountService.getRootUserId(basicUser));
            tester.setAppUid(appUid);
            tester.setTag(tag);
            tester.setEmail(email);
            tester.setDescription(description);

            testerService.save(tester);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/tester/del",method = RequestMethod.POST)
    public @ResponseBody RestResponse delTester(HttpServletRequest req,Integer testerId) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notNull(testerId,"id不能为空");

            BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            Tester tester = testerService.findById(testerId);
            if (tester == null || accountService.getRootUserId(basicUser) != tester.getUserId()) {
                throw new BizException("信息不存在");
            }
            testerService.deleteById(testerId);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/modelblacklist/list",method = RequestMethod.GET)
    public ModelAndView tester_list(HttpServletRequest req) {
        RestResponse restR = new RestResponse();
        BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
        List<Model> modelList = modelBlacklistService.findAllByUserId(accountService.getRootUserId(basicUser));

        restR.getData().put("user",basicUser);
        restR.getData().put("modelBlackList",modelList);
        restR.getData().put("appInfoList",appService.findAllAppInfoByUser(basicUser));
        return new ModelAndView("model_blacklist","restR",restR);
    }

    @RequestMapping(value = "/modelblacklist/add",method = RequestMethod.POST)
    public @ResponseBody RestResponse add_modelblacklist(HttpServletRequest req,String regularExp,String description) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notEpmty(regularExp,"正则表达式不能为空");
            BizAssert.notEpmty(description,"描述不能为空");
            try {
                Pattern.compile(regularExp);
            } catch (Throwable e) {
                throw new BizException("无效的正则表达式");
            }

            BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            Model model = modelBlacklistService.findByRegexp(accountService.getRootUserId(basicUser),regularExp);
            if (model != null) {
                throw new BizException("匹配该机型的正则已存在");
            }
            model = new Model();
            model.setUserId(accountService.getRootUserId(basicUser));
            model.setRegularExp(regularExp);
            model.setDescription(description);

            modelBlacklistService.save(model);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/modelblacklist/del",method = RequestMethod.POST)
    public @ResponseBody RestResponse del_modelblacklist(HttpServletRequest req,Integer modelblackId) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notNull(modelblackId,"id不能为空");

            BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            Model model = modelBlacklistService.findById(modelblackId);
            if (model == null || accountService.getRootUserId(basicUser) != model.getUserId()) {
                throw new BizException("信息不存在");
            }
            modelBlacklistService.delete(model);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/channel/list",method = RequestMethod.GET)
    public ModelAndView channel_list(HttpServletRequest req) {
        RestResponse restR = new RestResponse();
        BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
        List<Channel> modelList = channelService.findAllByUserId(accountService.getRootUserId(basicUser));

        restR.getData().put("user",basicUser);
        restR.getData().put("channelList",modelList);
        restR.getData().put("appInfoList",appService.findAllAppInfoByUser(basicUser));
        return new ModelAndView("channel_list","restR",restR);
    }

    @RequestMapping(value = "/channel/add",method = RequestMethod.POST)
    public @ResponseBody RestResponse add_channel(HttpServletRequest req,String channelName,String description) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notEpmty(channelName,"渠道名称不能为空");
            BizAssert.notEpmty(description,"描述不能为空");
            BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            Channel channel = channelService.findByUserIdAndName(accountService.getRootUserId(basicUser),channelName);
            if (channel != null) {
                throw new BizException("该渠道已存在");
            }
            channel = new Channel();
            channel.setUserId(accountService.getRootUserId(basicUser));
            channel.setChannelName(channelName);
            channel.setDescription(description);
            channelService.save(channel);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/channel/del",method = RequestMethod.POST)
    public @ResponseBody RestResponse del_channel(HttpServletRequest req,Integer channelId) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notNull(channelId,"id不能为空");

            BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            Channel channel = channelService.findById(channelId);
            if (channel == null || accountService.getRootUserId(basicUser) != channel.getUserId()) {
                throw new BizException("信息不存在");
            }
            channelService.delete(channel);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/app/create_version",method = RequestMethod.POST)
    public @ResponseBody RestResponse addVersion(HttpServletRequest req,String appUid,String versionName) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notEpmty(appUid,"应用号不能为空");
            BizAssert.notEpmty(versionName,"版本号不能为空");
            restR.getData().put("appUid",appUid);
            AppInfo appInfo = appService.findByUid(appUid);
            VersionInfo versionInfo = appService.findVersionByUidAndVersionName(appInfo,versionName);
            if (versionInfo != null) {
                throw new BizException("此版本已存在");
            }
            BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
            versionInfo = new VersionInfo();
            versionInfo.setUserId(accountService.getRootUserId(basicUser));
            versionInfo.setAppUid(appUid);
            versionInfo.setVersionName(versionName);
            appService.saveVersionInfo(versionInfo);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }

        return restR;
    }

    @RequestMapping(value = "/app/version",method = RequestMethod.GET)
    public ModelAndView app_version(HttpServletRequest req, String appUid, String versionName) {
        RestResponse restR = new RestResponse();
        BizAssert.notEpmty(appUid,"应用号不能为空");
        BizAssert.notEpmty(versionName,"版本号不能为空");

        AppInfo appInfo = appService.findByUid(appUid);
        VersionInfo versionInfo = appService.findVersionByUidAndVersionName(appInfo,versionName);
        if (versionInfo == null) {
            throw new BizException("该版本未找到: " + versionName);
        }
        //加载所有patch信息
        List<PatchInfo> patchInfoList = patchService.findByUidAndVersionName(appUid,versionName);

        restR.getData().put("appInfo",appInfo);
        restR.getData().put("versionInfo",versionInfo);
        restR.getData().put("patchInfoList",patchInfoList);

        BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
        List<AppInfo> appInfoList = appService.findAllAppInfoByUser(basicUser);
        restR.getData().put("user",basicUser);
        restR.getData().put("appInfoList",appInfoList);
        restR.getData().put("versionList",appService.findAllVersion(appInfo));
        restR.getData().put("maxPatchSize",maxPatchSize);
        return new ModelAndView("version","restR",restR);
    }

    @RequestMapping(value = "/patch/add",method = RequestMethod.POST)
    public ModelAndView patch_create(String appUid,String versionName,String description,@RequestParam("file") MultipartFile multipartFile) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notEpmty(appUid,"应用号不能为空");
            BizAssert.notEpmty(versionName,"版本号不能为空");
            BizAssert.notEpmty(description,"描述不能为空");
            BizAssert.notNull(multipartFile,"请选择文件");

            AppInfo appInfo = appService.findByUid(appUid);
            VersionInfo versionInfo = appService.findVersionByUidAndVersionName(appInfo,versionName);
            if (versionInfo == null) {
                throw new BizException("该版本未找到: " + versionName);
            }

            patchService.savePatch(appInfo,versionInfo,description,multipartFile);

            return new ModelAndView("redirect:/app/version?appUid=" + appUid + "&versionName=" + versionName);
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
            return new ModelAndView("redirect:/app/version?appUid=" + appUid + "&versionName=" + versionName + "&msg=" + HttpRequestUtils.urlEncode(e.getMessage()));
        }
    }

    @RequestMapping(value = "/patch",method = RequestMethod.GET)
    public ModelAndView patch_detail(HttpServletRequest req,Integer id,String appUid) {
        RestResponse restR = new RestResponse();
        BizAssert.notNull(id,"参数不能为空");
        PatchInfo patchInfo = patchService.findByIdAndAppUid(id,appUid);
        if (patchInfo == null) {
            throw new BizException("参数不正确");
        }
        if (patchInfo.getStatus() == PatchInfo.STATUS_UNPUBLISHED) {
            String tags = testerService.getAllTags(appUid);
            if (!StringUtils.isEmpty(tags)) {
                restR.getData().put("tags",tags + ";");
            }
        }
        AppInfo appInfo = appService.findByUid(patchInfo.getAppUid());
        VersionInfo versionInfo = appService.findVersionByUidAndVersionName(appInfo,patchInfo.getVersionName());
        if (versionInfo == null) {
            throw new BizException("该版本未找到: " + patchInfo.getVersionName());
        }
        BasicUser basicUser = (BasicUser) req.getSession().getAttribute(Constants.SESSION_LOGIN_USER);
        List<AppInfo> appInfoList = appService.findAllAppInfoByUser(basicUser);
        restR.getData().put("user",basicUser);
        restR.getData().put("appInfoList",appInfoList);
        restR.getData().put("appInfo",appInfo);
        restR.getData().put("versionInfo",versionInfo);
        restR.getData().put("patchInfo",patchInfo);
        return new ModelAndView("patch","restR",restR);
    }

    @RequestMapping(value = "/patch/info",method = RequestMethod.POST)
    public @ResponseBody RestResponse patch_info(HttpServletRequest req,Integer id,String appUid) {
        RestResponse restR = new RestResponse();
        BizAssert.notNull(id,"参数不能为空");
        PatchInfo patchInfo = patchService.findByIdAndAppUid(id,appUid);
        if (patchInfo == null) {
            throw new BizException("参数不正确");
        }
        if (patchInfo.getStatus() == PatchInfo.STATUS_UNPUBLISHED) {
            String tags = testerService.getAllTags(appUid);
            if (!StringUtils.isEmpty(tags)) {
                restR.getData().put("tags",tags + ";");
            }
        }
        AppInfo appInfo = appService.findByUid(patchInfo.getAppUid());
        VersionInfo versionInfo = appService.findVersionByUidAndVersionName(appInfo,patchInfo.getVersionName());
        if (versionInfo == null) {
            throw new BizException("该版本未找到: " + patchInfo.getVersionName());
        }
        restR.getData().put("patchInfo",patchInfo);
        restR.getData().put("successScale",patchInfo.getFormatApplyScale());
        return restR;
    }

    @RequestMapping(value = "/patch/normal_publish",method = RequestMethod.POST)
    public @ResponseBody RestResponse normal_publish(String appUid,Integer id) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notNull(id,"参数不能为空");
            PatchInfo patchInfo = patchService.findByIdAndAppUid(id,appUid);
            if (patchInfo == null) {
                throw new BizException("参数不正确");
            }
            if (patchInfo.getStatus() != PatchInfo.STATUS_PUBLISHED
                    || (patchInfo.getStatus() == PatchInfo.STATUS_PUBLISHED && patchInfo.getPublishType() == PatchInfo.PUBLISH_TYPE_GRAY)) {
                patchInfo.setStatus(PatchInfo.STATUS_PUBLISHED);
                patchInfo.setPublishType(PatchInfo.PUBLISH_TYPE_NORMAL);
                patchService.updateStatus(patchInfo);
            }
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }
        return restR;
    }

    @RequestMapping(value = "/patch/stop_publish",method = RequestMethod.POST)
    public @ResponseBody RestResponse stop_publish(String appUid,Integer id) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notNull(id,"参数不能为空");
            PatchInfo patchInfo = patchService.findByIdAndAppUid(id,appUid);
            if (patchInfo == null) {
                throw new BizException("参数不正确");
            }
            if (patchInfo.getStatus() != PatchInfo.STATUS_STOPPED) {
                patchInfo.setStatus(PatchInfo.STATUS_STOPPED);
                patchService.updateStatus(patchInfo);
            }
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }
        return restR;
    }

    @RequestMapping(value = "/patch/delete",method = RequestMethod.POST)
    public @ResponseBody RestResponse delete_patch(String appUid,Integer id) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notNull(id,"应用id不能为空");
            BizAssert.notNull(id,"参数不能为空");
            PatchInfo patchInfo = patchService.findByIdAndAppUid(id,appUid);
            if (patchInfo != null) {
                if (patchInfo.getStatus() == PatchInfo.STATUS_UNPUBLISHED
                        || patchInfo.getStatus() == PatchInfo.STATUS_STOPPED) {
                    patchService.deletePatch(patchInfo);
                }
                else {
                    throw new BizException("已发布状态的补丁包不允许删除");
                }
            }
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }
        return restR;
    }

    @RequestMapping(value = "/patch/gray_publish",method = RequestMethod.POST)
    public @ResponseBody RestResponse gray_publish(String appUid,Integer id,String tags) {
        RestResponse restR = new RestResponse();
        try {
            BizAssert.notNull(id,"参数不能为空");
            BizAssert.notEpmty(tags,"tags不能为空");

            PatchInfo patchInfo = patchService.findByIdAndAppUid(id,appUid);
            if (patchInfo == null) {
                throw new BizException("参数不正确");
            }

            if (patchInfo.getStatus() != PatchInfo.STATUS_PUBLISHED) {
                patchInfo.setStatus(PatchInfo.STATUS_PUBLISHED);
                patchInfo.setPublishType(PatchInfo.PUBLISH_TYPE_GRAY);
                patchInfo.setTags(tags);
                patchService.updateStatus(patchInfo);
            }
        } catch (BizException e) {
            restR.setCode(-1);
            restR.setMessage(e.getMessage());
        }
        return restR;
    }
}
