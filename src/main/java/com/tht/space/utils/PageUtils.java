package com.tht.space.utils;

import com.github.pagehelper.PageHelper;
import com.tht.space.entity.Page;
import org.apache.commons.lang3.ObjectUtils;

public class PageUtils extends PageHelper {

    public static void startPage(){
        Page page = Page.buildRequestPage();
        Integer pageIndex = page.getPageIndex();
        Integer pageSize = page.getPageSize();
        if (ObjectUtils.allNotNull(pageIndex,pageSize)){
            PageHelper.startPage(pageIndex,pageSize);
        }
    }
}
