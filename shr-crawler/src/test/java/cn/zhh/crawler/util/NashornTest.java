package cn.zhh.crawler.util;

import org.junit.Test;

import javax.script.Invocable;
import javax.script.ScriptEngine;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
public class NashornTest {

    @Test
    public void test() throws Exception {
        ScriptEngine scriptEngine = NashornUtils.getScriptEngine();
        scriptEngine.eval("function f(name) { return name }");
        Invocable invocable = (Invocable) scriptEngine;
        Object result = invocable.invokeFunction("f", "Peter Parker");
        System.out.println(result);
    }
}
