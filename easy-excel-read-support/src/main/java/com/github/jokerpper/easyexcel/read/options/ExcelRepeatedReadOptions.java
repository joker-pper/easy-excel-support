package com.github.jokerpper.easyexcel.read.options;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.github.jokerpper.easyexcel.read.callback.ExcelRepeatedReadOptionsCallback;
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
public class ExcelRepeatedReadOptions {

    /**
     * excel type
     */
    private ExcelTypeEnum excelTypeEnum;

    /**
     * 是否忽略空行
     */
    private Boolean ignoreEmptyRow;

    /**
     * 是否使用科学计数法,默认false
     */
    private Boolean useScientificFormat;

    /**
     * password
     */
    private String password;

    /**
     * callback
     */
    private ExcelRepeatedReadOptionsCallback callback;

}
