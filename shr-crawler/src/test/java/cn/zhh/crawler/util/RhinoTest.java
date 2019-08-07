package cn.zhh.crawler.util;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

/**
 * RhinoTest
 *
 * @author Zhou Huanghua
 */
public class RhinoTest {

    @Test
    public void test() {
        Context context = Context.enter();
        ScriptableObject scriptableObject = context.initStandardObjects();
        context.evaluateString(scriptableObject, "function f(param) { return param }", "", 1, null);
        Object var = scriptableObject.get("f", scriptableObject);

        Function test = (Function) var;
        Object result = test.call(context, scriptableObject, null, new Object[]{3});
        System.out.println(result);
    }
}
