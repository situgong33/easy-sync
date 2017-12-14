package com.hty.util.filesync.bean;

import java.io.Serializable;

public class FileItem implements Serializable {

    private static final long serialVersionUID = 6372583725878913705L;
    /**
     * 文件名称，从基路径开始的文件名称：base/path/demo.mp4
     **/
    private String filePath;
    /** 文件最后修改日期 */
    private long lastModify;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getLastModify() {
        return lastModify;
    }

    public void setLastModify(long lastModify) {
        this.lastModify = lastModify;
    }
}
