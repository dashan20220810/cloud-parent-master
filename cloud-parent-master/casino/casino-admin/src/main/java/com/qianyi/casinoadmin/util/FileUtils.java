package com.qianyi.casinoadmin.util;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSON;
import com.qianyi.casinoadmin.model.dto.SysPermissionDTONode;
import com.qianyi.casinocore.util.DTOUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件工具类
 *
 * @author lance
 * @since 2022 -03-02 14:25:32
 */
@Slf4j
public class FileUtils {


    /**
     * 读取resources下的文件
     *
     * @param path 入参释义
     * @return {@link InputStream} 出参释义
     * @author lance
     * @since 2022 -03-02 14:25:32
     */
    public static InputStream getResourceAsStream(String path) {
        return FileUtils.class.getResourceAsStream(path);
    }

    /**
     * 从resource中读取json文件并解析
     *
     * @param <T>   the type parameter
     * @param path  入参释义
     * @param clazz 入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -03-02 14:31:34
     */
    public static <T> List<T> readJsonFileAndParse(String path, Class<T> clazz) {
        try (InputStream in = getResourceAsStream(path)) {
            String jsonString = IoUtil.read(in, "utf-8");
            if (jsonString.startsWith("{")) {
                T item = JSON.parseObject(jsonString, clazz);
                return Collections.singletonList(item);
            }
            return JSON.parseArray(jsonString, clazz);
        } catch (Exception e) {
            log.error("读取Json文件失败:", e);
        }
        return Collections.emptyList();
    }


}
