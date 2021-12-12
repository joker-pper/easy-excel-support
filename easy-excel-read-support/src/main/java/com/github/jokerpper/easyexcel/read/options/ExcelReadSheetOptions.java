package com.github.jokerpper.easyexcel.read.options;

import com.alibaba.excel.read.listener.ReadListener;
import com.github.jokerpper.easyexcel.read.callback.ExcelReadSheetOptionsCallback;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author joker-pper
 */
@Getter
@Setter
@ToString
@Builder
public class ExcelReadSheetOptions<T> {

    /**
     * 表index
     */
    private Integer sheetNo;

    /**
     * 表名
     */
    private String sheetName;

    /**
     * head class
     */
    private Class<T> headClass;

    /**
     * head row number
     */
    private Integer headRowNumber;

    /**
     * 是否使用科学计数法,默认false
     */
    private Boolean useScientificFormat;

    /**
     * read listener
     */
    private ReadListener<T> readListener;

    /**
     * callback
     */
    private ExcelReadSheetOptionsCallback callback;
}
