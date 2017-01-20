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
    compile 'com.dx168.patchsdk:patchsdk:1.1.0'
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

    public MyApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
        originalApplication = new OriginalApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String appId = "20170112162040035-6936";
        String appSecret = "d978d00c0c1344959afa9d0a39d7dab3";
        PatchManager.getInstance().init(getApplication(), "http://xxx.xxx.xxx/hotfix-apis/", appId, appSecret, new ActualPatchManager() {
            @Override
            public void cleanPatch(Context context) {
                TinkerInstaller.cleanPatch(context);
            }

            @Override
            public void patch(Context context, String patchPath) {
                TinkerInstaller.onReceiveUpgradePatch(context, patchPath);
            }
        });
        PatchManager.getInstance().register(new Listener() {
            ...
        });
        PatchManager.getInstance().setTag("your tag");
        PatchManager.getInstance().setChannel("");
        PatchManager.getInstance().queryAndPatch();
        originalApplication.onCreate();
    }
}

````

- 3. TinkerResultService 通知 PatchManager 补丁合成结果

````
@Override
public void onPatchResult(final PatchResult result) {
	...
	if (result.isSuccess) {
		PatchManager.getInstance().onPatchFailure(result.rawPatchFilePath);
	} else {
		PatchManager.getInstance().onPatchSuccess(result.rawPatchFilePath);
	}
	...
}

````

- 4. LoadReporter 通知 PatchManager 补丁应用结果

````
@Override
public void onLoadResult(File patchDirectory, int loadCode, long cost) {
    ...
    switch (loadCode) {
        case ShareConstants.ERROR_LOAD_OK:
            PatchManager.getInstance().onLoadSuccess();
            ...
            break;
        default:
            PatchManager.getInstance().onLoadFailure();
            break;
    }
    ...
}
````

###三、补丁调试工具(patchtool)
扫描补丁管理后台的补丁二维码来下载补丁，可以立即应用补丁、重启应用，可以用来调试即将发布的补丁
