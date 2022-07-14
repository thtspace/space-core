package com.tht.space.entity;

import com.tht.space.utils.ServletUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Page {
    private static final String PAGE_INDEX_NAME = "pageIndex";
    private static final String PAGE_SIZE_NAME = "pageSize";

    private Integer pageIndex;
    private Integer pageSize;

    public static Page buildRequestPage(){
        Integer pageIndex = ServletUtil.getParameterToInt(PAGE_INDEX_NAME);
        Integer pageSize = ServletUtil.getParameterToInt(PAGE_SIZE_NAME);
        return new Page(pageIndex,pageSize);
    }

}
