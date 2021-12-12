package com.github.jokerpper.easyexcel.read.callback;

import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;

/**
 * @author joker-pper
 */
public interface ExcelReadSheetOptionsCallback {

    /**
     * 用于进行其他属性的设置或覆盖并返回当前对象
     *
     * @param excelReaderSheetBuilder
     * @return
     */
    ExcelReaderSheetBuilder handle(ExcelReaderSheetBuilder excelReaderSheetBuilder);

}
