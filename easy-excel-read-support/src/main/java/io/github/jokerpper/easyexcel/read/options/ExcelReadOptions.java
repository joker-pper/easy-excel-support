package io.github.jokerpper.easyexcel.read.options;

import com.alibaba.excel.support.ExcelTypeEnum;
import io.github.jokerpper.easyexcel.read.callback.ExcelReadOptionsCallback;
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
public class ExcelReadOptions<T> {

    /**
     * 是否为readAll,默认为false
     */
    private Boolean isReadAll;

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
     * excel type
     */
    private ExcelTypeEnum excelTypeEnum;

    /**
     * head row number
     */
    private Integer headRowNumber;

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
    private ExcelReadOptionsCallback callback;

}
