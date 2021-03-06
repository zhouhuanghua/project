package cn.zhh.portal.binlog;

import cn.zhh.common.util.ThrowableUtils;
import cn.zhh.portal.constant.PortalConsts;
import cn.zhh.portal.dto.PositionSearchVO;
import cn.zhh.portal.service.IPositionSearchService;
import cn.zhh.portal.util.ObjCopyUtils;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Binlog监听器
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class BinlogListener implements BinaryLogClient.EventListener, InitializingBean {

    @Autowired
    private Executor asyncExecutor;

    @Autowired
    private IPositionSearchService positionSearchService;

    private Long tableId;

    private final Map<Integer, String> indexFieldMap = new HashMap<>();

    @Override
    public void onEvent(Event event) {
        EventType eventType = event.getHeader().getEventType();
        if (eventType == EventType.TABLE_MAP) {
            TableMapEventData tableData = event.getData();
            if (Objects.equals(tableData.getDatabase(), PortalConsts.DB_NAME)
                    && Objects.equals(tableData.getTable(), PortalConsts.TB_NAME)) {
                tableId = tableData.getTableId();
            }
            return;
        }
        EventData eventData = event.getData();
        if (eventData instanceof WriteRowsEventData) {
            WriteRowsEventData writeData = (WriteRowsEventData) eventData;
            if (Objects.equals(writeData.getTableId(), tableId)) {
                asyncExecutor.execute(() -> push2ES(writeData));
            }
        }
    }

    private void push2ES(WriteRowsEventData data) {
        for (Serializable[] row : data.getRows()) {
            Map<String, Object> dataMap = new HashMap<>(row.length);
            for (int i = 0, max = row.length; i < max; i++) {
                dataMap.put(indexFieldMap.get(i), row[i]);
            }
            PositionSearchVO vo;
            try {
                vo = ObjCopyUtils.copyProperties(dataMap, PositionSearchVO.class, true);
            } catch (IllegalAccessException | InstantiationException e) {
                log.error("反射创建对象异常，e={}", ThrowableUtils.getStackTrace(e));
                continue;
            }
            positionSearchService.insert(vo);
            log.info("增量数据{}推送ES成功。", vo.toSimpleString());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        indexFieldMap.put(0, "id");
        indexFieldMap.put(1, "uniqueKey");
        indexFieldMap.put(2, "name");
        indexFieldMap.put(3, "salary");
        indexFieldMap.put(4, "city");
        indexFieldMap.put(5, "workExp");
        indexFieldMap.put(6, "education");
        indexFieldMap.put(7, "welfare");
        indexFieldMap.put(8, "description");
        indexFieldMap.put(9, "label");
        indexFieldMap.put(10, "workAddress");
        indexFieldMap.put(11, "publishTime");
        indexFieldMap.put(12, "url");
        indexFieldMap.put(13, "companyName");
        indexFieldMap.put(14, "companyLogo");
        indexFieldMap.put(15, "companyDevelopmentalStage");
        indexFieldMap.put(16, "companyScale");
        indexFieldMap.put(17, "companyDomain");
        indexFieldMap.put(18, "companyUrl");
        indexFieldMap.put(19, "companyIntroduction");
    }
}