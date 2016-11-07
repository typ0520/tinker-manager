###一、集成Tinker
app/build.gradle 配置，参考官方demo，也可以参考SDK里的sample

###二、集成SDK

1. `app/build.gradle`
````
dependencies {
    ...
    compile 'com.dx168.tmsdk:tmsdk:1.0.0-RC5'
}
````

2. `ApplicationLike`

必须继承 TinkerManagerApplicationLike
````
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.dx168.tmsdk.sample.MyApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public class MyApplicationLike extends TinkerManagerApplicationLike {

    private OriginalApplication originalApplication;

    public MyApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent, Resources[] resources, ClassLoader[] classLoader, AssetManager[] assetManager) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent, resources, classLoader, assetManager);
        originalApplication = new OriginalApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TinkerManager.getInstance().init(getApplication(), "your appId", "your appSecret", "http://xxx.xxx.com/hotfix-apis/api/patch");
        TinkerManager.getInstance().setTag("your tag"); //可用于灰度发布
        TinkerManager.getInstance().queryAndApplyPatch(new TinkerManagerListener() {
        ...

        originalApplication.onCreate();
    }
}
````