package air.svran.xp.htlhooker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * 功能介绍: XpHook工具类 <br/>
 * 调用方式: / <br/>
 * <p/>
 * 创建作者: Svran - 924633827@qq.com<br/>
 * 创建电脑: Svran-MY  <br/>
 * 创建时间: 2021/4/14 10:12 <br/>
 * 最后编辑: 2021/4/14 10:12 - Svran<br/>
 *
 * @author Svran
 */
public class SvranHookUtils {

    public static void setBooleanField(Class<?> cls, Object obj, String fieldName, boolean value) {
        Field field = XposedHelpers.findFieldIfExists(cls, fieldName);
        if (field != null) {
            XposedHelpers.setBooleanField(obj, fieldName, value);
            XposedBridge.log("Svran: 重置: " + fieldName + " -> " + value);
        } else {
            XposedBridge.log("Svran: Error:××××××××××××功能异常(方法: " + fieldName + " 未找到方法)××××××××××××\n.");
        }
    }

    public static boolean findMethod(String className, String methodName, ClassLoader classLoader, String tip, Object... parameterTypes) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == null) {
                XposedBridge.log("Svran: Error:××××××××××××功能异常(" + tip + " 第" + i + "个参数 null )××××××××××××\n.");
                return false;
            }
        }
        Class<?> cls = XposedHelpers.findClassIfExists(className, classLoader);
        if (cls == null) {
            XposedBridge.log("Svran: Error:××××××××××××功能异常(" + tip + " Class -> " + className + " <- 不存在! )××××××××××××\n.");
            return false;
        }
        Method method = XposedHelpers.findMethodExactIfExists(cls, methodName, parameterTypes);
        if (method == null) {
            XposedBridge.log("Svran: Error:××××××××××××功能异常(" + tip + " Method -> " + methodName + " <- 不存在! )××××××××××××\n.");
            return false;
        }
        XposedBridge.log("Svran: >>>>>>>>>>>>>>>>>(" + tip + " 找到该方法!)<<<<<<<<<<<<<<<<<<");
        return method != null;
    }

    public static boolean findMethod(Class<?> cls, String methodName, String tip, Object... parameterTypes) {
        if (cls == null) {
            XposedBridge.log("Svran: Error:××××××××××××功能异常(" + tip + " Class不存在! )××××××××××××\n.");
            return false;
        }
        Method method = XposedHelpers.findMethodExactIfExists(cls, methodName, parameterTypes);
        if (method == null) {
            XposedBridge.log("Svran: Error:××××××××××××功能异常(" + tip + " Method不存在! )××××××××××××\n.");
            return false;
        }
        XposedBridge.log("Svran: >>>>>>>>>>>>>>>>>(" + tip + " 找到该方法!)<<<<<<<<<<<<<<<<<<");
        return method != null;
    }

    private SvranHookUtils() {
    }
}
