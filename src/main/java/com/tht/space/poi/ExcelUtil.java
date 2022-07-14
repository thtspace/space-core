package com.tht.space.poi;

import com.tht.space.poi.annotation.Excel.Type;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.text.DecimalFormat;

@Slf4j
public class ExcelUtil<T> {

    /**
     * 工作表名称
     */
    protected String sheetName;

    /**
     * 导出类型（EXPORT:导出数据；IMPORT：导入模板）
     */
    protected Type type;

    /**
     * 工作薄对象
     */
    protected Workbook wb;

    /**
     * 数字格式
     */
    protected static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");

    /**
     * 实体对象
     */
    protected Class<T> clazz;
    /**
     * 开始行
     */
    protected int startRow = 0;

    public ExcelUtil(Class<T> clazz) {
        this.clazz = clazz;
    }
}
