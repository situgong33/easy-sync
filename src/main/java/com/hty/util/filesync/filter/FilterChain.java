package com.hty.util.filesync.filter;

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


    public static String filter(String file) {
        if(null != headFilter)
            return headFilter.filter(file);
        return file;
    }

}
