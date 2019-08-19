package cn.zhh.common.enums;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnumTest {

    @Test
    public void test01() {
        /*String str = "/job_detail/608a61408c15699b03Zz2N27F1Y~.html";
        String substring = str.substring(str.lastIndexOf("/") + 1, str.lastIndexOf("."));
        System.out.println(substring);*/

        String str = "发布于：2019-05-23 11:49";
        Pattern pattern = Pattern.compile("[\\d]{4}-[\\d]{2}-[\\d]{2}\\s[\\d]{2}:[\\d]{2}");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            System.out.println(matcher.group());
        }

    }

}