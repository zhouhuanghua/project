package cn.zhh.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * URL 传输对象
 *
 * @author Zhou Huanghua
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UrlDTO {

    private String url;

    private Integer retryCount;

    public String toSimpleString() {
        return url + "[" + retryCount + "]";
    }
}
