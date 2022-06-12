package gbench.util.lisp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import gbench.util.json.MyJson;

/**
 * 数据记录对象的实现
 * 
 * @author gbench
 *
 */
public class MyRecord implements IRecord, Serializable {

    /**
     * 序列号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 构造数据记录对象
     * 
     * @param data IRecord数据
     */
    public MyRecord(final Map<?, ?> data) {
        data.forEach((k, v) -> {
            this.data.put(k instanceof String ? (String) k : k + "", v);
        }); // forEach
    }

    /**
     * 默认构造函数 <br>
     * 构造空数据记录 {}
     * 
     */
    public MyRecord() {
        this(new LinkedHashMap<String, Object>());
    }

    @Override
    public IRecord set(final String key, final Object value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public IRecord remove(String key) {
        this.data.remove(key);
        return this;
    }

    @Override
    public IRecord build(final Object... kvs) {
        return MyRecord.REC(kvs);
    }

    @Override
    public Map<String, Object> toMap() {
        return this.data;
    }

    @Override
    public List<String> keys() {
        return new ArrayList<>(this.data.keySet());
    }

    /**
     * 
     */
    @Override
    public Object get(final String key) {
        return this.data.get(key);
    }

    @Override
    public IRecord duplicate() {
        final MyRecord _rec = new MyRecord();
        _rec.data.putAll(this.data);
        return _rec;
    }

    @Override
    public String json() {
        return MyRecord.toJson(this.toMap2());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.toMap());
    }

    @Override
    public String toString() {
        return this.data.toString();
    }

    /**
     * 转换成 json 格式
     * 
     * @param json json字符串
     * @return IRecord
     */
    public static IRecord fromJson(final String json) {
        return MyJson.fromJson(json);
    }

    /**
     * 转换成 json 格式
     * 
     * @param json json字符串
     * @throws Exception
     */
    public static IRecord fromJson2(final String json) throws Exception {
        return MyJson.fromJson2(json);
    }

    /**
     * 转换成 json 格式
     * 
     * @param obj 值对象
     * @return json 字符串
     */
    public static String toJson(final Object obj) {
        return MyJson.toJson(obj);
    }

    /**
     * 转换成 json 格式,带有异常抛出
     * 
     * @param obj 值对象
     * @return json 字符串
     * @throws Exception
     */
    public static String toJson2(final Object obj) throws Exception {
        return MyJson.toJson2(obj);
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
        final int n = kvs.length; // 参数数量
        final LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>(); // 中间数据缓存
        final Consumer<Tuple2<String, ?>> put_tuple = tup -> { // 元组处理
            data.put(tup._1, tup._2);
        }; // put_tuple
        final Consumer<Stream<Object>> put_stream = stream -> { // 数据流的处理
            stream.map(Tuple2.snb(0)).map(tuple -> { // 编号分组
                return Optional.ofNullable(tuple._2).map(e -> {
                    if (e instanceof Tuple2) { // Tuple2 元组类型
                        return (Tuple2<?, ?>) e;
                    } else if (e instanceof Map.Entry) { // Map.Entry 类型
                        final Map.Entry<?, ?> me = (Map.Entry<?, ?>) tuple._2;
                        return Tuple2.of(me.getKey(), me.getValue());
                    } else { // default 默认
                        return tuple;
                    } // if
                }).map(e -> e.map1(Object::toString)).orElse(null); // key 专为字符串类型 && 默认值为null
            }).filter(Objects::nonNull).forEach(put_tuple); //
        }; // put_stream
        final Consumer<Iterable<Object>> put_iterable = iterable -> { // iterable 数据处理
            put_stream.accept(StreamSupport.stream(iterable.spliterator(), false));
        }; // put_iterable
        final Function<String, IRecord> json_parse = line -> Optional.ofNullable(line) //
                .map(String::trim).map(ln -> { //
                    IRecord r = null;
                    try { // 乐观锁 假设用户输入的是合法的json
                        r = MyRecord.fromJson2(ln);
                    } catch (Exception e) { // 非合法的json
                        if (!(ln.startsWith("{") && ln.endsWith("}"))) { // 省略的 最外层的大括号
                            final String _ln = IRecord.FT("{$0}", ln); // 补充外层括号
                            r = MyRecord.fromJson(_ln);
                        } // if
                    } // try
                    return r;
                }).orElse(IRecord.REC()); // 尝试解析json

        if (n == 1) { // 单一参数情况
            final T single = kvs[0]; // 提取单值
            if (single instanceof Map) { // Map情况的数据处理
                ((Map<?, ?>) single).forEach((k, v) -> { // 键值的处理
                    final String key = k instanceof String ? (String) k : k + "";
                    data.put(key, v);
                }); // forEach
            } else if (single instanceof IRecord) {// IRecord 对象类型 复制对象数据
                data.putAll(((IRecord) single).toMap());
            } else if (single instanceof Collection) {// Collection 对象类型 复制对象数据
                @SuppressWarnings("unchecked")
                final Collection<Object> coll = (Collection<Object>) single;
                put_iterable.accept(coll);
            } else if (single instanceof Iterable) {// Iterable 对象类型 复制对象数据
                @SuppressWarnings("unchecked")
                final Iterable<Object> iterable = (Iterable<Object>) single;
                put_iterable.accept(iterable);
            } else if (single instanceof Iterator) {// Iterable 对象类型 复制对象数据
                @SuppressWarnings("unchecked")
                final Iterable<Object> iterable = () -> (Iterator<Object>) single;
                put_iterable.accept(iterable);
            } else if (single instanceof Stream) {// stream 对象类型 复制对象数据
                @SuppressWarnings("unchecked")
                final Stream<Object> stream = (Stream<Object>) single;
                put_stream.accept(stream);
            } else if (single instanceof String) { // 字符串类型的单个参数
                final String line = single.toString();
                if (url_pattern.matcher(line).matches()) { // url 格式的数据
                    final String ln = send(line); // 读取url
                    IRecord.REC(ln).forEach((_k, _v) -> { // 字段添加
                        data.put(_k, _v);
                    }); // forEach
                } else if (file_pattern.matcher(line).matches()) { // 文件格式的数据读取
                    final String ln = readTextFile(line); // 读取文件
                    IRecord.REC(ln).forEach((_k, _v) -> { // 字段添加
                        data.put(_k, _v);
                    }); // forEach
                } else { // 其他类型
                    final IRecord rec = json_parse.apply(line);
                    if (rec != null) { //
                        final Map<String, Object> _data = rec.toMap();
                        if (_data.size() > 0) { // 非空数据
                            data.putAll(_data);
                        } // if
                    } // if
                } //
            } else if (single instanceof File) { // 文件类型
                final String ln = readTextFile((File) single); // 读取文件
                IRecord.REC(ln).forEach((_k, _v) -> { // 字段添加
                    data.put(_k, _v);
                }); // forEach
            } else { // if
                // do nothing 省略其他单值情况
            } // if
        } else { // 键名减值序列
            for (int i = 0; i < n - 1; i += 2) {
                data.put(kvs[i].toString(), kvs[i + 1]);
            } // for
        } // if

        return new MyRecord(data);
    }

    /**
     * 简单的http 请求
     * 
     * @param url 请求url
     * @return 请求返回结果
     */
    public static String send(final String url) {
        return send(url, null);
    }

    /**
     * 简单的http 请求
     * 
     * @param url    请求url
     * @param params 请求参数,$content_type,$conn_timeout,$read_timeout 读写参数
     * @return 请求返回结果
     */
    public static String send(final String url, final IRecord params) {
        String ret = null;
        final IRecord adjusted_params = Optional.ofNullable(params).orElse(REC()); // 调整后的请求参数
        final IRecord req_params = adjusted_params.tupleS().filter(e -> !e._1.startsWith("$"))
                .collect(IRecord.recclc());
        final String boundary = generateBoundary();
        final String APPLICATION_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded;charset=utf8";
        final String APPLICATION_JSON = "application/json;charset:utf8";
        final String MULTIPART_FORM = IRecord.FT("multipart/form-data;boundary=$0", boundary);

        try {
            final String content_type = adjusted_params.strOpt("$content_type") //
                    .map(e -> e.toLowerCase()) // 统一使用小写格式
                    .map(e -> {
                        switch (e) {
                        case "form": { // 表单类型
                            return APPLICATION_WWW_FORM_URLENCODED;
                        }
                        case "json": { // JSON 类型
                            return APPLICATION_JSON;
                        }
                        case "multipart": { // JSON 类型
                            return MULTIPART_FORM;
                        }
                        default: { // 默认类型
                            return e;
                        }
                        }
                    }).orElse(APPLICATION_JSON);
            final String method = adjusted_params.strOpt("$method").orElse("GET").toUpperCase();
            final Integer conn_timeout = adjusted_params.i4Opt("$conn_timeout").orElse(10000);
            final Integer read_timeout = adjusted_params.i4Opt("$read_timeout").orElse(conn_timeout);
            final URL _url = new URL(url);
            final HttpURLConnection conn = (HttpURLConnection) _url.openConnection();

            conn.setConnectTimeout(conn_timeout);
            conn.setReadTimeout(read_timeout);

            conn.setRequestMethod(method);// 设置请求方式为post
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", content_type);
            conn.setRequestProperty("User-Agent", "MyRecord Multipart HTTP Client 1.0");

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);

            conn.connect();

            final OutputStream outputStream = conn.getOutputStream();
            final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

            if (req_params.size() > 0) {
                if (method.equals("GET")) { // GET 方法
                    // do nothing
                } else { // 其他方法
                    final String ctype = Optional.ofNullable(content_type.split("[;]")) //
                            .map(e -> e.length > 0 ? e[0] : null).orElse("application/json;");
                    switch (ctype) {
                    case "application/json": { // json 格式
                        final String data = req_params.json(); // json 数据
                        bw.write(data);
                        break;
                    } // json
                    case "multipart/form-data": { // json 格式
                        final DataOutputStream dos = new DataOutputStream(outputStream);
                        final Function<String, String> extension_of = line -> {
                            final int i = line.lastIndexOf(".");
                            return i >= 0 ? line.substring(i + 1) : "";
                        };
                        final Consumer<Tuple2<String, Object>> dispose = tup -> {
                            final String key = tup._1;
                            final Object value = tup._2;
                            final String newLine = "\r\n";
                            try {
                                dos.write(boundary.getBytes());
                                if (value instanceof File) {
                                    final int BLOCK_SIZE = 1024;
                                    final File file = (File) value;
                                    final String filename = file.getName();
                                    final String extension = extension_of.apply(filename);
                                    final String mimetype = REC(
                                            "jpg,application/octet-streamm,jpeg,application/octet-streamm," //
                                                    .split(",")).strOpt(extension).orElse("application/octet-streamm");
                                    final String tpl = "Content-Disposition:form-data;name=\"$0\";filename=\"$1\"$2Content-Type:$3;chartset=utf8$5$5";
                                    final String ln = IRecord.FT(tpl, key, filename, newLine, mimetype, "text/plan",
                                            newLine, newLine);
                                    System.out.println(ln);
                                    dos.write(ln.getBytes());
                                    byte[] bb = null;
                                    final FileInputStream fis = new FileInputStream(file);
                                    while ((bb = fis.readNBytes(BLOCK_SIZE)).length > 0) { // 读写数据
                                        dos.write(bb);
                                    } // while
                                    fis.close();
                                } else {
                                    final String tpl = "Content-Disposition:form-data;name=\"$0\"$1$1$2$3Content-Type:$4;chartset=utf8$5$5";
                                    final String ln = IRecord.FT(tpl, key, newLine, value, newLine, "text/plain",
                                            newLine, newLine);
                                    System.out.println(ln);
                                    dos.write(ln.getBytes());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        };
                        req_params.tupleS().forEach(dispose);
                        break;
                    } // json
                    default: { // 默认类型
                        final String data = req_params.tupleS() // 键值序列
                                .map(e -> e.map2(s -> urlencode(s + ""))) // URLencode
                                .map(e -> IRecord.FT("$0=$1", e._1, e._2)) // 健名&键值
                                .collect(Collectors.joining("&")); // 值的拼接
                        bw.write(data);
                    } // default
                    } // switch content_type
                } // if method
            } // if req_params

            bw.flush();
            bw.close();
            final BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            ret = br.lines().collect(Collectors.joining("\n"));
            br.close();
            ;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * 边界分割线
     * 
     * @return 边界分割线
     */
    public static String generateBoundary() {
        return "--------------------------" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 文本文件的读取
     * 
     * @param file 文本文件路径
     * @return 文本内容
     */
    public static String readTextFile(final String file) {
        return readTextFile(new File(file));
    }

    /**
     * 文本文件的读取
     * 
     * @param file 文本文件路径
     * @return 文本内容
     */
    public static String readTextFile(final File file) {
        return readTextFile(file, "UTF-8");
    }

    /**
     * 读取文本文件
     * 
     * @param file     文本文件
     * @param encoding 文件编码
     * @return 文本内容
     */
    public static String readTextFile(final String file, final String encoding) {
        return readTextFile(new File(file), encoding);
    }

    /**
     * 读取文本文件
     * 
     * @param file     文本文件
     * @param encoding 文件编码
     * @return 文本内容
     */
    public static String readTextFile(final File file, final String encoding) {
        String ret = null;
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            ret = br.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Translates a string into application/x-www-form-urlencoded format using utf8
     * encoding
     * 
     * @param line 数据行
     * @return URL编码
     * @return application/x-www-form-urlencoded format
     */
    public static String urlencode(final String line) {
        return urlencode(line, "utf8");
    }

    /**
     * Translates a string into application/x-www-form-urlencoded format using a
     * specific encoding scheme
     * 
     * @param line     String to be translated.
     * @param encoding The name of a supported characterencoding.
     * @return application/x-www-form-urlencoded format
     */
    public static String urlencode(final String line, final String encoding) {
        String ret = null;
        try {
            ret = URLEncoder.encode(line, encoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

    /**
     * 路径模式
     */
    final static Pattern file_pattern = Pattern.compile("(^//.|^/|^[a-zA-Z])?:?/.+(/$)?");

    /**
     * URL模式
     */
    final static Pattern url_pattern = Pattern
            .compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");
}