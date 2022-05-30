package gbench.util.data;

import java.io.Reader;
import java.io.Serializable;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import static gbench.util.data.DataApp.IRecord.REC;

/**
 * 数据应用
 * 
 * @author xuqinghua
 *
 */
public class DataApp {

    /**
     * 
     */
    public DataApp() {
        // do nothing
    }

    /**
     * 
     * @param dataSource
     */
    public DataApp(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取数据库连接
     * 
     * @return 数据库连接
     */
    public Connection getConnection() {

        Connection conn = null;

        try {
            conn = this.dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    };

    /**
     * 
     * @param sql
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<IRecord> sql2maybe(final String sql) {
        return (Optional<IRecord>) this.withTransaction(sess -> {
            final Optional<IRecord> opt = sess.sql2recordS(sql).findFirst();
            sess.setData(opt);
        });
    }

    /**
     * 
     * @param sql
     * @return
     */
    public DFrame sql2dframe(final String sql) {
        return (DFrame) this.withTransaction(sess -> {
            final DFrame dfm = sess.sql2x(sql);
            sess.setData(dfm); // 设置返回值
        });
    }

    /**
     * 表是否存在
     * 
     * @param tbl 数据表名
     * @return true 存在,false 不存在。
     */
    public Boolean tblExists(final String tbl) {
        return (Boolean) this.withTransaction(sess -> {
            final Boolean b = sess.isTablePresent(tbl);
            sess.setData(b); // 设置返回值
        });
    }

    /**
     * 
     * @param action
     * @return
     */
    public Object withTransaction(final ExceptionalConsumer<IJdbcSession<IRecord, Object, DFrame>> action) {
        final Connection conn = DataApp.this.getConnection();

        final IJdbcSession<IRecord, Object, DFrame> session = new AbstractJdbcSession<Object, DFrame>() { // 创建会话session
            @Override
            public Connection getConnection() {
                return conn;
            }

            @Override
            public Collector<IRecord, ?, DFrame> collectorX() {
                return DFrame.dfmclc;
            }
        };

        try {
            action.accept(session);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            session.clear(); // 清空所有流
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    // System.err.println("连接关闭");
                } // if
            } catch (SQLException e) {
                e.printStackTrace();
            } // try
        } // try

        return session.getData();
    }

    /**
     * 
     * @author xuqinghua
     *
     */
    public interface IRecord {

        /**
         * 根据键名进行取值
         * 
         * @param key 键名
         * @return 键key的值
         */
        Object get(final String key);

        /**
         * 设置键，若 key 与 老的 键 相同则 覆盖 老的值
         * 
         * @param key   新的 键名
         * @param value 键值
         * @return 对象本身
         */
        IRecord set(String key, Object value);

        /**
         * 除掉键 key 的值
         * 
         * @param key 新的 键名
         * @return 对象本身(移除了key)
         */
        IRecord remove(String key);

        /**
         * 数据复制
         * 
         * @return 当前对象的拷贝
         */
        IRecord duplicate();

        /**
         * 构建一个键名键值序列 指定的 IRecord
         * 
         * @param kvs Map结构（IRecord也是Map结构） 或是 键名,键值 序列。即 build(map) 或是
         *            build(key0,value0,key1,vlaue1,...) 的 形式， 特别注意 build(map) 时候，当且仅当
         *            kvs 的只有一个元素，即 build(map0,map1) 会被视为 键值序列
         * @return 新生成的IRecord
         */
        IRecord build(final Object... kvs);

        /**
         * 键名序列
         * 
         * @return 键名列表
         */
        List<String> keys();

        /**
         * 返回一个 Map 结构<br>
         * 非递归进行变换
         * 
         * @return 一个 键值 对儿 的 列表 [(key,map)]
         */
        Map<String, Object> toMap();

        /**
         * 构造 IRecord <br>
         * 按照构建器的 键名序列表，依次把objs中的元素与其适配以生成 IRecord <br>
         * {key0:objs[0],key1:objs[1],key2:objs[2],...}
         * 
         * @param <T>  元素类型
         * @param objs 值序列, 若 objs 为 null 则返回null, <br>
         *             若 objs 长度不足以匹配 keys 将采用 循环补位的仿制给予填充 <br>
         *             若 objs 长度为0则返回一个空对象{},注意是没有元素且不是null的对象
         * @return IRecord 对象 若 objs 为 null 则返回null
         */
        default public IRecord get(final Object... objs) {

            if (objs == null) { // 空值判断
                return null;
            }

            final int n = objs.length;
            final List<String> keys = this.keys();
            final int size = this.keys().size();
            final LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

            for (int i = 0; n > 0 && i < size; i++) {
                final String key = keys.get(i);
                final Object value = objs[i % n];
                data.put(key, value);
            } // for

            return this.build(data);
        }

        /**
         * 设置键，若 key 与 老的 键 相同则 覆盖 老的值
         * 
         * @param key   新的 键名
         * @param value 键值
         * @return 对象本身
         */
        default IRecord add(String key, Object value) {

            this.set(key, value);
            return this;
        }

        /**
         * 返回 key 所对应的 键值, String 类型
         * 
         * @param key 键名
         * @return key 所标定的 值
         */
        default String str(final String key) {

            final Object obj = this.get(key);

            if (obj == null) {
                return null;
            }

            return obj instanceof String ? (String) obj : obj + "";
        }

        /**
         * 返回 key 所对应的 键值, String 类型
         * 
         * @param key 键名
         * @return key 所标定的 值
         */
        default Double dbl(final String key) {

            final Object obj = this.get(key);

            return IRecord.obj2dbl(null).apply(obj);
        }

        /**
         * 返回 key 所对应的 键值, String 类型
         * 
         * @param key 键名
         * @return key 所标定的 值
         */
        default Integer i4(final String key) {

            final Object obj = this.get(key);

            return Optional.ofNullable(IRecord.obj2dbl(null).apply(obj)).map(Number::intValue).orElse(null);
        }

        /**
         * 生成 构建器
         * 
         * @param n     键数量,正整数
         * @param keyer 键名生成器 (i:从0开始)->key
         * @return IRecord 构造器
         */
        public static Builder rb(final int n, Function<Integer, ?> keyer) {

            final List<Object> keys = new ArrayList<Object>();
            for (int i = 0; i < n; i++) {
                keys.add(keyer.apply(i));
            }
            return new Builder(keys);
        }

        /**
         * 生成 构建器
         *
         * @param K    元素类型
         * @param keys 键名序列
         * @return keys 为格式的 构建器
         */
        @SafeVarargs
        public static <T> Builder rb(final T... keys) {

            final List<String> _keys = Arrays.asList(keys).stream().map(e -> e + "").collect(Collectors.toList());
            return new Builder(_keys);
        }

        /**
         * 生成 构建器
         * 
         * @param keys 键名序列, 用半角逗号 “,” 分隔
         * @return keys 为格式的 构建器
         */
        public static Builder rb(final String keys) {

            return rb(keys.split(","));
        }

        /**
         * 生成 构建器
         * 
         * @param keys 键名序列
         * @return keys 为格式的 构建器
         */
        public static Builder rb(final String[] keys) {

            return rb(Arrays.asList(keys));
        }

        /**
         * 生成 构建器
         * 
         * @param keys 键名序列
         * @return keys 为格式的 构建器
         */
        public static <T> Builder rb(final Iterable<T> keys) {

            return new Builder(keys);
        }

        /**
         * fill_template 的别名<br>
         * Term.FT("$0+$1=$2",1,2,3) 替换后的结果为 1+2=3
         * 
         * @param template 模版字符串，占位符$0,$1,$2,...
         * @param tt       模版参数序列
         * @return template 被模版参数替换后的字符串
         */
        public static String FT(final String template, final Object... tt) {

            final IRecord rec = IRecord.rb(tt.length, i -> "$" + i).get(tt);
            return fill_template(template, rec);
        }

        /**
         * fill_template 的别名<br>
         * 把template中的占位符用rec中对应名称key的值value给予替换,默认 非null值会用 单引号'括起来，<br>
         * 对于使用$开头/结尾的占位符,比如$name或name$等不采用单引号括起来。<br>
         * 还有对于 数值类型的值value不论占位符/key是否以$开头或结尾都不会用单引号括起来 。<br>
         * 例如 : FT("insert into tblname$ (name,sex) values
         * (#name,#sex)",REC("tblname$","user","#name","张三","#sex","男")) <br>
         * 返回: insert into user (name,sex) values ('张三','男') <br>
         * 
         * @param template           模版字符串
         * @param placeholder2values 关键词列表:占位符/key以及与之对应的值value集合
         * @return 把template中的占位符/key用placeholder2values中的值value给予替换
         */
        public static String FT(final String template, final IRecord placeholder2values) {

            return fill_template(template, placeholder2values);
        }

        /**
         * 把template中的占位符用rec中对应名称key的值value给予替换,默认 非null值会用 单引号'括起来，<br>
         * 对于使用$开头/结尾的占位符,比如$name或name$等不采用单引号括起来,<br>
         * 还有对于 数值类型的值value不论占位符/key是否以$开头或结尾都不会用单引号括起来 。<br>
         * 例如 : fill_template("insert into tblname$ (name,sex) values
         * (#name,#sex)",REC("tblname$","user","#name","张三","#sex","男")) <br>
         * 返回: insert into user (name,sex) values ('张三','男') <br>
         * 
         * @param template           模版字符串
         * @param placeholder2values 关键词列表:占位符/key以及与之对应的值value集合
         * @return 把template中的占位符/key用placeholder2values中的值value给予替换
         */
        public static String fill_template(final String template, final IRecord placeholder2values) {

            if (placeholder2values == null) { // 空值判断
                return template;
            }

            final int len = template.length(); // 模版的长度
            final StringBuilder buffer = new StringBuilder();// 工作缓存
            final List<String> keys = placeholder2values.keys().stream().sorted((a, b) -> -(a.length() - b.length())) // 按照从长到短的顺序进行排序。以保证keys的匹配算法采用的是贪婪算法。
                    .collect(Collectors.toList());// 键名 也就是template中的占位符号。
            final Map<Object, List<String>> firstCharMap = keys.stream()
                    .collect(Collectors.groupingBy(key -> key.charAt(0)));// keys的首字符集合，这是为了加快
            // 读取的步进速度
            int i = 0;// 当前读取的模版字符位置,从0开始。

            while (i < len) {// 从前向后[0,len)的依次读取模板字符串的各个字符
                // 注意:substring(0,0) 或是 substring(x,x) 返回的是一个长度为0的字符串 "",
                // 特别是,当 x大于等于字符串length会抛异常:StringIndexOutOfBoundsException
                final String line = template.substring(0, i);// 业已读过的模版字符串内容[0,i),当前所在位置为i等于line.length
                String placeholder = null;// 占位符号 的内容
                final List<String> kk = firstCharMap.get(template.charAt(i));// 以 i为首字符开头的keys
                if (kk != null) {// 使用firstCharMap加速步进速度读取模版字符串
                    for (final String key : kk) {// 寻找 可以被替换的key
                        final int endIndex = line.length() + key.length(); // 终点索引 exclusive
                        final boolean b = endIndex > len // 是否匹配到placeholder
                                ? false // 拼装的组合串(line+key) 超长（超过模板串的长度)
                                : template.substring(line.length(), endIndex).equals(key); // 当前位置之后刚好存在一个key模样的字符串
                        if (b) { // 定位到一个完整的占位符：长度合适（endIndex<=len) && 内容匹配 equals
                            placeholder = key; // 提取key作为占位符
                            break; // 确定了占位符 ,跳出本次i位置的kk循环。
                        } // if b 定位到一个完整的占位符
                    } // for key:kk
                } // if kk!=null

                if (placeholder != null) {// 已经发现了占位符号，用rec中的值给予替换
                    boolean isnumberic = false;// 默认不是数字格式
                    final Object value = placeholder2values.get(placeholder); // 提取占位符内容
                    if (placeholder.startsWith("$") || placeholder.endsWith("$"))
                        isnumberic = true; // value 是否是 强制 使用 数字格式，即用$作为前/后缀。
                    if (value instanceof Number)
                        isnumberic = true; // 如果值是数字类型就一定会不加引号的。即采用数字格式

                    buffer.append((isnumberic || value == null) ? value + "" : "'" + value + "'"); // 为字符串添加引号，数字格式或是null不加引号
                    i += placeholder.length();// 步进placeholder的长度，跳着前进
                } else {// 发现了占位符号则附加当前的位置的字符
                    buffer.append(template.charAt(i));
                    i++;// 后移一步
                } // if placeholder
            } // while

            return buffer.toString(); // 返回替换后的结果值
        }

        /**
         * 构建一个键名键值序列 指定的 IRecord
         * 
         * @param kvs Map结构（IRecord也是Map结构） 或是 键名,键值 序列。即 build(map) 或是
         *            build(key0,value0,key1,vlaue1,...) 的 形式， 特别注意 build(map) 时候，当且仅当
         *            kvs 的只有一个元素，即 build(map0,map1) 会被视为 键值序列
         * @return 新生成的IRecord
         */
        public static IRecord REC(final Object... kvs) {

            return MyRecord.REC(kvs); // 采用 MyRecord 来作为IRecord的默认实现函数
        }

        /**
         * 把一个数据对象转换为浮点数<br>
         * 对于 非法的数字类型 返回 defaultValue
         * 
         * 默认会尝试把时间类型也解释为数字,即 '1970-01-01 08:00:01' <br>
         * 也会被转换成一个 0时区 的 从1970年1月1 即 epoch time 以来的毫秒数<br>
         * 对于 中国 而言 位于+8时区, '1970-01-01 08:00:01' 会被解析为1000
         * 
         * @param <T>          函数的参数类型
         * @param defaultValue 非法的数字类型 返回 的默认值
         * @return t->dbl
         */
        static <T> Function<T, Double> obj2dbl(final Number defaultValue) {

            return (T obj) -> {
                if (obj instanceof Number) {
                    return ((Number) obj).doubleValue();
                }

                Double dbl = Optional.ofNullable(defaultValue).map(Number::doubleValue).orElse(null);
                try {
                    dbl = Double.parseDouble(obj.toString());
                } catch (Exception e) { //
                    // do nothing
                } // try

                return dbl;
            };
        }

    }

    /**
     * 托管的流
     * 
     * @author gbench
     *
     */
    interface IManagedStreams {

        /**
         * 获取托管数据流的集合,返回 活动流的 引用。
         * 
         * @return 托管数据流的集合
         */
        Set<Stream<?>> getActiveStreams();

        /**
         * 把所有的活动的流都给予清空
         */
        default void clear() {

            this.getActiveStreams().forEach(Stream::close); // 关闭活动流
            this.getActiveStreams().clear();// 活动流的库存清空
        }

        /**
         * 把所有的活动的流都给予清空 清空指定的 流对象
         * 
         * @param stream 指定的流对象
         */
        default void clear(final Stream<?> stream) {

            if (this.getActiveStreams().contains(stream)) {
                this.getActiveStreams().remove(stream);
            }
            stream.close();
        }

        /**
         * 托管一个流对象
         * 
         * @param stream 托管的流对象
         * @return IManagedStreams 本身 便于 实现链式编程
         */
        default void add(final Stream<?> stream) {

            this.getActiveStreams().add(stream);
        }

        /**
         * 把所有的活动的流都给予清空
         */
        default void dump() {

            this.getActiveStreams().forEach(e -> {
                System.out.println(e);
            });
        }
    }

    public interface IJdbcSession<T, D, X> extends IManagedStreams {

        Connection getConnection();

        Collector<IRecord, ?, X> collectorX();

        D getData();

        D setData(final D data);

        /**
         * 判断数据库表是否存在
         * 
         * @param tableName 表名(表名区分大小写)
         * 
         * @return 表是否存在
         */
        default boolean isTablePresent(final String tableName) {

            return this.isTablePresent(tableName, null, null);
        }

        /**
         * 判断数据库表是否存在
         * 
         * @param tableName 表名(表名区分大小写)
         * @param schema    表模式（表分组）
         * @param catalog   数据库
         * 
         * @return 表是否存在
         */
        default boolean isTablePresent(final String tableName, final String schema, final String catalog) {

            final Connection conn = this.getConnection();
            ResultSet tables = null;
            boolean flag = false;

            try {
                final DatabaseMetaData databaseMetaData = conn.getMetaData();
                final String[] JDBC_METADATA_TABLE_TYPES = { "TABLE" };
                tables = databaseMetaData.getTables(catalog, schema, tableName, JDBC_METADATA_TABLE_TYPES);
                flag = tables.next();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    tables.close();
                } catch (Exception e) {
                    System.err.println("Error closing meta data tables:" + e);
                    e.printStackTrace();
                }
            }

            return flag;
        }

        default X sql2x(final String sql) throws SQLException {

            final X x = this.sql2u(sql, null, this.collectorX());
            return x;
        }

        default <U> U sql2u(final String sqlpattern, final IRecord params, final Collector<IRecord, ?, U> collector)
                throws SQLException {

            final U result = this.sql2recordS(sqlpattern).collect(collector);
            return result;
        }

        default <U> U sql2u(final String sql, final Collector<IRecord, ?, U> collector) throws SQLException {

            return this.sql2u(sql, REC(), collector);
        }

        /**
         * 执行sql语句查询出结果集合。<br>
         * 一般不会尝试调用spp 进行sql解析,但是 当sql为"#开头的namedsql的时候，会调用spp给予解析") <br>
         * sql2records 的核心函数 <br>
         * 
         * <br>
         * 对于短路的流，注意调用 stream.close() 来释放数据库连接, 或者 是 调用 sess.clear 来给与清空。<br>
         *
         * @param sql sql 语句
         * @return IRecord类型的流
         * @throws SQLException
         */
        default Stream<IRecord> sql2recordS(final String sql) throws SQLException {

            final String _sql = sql.trim(); // 去除多余的首尾空格
            return this.psql2recordS(_sql, (Map<Integer, Object>) null);
        }

        /**
         * 
         * @param sql sql 语句
         * @return IRecord 的 数据流
         * @throws SQLException
         */
        default Stream<IRecord> sql2updateS(final String sql) throws SQLException {
            return this.psql2updateS(sql, null);
        }

        /**
         * sql2updateS 的列表形式
         * 
         * @param sql sql 语句
         * @return IRecord 的 数据列表
         * @throws SQLException
         */
        default List<IRecord> sql2execute(final String sql) throws SQLException {
            return this.sql2updateS(sql).collect(Collectors.toList());
        }

        /**
         * 查询结果集合
         *
         * @param sql    prepared sql 语句
         * @param params prepared sql 语句中占位符参数的值集合，位置从1开始
         * @return 查询结果集合
         * @throws SQLException
         */
        default Stream<IRecord> psql2recordS(String sql, Map<Integer, ?> params) throws SQLException {

            final Stream<IRecord> stream = IJdbcSession.psql2recordS(getConnection(), sql, params, SQL_MODE.QUERY,
                    false);
            this.add(stream);
            return stream;
        }

        /**
         * 更新数据:execute 与update 同属于与 update
         *
         * @param sql    prepared sql 语句
         * @param params 占位符的对应值的Map,位置从1开始
         * @return 更新结果集合函数有 generatedKeys 键值
         * @throws SQLException
         */
        default Stream<IRecord> psql2updateS(String sql, Map<Integer, Object> params) throws SQLException {

            return IJdbcSession.psql2recordS(this.getConnection(), sql, params, SQL_MODE.UPDATE, false);
        }

        /**
         * 生成sql语句．
         *
         * @param conn   数据库连接
         * @param mode   语句的类型 UPDATE ,QUERY_SCROLL
         * @param sql    语句：含有占位符
         * @param params sql 参数: {Key:Integer-Value:Object},Key 从1开始。 params
         *               为null时候不予进行sql语句填充
         * @return PreparedStatement
         * @throws SQLException
         */
        public static PreparedStatement pstmt(final Connection conn, final SQL_MODE mode, final String sql,
                final Map<Integer, ?> params) throws SQLException {

            PreparedStatement ps = null;
            if (mode == SQL_MODE.UPDATE) {
                ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);// 生成数据主键
            } else if (mode == SQL_MODE.QUERY_SCROLL) {
                ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            } else {
                ps = conn.prepareStatement(sql);
            }

            final ParameterMetaData pm = ps.getParameterMetaData();// 参数元数据
            try {
                final int pcnt = pm.getParameterCount(); // paramcnt;
                if (pcnt > 0 && params != null && params.size() > 0) {
                    for (final Integer paramIndex : params.keySet()) {
                        if (pcnt < paramIndex) {
                            continue;// 对于超出sql的参数位置范围的 参数给予舍弃
                        }

                        final Object value = params.get(paramIndex);
                        ps.setObject(paramIndex, value);// 设置参数
                    } // for
                } // if
            } catch (UnsupportedOperationException e) {
                if (params != null && params.size() > 0) {
                    for (final Integer paramIndex : params.keySet()) {
                        final Object value = params.get(paramIndex);
                        ps.setObject(paramIndex, value);// 设置参数
                    } // for
                } // if
            } // try

            return ps;
        }// pstmt

        /**
         * 提取列标签:数组类型的返回结果
         *
         * @param rs 结果集合
         * @return 标签数组
         * @throws SQLException
         */
        public static String[] labels(final ResultSet rs) throws SQLException {

            String[] aa = null;
            if (rs == null) {
                return null;
            }

            final ResultSetMetaData rsm = rs.getMetaData();
            final int n = rsm.getColumnCount();

            aa = new String[n];

            for (int i = 1; i <= n; i++) {
                aa[i - 1] = rsm.getColumnLabel(i);
            }

            return aa;
        }

        /**
         * 从ResultSet 当前游标位置 中读取一条数据。<br>
         * 不对结果集合ResultSet做任何改变,游标位置需要事先设置好<br>
         * 即不会移动ResultSet的cursor。所以第一条数据的时候需要在外部进行rs.next()<br>
         * Record 的key 采用rs.getMetaData().getColumnLabel(索引）来获取。<br>
         * 
         * @param rs   结果集合
         * @param lbls 键名集合
         * @return 结果集数据的record的表示,出现异常则返回null
         */
        public static IRecord readline(final ResultSet rs, final String[] lbls) {

            IRecord rec = null;// 默认的非法结果

            try {
                rec = REC();
                final int n = lbls.length;
                for (int i = 0; i < n; i++) {// 读取当前行的各个字段信息。
                    final String name = lbls[i]; // 提取键名
                    final Object value = rs.getObject(i + 1); // 提取键值
                    final Object _value = Optional.ofNullable(value)
                            .map(v -> v instanceof Clob ? (Object) clob2str((Clob) v) : null).orElse(value);
                    rec.add(name, _value);
                } // for
            } catch (Exception e) {
                e.printStackTrace();
            } // try

            return rec;
        }

        /**
         * Clob 转 字符串(Long.MAX_VALUE字节)
         * 
         * @param clog 字符类大对象
         * @return 字符串
         */
        public static String clob2str(final Clob clog) {

            return clob2str(clog, null);
        }

        /**
         * Clob 转 字符串
         * 
         * @param clog 字符类大对象
         * @param size 最大字节数量
         * @return 字符串
         */
        public static String clob2str(final Clob clog, final Long size) {

            if (null == clog) {
                return null;
            } else {
                String line = null;
                try {
                    final Reader reader = clog.getCharacterStream();
                    final long n = (Math.min(Optional.ofNullable(size).orElse(Long.MAX_VALUE), clog.length()));
                    char[] buffer = new char[(int) n];
                    reader.read(buffer);
                    line = new String(buffer);
                } catch (Exception e) {
                    e.printStackTrace();
                } // try
                return line;
            } // if
        }

        /**
         * 数据迭代输出
         * 
         * @param <T>
         * @param seed    数据种子
         * @param hasNext 是否含所有下一个元素
         * @param next    求取下一个元素
         * @return T 类型的元素流
         */
        public static <T> Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {

            Objects.requireNonNull(next);
            Objects.requireNonNull(hasNext);
            final Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE,
                    Spliterator.ORDERED | Spliterator.IMMUTABLE) {
                T prev;
                boolean started, finished;

                @Override
                public boolean tryAdvance(final Consumer<? super T> action) {

                    Objects.requireNonNull(action);
                    if (finished)
                        return false;
                    T t;
                    if (started)
                        t = next.apply(prev);
                    else {
                        t = seed;
                        started = true;
                    }
                    if (!hasNext.test(t)) {
                        prev = null;
                        finished = true;
                        return false;
                    }
                    action.accept(prev = t);

                    return true;
                }

                @Override
                public void forEachRemaining(final Consumer<? super T> action) {

                    Objects.requireNonNull(action);
                    if (finished)
                        return;
                    finished = true;
                    T t = started ? next.apply(prev) : seed;
                    prev = null;
                    while (hasNext.test(t)) {
                        action.accept(t);
                        t = next.apply(t);
                    }
                }
            };

            return StreamSupport.stream(spliterator, false);
        }

        /**
         * 
         * @param <T>
         * @param exceptioncs
         * @return
         */
        public static <T> Consumer<T> trycatch(final ExceptionalConsumer<T> exceptioncs) {

            return t -> {
                try {
                    exceptioncs.accept(t);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            };
        }

        /**
         * 
         * @param <T>
         * @param <U>
         * @param exceptionFunction
         * @return
         */
        public static <T, U> Function<T, U> trycatch(final ExceptionalFunction<T, U> exceptionFunction) {

            return t -> {
                try {
                    return exceptionFunction.apply(t);
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (Throwable e) {
                    e.printStackTrace();
                } // try// try

                return null;
            };// 返回一个Function
        }

        /**
         * 从当前游标开始 (不包含)依次向后读取数据 <br>
         * <br>
         * Record 的key 采用rs.getMetaData().getColumnLabel(索引）来获取。<br>
         * 
         * @param rs       结果集合
         * @param callback 执行结束的回调函数，比如 关闭 数据集、语句、连接 之类的 收尾操作。
         * @return 结果集数据的record的流 [rec]
         * @throws SQLException
         */
        public static Stream<IRecord> readlineS(final ResultSet rs, final Runnable callback) throws SQLException {

            final String[] lbls = IJdbcSession.labels(rs);
            final AtomicBoolean stopflag = new AtomicBoolean(false); // 是否达到末端
            final Stream<IRecord> stream = !rs.next() // 检查是否存在有后继
                    ? Stream.of() // 空列表
                    : IJdbcSession.iterate( // 生成流对象
                            readline(rs, lbls), // 初始值
                            previous -> !stopflag.get(), // 是否到达末端
                            previous -> { // next
                                return IJdbcSession.trycatch((ResultSet r) -> { // 读取 resultset
                                    if (r.next()) { // 先移动然后读取
                                        return readline(r, lbls); // 读取结果集
                                    } else { // 已经读取到了最后一条数据,返回null
                                        callback.run(); // 执行回调函数
                                        stopflag.set(true); // 设置结束标志
                                        return null; // 返回空值
                                    } // if
                                }).apply(rs); // trycatch
                            }); // iterate

            return stream;
        }

        /**
         * 查询结果集合 <br>
         * 
         * 对于短路的流，注意调用 stream.close() 来释放数据库连接, 或者 是 调用 sess.clear 来给与清空。<br>
         * 
         * @param connection 数据库连接
         * @param sql        查询或更新语句
         * @param params     sql的占位参数, params 为空的时候 不予 进行 sql 语句填充
         * @param sqlmode    sql的模式，更新还是查询
         * @param close_conn 结束时候是否关闭 connection 数据连接
         * @return IRecord 的 数据流 [rec]
         * @throws SQLException
         */
        static public Stream<IRecord> psql2recordS(final Connection connection, final String sql,
                final Map<Integer, ?> params, final SQL_MODE sqlmode, final boolean close_conn) throws SQLException {

            final PreparedStatement pstmt = IJdbcSession.pstmt(connection, sqlmode, sql, params); // 创建查询语句

            ResultSet _rs = null;

            if (sqlmode.equals(SQL_MODE.UPDATE)) { // 更新模式
                pstmt.executeUpdate(); // 批处理的执行
                _rs = pstmt.getGeneratedKeys();
                if (_rs == null) {
                    _rs = pstmt.getResultSet();
                } // if
            } else { // 非更新模式
                _rs = pstmt.executeQuery();
            } // if

            final ResultSet rs = _rs;
            final Runnable closeHandler = () -> { // 关闭操作的回调函数
                try {
                    if (!rs.isClosed()) { // 关闭结果集
                        rs.close();
                    } // !rs.isClosed()

                    if (!pstmt.isClosed()) { // 关闭语句集合
                        pstmt.close();
                    } // !pstmt.isClosed())

                    if (close_conn && !connection.isClosed()) { // 关闭数据库连接
                        connection.close();
                    } // close_conn && !connection.isClosed()
                } catch (Exception e) {
                    e.printStackTrace();
                } // try
            }; // 关闭操作的回调函数

            return rs == null // 结果集检查
                    ? Stream.empty() // 空结果
                    : IJdbcSession // 连接会话
                            .readlineS(rs, closeHandler) // 读取数据行
                            .onClose(closeHandler); // 加入关闭处理子
        }
    }

    /**
     * 可以抛出异常的消费函数
     * 
     * @author gbench
     *
     * @param <T> 源数据类型
     */
    @FunctionalInterface
    public interface ExceptionalConsumer<T> {
        /**
         * 数据消费函数
         * 
         * @param t 函数参数
         * @return U类型的函数
         * @throws Exception 异常
         * @throws Throwable 抛出物
         */
        void accept(T t) throws Exception, Throwable;
    };

    /**
     * 带有抛出异常的函数
     *
     * @param <T> 参数类型
     * @param <U> 返回类型
     * @author xuqinghua
     */
    public interface ExceptionalFunction<T, U> {
        U apply(T t) throws Exception;
    }

    /**
     * IRecord 构建器
     * 
     * @author gbench
     *
     */
    public static class Builder {

        /**
         * 构造IRecord构造器
         * 
         * @param keys 键名列表的迭代器
         */
        public <T> Builder(final Iterable<T> keys) {
            this.keys = StreamSupport.stream(keys.spliterator(), false).limit(10000).map(e -> e + "")
                    .collect(Collectors.toList());
        }

        /**
         * 系统构建
         * 
         * @param kvs
         * @return
         */
        public IRecord build(final Object... kvs) {
            return MyRecord.REC(kvs);
        }

        /**
         * 构造 IRecord <br>
         * 按照构建器的 键名序列表，依次把objs中的元素与其适配以生成 IRecord <br>
         * {key0:objs[0],key1:objs[1],key2:objs[2],...}
         * 
         * @param <T>  元素类型
         * @param objs 值序列, 若 objs 为 null 则返回null, <br>
         *             若 objs 长度不足以匹配 keys 将采用 循环补位的仿制给予填充 <br>
         *             若 objs 长度为0则返回一个空对象{},注意是没有元素且不是null的对象
         * @return IRecord 对象 若 objs 为 null 则返回null
         */
        public IRecord get(final Object... objs) {
            if (objs == null) { // 空值判断
                return null;
            }

            final int n = objs.length;
            final int size = this.keys.size();
            final LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

            for (int i = 0; n > 0 && i < size; i++) {
                final String key = keys.get(i);
                final Object value = objs[i % n];
                data.put(key, value);
            } // for

            return this.build(data);
        }

        private List<String> keys = new ArrayList<>();
    }

    /**
     * 数据记录对象的实现
     * 
     * @author gbench
     *
     */
    public static class MyRecord extends LinkedHashMap<String, Object> implements IRecord, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * 构造数据记录对象
         * 
         * @param data IRecord数据
         */
        public MyRecord(final Map<?, ?> data) {

            data.forEach((k, v) -> {
                this.put(k instanceof String ? (String) k : k + "", v);
            }); // forEach
        }

        /**
         * 构造空数据记录
         * 
         */
        public MyRecord() {

            this(new LinkedHashMap<String, Object>());
        }

        @Override
        public IRecord set(final String key, final Object value) {

            this.put(key, value);
            return this;
        }

        @Override
        public IRecord remove(String key) {

            this.remove(key);
            return this;
        }

        @Override
        public IRecord build(final Object... kvs) {

            return MyRecord.REC(kvs);
        }

        @Override
        public Map<String, Object> toMap() {

            return this;
        }

        @Override
        public List<String> keys() {

            return new ArrayList<>(this.keySet());
        }

        /**
         * 提取指定key的数据
         */
        @Override
        public Object get(final String key) {

            return super.get(key);
        }

        @Override
        public IRecord duplicate() {

            final MyRecord _rec = new MyRecord();
            _rec.putAll(this);
            return _rec;
        }

        @Override
        public int hashCode() {

            return Objects.hash(this.toMap());
        }

        /**
         * Iterable 转 List
         * 
         * @param <T>      元素类型
         * @param iterable 可迭代类型
         * @param maxSize  最大元素长度
         * @return 元素类型
         */
        public static <T> List<T> iterable2list(final Iterable<T> iterable, final long maxSize) {

            final ArrayList<T> aa = new ArrayList<>();
            StreamSupport.stream(iterable.spliterator(), false).limit(maxSize).forEach(aa::add);
            return aa;
        }

        /**
         * 标准版的记录生成器, map 生成的是LinkedRecord
         * 
         * @param <T> 类型占位符
         * @param kvs 键,值序列:key0,value0,key1,value1,.... <br>
         *            Map结构（IRecord也是Map结构） 或是 键名,键值 序列。即 build(map) 或是
         *            build(key0,value0,key1,vlaue1,...) 的 形式， 特别注意 build(map) 时候，当且仅当
         *            kvs 的只有一个元素，即 build(map0,map1) 会被视为 键值序列
         * @return IRecord对象
         */
        @SafeVarargs
        public static <T> IRecord REC(final T... kvs) {

            final int n = kvs.length;
            final LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

            if (n == 1) { // 单一参数情况
                final T obj = kvs[0];
                if (obj instanceof Map) { // Map情况的数据处理
                    ((Map<?, ?>) obj).forEach((k, v) -> { // 键值的处理
                        data.put(k + "", v);
                    }); // forEach
                } else if (obj instanceof IRecord) {// IRecord 对象类型 复制对象数据
                    data.putAll(((IRecord) obj).toMap());
                } // if
            } else { // 键名减值序列
                for (int i = 0; i < n - 1; i += 2) {
                    data.put(kvs[i].toString(), kvs[i + 1]);
                }
            } // if

            return new MyRecord(data);
        }
    }

    /**
     * 
     * @author Administrator
     *
     */
    public static abstract class AbstractJdbcSession<T, X> implements IJdbcSession<IRecord, T, X> {

        @Override
        public Set<Stream<?>> getActiveStreams() {

            return activeStreams;
        }

        /**
         * 会话中的数据内容
         *
         * @return session中的数据类型。
         */
        public T getData() {

            return this.data;
        }

        /**
         * 会话中的数据内容
         *
         * @return session中的数据类型。
         */
        public T setData(final T data) {

            return this.data = data;
        }

        @Override
        public abstract Connection getConnection();

        private T data;
        private Set<Stream<?>> activeStreams = new LinkedHashSet<>();
    }

    /**
     * 
     * @author xuqinghua
     *
     */
    public static class DFrame extends LinkedList<IRecord> {

        /**
         * 构造函数
         * 
         * @param data 源数据
         */
        public DFrame(final List<IRecord> data) {

            this.addAll(data);
        }

        /**
         * 行数据流
         * 
         * @return 行数据流
         */
        public Stream<IRecord> rowS() {

            return this.stream();
        }

        /**
         * 数据内容格式化
         */
        public String toString() {

            if (this.size() > 0) {
                final StringBuffer buffer = new StringBuffer();
                final IRecord first = this.get(0);
                final List<String> keys = first.keys();
                final String header = keys.stream().collect(Collectors.joining("\t"));
                final String body = this.rowS().map(e -> keys.stream().map(e::str).collect(Collectors.joining("\t")))
                        .collect(Collectors.joining("\n"));
                buffer.append(header + "\n");
                buffer.append(body);
                return buffer.toString();
            } else {
                return "DFrame(empty)";
            }
        }

        /**
         * 序列号
         */
        private static final long serialVersionUID = 3677521717607148260L;

        /**
         * IRecord 类型的归集器 [rec:行]->dfm,行法归集
         */
        public static Collector<IRecord, ?, DFrame> dfmclc = Collector.of((Supplier<List<IRecord>>) ArrayList::new,
                List::add, (left, right) -> {
                    left.addAll(right);
                    return left;
                }, e -> {
                    return new DFrame(e);
                });
    }

    /**
     * 请求模式
     */
    public enum SQL_MODE {
        QUERY, // 查询模式
        UPDATE, // 更新模式
        QUERY_SCROLL // d带有滚动的查询模式
    }

    /**
     * 文本格式化输出
     * 
     * @param objects 对象序列
     * @return 格式化输出的文本
     */
    public static String println(final Object... objects) {

        final String line = Arrays.stream(objects).map(e -> "" + e).collect(Collectors.joining("\n"));
        System.out.println(line);
        return line;
    }

    private DataSource dataSource; // 注入系统的数据源
} // JdbcApp
