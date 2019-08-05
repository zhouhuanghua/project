package cn.zhh.common.util;

import org.junit.Test;

public class SimpleBloomFilterTest {

    @Test
    public void test() {
        SimpleBloomFilter filter = new SimpleBloomFilter();
        String uniqueKey = "12345";
        System.out.println(filter.contains(uniqueKey));
        filter.add(uniqueKey);
        System.out.println(filter.contains(uniqueKey));
    }
}