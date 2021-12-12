package com.github.jokerpper.easyexcel.read.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.github.jokerpper.easyexcel.read.callback.ExcelReadOptionsCallback;
import com.github.jokerpper.easyexcel.read.callback.ExcelReadSheetOptionsCallback;
import com.github.jokerpper.easyexcel.read.callback.ExcelRepeatedReadOptionsCallback;
import com.github.jokerpper.easyexcel.read.options.ExcelRepeatedReadOptions;
import com.github.jokerpper.easyexcel.read.options.ExcelReadOptions;
import com.github.jokerpper.easyexcel.read.options.ExcelReadSheetOptions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author joker-pper
 */
public class ExcelReadSupportUtils {

    private ExcelReadSupportUtils() {
    }

    /**
     * 通过文件流和readListener读取excel
     *
     * @param inputStream
     * @param readListener
     * @param <T>
     */
    public static <T> void read(InputStream inputStream, ReadListener<T> readListener) {
        read(inputStream, readListener, null);
    }

    /**
     * 通过文件流、表头class和readListener读取excel
     *
     * @param inputStream
     * @param headClass
     * @param readListener
     * @param <T>
     */
    public static <T> void read(InputStream inputStream, Class<T> headClass, ReadListener<T> readListener) {
        read(inputStream, readListener, ExcelReadOptions.<T>builder().headClass(headClass).build());
    }

    /**
     * 通过文件流、readListener和options读取excel
     *
     * @param inputStream
     * @param readListener
     * @param readOptions
     * @param <T>
     */
    @SuppressWarnings("rawtypes")
    public static <T> void read(InputStream inputStream, ReadListener<T> readListener, ExcelReadOptions<T> readOptions) {
        ExcelReaderBuilder readerBuilder = EasyExcel.read(inputStream).registerReadListener(readListener);
        ExcelReaderSheetBuilder readerSheetBuilder;
        if (readOptions != null) {
            readerBuilder.head(readOptions.getHeadClass())
                    .excelType(readOptions.getExcelTypeEnum())
                    .headRowNumber(readOptions.getHeadRowNumber())
                    .ignoreEmptyRow(readOptions.getIgnoreEmptyRow())
                    .useScientificFormat(readOptions.getUseScientificFormat())
                    .password(readOptions.getPassword());

            ExcelReadOptionsCallback optionsCallback = readOptions.getCallback();
            if (optionsCallback != null) {
                //用于进行其他属性的设置或覆盖
                ExcelReaderBuilder handledReaderBuilder = optionsCallback.handle(readerBuilder);
                if (handledReaderBuilder != readerBuilder) {
                    throw new RuntimeException(String.format("Not Support New ExcelReaderBuilder Instance!"));
                }
            }

            if (Boolean.TRUE.equals(readOptions.getIsReadAll())) {
                //读取全部表(注: 多个表格都要能被同一个readListener处理)
                readerBuilder.doReadAll();
            } else {
                //读取单个表
                if (readOptions.getSheetNo() != null || readOptions.getSheetName() != null) {
                    readerSheetBuilder = readerBuilder.sheet(readOptions.getSheetNo(), readOptions.getSheetName());
                } else {
                    readerSheetBuilder = readerBuilder.sheet();
                }
                readerSheetBuilder.doRead();
            }
        } else {
            readerSheetBuilder = readerBuilder.sheet();
            readerSheetBuilder.doRead();
        }
    }

    /**
     * 一次读取多个表
     *
     * @param inputStream
     * @param readOptions
     * @param readSheetOptionsList
     */
    public static void repeatedRead(InputStream inputStream, ExcelRepeatedReadOptions readOptions, List<ExcelReadSheetOptions<?>> readSheetOptionsList) {
        if (readSheetOptionsList == null || readSheetOptionsList.isEmpty()) {
            throw new IllegalArgumentException("readSheetOptionsList Must Be Not Empty!");
        }

        ExcelReader excelReader = null;
        try {
            ExcelReaderBuilder readerBuilder = EasyExcel.read(inputStream)
                    .excelType(readOptions.getExcelTypeEnum())
                    .ignoreEmptyRow(readOptions.getIgnoreEmptyRow())
                    .useScientificFormat(readOptions.getUseScientificFormat())
                    .password(readOptions.getPassword());

            ExcelRepeatedReadOptionsCallback optionsCallback = readOptions.getCallback();
            if (optionsCallback != null) {
                //用于进行其他属性的设置或覆盖
                ExcelReaderBuilder handledReaderBuilder = optionsCallback.handle(readerBuilder);
                if (handledReaderBuilder != readerBuilder) {
                    throw new RuntimeException(String.format("Not Support New ExcelReaderBuilder Instance!"));
                }
            }
            excelReader = readerBuilder.build();

            //读取多个表
            List<ReadSheet> readSheetList = getReadSheetList(readSheetOptionsList);
            excelReader.read(readSheetList);
        } finally {
            if (excelReader != null) {
                excelReader.finish();
            }
        }
    }

    /**
     * 获取readSheetList
     *
     * @param readSheetOptionsList
     * @return
     */
    private static List<ReadSheet> getReadSheetList(List<ExcelReadSheetOptions<?>> readSheetOptionsList) {
        List<ReadSheet> readSheetList = new ArrayList<>(readSheetOptionsList.size());
        for (ExcelReadSheetOptions readSheetOptions : readSheetOptionsList) {
            readSheetList.add(getReadSheet(readSheetOptions));
        }
        return readSheetList;
    }

    /**
     * 获取readSheet
     *
     * @param readSheetOptions
     * @param <T>
     * @return
     */
    private static <T> ReadSheet getReadSheet(ExcelReadSheetOptions<T> readSheetOptions) {
        ExcelReaderSheetBuilder readerSheetBuilder = EasyExcel.readSheet(readSheetOptions.getSheetNo(), readSheetOptions.getSheetName())
                .head(readSheetOptions.getHeadClass())
                .headRowNumber(readSheetOptions.getHeadRowNumber())
                .useScientificFormat(readSheetOptions.getUseScientificFormat())
                .registerReadListener(readSheetOptions.getReadListener());

        ExcelReadSheetOptionsCallback optionsCallback = readSheetOptions.getCallback();
        if (optionsCallback != null) {
            //用于进行其他属性的设置或覆盖
            ExcelReaderSheetBuilder handledReaderSheetBuilder = optionsCallback.handle(readerSheetBuilder);
            if (handledReaderSheetBuilder != readerSheetBuilder) {
                throw new RuntimeException(String.format("Not Support New ExcelReaderSheetBuilder Instance!"));
            }
        }
        return readerSheetBuilder.build();
    }
}
