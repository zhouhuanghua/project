package cn.zhh.portal.binlog;

import cn.zhh.common.util.ThrowableUtils;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * BinlogRunner
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class BinlogRunner implements CommandLineRunner {

    @Autowired
    private BinaryLogClient binaryLogClient;

    @Autowired
    private BinlogListener binlogListener;

    @Override
    public void run(String... strings) throws Exception {
        binaryLogClient.registerEventListener(binlogListener);
        new ConnectRunner("binlog-connect").start();
    }

    private class ConnectRunner extends Thread {

        private ConnectRunner(String name) {
            super(name);
        }

        @Override
        public void run() {
            try {
                binaryLogClient.connect();
            } catch (IOException e) {
                log.error("BinaryLogClient connect failed. e={}", ThrowableUtils.getStackTrace(e));
            }
        }
    }
}
