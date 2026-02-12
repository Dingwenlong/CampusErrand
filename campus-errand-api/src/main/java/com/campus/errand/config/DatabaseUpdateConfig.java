package com.campus.errand.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUpdateConfig implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // 修复数据库字符集
            fixDatabaseCharset();

            // 检查并创建轮播图表
            ensureBannerTable();
            
            // 检查并添加 tb_task 表的字段
            addTaskTableColumns();
            
            // 检查并添加 tb_config 表的字段
            addConfigTableColumns();
            
            System.out.println("数据库表结构更新完成");
        } catch (Exception e) {
            System.err.println("数据库表结构更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fixDatabaseCharset() {
        try {
            // 修改数据库字符集为 utf8mb4
            jdbcTemplate.execute("ALTER DATABASE campus_errand CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("已修改数据库字符集为 utf8mb4");
            
            // 修改 tb_config 表字符集
            jdbcTemplate.execute("ALTER TABLE tb_config CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("已修改 tb_config 表字符集为 utf8mb4");
            
            // 修改 tb_task 表字符集
            jdbcTemplate.execute("ALTER TABLE tb_task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("已修改 tb_task 表字符集为 utf8mb4");
        } catch (Exception e) {
            System.err.println("修改字符集失败: " + e.getMessage());
        }
    }

    private void addTaskTableColumns() {
        // 检查 cancel_time 字段是否存在
        if (!columnExists("tb_task", "cancel_time")) {
            jdbcTemplate.execute("ALTER TABLE tb_task ADD COLUMN cancel_time DATETIME DEFAULT NULL COMMENT '取消时间' AFTER is_urgent");
            System.out.println("已添加 tb_task.cancel_time 字段");
        }

        // 检查 cancel_type 字段是否存在
        if (!columnExists("tb_task", "cancel_type")) {
            jdbcTemplate.execute("ALTER TABLE tb_task ADD COLUMN cancel_type TINYINT DEFAULT NULL COMMENT '取消类型：1-用户取消 2-系统自动取消' AFTER cancel_time");
            System.out.println("已添加 tb_task.cancel_type 字段");
        }

        // 检查 cancel_reason 字段是否存在
        if (!columnExists("tb_task", "cancel_reason")) {
            jdbcTemplate.execute("ALTER TABLE tb_task ADD COLUMN cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因' AFTER cancel_type");
            System.out.println("已添加 tb_task.cancel_reason 字段");
        }
    }

    private void addConfigTableColumns() {
        // 检查 category 字段是否存在
        if (!columnExists("tb_config", "category")) {
            jdbcTemplate.execute("ALTER TABLE tb_config ADD COLUMN category VARCHAR(50) DEFAULT 'other' COMMENT '配置分类' AFTER description");
            System.out.println("已添加 tb_config.category 字段");
        }
    }

    private void ensureBannerTable() {
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS tb_banner (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID', " +
            "title VARCHAR(100) NOT NULL COMMENT '标题', " +
            "content VARCHAR(255) DEFAULT NULL COMMENT '内容描述', " +
            "bg_color VARCHAR(255) DEFAULT NULL COMMENT '背景色', " +
            "image_url VARCHAR(500) DEFAULT NULL COMMENT '图片URL', " +
            "link_url VARCHAR(500) DEFAULT NULL COMMENT '链接URL', " +
            "sort_order INT DEFAULT 0 COMMENT '排序，数字越小越靠前', " +
            "status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用', " +
            "create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
            "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
            "INDEX idx_status (status), " +
            "INDEX idx_sort_order (sort_order)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表'"
        );
        System.out.println("已确保 tb_banner 表存在");
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() " +
                        "AND TABLE_NAME = ? " +
                        "AND COLUMN_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, columnName);
            return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("检查字段是否存在时出错: " + e.getMessage());
            return false;
        }
    }
}
