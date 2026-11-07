package de.robv.android.xposed;
import java.lang.reflect.Member;
public class XposedHelpers {
    public static Object findAndHookMethod(Class<?> cls, String methodName, Object... parameterTypesAndCallback) { return null; }
    public static Object findAndHookMethod(String className, ClassLoader cl, String methodName, Object... parameterTypesAndCallback) { return null; }
    public static Class<?> findClass(String className, ClassLoader cl) { return null; }
    public static Object callMethod(Object obj, String methodName, Object... args) { return null; }
    public static Object callStaticMethod(Class<?> cls, String methodName, Object... args) { return null; }
    public static Object getObjectField(Object obj, String fieldName) { return null; }
    public static void setObjectField(Object obj, String fieldName, Object value) {}
}
