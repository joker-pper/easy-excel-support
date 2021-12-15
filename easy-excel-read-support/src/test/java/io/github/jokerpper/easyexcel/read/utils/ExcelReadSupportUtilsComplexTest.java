package io.github.jokerpper.easyexcel.read.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import io.github.jokerpper.easyexcel.read.options.ExcelReadOptions;
import io.github.jokerpper.easyexcel.read.listener.AbstractBatchResolveAndConvertReadListener;
import io.github.jokerpper.easyexcel.read.listener.BreakReadListener;
import io.github.jokerpper.easyexcel.read.listener.BatchResolveReadListener;
import io.github.jokerpper.easyexcel.read.options.ExcelReadSheetOptions;
import io.github.jokerpper.easyexcel.read.options.ExcelRepeatedReadOptions;
import io.github.jokerpper.easyexcel.read.utils.model.Read001;
import io.github.jokerpper.easyexcel.read.utils.model.Read001ByIndex;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelReadSupportUtilsComplexTest {

    /**
     * 携带密码的读取
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithPassword() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001-encrypted.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        String password = "123456";
        ExcelReadOptions<Read001> readOptions = ExcelReadOptions.<Read001>builder().headClass(Read001.class).password(password).build();

        ReadListener<Read001> readListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        });
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);

    }

    /**
     * 通过默认的head row num读取数据
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithDefaultHeadRowNum() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelReadOptions<Read001> readOptions = ExcelReadOptions.<Read001>builder().headClass(Read001.class).headRowNumber(1).build();

        ReadListener<Read001> readListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        });
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);
    }


    /**
     * 通过指定head row num读取数据
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithOtherHeadRowNum() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001-head-row-num.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelReadOptions<Read001ByIndex> readOptions = ExcelReadOptions.<Read001ByIndex>builder().headClass(Read001ByIndex.class).headRowNumber(2).build();

        ReadListener<Read001ByIndex> readListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        });
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);
    }


    /**
     * 动态解析自定义映射结果 (列名 -> 列值)
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithOtherHeadRowNumForCustomData() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelReadOptions readOptions = ExcelReadOptions.builder().headRowNumber(0).build();

        ReadListener readListener = new AbstractBatchResolveAndConvertReadListener<Map<String, String>>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        }) {

            private Map<Integer, String> headMap = new HashMap<>(8);

            @Override
            protected Map<String, String> convert(Map<Integer, Object> data, AnalysisContext context) {
                int rowIndex = context.readRowHolder().getRowIndex();
                if (rowIndex == 0) {
                    data.forEach((cellIndex, cellValue) -> {
                        //为表头时放入headMap, key: 列索引 value: 列名
                        headMap.put(cellIndex, Optional.ofNullable(cellValue).map(String::valueOf).orElse(String.format("列%s(系统生成)", cellIndex)));
                    });
                    return null;
                }
                Map<String, String> resultMap = new HashMap<>(8);
                data.forEach((cellIndex, cellValue) -> {
                    //按照列名对应的值进行处理
                    resultMap.put(headMap.get(cellIndex), Optional.ofNullable(cellValue).map(String::valueOf).orElse(null));
                });
                return resultMap;
            }
        };
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);
    }


    /**
     * 验证表头是否合法(并在表头的最后一行时停止读取)
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithValidateHead() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001-head-row-num.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelReadOptions<Read001ByIndex> readOptions = ExcelReadOptions.<Read001ByIndex>builder().headClass(Read001ByIndex.class)
                .headRowNumber(2)
                .build();

        List<List<String>> headRowsList = new ArrayList<>(2);
        headRowsList.add(Arrays.asList("姓名", "年龄", "性别", "备注"));
        headRowsList.add(Arrays.asList("制作时间"));

        ReadListener<Read001ByIndex> readListener = new BreakReadListener<Read001ByIndex>() {

            @Override
            public void invokeHeadByString(Map<Integer, String> headMap, AnalysisContext context) {
                System.out.println(String.format("head: %s", headMap));
                //验证为正确的表头
                int rowIndex = context.readRowHolder().getRowIndex();

                int headEndIndex = headRowsList.size() - 1;

                List<String> headRowList = headRowsList.get(rowIndex);

                List<String> rowTextList = headMap.values().stream().filter(it -> it != null && !it.isEmpty()).map(String::trim).collect(Collectors.toList());

                if (rowIndex == headEndIndex) {
                    //为表头的最后一行时,设置停止读取
                    makeBreakRead();

                    if (rowTextList.size() != headRowList.size() || Optional.ofNullable(headMap.get(0)).map(String::trim).map(it -> !it.startsWith(headRowList.get(0))).orElse(true)) {
                        //当size不一致或第一列不是以对应的字符串开始时
                        throw new IllegalArgumentException("无法识别的Excel文件,请确认是否上传正确！");
                    }

                } else {
                    if (!headRowList.equals(rowTextList)) {
                        //当结果不一致时
                        throw new IllegalArgumentException("无法识别的Excel文件,请确认是否上传正确！");
                    }
                }
            }

            @Override
            public void invoke(Read001ByIndex data, AnalysisContext context) {
                //这里输出结果,验证是否会停止读取数据
                System.out.println(String.format("当前行数据为: %s", data));
                throw new RuntimeException("当前示例只是验证表头是否正确,但并未停止读取数据行!");
            }

        };

        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);

    }

    /**
     * 读取多个表,一次全部读取(表格式内容需一致)
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithMoreSheetByReadAll() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001-more-sheet.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelReadOptions<Read001> readOptions = ExcelReadOptions.<Read001>builder().headClass(Read001.class).isReadAll(true).build();
        ReadListener<Read001> readListener = new BatchResolveReadListener<>(10, dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        });
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);

    }


    /**
     * 读取多个表 - 多次读取
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithMoreSheet() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001-more-sheet.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        //读取表一的数据
        ExcelReadOptions<Read001> readOptions = ExcelReadOptions.<Read001>builder().headClass(Read001.class).build();
        ReadListener<Read001> readListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("(表一)当前要批量处理的数据为:\n %s", dataList));
        });
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);

        System.out.println("-------——分隔符-------——");

        //读取表二的数据
        inputStream = new FileInputStream(inputFile);
        readOptions = ExcelReadOptions.<Read001>builder().headClass(Read001.class).sheetNo(1).build();
        readListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("(表二)当前要批量处理的数据为:\n %s", dataList));
        });
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);
    }

    /**
     * 读取多个表 - 一次读取
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithMoreSheetByRepeatedRead() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001-more-sheet-repeated.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelRepeatedReadOptions readOptions = ExcelRepeatedReadOptions.builder().build();
        List<ExcelReadSheetOptions<?>> readSheetOptionsList = new ArrayList<>(8);

        //读取表一的数据
        ReadListener<Read001> readListener1 = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("(表一)当前要批量处理的数据为:\n %s", dataList));
        });
        readSheetOptionsList.add(ExcelReadSheetOptions.<Read001>builder().sheetNo(0).headClass(Read001.class).readListener(readListener1).build());

        //读取表二的数据
        ReadListener<Read001ByIndex> readListener2 = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("(表二)当前要批量处理的数据为:\n %s", dataList));
        });
        readSheetOptionsList.add(ExcelReadSheetOptions.<Read001ByIndex>builder().sheetNo(1).headRowNumber(2).headClass(Read001ByIndex.class).readListener(readListener2).build());

        ExcelReadSupportUtils.repeatedRead(inputStream, readOptions, readSheetOptionsList);
    }

}