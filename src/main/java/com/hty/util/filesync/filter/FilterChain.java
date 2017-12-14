package com.hty.util.filesync.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilterChain {

    private static Filter headFilter;

    private FilterChain() {
    }

    public static synchronized void addFilter(Filter filter) {
        if(null != filter) {
            if(null != headFilter) {
                headFilter.addFilter(filter);
            } else {
                headFilter = filter;
            }
        }
    }


    public static File filter(File file) {
        if(null != headFilter)
            return headFilter.filter(file);
        return file;
    }

}
