package cn.zhh.crawler.service;

import cn.zhh.common.dto.PositionInfo;
import cn.zhh.common.enums.ErrorEnum;
import cn.zhh.common.util.BusinessException;
import cn.zhh.crawler.constant.SysConsts;
import cn.zhh.crawler.util.FunctionUtils;
import cn.zhh.crawler.util.HttpClientUtils;
import cn.zhh.crawler.util.MapUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Map;
import java.util.Objects;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
public class ZhilianService {
    public static void main(String[] args) throws Exception {
        String url = "https://fe-api.zhaopin.com/c/i/sou";
        Map<String, String> params = MapUtils.buildMap("pageSize", "90", "cityId", "530", "kw", "Java", "kt", "3", "sortType", "publish");
        String rspStr = HttpClientUtils.get(url, params, null);
        JSONObject response = JSONObject.parseObject(rspStr);
        if (!Objects.equals(response.getInteger("code"), 200)) {
            throw new BusinessException(ErrorEnum.BAD_REQUEST, "请求失败！");
        }
        JSONObject data = response.getJSONObject("data");
        Integer totalCount = data.getInteger("count");
        JSONArray results = data.getJSONArray("results");
        for (int i = 0; i < results.size(); i++) {
            JSONObject result = results.getJSONObject(i);
            PositionInfo positionInfo = new PositionInfo();
            positionInfo.setName(result.getString("jobName"));
            positionInfo.setSource("智联");
            positionInfo.setSalary(result.getString("salary"));
            positionInfo.setCity(result.getJSONObject("city").getString("display"));
            positionInfo.setWorkExp(result.getJSONObject("workingExp").getString("name"));
            positionInfo.setEducation(result.getJSONObject("eduLevel").getString("name"));
            positionInfo.setWelfare(getWelfare(result.getJSONArray("welfare")));
            positionInfo.setLabel(getPositionLabel(result.getJSONObject("positionLabel")));
            positionInfo.setPublishTime(result.getDate("updateDate"));
            positionInfo.setUrl(result.getString("positionURL"));
            JSONObject company = result.getJSONObject("company");
            positionInfo.setCompanyName(company.getString("name"));
            positionInfo.setCompanyLogo(result.getString("companyLogo"));
            positionInfo.setCompanyScale(company.getJSONObject("size").getString("name"));

            // 从详情页补充数据
            analysisPositionDetail(positionInfo.getUrl(), positionInfo);

            // 推送MQ
            // todo
            System.out.println(positionInfo);

            if (i > 0) break;
        }

    }

    private static String getWelfare(JSONArray jsonArray) {
        if (jsonArray.isEmpty()) {
            return "";
        }
        return jsonArray.toJavaList(String.class)
                .stream()
                .reduce((s1, s2) -> s1 + "," + s2).orElse("");
    }

    private static String getPositionLabel(JSONObject jsonObject) {
        JSONArray skillLabelArray = jsonObject.getJSONArray("skillLabel");
        if (skillLabelArray.isEmpty()) {
            return "";
        }
        return skillLabelArray.toJavaList(JSONObject.class)
                .stream()
                .map(jsonObj -> jsonObj.getString("value"))
                .reduce((s1, s2) -> s1 + "," + s2).orElse("");
    }

    private static PositionInfo analysisPositionDetail(String positionDetailUrl, PositionInfo positionInfo) throws Exception {
        String htmlPage = HttpClientUtils.get(positionDetailUrl, null, null);
        Document document = Jsoup.parse(htmlPage);

    // start---------职位描述
        Element describtionElement = document.selectFirst("div[class=describtion]");
        StringBuilder describtion = new StringBuilder();
        describtion.append("职能要求：").append(SysConsts.LINE_SEPARATOR);
        // 技能要求
        Element describtionSkill = describtionElement.selectFirst("div[class=describtion__skills-content]");
        FunctionUtils.runIfNonNull(describtionSkill, () -> {
            describtionSkill.children().forEach(element -> {
                describtion.append(element.text()).append(" ");
            });
        });
        // 岗位描述与职位要求
        describtion.append(SysConsts.LINE_SEPARATOR);
        Element describtion_Detail = describtionElement.selectFirst("div[class=describtion__detail-content]");
        FunctionUtils.runIfNonNull(describtion_Detail, () -> {
            describtion_Detail.children().forEach(element -> {
                if (element.children().isEmpty()) {
                    describtion.append(element.text()).append(SysConsts.LINE_SEPARATOR);
                } else {
                    element.children().forEach(e -> {
                        describtion.append(e.text()).append(SysConsts.LINE_SEPARATOR);
                    });
                }
            });
        });
        positionInfo.setDescription(describtion.toString());
    // end---------职位描述

        // 工作地址
        Element jobAddressElement = document.selectFirst("span[class=job-address__content-text]");
        FunctionUtils.runIfNonNull(jobAddressElement, () -> {
            positionInfo.setWorkAddress(jobAddressElement.text());
        });

        // 公司发展阶段（无）

        // 经营领域
        Element companyDomainElement = document.selectFirst("button[class='company__industry']");
        FunctionUtils.runIfNonNull(companyDomainElement, () -> {
            positionInfo.setCompanyDomain(companyDomainElement.text());
        });

        // 公司主页
        Element companyUrlElement = document.selectFirst("a[class=company__page-site]");
        FunctionUtils.runIfNonNull(companyUrlElement, () -> {
            positionInfo.setCompanyUrl(companyUrlElement.attr("href"));
        });



        // 公司介绍
        Element companyIntroductionElement = document.selectFirst("div[class=company__description]");
        FunctionUtils.runIfNonNull(companyIntroductionElement, () -> {
            positionInfo.setCompanyIntroduction(companyIntroductionElement.text());
        });

        return positionInfo;
    }
}
