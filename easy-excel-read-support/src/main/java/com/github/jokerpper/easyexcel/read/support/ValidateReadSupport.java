package com.github.jokerpper.easyexcel.read.support;


/**
 * @author joker-pper
 */
public interface ValidateReadSupport<T> {

    /**
     * 验证数据是否符合
     *
     * @param data
     */
    void validate(T data);
}
