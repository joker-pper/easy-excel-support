package io.github.jokerpper.easyexcel.read.listener;

import com.alibaba.excel.context.AnalysisContext;

/**
 * @author joker-pper
 */
public class BreakReadListener<T> implements DefaultReadListener<T> {

    private boolean isBreakRead = false;

    /**
     * 设置停止读取
     */
    public void makeBreakRead() {
        isBreakRead = true;
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return !isBreakRead;
    }

}
