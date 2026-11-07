package de.robv.android.xposed;
import java.lang.reflect.Member;
public abstract class XC_MethodHook {
    protected XC_MethodHook() {}
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {}
    
    public class Unhook {
        protected Unhook() {}
        public Member getHookedMethod() { return null; }
        public void unhook() {}
    }
    
    public static class MethodHookParam {
        public Object[] args;
        public Object thisObject;
        public Member method;
        public Object getResult() { return null; }
        public void setResult(Object result) {}
        public Throwable getThrowable() { return null; }
        public void setThrowable(Throwable throwable) {}
        public Object getObjectExtra(Object key) { return null; }
        public void setObjectExtra(Object key, Object value) {}
    }
}
