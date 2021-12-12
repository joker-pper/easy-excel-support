package com.github.jokerpper.easyexcel.read.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.read.listener.ReadListener;

import java.util.Map;

/**
 * @author joker-pper
 */
public interface DefaultReadListener<T> extends ReadListener<T> {

    /**
     * When analysis one head row trigger invoke function.
     *
     * @param headMap
     * @param context
     */
    @Override
    default void invokeHead(Map<Integer, CellData> headMap, AnalysisContext context) {
    }

    /**
     * When analysis one row trigger invoke function.
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context analysis context
     */
    @Override
    default void invoke(T data, AnalysisContext context) {
    }

    /**
     * All listeners receive this method when any one Listener does an error report. If an exception is thrown here, the
     * entire read will terminate.
     *
     * @param exception
     * @param context
     * @throws Exception
     */
    @Override
    default void onException(Exception exception, AnalysisContext context) throws Exception {
        throw exception;
    }

    /**
     * The current method is called when extra information is returned
     *
     * @param extra   extra information
     * @param context analysis context
     */
    @Override
    default void extra(CellExtra extra, AnalysisContext context) {
    }

    /**
     * Verify that there is another piece of data.You can stop the read by returning false
     *
     * @param context
     * @return
     */
    @Override
    default boolean hasNext(AnalysisContext context) {
        return true;
    }

    /**
     * if have something to do after all analysis
     *
     * @param context
     */
    @Override
    default void doAfterAllAnalysed(AnalysisContext context) {
    }
}
