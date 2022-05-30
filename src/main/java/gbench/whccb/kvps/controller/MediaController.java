package gbench.whccb.kvps.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gbench.util.io.FileSystem;
import gbench.util.lisp.IRecord;

/**
 * 
 * 媒体文件的控制器
 * 
 * @author xuqinghua
 *
 */
@RequestMapping("media")
@RestController
public class MediaController {
    /**
     * 提取任务定义
     * 
     * http://localhost:8090/kvps/media/file/upload
     * 
     * @param file 文件名称
     * @return {code,destFile,extension,downlodUrl}
     */
    @RequestMapping("file/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {

        final IRecord rec = IRecord.REC("code", 0);

        if (file.isEmpty()) {
            rec.add("message", "上传文件，请选择文件");
        }

        final String fileName = file.getOriginalFilename();
        final String home = uploadHome.replaceAll("[/\\\\]+$", ""); // 去掉尾部的"/"
        final File destFile = new File(IRecord.FT("$0/$1", home, fileName));
        final String extension = FileSystem.extensionpicker(destFile.getAbsolutePath()).toLowerCase();
        final String mimeType = mimetype(extension);
        final String downloadUrl = IRecord.FT("/$0/media/file/download?key=$1&mimetype=$2", appName,
                destFile.getAbsoluteFile().getAbsolutePath().replace("\\", "/"), mimeType);
        try {
            file.transferTo(destFile);
            rec.add("message", "文件上传成功", "destFile", path(destFile), "extension", extension, "downloadUrl",
                    downloadUrl);
        } catch (Exception e) {
            e.printStackTrace();
            rec.add("message", e.getMessage());
        } // try

        return rec.toMap2();
    }

    /**
     * 
     * http://localhost:8090/kvps/media/file/download?key=E:/slicee/temp/kvps/excel/upload/temoc-shrug.png&mimetype=image/png
     * 
     * @param key      键名
     * @param mimetype 媒体类型 图片类型：image/png, 文本类型：text/plain
     * @param flag     在返回值值中写 attachment;fileName
     * @param response 响应对象
     */
    @ResponseBody
    @RequestMapping(value = "file/download")
    public void download(final String key, final String mimetype, final Boolean flag,
            final HttpServletResponse response) {

        final File file = new File(key);

        try (//
                final OutputStream os = response.getOutputStream();
                final FileInputStream fis = new FileInputStream(file)) {
            final byte[] oo = new byte[fis.available()];

            response.setContentType(mimetype); // 写入媒体类型
            Optional.ofNullable(flag).map(e -> !e ? null : e) //
                    .ifPresent(e -> { // 写出下载文件名
                        response.setHeader("Content-Disposition",
                                IRecord.FT("attachment;fileName=$0", new File(key).getName()));
                    });
            fis.read(oo);
            os.write(oo);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } // try

    }

    /**
     * 提取任务定义
     * 
     * http://localhost:8090/kvps/media/file/write
     * 
     * @param key   文件名称
     * @param lines 文件数据行
     * @return {code,destFile,extension,downlodUrl}
     */
    @RequestMapping("file/write")
    public Map<String, Object> write(final String key, final String lines) {

        final IRecord rec = IRecord.REC("code", 0);
        final String path = key; // key 理解为path
        final File pfile = new File(path).getParentFile();
        if (!pfile.exists()) { // 创建文件目录
            pfile.mkdirs();
        }
        final File pathfile = writeKey(path, lines);
        final String extension = FileSystem.extensionpicker(path);
        final String mimeType = mimetype(extension);
        final String downloadUrl = IRecord.FT("/$0/media/file/download?key=$1&mimetype=$2", appName, path(pathfile),
                mimeType);
        rec.add("message", "文件写入成功", "destFile", path(pathfile), "extension", extension, "downloadUrl", downloadUrl);
        return rec.toMap();
    }

    /**
     * 提取任务定义
     * 
     * http://localhost:8090/kvps/media/file/list?key=E:/slicee/temp/kvps/ufms
     * 
     * @param key 文件名称
     * @return {code,destFile,extension,downlodUrl}
     */
    @RequestMapping("file/list")
    public Map<String, Object> list(final String key) {

        final IRecord rec = IRecord.REC("code", 0);
        final List<File> listfiles = new ArrayList<File>();

        FileSystem.tranverse(new File(key), listfiles::add);
        final List<Map<String, Object>> files = listKey(key);

        rec.add("message", "文件读取成功", "files", files);
        return rec.toMap();
    }

    /**
     * 读取key表标识的文件列表
     * 
     * @param key 列表标识
     * @return [{code,destFile,extension,downlodUrl}]
     */
    public List<Map<String, Object>> listKey(final String key) {

        final List<File> listfiles = new ArrayList<File>();
        FileSystem.tranverse(new File(key), listfiles::add);

        final List<Map<String, Object>> files = listfiles.stream().map(pathfile -> {
            final String path = pathfile.getAbsolutePath();
            final String extension = FileSystem.extensionpicker(path);
            final String mimetype = mimetype(extension);
            final String url = IRecord.FT("/$0/media/file/download?key=$1&mimetype=$2", appName, path(pathfile),
                    mimetype);
            final String name = pathfile.getName();
            return IRecord.REC("name", name, "extension", extension, "file", pathfile, "url", url);
        }).map(e -> e.toMap2()).collect(Collectors.toList());

        return files;
    }

    /**
     * 把文本lines以key的方式进行写入
     * 
     * @param key   文件名 标识key
     * @param lines 文件正文
     * @return 文件对象
     */
    public static File writeKey(final String key, final String lines) {

        final File myfile = FileSystem.fileOf(key, MediaController.class.getClass());
        FileSystem.utf8write(key, () -> lines);

        return myfile;
    }

    /**
     * 获取文件的 mimeType
     * 
     * @param extension 扩展名
     * @return mimeType
     */
    public static String mimetype(final String extension) {

        final String mimeType = Optional.ofNullable(extension).map(e -> {
            final List<String> xvec = Stream.of("xls,xlsx".split("[,]+")).collect(Collectors.toList());
            if (xvec.contains(e)) { // excel 文件的处理
                return "application/excel";
            } else if ("jpg".equals(e) || "jpeg".equals(e)) {
                return "image/jpeg";
            } else if ("png".equals(e) || "png".equals(e)) {
                return "image/png";
            } else if ("bmp".equals(e)) {
                return "image/bmp";
            } else if ("png".equals(e)) {
                return "image/png";
            } else if ("bz2".equals(e)) {
                return "application/x-bzip2";
            } else if ("bz".equals(e)) {
                return "application/x-bzip";
            } else if ("json".equals(e)) {
                return "application/json";
            } else {
                return null;
            } // if
        }).orElse("text/plain"); // optional
        return mimeType;
    }

    /**
     * 提取文件的路劲
     * 
     * @param file 文件对象
     * @return 文件路径
     */
    public static String path(final File file) {
        return file.getAbsolutePath().replace("\\", "/");
    }

    @Value("${spring.application.name:kvps}")
    private String appName;
    @Value("${excel.upload.home:E:/slicee/temp/kvps/excel/upload}")
    private String uploadHome;
    @Value("${excel.output.home:E:/slicee/temp/kvps/excel/output}")
    private String outputHome;

}
