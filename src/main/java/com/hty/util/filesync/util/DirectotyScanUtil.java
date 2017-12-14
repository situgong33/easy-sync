package com.hty.util.filesync.util;

import com.hty.baseframe.common.util.StringUtil;
import com.hty.util.filesync.filter.FilterChain;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DirectotyScanUtil {

    public static void scan(File base, String pathbuilder, Set<String> set, boolean root, boolean ignoreFilter) {
        if(root) {
            pathbuilder = "";
        } else {
            if(!StringUtil.isEmpty(pathbuilder))
                pathbuilder += ("/" + base.getName());
            else
                pathbuilder += base.getName();
        }
        if(base.isDirectory()) {
            File[] children = base.listFiles();
            if(null != children) {
                for(File file : children) {
                    scan(file, pathbuilder, set, false, ignoreFilter);
                }
            }
        } else {
            if(!ignoreFilter) {
                base = FilterChain.filter(base);
                if(null != base) {
                    set.add(pathbuilder);
//                    System.out.println(pathbuilder);
                }
            } else {
                set.add(pathbuilder);
            }
        }
    }


    public static void main(String[] args) {
        Set<String> set = new HashSet<String>();
        scan(new File("D:\\词库"), "", set, true, false);
        System.out.println(set);

    }
}
