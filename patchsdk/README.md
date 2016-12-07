[ ![Download](https://api.bintray.com/packages/typ0520/maven/com.dx168.patchsdk%3Apatchsdk/images/download.svg) ](https://bintray.com/typ0520/maven/com.dx168.patchsdk%3Apatchsdk/_latestVersion)

###一、集成Tinker
app/build.gradle 配置，参考官方 sample，也可以参考SDK里的 tinker-sample

###二、集成SDK

- 1. app/build.gradle

````gradle
repositories {
    jcenter()
}

dependencies {
    ...
    compile 'com.dx168.patchsdk:patchsdk:1.0.9'
}
````

- 2. ApplicationLike

继承 TinkerApplicationLike
````
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.dx168.patchsdk.sample.MyApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public class MyApplicationLike extends TinkerApplicationLike {

    private OriginalApplication originalApplication;

    public MyApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent, Resources[] resources, ClassLoader[] classLoader, AssetManager[] assetManager) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent, resources, classLoader, assetManager);
        originalApplication = new OriginalApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PatchManager.getInstance().init(getApplication(), "http://xxx.xxx.com/hotfix-apis/", "your appId", "your appSecret");
        PatchManager.getInstance().setTag("your tag"); //可用于灰度发布
        PatchManager.getInstance().setChannel("your channel");
        PatchManager.getInstance().queryAndApplyPatch(new PatchListener() {
        ...

        originalApplication.onCreate();
    }
}

````

- 3. TinkerResultService 通知 PatchManager 补丁应用结果

````
if (result.isSuccess) {
    PatchManager.getInstance().onApplySuccess(result.rawPatchFilePath);
} else {
    PatchManager.getInstance().onApplyFailure(result.rawPatchFilePath, "");
}

````

- 4. LoadReporter 通知 PatchManager 补丁应用失败

````
PatchManager.getInstance().onApplyFailure(patchFile.getAbsolutePath(), "errorCode=" + errorCode);
````

###三、补丁调试工具(patchtool)
扫描补丁管理后台的补丁二维码来下载补丁，可以立即应用补丁、重启应用，可以用来调试即将发布的补丁
