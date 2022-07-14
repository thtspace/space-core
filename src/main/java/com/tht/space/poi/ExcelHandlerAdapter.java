package com.tht.space.poi;

/**
 * Excel数据格式处理适配器
 */
public interface ExcelHandlerAdapter {

    /** 导出格式化 */
    String exportFormat(Object value);

    /** 导出格式化 */
    Object importFormat(Object value);
}
