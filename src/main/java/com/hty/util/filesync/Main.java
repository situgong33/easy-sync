package com.hty.util.filesync;

import com.hty.util.filesync.client.FileSyncClient;

/**
 * 程序启动类
 * @version 1.0
 * @author Hetianyi 12/07/2017
 */
public class Main {


    public static void main(String[] args) {
        Runnable service = new FileSyncClient();
        Thread t = new Thread(service);
        t.start();
    }

}
