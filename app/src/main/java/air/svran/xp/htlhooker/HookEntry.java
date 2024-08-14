package air.svran.xp.htlhooker;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {

    public static final String PACKAGE_NAME_DD = "com.wm.dmall";
    public static final String PACKAGE_NAME_YH = "cn.yonghui.hyd";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        switch (lpparam.packageName) {
            case PACKAGE_NAME_DD:
                new Dd(lpparam);
                break;
            case PACKAGE_NAME_YH:
                new Yh(lpparam);
                break;
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
    }
}