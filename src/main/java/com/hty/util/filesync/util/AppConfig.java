package com.hty.util.filesync.util;

import com.hty.baseframe.common.util.StringUtil;
import com.hty.util.filesync.filter.Filter;
import com.hty.util.filesync.filter.FilterChain;
import com.hty.util.filesync.filter.impl.ExcludeFilterImpl;
import com.hty.util.filesync.filter.impl.IncludeFilterImpl;
import com.hty.util.filesync.server.FileSyncServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;

/**
 * 系统配置类
 */
public class AppConfig {
    private static final Log logger = LogFactory.getLog(AppConfig.class);

    /*参数*/
    private Map<String, String> params = new HashMap<String, String>();
    /*单实例*/
    private static AppConfig instance;

    private AppConfig() {
    }

    /**
     * 获得单实例
     * @return
     */
    public static synchronized AppConfig getInstance () {
        if(null == instance) {
            instance = new AppConfig();
            File conf = new File("sync.properties").getAbsoluteFile();
            Properties props = new Properties();
            try {
                props.load(new InputStreamReader(new FileInputStream(conf), "UTF-8"));
                for (Iterator it = props.keySet().iterator(); it.hasNext();) {
                    String key = (String) it.next();
                    String value = (String) props.get(key);
                    System.out.println("- " + key + " = " + value);

                    instance.addProperty(key, value);

                    if(key.startsWith("exclude_filter")) {
                        if(StringUtil.isEmpty(value)) {
                            throw new IllegalArgumentException("Value of property '"+ key +"' must be a directory!");
                        } else {
                            if(null != value) {
                                logger.info("Exclude Filter: " + value);
                                Filter filter = new ExcludeFilterImpl(key, value);
                                FilterChain.addFilter(filter);
                            }
                        }
                    }

                    if(key.startsWith("include_filter")) {
                        if(StringUtil.isEmpty(value)) {
                            throw new IllegalArgumentException("Value of property '"+ key +"' must be a directory!");
                        } else {
                            if(null != value) {
                                logger.info("Include Filter: " + value);
                                Filter filter = new IncludeFilterImpl(key, value);
                                FilterChain.addFilter(filter);
                            }
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(Map.Entry<String, String> entry : instance.params.entrySet()) {

                String key = "mode";
                String value = instance.params.get(key);
                if(null == value ||
                        (!"server".equals(value) && !"client".equals(value))) {
                    throw new IllegalArgumentException("Value of property 'mode' must be 'server' or 'client'!");
                }

                key = "listen";
                value = instance.params.get(key);
                if(null == value || !value.matches("[0-9]+")) {
                    throw new IllegalArgumentException("Value of property 'listen' must be a port number!");
                } else {
                    instance.addProperty(key, value);
                }

                key = "listen";
                value = instance.params.get(key);
                if(null == value || !value.matches("[0-9]+")) {
                    throw new IllegalArgumentException("Value of property 'listen' must be a port number!");
                } else {
                    instance.addProperty(key, value);
                }

                key = "buf_size";
                value = instance.params.get(key);
                if("buf_size".equals(key)) {
                    try {
                        int bufSize = Integer.valueOf(value);
                        if(bufSize <= 0) {
                            bufSize = 10240;
                            instance.addProperty(key, ""+bufSize);
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Value of property 'buf_size' must be a number!");
                    }
                }

                key = "overwrite";
                value = instance.params.get(key);
                if("overwrite".equals(key)) {
                    if(null == value ||
                            (!"true".equals(value) && !"false".equals(value))) {
                        throw new IllegalArgumentException("Value of property 'overwrite' must be 'true' or 'false'!");
                    } else {
                        instance.addProperty(key, value);
                    }
                }

                key = "delete_after_sync";
                value = instance.params.get(key);
                if("delete_after_sync".equals(key)) {
                    if(null == value ||
                            (!"true".equals(value) && !"false".equals(value))) {
                        throw new IllegalArgumentException("Value of property 'delete_after_sync' must be 'true' or 'false'!");
                    } else {
                        instance.addProperty(key, value);
                    }
                }

                String mode = instance.params.get("mode");
                if("server".equals(mode)) {
                    key = "src_dir";
                } else {
                    key = "dest_dir";
                }
                value = instance.params.get(key);
                if(StringUtil.isEmpty(value)) {
                    throw new IllegalArgumentException("Value of property '"+ key +"' must be a directory!");
                } else {
                    instance.addProperty(key, value);
                }

                /////
                if("client".equals(mode)) {
                    key = "host";
                    value = instance.params.get(key);
                    if(StringUtil.isEmpty(value)) {
                        throw new IllegalArgumentException("Value of property '"+ key +"' must be a valid host!");
                    } else {
                        instance.addProperty(key, value);
                    }
                }

            }

         }
        return instance;
    }



    private void addProperty(String key, String value) {
        this.params.put(key, value);
    }

    public String getProperty(String key) {
        return this.params.get(key);
    }

}
