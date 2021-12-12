package com.github.jokerpper.easyexcel.read.utils.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Read001 implements Serializable {

    private static final long serialVersionUID = -1988642503666670574L;

    /**
     * 姓名, 通过列索引匹配
     */
    @ExcelProperty(index = 0)
    private String name;

    /**
     * 年龄, 通过列名匹配(需一致,否则匹配不到值)
     *
     * <p>
     * 注: 若涉及指定数据头行索引值非1时,须指定列索引进行匹配
     * </p>
     */
    @ExcelProperty("年龄")
    private Integer age;

    /**
     * 性别, 通过列名匹配(需一致,否则匹配不到值)
     */
    @ExcelProperty("性别")
    private String sex;

    /**
     * 备注
     */
    @ExcelProperty(index = 3)
    private String remark;
}
