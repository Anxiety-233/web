package com.lab.equipment.util;

import cn.hutool.core.bean.BeanUtil;
import org.apache.commons.io.IOUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CSV导出工具类
 */
public class CsvExportUtil {

    // 日志导出的字段映射（与表头对应）
    private static final String[] LOG_EXPORT_FIELDS = {
            "id", "username", "operation", "ip", "browser", "requestParams", "responseResult", "operationTime"
    };

    public static <T> void exportCSV(HttpServletResponse response, String fileName, String[] headers, List<T> dataList) throws Exception {
        // 解决中文乱码 + 设置响应头
        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setCharacterEncoding("UTF-8");

        OutputStream out = response.getOutputStream();
        // 写入UTF-8 BOM头，解决Excel中文乱码
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        // 写入表头
        out.write(String.join(",", headers).getBytes(StandardCharsets.UTF_8));
        out.write("\n".getBytes(StandardCharsets.UTF_8));

        // 写入数据（按表头字段顺序）
        for (T data : dataList) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < LOG_EXPORT_FIELDS.length; i++) {
                Object value = BeanUtil.getFieldValue(data, LOG_EXPORT_FIELDS[i]);
                String valueStr = value == null ? "" : value.toString().replace(",", "，"); // 替换逗号避免列错位
                sb.append(valueStr);
                if (i < LOG_EXPORT_FIELDS.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
        out.flush();
        IOUtils.close(out);
    }
}