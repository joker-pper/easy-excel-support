package io.github.jokerpper.easyexcel.read.utils;

import com.alibaba.excel.context.AnalysisContext;
import io.github.jokerpper.easyexcel.read.listener.AbstractBatchResolveAndConvertReadListener;
import io.github.jokerpper.easyexcel.read.listener.BatchResolveReadListener;
import io.github.jokerpper.easyexcel.read.listener.DefaultReadListener;
import io.github.jokerpper.easyexcel.read.listener.ValidateReadListener;
import io.github.jokerpper.easyexcel.read.utils.model.Read001;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class ExcelReadSupportUtilsTest {

    /**
     * 不创建映射类的读 - Map<Integer, Object>
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readBySimple() throws FileNotFoundException {

        File inputFile = new File("src/test/resources/files/read-001.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);
        ExcelReadSupportUtils.read(inputStream, new DefaultReadListener<Map<Integer, Object>>() {

            @Override
            public void invoke(Map<Integer, Object> data, AnalysisContext context) {

                System.out.println(String.format("当前行数据为: %s", data));
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("数据解析完毕...");

            }

        });

    }

    /**
     * 创建映射类最简单的读
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readBySimpleWithClass() throws FileNotFoundException {

        File inputFile = new File("src/test/resources/files/read-001.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelReadSupportUtils.read(inputStream, Read001.class, new DefaultReadListener<Read001>() {

            @Override
            public void invoke(Read001 data, AnalysisContext context) {
                System.out.println(String.format("当前行数据为: %s", data));
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("数据解析完毕...");

            }
        });

    }

    /**
     * 通过BatchResolveReadListener读取进行批量处理
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithBatchResolveListener() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        BatchResolveReadListener<Read001> readListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        });

        ExcelReadSupportUtils.read(inputStream, Read001.class, readListener);
        System.out.println(String.format("处理的总数据个数为: %s", readListener.getTotalCount()));

    }

    /**
     * 通过AbstractBatchResolveAndConvertReadListener自定义转换并批量处理
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithBatchResolveAndConvertListener() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);
        AbstractBatchResolveAndConvertReadListener<Read001> readListener = new AbstractBatchResolveAndConvertReadListener<Read001>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        }) {
            @Override
            protected Read001 convert(Map<Integer, Object> data, AnalysisContext context) {
                Read001 read001 = new Read001();
                read001.setName((String) data.get(0));
                read001.setAge(Optional.ofNullable(data.get(1)).map(String::valueOf).map(Integer::valueOf).orElse(null));
                read001.setSex((String) data.get(2));
                return read001;
            }
        };
        ExcelReadSupportUtils.read(inputStream, readListener);

        System.out.println(String.format("处理的总数据个数为: %s", readListener.getTotalCount()));
    }


    /**
     * 通过BatchResolveReadListener读取并验证数据
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithBatchResolveReadListenerAndValidate() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        //第一遍先校验数据(如果有要求是文件中必须不包含错误数据时)
        BatchResolveReadListener<Read001> toValidateReadListener = new BatchResolveReadListener<Read001>(dataList -> {
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        }) {
            @Override
            public void validate(Read001 data) {
                if (data == null || data.getName() == null && data.getRemark() == null) {
                    //校验数据(根据具体要求来校验)
                    throw new IllegalArgumentException("excel has invalid data!");
                }
            }
        };
        ExcelReadSupportUtils.read(inputStream, Read001.class, toValidateReadListener);
        System.out.println(String.format("处理的总数据个数为: %s", toValidateReadListener.getTotalCount()));

        //(模拟)第二遍再进行真正的持久化操作(注: 也可以把之前的解析的数据保存到text、json等再做持久化操作)
        inputStream = new FileInputStream(inputFile);
        BatchResolveReadListener<Read001> toSaveReadListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库
            System.out.println(String.format("(模拟)当前保存的数据为:\n %s", dataList));
        });

        ExcelReadSupportUtils.read(inputStream, Read001.class, toSaveReadListener);
        System.out.println(String.format("(模拟)保存的总数据个数为: %s", toSaveReadListener.getTotalCount()));
    }


    /**
     * 通过ValidateReadListener简单验证再通过BatchResolveReadListener读取数据
     *
     * @throws FileNotFoundException
     */
    @Test
    public void readWithValidateReadListenerAndBatchResolveReadListener() throws FileNotFoundException {
        File inputFile = new File("src/test/resources/files/read-001.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        //第一遍先校验数据(如果有要求是文件中必须不包含错误数据时)
        ValidateReadListener<Read001> toValidateReadListener = data -> {
            if (data == null || data.getName() == null && data.getRemark() == null) {
                //校验数据(根据具体要求来校验)
                throw new IllegalArgumentException("excel has invalid data!");
            }
        };
        ExcelReadSupportUtils.read(inputStream, Read001.class, toValidateReadListener);

        //(模拟)第二遍再进行真正的持久化操作(注: 也可以把之前的解析的数据保存到text、json等再做持久化操作)
        inputStream = new FileInputStream(inputFile);
        BatchResolveReadListener<Read001> toSaveReadListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库
            System.out.println(String.format("(模拟)当前保存的数据为:\n %s", dataList));
        });

        ExcelReadSupportUtils.read(inputStream, Read001.class, toSaveReadListener);
        System.out.println(String.format("(模拟)保存的总数据个数为: %s", toSaveReadListener.getTotalCount()));
    }


}