package gbench.util.lisp;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 二维元组 (t,u) 仅含有两个元素的数据结构，形式简单 但是 内涵极为丰富
 *
 * @param <T> 第一位置 元素类型
 * @param <U> 第而位置 元素类型
 */
public class Tuple2<T, U> {

    /**
     * @param _1 第一位置元素
     * @param _2 第二位置元素
     */
    public Tuple2(final T _1, final U _2) {
        this._1 = _1;
        this._2 = _2;
    }

    /**
     * 返回第一元素
     *
     * @return 第一元素
     */
    public T _1() {
        return this._1;
    }

    /**
     * 返回 第二元素
     *
     * @return 第二元素
     */
    public U _2() {
        return this._2;
    }

    /**
     * 1#位置 元素变换
     * 
     * @param <X>    mapper 的结果类型
     * @param mapper 元素变化函数 t-x
     * @return 变换后的 元素 (x,u)
     */
    public <X> Tuple2<X, U> map1(final Function<T, X> mapper) {
        return TUP2(mapper.apply(this._1), this._2);
    }

    /**
     * 2#位置 元素变换
     * 
     * @param <X>    mapper 的结果类型
     * @param mapper 元素变化函数 u-x
     * @return 变换后的 元素 (t,x)
     */
    public <X> Tuple2<T, X> map2(final Function<U, X> mapper) {
        return TUP2(this._1, mapper.apply(this._2));
    }

    /**
     * 对象复制
     * 
     * @return 复制的对象
     */
    public Tuple2<T, U> duplicate() {
        return TUP2(this._1, this._2);
    }

    /**
     * 元素位置互换
     * 
     * @return (u,t)
     */
    public Tuple2<U, T> swap() {
        return TUP2(this._2, this._1);
    }

    /**
     * 元素位置互换
     * 
     * @param <X>    元素类型
     * @param mapper 元祖变换函数 (u,t)->X
     * @return X 类型结果
     */
    public <X> X swap(final Function<Tuple2<U, T>, X> mapper) {
        return mapper.apply(this.swap());
    }

    /**
     * 智能版的数组转换 <br>
     * 视 Tuple2 为一个Object的二元数组[_1,_2],然后调用mapper 给予变换<br>
     * 
     * @param <X>    mapper 结果的类型
     * @param mapper [o]->x 数组变换函数
     * @return X类型结果
     */
    public <X> X arrayOf(final Function<Object[], X> mapper) {
        return mapper.apply(new Object[] { this._1, this._2 });
    }

    /**
     * 转成列表结构
     * 
     * @return 列表结构
     */
    @SuppressWarnings("unchecked")
    public <X> List<X> toList(final Class<X> clazz) {
        return Arrays.asList((X) this._1, (X) this._2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this._1, this._2);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Tuple2) {
            @SuppressWarnings("unchecked")
            final Tuple2<Object, Object> another = (Tuple2<Object, Object>) obj;
            return eql.test(this._1, another._1) && eql.test(this._2, another._2);
        } else {
            return false;
        }
    }

    /**
     * 数据格式化
     * 
     * @return 数据格式化
     */
    public String toString() {
        return "(" + this._1 + "," + this._2 + ")";
    }

    /**
     * 构造一个二元组
     * 
     * @param t   第一元素
     * @param u   第二元素
     * @param <T> 第一元素类型
     * @param <U> 第二元素类型
     * @return 二元组对象的构造
     */
    public static <T, U> Tuple2<T, U> of(final T t, final U u) {
        return new Tuple2<T, U>(t, u);
    }

    /**
     * 生成一个 (x,y) 类型的 comparator <br>
     * (升顺序,先比较第一元素键名,然后比较键值)
     * 
     * @param <X> 第一元素类型
     * @param <Y> 第二元素类型
     * @return 生成一个 comparator
     */
    public static <X, Y> Comparator<Tuple2<X, Y>> defaultComparator() {
        @SuppressWarnings("unchecked")
        final BiFunction<Object, Object, Integer> cmp = (a, b) -> { // 生成比较函数
            if (a == null && b == null)
                return 0;
            else if (a == null) {
                return -1;
            } else if (b == null) {
                return 1;
            } else { // a b 均为 非空
                int ret = -1; // 默认返回值
                try {
                    if (a instanceof String || b instanceof String) {
                        throw new Exception("字符串比较,设计的跳转异常，并非错误，这是一条类似于goto 的跳转语句写法");
                    } else {
                        ret = ((Comparable<Object>) a).compareTo(b);
                    } // if
                } catch (final Exception e) {
                    final String _a = a.toString();
                    final String _b = b.toString();
                    try { // 尝试做数字解析
                        ret = ((Double) Double.parseDouble(_a)).compareTo(Double.parseDouble(_b));
                    } catch (Exception p) {
                        ret = _a.compareTo(_b);
                    } // try
                } // try

                return ret; // 返回比较结果
            } // if
        }; // cmp 比较函数

        return (tup1, tup2) -> {
            final int a = cmp.apply(tup1._1, tup2._1);
            if (a == 0) {
                return cmp.apply(tup1._2, tup2._2);
            } else {
                return a;
            } // if
        };
    }

    /**
     * 
     * @param tup
     * @return
     */
    public static Stream<Object> flatS(final Tuple2<?, ?> tup) {
        return flat(tup).stream();
    }

    /**
     * 
     * @param tup
     * @return
     */
    public static List<Object> flat(final Tuple2<?, ?> tup) {
        final List<Object> ll = new LinkedList<Object>();
        final Stack<Object> stack = new Stack<Object>();

        stack.push(tup);
        while (!stack.isEmpty()) {
            final Object p = stack.pop();
            if (p == null)
                continue;

            if (p instanceof Tuple2) {
                @SuppressWarnings("unchecked")
                final Tuple2<Object, Object> _tup = (Tuple2<Object, Object>) p;
                Stream.of(_tup._2(), _tup._1()).forEach(stack::push);
            } else {
                ll.add(p);
            }
        }

        return ll;
    }

    /**
     * 构造一个二元组
     * 
     * @param _1  第一元素
     * @param _2  第二元素
     * @param <T> 第一元素类型
     * @param <U> 第二元素类型
     * @return 二元组对象的构造
     */
    public static <T, U> Tuple2<T, U> TUP2(final T _1, final U _2) {
        return Tuple2.of(_1, _2);
    }

    /**
     * 构造一个二元组 <br>
     * 提取tt前两个元素组成 Tuple2
     * 
     * @param tt  数组元素,提取tt前两个元素组成 Tuple2, (tt[0],tt[1])
     * @param <T> 第一元素类型,第二元素类型
     * @return 二元组对象的构造
     */
    public static <T> Tuple2<T, T> TUP2(final T[] tt) {
        if (tt == null || tt.length < 1) {
            return null;
        }

        final T _1 = tt.length >= 1 ? tt[0] : null;
        final T _2 = tt.length >= 2 ? tt[0] : null;

        return new Tuple2<>(_1, _2);
    }

    /**
     * 序列号生成器 <br>
     * Serial Number Builder
     * 
     * @param <T>   元素
     * @param start 开始号码
     * @return t->(int,t) 的标记函数
     */
    public static <T> Function<T, Tuple2<Integer, T>> snbuilder(final int start) {
        final AtomicInteger sn = new AtomicInteger(start);
        return t -> TUP2(sn.getAndIncrement(), t);
    }

    /**
     * snbuilder 的简写 <br>
     * 键名，键值 生成器 <br>
     * 开始号码为为0
     * 
     * @param <T>   元素
     * @param start 开始号码
     * @return t->(int,t) 的标记函数
     */
    public static <T> Function<T, Tuple2<Integer, T>> snb(final Integer start) {
        return snbuilder(start);
    }

    /**
     * 键名，键值 生成器 <br>
     * 开始号码为为0
     * 
     * @param <T> 元素类型
     * @return t->(int,t) 的标记函数
     */
    public static <T> Function<T, Tuple2<Integer, T>> snbuilder() {
        return snbuilder(0);
    }

    /**
     * 判断两个对象是否等价
     */
    public final static BiPredicate<Object, Object> eql = (a, b) -> a != null ? a.equals(b) : b == null;

    public final T _1; // 第一位置元素
    public final U _2; // 第二位置元素

}