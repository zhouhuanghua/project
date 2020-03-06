package cn.zhh.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常工具类
 *
 * @author Zhou Huanghua
 */
public class ThrowableUtils {

    private ThrowableUtils() {
        throw new UnsupportedOperationException("不支持创建实例！");
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
