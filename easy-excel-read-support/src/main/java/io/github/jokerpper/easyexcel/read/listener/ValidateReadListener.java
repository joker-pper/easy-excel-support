package io.github.jokerpper.easyexcel.read.listener;

import com.alibaba.excel.context.AnalysisContext;
import io.github.jokerpper.easyexcel.read.support.ValidateReadSupport;

/**
 * @author joker-pper
 */
public interface ValidateReadListener<T> extends DefaultReadListener<T>, ValidateReadSupport<T> {

    @Override
    default void invoke(T data, AnalysisContext context) {
        //重写,仅用于校验数据是否正确
        validate(data);
    }
}
