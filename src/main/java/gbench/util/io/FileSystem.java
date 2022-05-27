package gbench.util.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSystem {
    
    /**
     * 把path路径转传承文件 对象 <br>
     * 
     * 生成当前类的默认数据存放路径：用于规范文件系统资源。<br>
     * &nbsp;1)当clazz为null时候,优先判断 file 是否构成一个本地文件路径,如果构成 则直接返回 <br>
     * &nbsp; 否则尝试在类文件根路径进行(FileSyste.class加载器的根路径)文件找寻： <br>
     * &nbsp;&nbsp;类似于
     * C:\Users\gbench\eclipse-workspace\Plutus\misc-service-provider\target\ <br>
     * &nbsp;&nbsp;如果存在则相应文件则返回对应路径，否则返回file<br>
     * &nbsp; 2) 当class不为空则在class同级的目录下寻找对应的文件<br>
     * 路径中 "\"会被替换成"/",并且多个"/"会被视做一个：即"/a//b/c"会被视作"/a/b/c"<br>
     * <br>
     * path for(file:eg2001_900000,class:class
     * gbench.web.apps.finance.kline.KLineController):<br>
     * nullgbench/web/apps/finance/kline/eg2001_900000 <br>
     * <br>
     * path for(file:nullgbench/web/apps/finance/kline/eg2001_900000,class:null):
     * <br>
     * nullgbench/web/apps/finance/kline/eg2001_900000 <br>
     * 
     * @param filepath  文件路径
     * @param clazz 相对于位置的基点：与file文件同级的目录的class文件,null 则返回file本身
     * @return file 的文件的路径：可以通过new File(path(file,class)) 可以直接读取文件。<br>
     *         当采用jar问运行的时候是相对路径： <br>
     *         比如：path for (file:nullgbench/web/apps/finance/kline/eg2001_60000,class:null): <br>
     *         nullgbench/web/apps/finance/kline/eg2001_60000 <br>
     *         new File(new File(path(file,class)))。getAbsolutePath(): <br>
     *         却可以返回相对于运行路径D:\sliced\ws\gitws\moxi-agent\moxi\target的绝对路径 <br>
     *         D:\sliced\ws\gitws\moxi-agent\moxi\target\nullgbench\web\apps\finance\kline\eg2001_60000 <br>
     *
     *         当文件展开运行的时候是绝对路径：
     */
    public static File fileOf(final String filepath, final Class<?> clazz) {
        return new File(path(filepath, clazz));
    }
    
    
    /**
     * 生成当前类的默认数据存放路径：用于规范文件系统资源。<br>
     * &nbsp;1)当clazz为null时候,优先判断 file 是否构成一个本地文件路径,如果构成 则直接返回 <br>
     * &nbsp; 否则尝试在类文件根路径进行(FileSyste.class加载器的根路径)文件找寻： <br>
     * &nbsp;&nbsp;类似于
     * C:\Users\gbench\eclipse-workspace\Plutus\misc-service-provider\target\ <br>
     * &nbsp;&nbsp;如果存在则相应文件则返回对应路径，否则返回file<br>
     * &nbsp; 2) 当class不为空则在class同级的目录下寻找对应的文件<br>
     * 路径中 "\"会被替换成"/",并且多个"/"会被视做一个：即"/a//b/c"会被视作"/a/b/c"<br>
     * <br>
     * path for(file:eg2001_900000,class:class
     * gbench.web.apps.finance.kline.KLineController):<br>
     * nullgbench/web/apps/finance/kline/eg2001_900000 <br>
     * <br>
     * path for(file:nullgbench/web/apps/finance/kline/eg2001_900000,class:null):
     * <br>
     * nullgbench/web/apps/finance/kline/eg2001_900000 <br>
     * 
     * @param file  文件路径
     * @param clazz 相对于位置的基点：与file文件同级的目录的class文件,null 则返回file本身
     * @return file的文件的路径：可以通过new File(path(file,class)) 可以直接读取文件。<br>
     *         当采用jar问运行的时候是相对路径： <br>
     *         比如：path
     *         for(file:nullgbench/web/apps/finance/kline/eg2001_60000,class:null):
     *         <br>
     *         nullgbench/web/apps/finance/kline/eg2001_60000 <br>
     *         new File(new File(path(file,class)))。getAbsolutePath(): <br>
     *         却可以返回相对于运行路径D:\sliced\ws\gitws\moxi-agent\moxi\target的绝对路径 <br>
     *         D:\sliced\ws\gitws\moxi-agent\moxi\target\nullgbench\web\apps\finance\kline\eg2001_60000
     *         <br>
     *
     *         当文件展开运行的时候是绝对路径：
     */
    public static String path(final String file, final Class<?> clazz) {
        String path = null;// 结果返回值
        if(new File(file).exists()) {// file本身业已构成完整的文件路径
            path = file;// 绝对存在由对应的文静则直接返回该路径：即这是一个有效的文件路径。
        } else if( file.matches("^\\s*[/\\\\].+$")  // linux 结构的路径 以 /或\开头
              || file.matches("\\s*^[a-zA-Z]\\s*:\\s*[/\\\\].+$") // windows 结构的路径 以 A:\开头
        ) { // 判断是否是绝对路径
            return file;
        }else {// 构造一个相对于clazz所在位置的文件路径。
            ClassLoader classLoader = clazz!=null?clazz.getClassLoader() //  根据classloader 以资源的方式获取路径
                :FileSystem.class.getClassLoader();// 默认为FileSystem.class所在的根路径位置。
            String home = null;// 基准根路径
            try {// 注意需要采用.toURI()把：包路径
                home = classLoader.getResource("").toURI().getPath()+// classLoader  下的根路径
                    (clazz==null?"":clazz.getPackage().getName().replace(".", "/")); // 非空的clazz加入相对于类根路径
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            path = (home+"/"+file).replace("\\", "/").replaceAll("/+", "/");// 路径拼接。
        }//if
        //System.out.println("path for(file:"+file+",class:"+clazz+"):\n\t"+path+"\n");
        return path;
    }
    
    
    /**
     * 文件遍历
     * 
     * @param file 文件目录
     * @param cs   回调函数
     */
    public static void tranverse(File file, Consumer<File> cs) {
        if (file == null || !file.exists())
            return;
        if (file.isFile())
            cs.accept(file);
        else if (file.isDirectory()) {
            Arrays.stream(file.listFiles()).forEach(e -> tranverse(e, cs));
        }
    }
    
    
    /**
     * 把window 路径名转换成unix路径名
     * @param path 路径名
     * @return unix 格式的path
     */
    public static String unixpath(String path) {
        if(path==null)return null;
        return path.replaceAll("[\\\\]+", "/").trim();
    }
    
    /**
     * 提取一个全路径文件的扩展名
     * @param fullname 文件全路径名，例如c:/a/b/c.jpg
     * @return 文件的简单名，不包含路径,例如 c.jpg
     */
    public static String extensionpicker(String fullname) {
        fullname = unixpath(fullname);
        if(fullname.indexOf(".")<=0)return "";
        Matcher matcher = Pattern.compile("([^\\\\.]+$)").matcher(fullname);
        return matcher.find()?matcher.group(1):null;
    }
    
    /**
     * 
     * @param file 文件绝对路径
     * @param contentSuppler  文件书写内容
     */
    public static boolean utf8write(String file,Supplier<String> contentSuppler) {
        return write(new File(file),"utf8",contentSuppler);
    }
    
    /**
     * 提取文件的路径
     * 
     * @param file 文件对象
     * @return 文件路径
     */
    public static String path(final File file) {
        return file.getAbsolutePath().replace("\\", "/");
    }
    
    /**
     * 
     * @param file 文件对象
     * @param contentSuppler  文件书写内容
     */
    public static boolean write(File file,String encoding,Supplier<String> contentSuppler) {
        if(contentSuppler==null || file == null )return false;
        BufferedWriter bw = null;
        try {
            File pfile = file.getParentFile();
            if(!file.exists())pfile.mkdirs();
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),encoding));
            bw.write(contentSuppler.get());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {bw.close();} catch (IOException e) {e.printStackTrace(); return false;}
        }
        
        return true;
    }

}
