package com.hty.util.filesync.handler;

import com.hty.util.filesync.util.AppConfig;
import com.hty.util.filesync.util.ProgressPrinter;
import org.apache.commons.net.ftp.*;

import java.io.*;
import java.util.Set;

public class FTPDownloader {

    private final FTPClient client;

    private static FTPDownloader downloader;

    private String LOCAL_CHARSET = "GBK";
    private String SERVER_CHARSET = "ISO-8859-1";

    AppConfig config = AppConfig.getInstance();

    public FTPDownloader() {
        client = new FTPClient();
    }

    private boolean connect() throws IOException {
        String host = config.getProperty("host");
        String ftp_port = config.getProperty("ftp_port");

        try {
            int reply;
            System.out.println("Connected to " + host + " on " + ftp_port);
            client.connect(host, Integer.valueOf(ftp_port));
            // After connection attempt, you should check the reply code to verify
            // success.
            reply = client.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                System.err.println("FTP server refused connection.");
                return false;
            }
            System.out.println("连接成功");
            client.enterLocalPassiveMode();


            if (FTPReply.isPositiveCompletion(client.sendCommand("OPTS UTF8", "ON"))) {// 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
                LOCAL_CHARSET = "UTF-8";
            }
            client.setControlEncoding(LOCAL_CHARSET);

            client.setControlEncoding("UTF-8");
            client.setDataTimeout(30000);
        } catch (IOException e) {
            if (client.isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException f) {
                    // do nothing
                }
            }
            System.err.println("Could not connect to server.");
            e.printStackTrace();
            return false;
        }

        try {
            String username = config.getProperty("ftp_user");
            String password = config.getProperty("ftp_password");

            if (!client.login(username, password)) {
                client.logout();
                System.out.println("登录验证失败");
            }

            System.out.println("Remote system is " + client.getSystemType());

            client.setFileType(FTP.BINARY_FILE_TYPE);
            System.out.println("登录成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 获取单实例
     * @return
     */
    public static synchronized FTPDownloader getInstance() {
        return new FTPDownloader();
    }

    public void download(Set<String> paths) throws IOException {

        if(connect()) {
            System.out.println("连接登录成功");
            System.out.println("下载文件：\n" + paths);
            String localBasePath = config.getProperty("dest_dir");
            String serverBasePath = config.getProperty("src_dir");
            int bufSize = Integer.valueOf(config.getProperty("buf_size"));
            boolean overwrite = config.getProperty("overwrite").equals("true");
            boolean delete_after_sync = config.getProperty("delete_after_sync").equals("true");

            if(client.changeWorkingDirectory(serverBasePath)) {
                int index = 0;
                if(null != paths)
                for(String path : paths) {
                    index++;
                    File localFile = new File(localBasePath, path);
                    if(localFile.exists() && !overwrite) {
                        System.out.println("文件：" + path + "已存在，跳过");
                        if(delete_after_sync)
                            client.deleteFile(new String(path.getBytes(LOCAL_CHARSET), SERVER_CHARSET));
                    } else {
                        FTPFile ftpFile = client.mlistFile(serverBasePath + "/" + path);
                        if(null == ftpFile) {
                            ftpFile = new FTPFile();
                            ftpFile.setName(localFile.getName());
                            ftpFile.setSize(0);
                        }
                        ProgressPrinter progressor = new ProgressPrinter(localFile.getName(),
                                ftpFile.getSize() <= 0 ? 0 : ftpFile.getSize(), "["+ index +"/"+ paths.size() +"]");
                        InputStream ips = null;
                        OutputStream ops = null;
                        try {
                            ips = client.retrieveFileStream(new String(path.getBytes(LOCAL_CHARSET), SERVER_CHARSET));
                            int replyCode = client.getReplyCode();
                            System.out.println(replyCode);
                            if(replyCode != 150) {
                                throw new IllegalStateException("下载异常：" + replyCode);
                            }
                            System.out.println(client.getReplyString());
                            File tmpFile = new File(localBasePath, path + ".syncing");
                            if(!tmpFile.getParentFile().exists()) {
                                tmpFile.getParentFile().mkdirs();
                            }
                            ops = new FileOutputStream(tmpFile);
                            byte[] buf = new byte[bufSize];
                            int len;
                            while((len = ips.read(buf)) != -1) {
                                ops.write(buf, 0, len);
                                ops.flush();
                                progressor.updateTotalBytes(len);
                            }
                            ops.close();
                            client.completePendingCommand();
                            //下载结束，将缓存文件重命名为正确文件名称
                            tmpFile.renameTo(localFile);
                            if(delete_after_sync)
                                client.deleteFile(new String(path.getBytes(LOCAL_CHARSET), SERVER_CHARSET));
                        } catch (Exception e) {
                            System.out.println("下载文件失败：" + path + "\n" + e.getMessage());
                            break;
                        } finally {
                            try {
                                if(null != ops) {
                                    ops.close();
                                }
                            } catch (IOException e) {
                                break;
                            }
                            progressor.cancelTimer();
                        }
                    }
                }
            } else {
                System.out.println("FTP客户端无法定位到目标文件夹！");
                try {
                    client.disconnect();
                } catch (IOException e) {
                }
            }


        } else {
            System.out.println("连接失败");
        }
    }
}
