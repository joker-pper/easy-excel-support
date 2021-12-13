package io.github.jokerpper.easyexcel.read.utils.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Read001ByIndex implements Serializable {

    private static final long serialVersionUID = -7548839262013741776L;

    /**
     * 姓名, 通过列索引匹配
     */
    @ExcelProperty(index = 0)
    private String name;

    /**
     * 年龄
     */
    @ExcelProperty(index = 1)
    private Integer age;

    /**
     * 性别
     */
    @ExcelProperty(index = 2)
    private String sex;

    /**
     * 备注
     */
    @ExcelProperty(index = 3)
    private String remark;

}
