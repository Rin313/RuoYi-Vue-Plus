package org.dromara.common.core.utils.file;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.dromara.common.core.BizException;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件处理工具类
 *
 */
public class FileUtils {

    /**
     * 下载文件名重新编码
     *
     * @param response     响应对象
     * @param realFileName 真实文件名
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) {
        String percentEncodedFileName = percentEncode(realFileName);
        String contentDispositionValue = "attachment; filename=%s;filename*=utf-8''%s".formatted(percentEncodedFileName, percentEncodedFileName);
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        response.setHeader("Content-disposition", contentDispositionValue);
        response.setHeader("download-filename", percentEncodedFileName);
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8);
        return encode.replaceAll("\\+", "%20");
    }

    /** Web 常用图片后缀 */
    public static final Set<String> WEB_IMAGE_EXTS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp"
    );
    
    /** 全部图片后缀 */
    public static final Set<String> ALL_IMAGE_EXTS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp", 
        "bmp", "tiff", "tif", "svg", "ico", "heic", "avif"
    );
    
    // ========== 常用图片 MIME 类型 ==========
    
    /** Web 常用图片 MIME */
    public static final Set<String> WEB_IMAGE_MIMES = Set.of(
        "image/jpeg",
        "image/png", 
        "image/gif",
        "image/webp"
    );
    
    /** 全部图片 MIME */
    public static final Set<String> ALL_IMAGE_MIMES = Set.of(
        "image/jpeg",
        "image/png",
        "image/gif", 
        "image/webp",
        "image/bmp",
        "image/x-ms-bmp",
        "image/tiff",
        "image/svg+xml",
        "image/x-icon",
        "image/vnd.microsoft.icon",
        "image/heic",
        "image/heif",
        "image/avif"
    );
    private static final Tika tika = new Tika();
    private static final String basePath=SpringUtils.getProperty("file.base-path");
    public static String save(MultipartFile file, Integer MB,Set<String> allowedExts,Set<String> allowedMimes) {
        if (file == null || file.isEmpty()) throw new BizException("空文件");//TODO：前端没法清空提交，没法在已经提交后修改
        //直接抛异常，则不允许提交无封面文件；在外检查空或返回null，则无法进行删除；返回空字符串，则每次修改都会删除
        if (file.getSize() > MB*1024*1024l) throw new RuntimeException("上传文件大小不超过"+MB+"MB");
        String originalFilename = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(originalFilename).toLowerCase();
        if (!allowedExts.contains(ext)) throw new RuntimeException("不支持的文件类型");
        try (InputStream stream = file.getInputStream()) {
            if(!allowedMimes.contains(tika.detect(stream)))
                throw new RuntimeException("文件内容异常");
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败", e);
        }
        try {
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path dirPath = Paths.get(basePath).toAbsolutePath().normalize().resolve(dateFolder);
            Files.createDirectories(dirPath);
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path targetPath = dirPath.resolve(newFilename);
            file.transferTo(targetPath);
            return dateFolder + "/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("保存失败", e);
        }
    }
}