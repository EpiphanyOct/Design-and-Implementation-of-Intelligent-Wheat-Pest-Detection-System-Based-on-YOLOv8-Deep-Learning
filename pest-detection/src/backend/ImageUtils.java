package com.topwheat.pestdetect.util;

/**
 * 图片工具类
 * Feature: Pest Detection
 */
public class ImageUtils {

    /**
     * 验证图片是否有效
     * @param imageBytes 图片字节数组
     * @return 有效返回true
     */
    public static boolean isImageValid(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 10) {
            return false;
        }

        // 检查JPEG文件头 (FF D8 FF)
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) {
            return true;
        }

        // 检查PNG文件头 (89 50 4E 47)
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50
                && imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return true;
        }

        // 检查BMP文件头 (42 4D)
        if (imageBytes[0] == (byte) 0x42 && imageBytes[1] == (byte) 0x4D) {
            return true;
        }

        return false;
    }

    /**
     * 获取图片格式
     */
    public static String getImageFormat(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 10) {
            return "unknown";
        }

        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) {
            return "jpeg";
        }

        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50) {
            return "png";
        }

        if (imageBytes[0] == (byte) 0x42 && imageBytes[1] == (byte) 0x4D) {
            return "bmp";
        }

        return "unknown";
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 生成图片存储路径
     */
    public static String generateImagePath(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = getFileExtension(originalFilename);
        return "/uploads/detect_" + timestamp + "." + extension;
    }
}
