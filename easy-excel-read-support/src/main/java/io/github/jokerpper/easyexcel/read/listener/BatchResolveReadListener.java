package io.github.jokerpper.easyexcel.read.listener;

import com.alibaba.excel.context.AnalysisContext;
import io.github.jokerpper.easyexcel.read.support.ValidateReadSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author joker-pper
 */
public class BatchResolveReadListener<T> extends BreakReadListener<T> implements ValidateReadSupport<T> {

    /**
     * 默认批量处理的数量
     */
    private static int DEFAULT_BATCH_COUNT = 500;

    /**
     * 处理数据的consumer,可用于持久化到数据库等操作
     */
    private final Consumer<List<T>> resolveConsumer;

    /**
     * 批量处理的数量
     */
    private final int batchCount;

    /**
     * 处理的总数量
     */
    private long totalCount;

    /**
     * 待处理的数据列表
     */
    private final List<T> toResolveDataList;

    public BatchResolveReadListener(Consumer<List<T>> resolveConsumer) {
        this(DEFAULT_BATCH_COUNT, resolveConsumer);
    }

    public BatchResolveReadListener(int batchCount, Consumer<List<T>> resolveConsumer) {
        this.resolveConsumer = resolveConsumer;
        this.batchCount = batchCount;
        this.totalCount = 0L;
        toResolveDataList = new ArrayList<>(batchCount);
    }

    /**
     * 验证数据是否符合
     *
     * @param data
     */
    @Override
    public void validate(T data) {
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        //该行解析完成后

        //验证数据是否符合
        validate(data);

        toResolveDataList.add(data);
        totalCount++;
        if (toResolveDataList.size() >= batchCount) {
            try {
                //进行处理数据
                resolveConsumer.accept(toResolveDataList);
            } finally {
                toResolveDataList.clear();
            }
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        //数据解析完毕时
        if (!toResolveDataList.isEmpty()) {
            try {
                //进行处理剩下的数据
                resolveConsumer.accept(toResolveDataList);
            } finally {
                toResolveDataList.clear();
            }
        }
    }


    /**
     * 获取处理的总数据个数
     *
     * @return
     */
    public long getTotalCount() {
        return totalCount;
    }
}
