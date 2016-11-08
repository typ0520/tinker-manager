###一、集成Tinker
app/build.gradle 配置，参考官方 sample，也可以参考SDK里的 tinker-sample

###二、集成SDK

- 1. app/build.gradle
````
dependencies {
    ...
    compile 'com.dx168.patchsdk:patchsdk:1.0.0-RELEASE'
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
        PatchManager.getInstance().init(getApplication(), "your appId", "your appSecret", "http://xxx.xxx.com/hotfix-apis/api/patch");
        PatchManager.getInstance().setTag("your tag"); //可用于灰度发布
        PatchManager.getInstance().queryAndApplyPatch(new TinkerManagerListener() {
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
    PatchManager.getInstance().onApplyFailure("");
}
````
