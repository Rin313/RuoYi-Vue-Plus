package org.dromara.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.metadata.IPage;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.mybatis.core.PageUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.common.mybatis.helper.DataBaseHelper;

import org.dromara.generator.domain.GenTable;
import org.dromara.generator.domain.GenTableColumn;
import org.dromara.generator.service.GenTableService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成 操作处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/gen")
public class GenController {

    private final GenTableService genTableService;

    /**
     * 查询代码生成列表
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/list")
    public IPage<GenTable> genList(GenTable genTable, PageQuery pageQuery) {
        return genTableService.selectPageGenTableList(genTable, pageQuery);
    }

    /**
     * 修改代码生成业务
     *
     * @param tableId 表ID
     */
    @RepeatSubmit()
    @SaCheckPermission("tool:gen:query")
    @GetMapping(value = "/{tableId}")
    public Map<String, Object> getInfo(@PathVariable Long tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        List<GenTable> tables = genTableService.selectGenTableAll();
        List<GenTableColumn> list = genTableService.selectGenTableColumnListByTableId(tableId);
        Map<String, Object> map = new HashMap<>(3);
        map.put("info", table);
        map.put("rows", list);
        map.put("tables", tables);
        return map;
    }

    /**
     * 查询数据库列表
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/db/list")
    public IPage<GenTable> dataList(GenTable genTable, PageQuery pageQuery) {
        return genTableService.selectPageDbTableList(genTable, pageQuery);
    }

    /**
     * 查询数据表字段列表
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping(value = "/column/{tableId}")
    public IPage<GenTableColumn> columnList(@PathVariable("tableId") Long tableId) {
        List<GenTableColumn> list = genTableService.selectGenTableColumnListByTableId(tableId);
        return PageUtils.toPage(list);
    }

    /**
     * 导入表结构（保存）
     *
     * @param tables   表名串
     * @param dataName 数据源名称
     */
    @SaCheckPermission("tool:gen:import")
    @Lock4j(keys = {"#dataName"}, acquireTimeout = 10000)
    @RepeatSubmit()
    @PostMapping("/importTable")
    public void importTableSave(String tables, String dataName) {
        String[] tableNames = Convert.toStrArray(tables);
        // 查询表信息
        List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames, dataName);
        genTableService.importGenTable(tableList, dataName);
    }

    /**
     * 删除代码生成
     *
     * @param tableIds 表ID串
     */
    @SaCheckPermission("tool:gen:remove")
    @DeleteMapping("/{tableIds}")
    public void delete(@PathVariable Long[] tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
    }

    /**
     * 预览代码
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:preview")
    @GetMapping("/preview/{tableId}")
    public Map<String, String> preview(@PathVariable("tableId") Long tableId) throws IOException {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return dataMap;
    }

    /**
     * 生成代码（下载方式）
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:code")
    @GetMapping("/download/{tableId}")
    public void download(HttpServletResponse response, @PathVariable("tableId") Long tableId) throws IOException {
        byte[] data = genTableService.downloadCode(tableId);
        genCode(response, data);
    }

    /**
     * 生成代码（自定义路径）
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:code")
    @GetMapping("/genCode/{tableId}")
    public void genCode(@PathVariable("tableId") Long tableId) {
        genTableService.generatorCode(tableId);
    }

    /**
     * 同步数据库
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:edit")
    @Lock4j(keys = {"#tableId"}, acquireTimeout = 5000)
    @GetMapping("/synchDb/{tableId}")
    public void synchDb(@PathVariable("tableId") Long tableId) {
        genTableService.synchDb(tableId);
    }

    /**
     * 批量生成代码
     *
     * @param tableIdStr 表ID串
     */
    @SaCheckPermission("tool:gen:code")
    @GetMapping("/batchGenCode")
    public void batchGenCode(HttpServletResponse response, String tableIdStr) throws IOException {
        String[] tableIds = Convert.toStrArray(tableIdStr);
        byte[] data = genTableService.downloadCode(tableIds);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"ruoyi.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IoUtil.write(response.getOutputStream(), false, data);
    }

    /**
     * 查询数据源名称列表
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping(value = "/getDataNames")
    public List<String> getCurrentDataSourceNameList() {
        return DataBaseHelper.getDataSourceNameList();
    }
}
