

# easy-excel-support

    基于EasyExcel进行封装的项目,提供一定的常用读写能力,希望可以进一步简化EasyExcel的使用.

## 版本

+ java 1.8+

+ EasyExcel 3.x

## easy-excel-read-support

### 功能

+ 简化使用

+ 基于Options包含常规属性并提供入口设置其他属性

+ 提供部分listener支持业务


### 核心API

```

<T> void read(InputStream inputStream, ReadListener<T> readListener)

<T> void read(InputStream inputStream, Class<T> headClass, ReadListener<T> readListener)

<T> void read(InputStream inputStream, ReadListener<T> readListener, ExcelReadOptions<T> readOptions)

void repeatedRead(InputStream inputStream, ExcelRepeatedReadOptions readOptions, List<ExcelReadSheetOptions<?>> readSheetOptionsList)
```


### @ExcelProperty使用问题

```
1. 通过列名匹配(需一致,否则匹配不到值),如果名字重复会导致只有一个字段读取到数据

2. 若涉及指定数据头行索引值非1时,须指定列索引进行匹配
```


### 常见场景示例

> 不创建映射类最简单的读

```

        File inputFile = new File("src\\test\\resources\\files\\read-001.xlsx");
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

```

> 创建映射类最简单的读

```

        File inputFile = new File("src\\test\\resources\\files\\read-001.xlsx");
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

```

> 通过BatchResolveReadListener读取进行批量处理

```

        File inputFile = new File("src\\test\\resources\\files\\read-001.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        BatchResolveReadListener<Read001> readListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        });

        ExcelReadSupportUtils.read(inputStream, Read001.class, readListener);
        System.out.println(String.format("处理的总数据个数为: %s", readListener.getTotalCount()));

```

> 通过BatchResolveReadListener读取并验证数据

```

        File inputFile = new File("src\\test\\resources\\files\\read-001.xlsx");
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
    
```

> 通过ValidateReadListener简单验证再通过BatchResolveReadListener读取数据

```
        File inputFile = new File("src\\test\\resources\\files\\read-001.xlsx");
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
    
```


> 通过AbstractBatchResolveAndConvertReadListener自定义转换并批量处理

```
        
        File inputFile = new File("src\\test\\resources\\files\\read-001.xlsx");
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

```

> 动态解析自定义映射结果 (列名 -> 列值)

```

        File inputFile = new File("src\\test\\resources\\files\\read-001.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelReadOptions readOptions = ExcelReadOptions.builder().headRowNumber(0).build();

        ReadListener readListener = new AbstractBatchResolveAndConvertReadListener<Map<String, String>>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        }) {

            private Map<Integer, String> headMap = new HashMap<>(8);

            @Override
            protected Map<String, String> convert(Map<Integer, Object> data, AnalysisContext context) {
                int rowIndex = context.readRowHolder().getRowIndex();
                if (rowIndex == 0) {
                    data.forEach((cellIndex, cellValue) -> {
                        //为表头时放入headMap, key: 列索引 value: 列名
                        headMap.put(cellIndex, Optional.ofNullable(cellValue).map(String::valueOf).orElse(String.format("列%s(系统生成)", cellIndex)));
                    });
                    return null;
                }
                Map<String, String> resultMap = new HashMap<>(8);
                data.forEach((cellIndex, cellValue) -> {
                    //按照列名对应的值进行处理
                    resultMap.put(headMap.get(cellIndex), Optional.ofNullable(cellValue).map(String::valueOf).orElse(null));
                });
                return resultMap;
            }
        };
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);
        
```

> 验证表头是否合法(并在表头的最后一行时停止读取)

```
        
        File inputFile = new File("src\\test\\resources\\files\\read-001-head-row-num.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelReadOptions<Read001ByIndex> readOptions = ExcelReadOptions.<Read001ByIndex>builder().headClass(Read001ByIndex.class)
                .headRowNumber(2)
                .build();

        List<List<String>> headRowsList = new ArrayList<>(2);
        headRowsList.add(Arrays.asList("姓名", "年龄", "性别", "备注"));
        headRowsList.add(Arrays.asList("制作时间"));

        ReadListener<Read001ByIndex> readListener = new BreakReadListener<Read001ByIndex>() {

            @Override
            public void invokeHeadByString(Map<Integer, String> headMap, AnalysisContext context) {
                System.out.println(String.format("head: %s", headMap));
                //验证为正确的表头
                int rowIndex = context.readRowHolder().getRowIndex();

                int headEndIndex = headRowsList.size() - 1;

                List<String> headRowList = headRowsList.get(rowIndex);

                List<String> rowTextList = headMap.values().stream().filter(it -> it != null && !it.isEmpty()).map(String::trim).collect(Collectors.toList());

                if (rowIndex == headEndIndex) {
                    //为表头的最后一行时,设置停止读取
                    makeBreakRead();

                    if (rowTextList.size() != headRowList.size() || Optional.ofNullable(headMap.get(0)).map(String::trim).map(it -> !it.startsWith(headRowList.get(0))).orElse(true)) {
                        //当size不一致或第一列不是以对应的字符串开始时
                        throw new IllegalArgumentException("无法识别的Excel文件,请确认是否上传正确！");
                    }

                } else {
                    if (!headRowList.equals(rowTextList)) {
                        //当结果不一致时
                        throw new IllegalArgumentException("无法识别的Excel文件,请确认是否上传正确！");
                    }
                }
            }

            @Override
            public void invoke(Read001ByIndex data, AnalysisContext context) {
                //这里输出结果,验证是否会停止读取数据
                System.out.println(String.format("当前行数据为: %s", data));
                throw new RuntimeException("当前示例只是验证表头是否正确,但并未停止读取数据行!");
            }

        };

        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);
        
```

> 读取多个表,一次全部读取(表格式内容需一致)

```
        File inputFile = new File("src\\test\\resources\\files\\read-001-more-sheet.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelReadOptions<Read001> readOptions = ExcelReadOptions.<Read001>builder().headClass(Read001.class).isReadAll(true).build();
        ReadListener<Read001> readListener = new BatchResolveReadListener<>(10, dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("当前要批量处理的数据为:\n %s", dataList));
        });
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);

```

> 读取多个表 - 多次读取

```
        File inputFile = new File("src\\test\\resources\\files\\read-001-more-sheet.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        //读取表一的数据
        ExcelReadOptions<Read001> readOptions = ExcelReadOptions.<Read001>builder().headClass(Read001.class).build();
        ReadListener<Read001> readListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("(表一)当前要批量处理的数据为:\n %s", dataList));
        });
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);

        System.out.println("-------——分隔符-------——");

        //读取表二的数据
        inputStream = new FileInputStream(inputFile);
        readOptions = ExcelReadOptions.<Read001>builder().headClass(Read001.class).sheetNo(1).build();
        readListener = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("(表二)当前要批量处理的数据为:\n %s", dataList));
        });
        ExcelReadSupportUtils.read(inputStream, readListener, readOptions);
    
```

> 读取多个表 - 一次读取

```
        File inputFile = new File("src\\test\\resources\\files\\read-001-more-sheet-repeated.xlsx");
        InputStream inputStream = new FileInputStream(inputFile);

        ExcelRepeatedReadOptions readOptions = ExcelRepeatedReadOptions.builder().build();
        List<ExcelReadSheetOptions<?>> readSheetOptionsList = new ArrayList<>(8);

        //读取表一的数据
        ReadListener<Read001> readListener1 = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("(表一)当前要批量处理的数据为:\n %s", dataList));
        });
        readSheetOptionsList.add(ExcelReadSheetOptions.<Read001>builder().sheetNo(0).headClass(Read001.class).readListener(readListener1).build());

        //读取表二的数据
        ReadListener<Read001ByIndex> readListener2 = new BatchResolveReadListener<>(dataList -> {
            //进行处理该批数据,e.g: 持久化到数据库 (不能保证整批数据全是合法的,如有特殊需求需要额外处理)
            System.out.println(String.format("(表二)当前要批量处理的数据为:\n %s", dataList));
        });
        readSheetOptionsList.add(ExcelReadSheetOptions.<Read001ByIndex>builder().sheetNo(1).headRowNumber(2).headClass(Read001ByIndex.class).readListener(readListener2).build());

        ExcelReadSupportUtils.repeatedRead(inputStream, readOptions, readSheetOptionsList);
    
```