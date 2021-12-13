package io.github.jokerpper.easyexcel.read.callback;

import com.alibaba.excel.read.builder.ExcelReaderBuilder;

/**
 * @author joker-pper
 */
public interface ExcelRepeatedReadOptionsCallback {

    /**
     * 用于进行其他属性的设置或覆盖并返回当前对象
     *
     * @param excelReaderBuilder
     * @return
     */
    ExcelReaderBuilder handle(ExcelReaderBuilder excelReaderBuilder);

}
