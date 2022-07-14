package com.tht.space.poi;

import com.tht.space.poi.annotation.Excel;
import com.tht.space.poi.annotation.Excel.Type;
import com.tht.space.poi.annotation.Excels;
import com.tht.space.utils.EnumUtils;
import com.tht.space.utils.ImageUtils;
import com.tht.space.utils.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: space-core
 * @author: tht
 * @create: 2022-07-13 11:21
 * @description:
 **/
@Slf4j
public class ExportExcelUtil extends ExcelUtil<T> {
    /** Excel sheet最大行数，默认65536 */
    public static final int sheetSize = 65536;
    /** 工作表对象 */
    protected Sheet sheet;

    /** 样式列表 */
    private Map<String, CellStyle> styles;

    /** 导入导出数据列表 */
    private List<T> list;

    /** 注解列表 */
    private List<Object[]> fields;

    /** 最大高度 */
    private short maxHeight;

    /** 标题 */
    private String title;
    /** 统计列表 */
    private Map<Integer, Double> statistics = new HashMap<>();


    /**
     * 对list数据源将其里面的数据导出到excel表单
     *
     * @param response  返回数据
     * @param list      导出数据集合
     * @param sheetName 工作表的名称
     */
    public void exportExcel(HttpServletResponse response, List<T> list, String fileName, String sheetName) {
        exportExcel(response, list, fileName, sheetName, StringUtils.EMPTY);
    }

    @SneakyThrows
    public void exportExcel(HttpServletResponse response, List<T> list, String fileName, String sheetName, String title) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 使用postman名称会出现乱码，浏览器不会
        fileName = fileName + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        fileName = URLEncoder.encode(fileName, "utf-8");
        String headStr = "attachment; filename=\"" + fileName + "\"";
        response.setHeader("Content-Disposition", headStr);

        this.init(list, sheetName, title);
        exportExcel(response);
    }

    private void init(List<T> list, String sheetName, String title) {
        if (list == null) {
            list = new ArrayList<T>();
        }
        this.list = list;
        this.sheetName = sheetName;
        this.type = Type.EXPORT;
        this.title = title;
        createExcelField();
        createWorkbook();
        createTitle();
    }

    /**
     * 得到所有定义字段
     */
    private void createExcelField() {
        this.fields = getFields();
        this.fields = this.fields.stream().sorted(Comparator.comparing(objects -> ((Excel) objects[1]).sort())).collect(Collectors.toList());
        this.maxHeight = getRowHeight();
    }

    /**
     * 获取字段注解信息
     */
    private List<Object[]> getFields() {
        List<Object[]> fields = new ArrayList<Object[]>();
        List<Field> tempFields = new ArrayList<>();
        tempFields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
        tempFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        for (Field field : tempFields) {
            if (field.isAnnotationPresent(Excel.class)) {
                Excel attr = field.getAnnotation(Excel.class);
                if (attr != null && (attr.type() == Type.ALL || attr.type() == Type.EXPORT)) {
                    field.setAccessible(true);
                    fields.add(new Object[]{field, attr});
                }
            } else if (field.isAnnotationPresent(Excels.class)) {
                Excels attrs = field.getAnnotation(Excels.class);
                Excel[] excels = attrs.value();
                for (Excel attr : excels) {
                    if (attr != null && (attr.type() == Type.ALL || attr.type() == Type.EXPORT)) {
                        field.setAccessible(true);
                        fields.add(new Object[]{field, attr});
                    }
                }
            }
        }
        return fields;
    }

    /**
     * 根据注解获取最大行高
     */
    private short getRowHeight() {
        double maxHeight = 0;
        for (Object[] os : this.fields) {
            Excel excel = (Excel) os[1];
            maxHeight = Math.max(maxHeight, excel.height());
        }
        return (short) (maxHeight * 20);
    }

    /**
     * 创建一个工作簿
     */
    public void createWorkbook() {
        this.wb = new SXSSFWorkbook(500);
        this.sheet = wb.createSheet();
        wb.setSheetName(0, sheetName);
        this.styles = createStyles(wb);
    }

    /**
     * 创建表格样式
     *
     * @param wb 工作薄对象
     * @return 样式列表
     */
    private Map<String, CellStyle> createStyles(Workbook wb) {
        // 写入各条记录,每条记录对应excel表中的一行
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font titleFont = wb.createFont();
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBold(true);
        style.setFont(titleFont);
        styles.put("title", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        Font dataFont = wb.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 10);
        style.setFont(dataFont);
        styles.put("data", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = wb.createFont();
        headerFont.setFontName("Arial");
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(headerFont);
        styles.put("header", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font totalFont = wb.createFont();
        totalFont.setFontName("Arial");
        totalFont.setFontHeightInPoints((short) 10);
        style.setFont(totalFont);
        styles.put("total", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.LEFT);
        styles.put("data1", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.CENTER);
        styles.put("data2", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        styles.put("data3", style);

        return styles;
    }

    /**
     * 创建excel第一行标题
     */
    public void createTitle() {
        if (StringUtils.isNotEmpty(title)) {
            Row titleRow = sheet.createRow(startRow == 0 ? startRow++ : 0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellStyle(styles.get("title"));
            titleCell.setCellValue(title);
            sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), titleRow.getRowNum(),
                    this.fields.size() - 1));
        }
    }


    /**
     * 对list数据源将其里面的数据导入到excel表单
     */
    public void exportExcel(HttpServletResponse response) {
        try {
            writeSheet();
            wb.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("导出Excel异常{}", e.getMessage());
        } finally {
            IOUtils.closeQuietly(wb);
        }
    }


    /**
     * 创建写入数据到Sheet
     */
    public void writeSheet() {
        // 取出一共有多少个sheet.
        int sheetNo = Math.max(1, (int) Math.ceil(list.size() * 1.0 / sheetSize));
        for (int index = 0; index < sheetNo; index++) {
            createSheet(sheetNo, index);
            // 产生一行
            Row row = sheet.createRow(startRow);
            int column = 0;
            // 写入各个字段的列头名称
            for (Object[] os : fields) {
                Excel excel = (Excel) os[1];
                this.createCell(excel, row, column++);
            }
            if (Type.EXPORT.equals(type)) {
                fillExcelData(index, row);
                addStatisticsRow();
            }
        }
    }

    /**
     * 创建工作表
     *
     * @param sheetNo sheet数量
     * @param index   序号
     */
    public void createSheet(int sheetNo, int index) {
        // 设置工作表的名称.
        if (sheetNo > 1 && index > 0) {
            this.sheet = wb.createSheet();
            this.createTitle();
            wb.setSheetName(index, sheetName + index);
        }
    }


    /**
     * 创建列
     *
     * @param attr
     * @param row
     * @param column
     * @return
     */
    public Cell createCell(Excel attr, Row row, int column) {
        // 创建列
        Cell cell = row.createCell(column);
        // 写入列信息
        cell.setCellValue(attr.name());
        setDataValidation(attr, row, column);
        cell.setCellStyle(styles.get("header"));
        return cell;
    }

    /**
     * 创建表格样式
     */
    public void setDataValidation(Excel attr, Row row, int column) {
        if (attr.name().indexOf("注：") >= 0) {
            sheet.setColumnWidth(column, 6000);
        } else {
            // 设置列宽
            sheet.setColumnWidth(column, (int) ((attr.width() + 0.72) * 256));
        }
        // 如果设置了提示信息则鼠标放上去提示.
        if (StringUtils.isNotEmpty(attr.prompt())) {
            // 这里默认设了2-101列提示.
            setXSSFPrompt(sheet, "", attr.prompt(), 1, 100, column, column);
        }
        // 如果设置了combo属性则本列只能选择不能输入
        List<String> textList = new ArrayList<>(Arrays.asList(attr.combo()));
        List<String> comboEnumTexts = getComboEnumTexts(attr);
        if (comboEnumTexts != null && !comboEnumTexts.isEmpty()) {
            textList.addAll(comboEnumTexts);
        }
        if (!textList.isEmpty()) {
            // 这里默认设了2-101列只能选择不能输入.
            setXSSFValidation(sheet, textList.toArray(new String[0]), startRow + 1, 100, column, column);
        }
    }

    private List<String> getComboEnumTexts(Excel attr) {
        List<String> texts = new ArrayList<>();
        if (attr.comboEnum() == null) {
            return texts;
        }
        try {
            Class<?> clazz = attr.comboEnum();
            if (!clazz.isEnum()) {
                return texts;
            }
            Object[] enumConstants = clazz.getEnumConstants();
            if (StringUtils.isNotBlank(attr.comboEnumField())) {
                String fieldName = attr.comboEnumField();
                Field field = clazz.getDeclaredField(fieldName);
                for (Object enumConstant : enumConstants) {
                    field.setAccessible(true);
                    Object val = field.get(enumConstant);
                    texts.add(val.toString());
                }
            } else {
                List<String> collect = Arrays.stream(enumConstants).map(Object::toString).collect(Collectors.toList());
                texts.addAll(collect);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("操作失败,请检查@Excel注解comboEnum()和comboEnumField()属性是否正确");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return texts;
    }

    /**
     * 设置 POI XSSFSheet 单元格提示
     *
     * @param sheet         表单
     * @param promptTitle   提示标题
     * @param promptContent 提示内容
     * @param firstRow      开始行
     * @param endRow        结束行
     * @param firstCol      开始列
     * @param endCol        结束列
     */
    public void setXSSFPrompt(Sheet sheet, String promptTitle, String promptContent, int firstRow, int endRow,
                              int firstCol, int endCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createCustomConstraint("DD1");
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        DataValidation dataValidation = helper.createValidation(constraint, regions);
        dataValidation.createPromptBox(promptTitle, promptContent);
        dataValidation.setShowPromptBox(true);
        sheet.addValidationData(dataValidation);
    }

    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet    要设置的sheet.
     * @param textList 下拉框显示的内容
     * @param firstRow 开始行
     * @param endRow   结束行
     * @param firstCol 开始列
     * @param endCol   结束列
     * @return 设置好的sheet.
     */
    public void setXSSFValidation(Sheet sheet, String[] textList, int firstRow, int endRow, int firstCol, int endCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        // 加载下拉列表内容
        DataValidationConstraint constraint = helper.createExplicitListConstraint(textList);
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        // 数据有效性对象
        DataValidation dataValidation = helper.createValidation(constraint, regions);
        // 处理Excel兼容性问题
        if (dataValidation instanceof XSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }

        sheet.addValidationData(dataValidation);
    }

    /**
     * 填充excel数据
     *
     * @param index 序号
     * @param row   单元格行
     */
    public void fillExcelData(int index, Row row) {
        int startNo = index * sheetSize;
        int endNo = Math.min(startNo + sheetSize, list.size());
        for (int i = startNo; i < endNo; i++) {
            row = sheet.createRow(i + 1 + startRow - startNo);
            // 得到导出对象.
            T vo = (T) list.get(i);
            int column = 0;
            for (Object[] os : fields) {
                Field field = (Field) os[0];
                Excel excel = (Excel) os[1];
                this.addCell(excel, row, vo, field, column++);
            }
        }
    }

    /**
     * 添加单元格
     */
    public Cell addCell(Excel attr, Row row, T vo, Field field, int column) {
        Cell cell = null;
        try {
            // 设置行高
            row.setHeight(maxHeight);
            // 根据Excel中设置情况决定是否导出,有些情况需要保持为空,希望用户填写这一列.
            if (attr.isExport()) {
                // 创建cell
                cell = row.createCell(column);
                int align = attr.align().value();
                cell.setCellStyle(styles.get("data" + (align >= 1 && align <= 3 ? align : "")));

                // 用于读取对象中的属性
                Object value = getTargetValue(vo, field, attr);
                String dateFormat = attr.dateFormat();
                String readConverterExp = attr.readConverterExp();
                String separator = attr.separator();
                if (StringUtils.isNotBlank(dateFormat) && value != null) {
                    if (value instanceof Date) {
                        cell.setCellValue(new SimpleDateFormat(dateFormat).format((Date) value));
                    } else if (value instanceof LocalDate) {
                        cell.setCellValue(DateTimeFormatter.ofPattern(dateFormat).format((LocalDate) value));
                    } else if (value instanceof LocalDateTime) {
                        cell.setCellValue(DateTimeFormatter.ofPattern(dateFormat).format((LocalDateTime) value));
                    }
                } else if (StringUtils.isNotEmpty(readConverterExp) && value != null) {
                    cell.setCellValue(convertByExp(value.toString(), readConverterExp, separator));
                } else if (value instanceof BigDecimal && -1 != attr.scale()) {
                    cell.setCellValue((((BigDecimal) value).setScale(attr.scale(), attr.roundingMode())).toString());
                } else if (value instanceof Enum && StringUtils.isNotBlank(attr.enumAttr())) {
                    cell.setCellValue(StringUtils.toStr(EnumUtils.getFieldValue((Enum<?>) value, "display")));
                } else if (!attr.handler().equals(ExcelHandlerAdapter.class)) {
                    cell.setCellValue(dataFormatHandlerAdapter(value, attr));
                } else {
                    // 设置列类型
                    setCellVo(value, attr, cell);
                }
                addStatisticsData(column, StringUtils.toStr(value), attr);
            }
        } catch (Exception e) {
            log.error("导出Excel失败{}", e);
        }
        return cell;
    }

    /**
     * 获取bean中的属性值
     *
     * @param vo    实体对象
     * @param field 字段
     * @param excel 注解
     * @return 最终的属性值
     * @throws Exception
     */
    private Object getTargetValue(T vo, Field field, Excel excel) throws Exception {
        Object o = field.get(vo);
        if (StringUtils.isNotEmpty(excel.targetAttr())) {
            String target = excel.targetAttr();
            if (target.contains(".")) {
                String[] targets = target.split("[.]");
                for (String name : targets) {
                    o = getValue(o, name);
                }
            } else {
                o = getValue(o, target);
            }
        }
        if (StringUtils.isNotBlank(excel.comboEnumField()) && o != null) {
            Class<?> clazz = o.getClass();
            Field declaredField = clazz.getDeclaredField(excel.comboEnumField());
            declaredField.setAccessible(true);
            o = declaredField.get(o);

        }
        return o;
    }

    private Object getValue(Object o, String name) throws Exception {
        if (o != null && StringUtils.isNotEmpty(name)) {
            Class<?> clazz = o.getClass();
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            o = field.get(o);
        }
        return o;
    }

    /**
     * 解析导出值 0=男,1=女,2=未知
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String convertByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(",");
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(separator, propertyValue)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[0].equals(value)) {
                        propertyString.append(itemArray[1] + separator);
                        break;
                    }
                }
            } else {
                if (itemArray[0].equals(propertyValue)) {
                    return itemArray[1];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 数据处理器
     *
     * @param value 数据值
     * @param excel 数据注解
     * @return
     */
    public String dataFormatHandlerAdapter(Object value, Excel excel) {
        try {
            ExcelHandlerAdapter excelHandlerAdapter = (ExcelHandlerAdapter) excel.handler().newInstance();
            return excelHandlerAdapter.exportFormat(value);
        } catch (Exception e) {
            log.error("不能格式化数据:{}\n{}", excel.handler(), e.getMessage());
        }
        return (String) value;
    }

    /**
     * 设置单元格信息
     *
     * @param value 单元格值
     * @param attr  注解相关
     * @param cell  单元格信息
     */
    public void setCellVo(Object value, Excel attr, Cell cell) {
        if (Excel.ColumnType.STRING == attr.cellType()) {
            String cellValue = StringUtils.toStr(value);
            // 对于任何以表达式触发字符 =-+@开头的单元格，直接使用tab字符作为前缀，防止CSV注入。
            // if (StringUtils.containsAny(cellValue, FORMULA_STR)) {
            //     cellValue = StringUtils.replaceEach(cellValue, FORMULA_STR, new String[]{"\t=", "\t-", "\t+", "\t@"});
            // }
            cell.setCellValue(StringUtils.isBlank(cellValue) ? attr.defaultValue() : cellValue + attr.suffix());
        } else if (Excel.ColumnType.NUMERIC == attr.cellType()) {
            if (value != null) {
                cell.setCellValue(StringUtils.contains((String) value, ".") ? Double.parseDouble((String) value) : Integer.parseInt((String) value));
            }
        } else if (Excel.ColumnType.IMAGE == attr.cellType()) {
            ClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) cell.getColumnIndex(), cell.getRow().getRowNum(), (short) (cell.getColumnIndex() + 1), cell.getRow().getRowNum() + 1);
            String imagePath = (String) value;
            if (StringUtils.isNotEmpty(imagePath)) {
                byte[] data = ImageUtils.getImage(imagePath);
                getDrawingPatriarch(cell.getSheet()).createPicture(anchor,
                        cell.getSheet().getWorkbook().addPicture(data, getImageType(data)));
            }
        }
    }

    /**
     * 获取画布
     */
    public static Drawing<?> getDrawingPatriarch(Sheet sheet) {
        if (sheet.getDrawingPatriarch() == null) {
            sheet.createDrawingPatriarch();
        }
        return sheet.getDrawingPatriarch();
    }

    /**
     * 获取图片类型,设置图片插入类型
     */
    public int getImageType(byte[] value) {
        String type = ImageUtils.getFileExtendName(value);
        if ("JPG".equalsIgnoreCase(type)) {
            return Workbook.PICTURE_TYPE_JPEG;
        } else if ("PNG".equalsIgnoreCase(type)) {
            return Workbook.PICTURE_TYPE_PNG;
        }
        return Workbook.PICTURE_TYPE_JPEG;
    }

    /**
     * 合计统计信息
     */
    private void addStatisticsData(Integer index, String text, Excel entity) {
        if (entity != null && entity.isStatistics()) {
            Double temp = 0D;
            if (!statistics.containsKey(index)) {
                statistics.put(index, temp);
            }
            try {
                temp = Double.valueOf(text);
            } catch (NumberFormatException e) {
            }
            statistics.put(index, statistics.get(index) + temp);
        }
    }

    /**
     * 创建统计行
     */
    public void addStatisticsRow() {
        if (statistics.size() > 0) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            Set<Integer> keys = statistics.keySet();
            Cell cell = row.createCell(0);
            cell.setCellStyle(styles.get("total"));
            cell.setCellValue("合计");

            for (Integer key : keys) {
                cell = row.createCell(key);
                cell.setCellStyle(styles.get("total"));
                cell.setCellValue(DOUBLE_FORMAT.format(statistics.get(key)));
            }
            statistics.clear();
        }
    }


    public ExportExcelUtil(Class<T> clazz) {
        super(clazz);
    }
}
