package com.tht.space.poi;

import com.tht.space.poi.annotation.Excel;
import com.tht.space.poi.annotation.Excels;
import com.tht.space.poi.annotation.Excel.Type;
import com.tht.space.utils.LocalDateUtils;
import com.tht.space.utils.ReflectUtil;
import com.tht.space.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @program: space-core
 * @author: tht
 * @create: 2022-07-13 10:10
 * @description:
 **/
@Slf4j
public class ImportExcelUtil extends ExcelUtil<T> {
    /**
     * 实体对象
     */
    public Class<T> clazz;



    public List<T> importExcel(InputStream is) throws Exception {
        return importExcel(is, StringUtils.EMPTY);
    }

    public List<T> importExcel(InputStream is, String sheetName) throws Exception {
        this.type = Type.IMPORT;
        this.wb = WorkbookFactory.create(is);
        List<T> list = new ArrayList<>();
        Sheet sheet = StringUtils.isNotEmpty(sheetName) ? wb.getSheet(sheetName) : wb.getSheetAt(0);
        if (sheet == null) {
            throw new RuntimeException("文件指定sheet名[" + sheetName + "]不存在");
        }
        // 获取最后一个非空行的下标
        int rows = sheet.getLastRowNum();
        if (rows <= 0) {
            return list;
        }
        // 获取列名与序号
        Map<String, Integer> cellMap = getHeardInfo(sheet.getRow(startRow));
        //初始化fieldMap
        Map<Integer, Field> fieldsMap = new HashMap<>();
        Map<Integer, Field> fieldsAttrMap = new HashMap<>();
        initFieldMap(cellMap, fieldsMap, fieldsAttrMap);
        //加载数据
        for (int i = startRow + 1; i <= rows; i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            T entity = null;
            for (Integer key : fieldsMap.keySet()) {
                entity = entity == null ? clazz.newInstance() : entity;
                Object cellValue = getCellValue(row, key);
                Field field = fieldsMap.get(key);
                String propertyName = field.getName();
                Excel attr = field.getAnnotation(Excel.class);
                if (attr == null) {
                    Excels excels = field.getAnnotation(Excels.class);
                    if (excels != null) {
                        String targetAttr = fieldsAttrMap.get(key).getName();
                        for (Excel excel : excels.value()) {
                            if (targetAttr.equals(excel.targetAttr())) {
                                attr = excel;
                                propertyName = field.getName() + "." + targetAttr;
                                field = field.getType().getDeclaredField(targetAttr);
                                field.setAccessible(true);
                                break;
                            }
                        }
                    }
                }
                if (StringUtils.isNotBlank(cellValue.toString())) {
                    Object val = getVal(cellValue.toString(), field, attr);
                    ReflectUtil.invokeSetter(entity, propertyName, val);
                }
            }
            list.add(entity);
        }
        return list;
    }


    /**
     * 获取表头信息
     *
     * @param heard 行对象
     * @return map key-列名 value-序号
     */
    private Map<String, Integer> getHeardInfo(Row heard) {
        Map<String, Integer> cellMap = new HashMap<>();
        for (int i = 0; i < heard.getPhysicalNumberOfCells(); i++) {
            Cell cell = heard.getCell(i);
            if (cell != null) {
                cellMap.put(cell.getStringCellValue(), i);
            }
        }
        return cellMap;
    }

    /**
     * 初始化fieldsMap和fieldsAttrMap
     *
     * @param cellMap       列名与序号的map
     * @param fieldsMap     fieldsMap
     * @param fieldsAttrMap fieldsAttrMap
     */
    private void initFieldMap(Map<String, Integer> cellMap, Map<Integer, Field> fieldsMap, Map<Integer, Field> fieldsAttrMap) {
        // 获取类与其父类的所有属性
        List<Field> allFields = Arrays.asList(clazz.getDeclaredFields());
        if (!Objects.equals(clazz.getSuperclass(), Object.class)) {
            List<Field> fields = Arrays.asList(clazz.getSuperclass().getDeclaredFields());
            allFields.addAll(fields);
        }
        // 将属性与注解的序号组合成map
        for (Field field : allFields) {
            Excel attr = field.getAnnotation(Excel.class);
            Excels excels = field.getAnnotation(Excels.class);
            if (attr != null) {
                pushFiledMap(cellMap, fieldsMap, field, attr);
            } else if (excels != null) {
                for (Excel excel : excels.value()) {
                    Field attrField = null;
                    try {
                        attrField = field.getType().getDeclaredField(excel.targetAttr());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    pushFiledMap(cellMap, fieldsMap, field, excel);
                    pushFiledMap(cellMap, fieldsAttrMap, attrField, excel);
                }
            }
        }
    }

    private void pushFiledMap(Map<String, Integer> cellMap, Map<Integer, Field> fieldsMap, Field field, Excel attr) {
        if (attr != null && (attr.type() == Type.ALL || attr.type() == type)) {
            field.setAccessible(true);
            Integer column = cellMap.get(attr.name());
            if (column != null) {
                fieldsMap.put(column, field);
            }
        }
    }

    /**
     * 判断该行是否为空
     *
     * @param row 行对象
     * @return boolean 空行-true
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }


    /**
     * 获取指定行和列的值
     *
     * @param row    行
     * @param column 列
     * @return 值
     */
    private Object getCellValue(Row row, int column) {
        if (row == null) {
            return null;
        }
        Object val = "";
        try {
            Cell cell = row.getCell(column);
            if (cell != null) {
                if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                    val = cell.getNumericCellValue();
                    if (DateUtil.isCellDateFormatted(cell)) {
                        val = cell.getLocalDateTimeCellValue();
                    } else {
                        if ((Double) val % 1 != 0) {
                            val = new BigDecimal(val.toString());
                        } else {
                            val = new DecimalFormat("0").format(val);
                        }
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    val = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.BOOLEAN) {
                    val = cell.getBooleanCellValue();
                } else if (cell.getCellType() == CellType.ERROR) {
                    val = cell.getErrorCellValue();
                }

            }
        } catch (Exception e) {
            return val;
        }
        return val;
    }


    private Object getVal(String cellValue, Field field, Excel attr) throws Exception {
        if (attr.handler() != null) {
            return importFormatHandlerAdapter(cellValue,attr);
        }

        Object val = null;
        Class<?> fieldType = field.getType();
        String exp = attr.readConverterExp();
        if (StringUtils.isNotEmpty(exp)) {
            cellValue = reverseByExp(cellValue, exp, attr.separator());
        }
        // 转换为对于类型的值
        if (String.class == fieldType) {
            val = cellValue;
        } else if ((Integer.TYPE == fieldType || Integer.class == fieldType) && StringUtils.isNumeric(cellValue)) {
            val = Integer.parseInt(cellValue);
        } else if (Long.TYPE == fieldType || Long.class == fieldType) {
            val = Long.parseLong(cellValue);
        } else if (Double.TYPE == fieldType || Double.class == fieldType) {
            val = Double.parseDouble(cellValue);
        } else if (Float.TYPE == fieldType || Float.class == fieldType) {
            val = Float.parseFloat(cellValue);
        } else if (BigDecimal.class == fieldType) {
            val = BigDecimal.valueOf(Double.parseDouble(cellValue));
        } else if (Boolean.TYPE == fieldType || Boolean.class == fieldType) {
            if (StringUtils.isNotBlank(cellValue)) {
                switch (cellValue) {
                    case "true":
                    case "yes":
                    case "ok":
                    case "1":
                        val = true;
                        break;
                    default:
                        val = false;
                }
            }
        } else if (LocalDate.class == fieldType) {
            val = LocalDateUtils.toLocalDate(cellValue);
        } else if (LocalDateTime.class == fieldType) {
            val = LocalDateUtils.toLocalDateTime(cellValue);
        } else if (Date.class == fieldType) {
            String[] datePatterns = {
                    "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
                    "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
                    "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};
            DateUtils.parseDate(cellValue, datePatterns);
        } else if (fieldType.isEnum()) {
            String enumAttr = StringUtils.defaultString(attr.enumAttr(), attr.comboEnumField());
            for (Object enumConstant : fieldType.getEnumConstants()) {
                Field enumField = enumConstant.getClass().getDeclaredField(enumAttr);
                enumField.setAccessible(true);
                Object enumVal = enumField.get(enumConstant);
                if (enumVal != null && cellValue.equals(enumVal + "")) {
                    val = enumConstant;
                    break;
                }
            }
        }
        return val;
    }


    /**
     * 根据转换方法获取值
     *
     * @param oldValue
     * @param convertMethod 转换方法
     * @return 转换后的值
     * @throws Exception
     */
    private Object attrConvert(Object oldValue, String convertMethod) throws Exception {
        Object value = null;
        if (oldValue != null && StringUtils.isNotBlank(convertMethod)) {
            String clazzName = StringUtils.substringBeforeLast(convertMethod, ".");
            String methodName = StringUtils.substringAfterLast(convertMethod, ".");
            value = ReflectUtil.invokeMethod(
                    Class.forName(clazzName).newInstance(),
                    methodName,
                    new Class[]{oldValue.getClass()},
                    new Object[]{oldValue}
            );
        }
        return value;
    }

    /**
     * 数据处理器
     *
     * @param value 数据值
     * @param excel 数据注解
     * @return
     */
    public Object importFormatHandlerAdapter(Object value, Excel excel) {
        try {
            ExcelHandlerAdapter excelHandlerAdapter = (ExcelHandlerAdapter) excel.handler().newInstance();
            return excelHandlerAdapter.importFormat(value);
        } catch (Exception e) {
            log.error("不能格式化数据:{}\n{}", excel.handler(), e.getMessage());
        }
        return value;
    }


    /**
     * 反向解析值  0=男,1=女,2=未知
     *
     * @param propertyValue 参数值
     * @param exp           翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    private String reverseByExp(String propertyValue, String exp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = exp.split(",");
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(separator, propertyValue)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[1].equals(value)) {
                        propertyString.append(itemArray[0]).append(separator);
                    }
                }
            } else {
                if (itemArray[1].equals(propertyValue)) {
                    propertyString.append(itemArray[0]);
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }


    public ImportExcelUtil(Class<T> clazz, Integer startRow) {
        super(clazz);
        if (startRow != null) this.startRow = startRow;
    }


}
