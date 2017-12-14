package com.hty.util.filesync.handler;

import com.hty.util.filesync.client.FileSyncClient;
import com.hty.util.filesync.server.FileSyncServer;
import com.hty.util.filesync.util.AppConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ModeDispatch {
    private static final Log logger = LogFactory.getLog(ModeDispatch.class);

    public static void dispatch() {
        AppConfig config = AppConfig.getInstance();
        String mode = config.getProperty("mode");
        Runnable service = null;
        if("server".equals(mode)) {
            service = new FileSyncServer();
        } else if("client".equals(mode)) {
            service = new FileSyncClient();
        }
        logger.info("App started in " + mode + " mode.");
        Thread t = new Thread(service);
        t.start();
    }

}
