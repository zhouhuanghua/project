package cn.zhh.crawler.util;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * NashornUtils
 *
 * @author Zhou Huanghua
 */
public class NashornUtils {

    private static final String ENGINE_NAME = "nashorn";

    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName(ENGINE_NAME);

    public static ScriptEngine getScriptEngine() {
       return SCRIPT_ENGINE;
    }
}
