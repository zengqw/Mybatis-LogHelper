package com.github.zengqw.store.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.zengqw.store.CreateTableParam;

public class SQLUtil {

	/**
	 * 清空表
	 * 
	 * @param ms
	 * @param tableName
	 */
	public static void dropTable(MappedStatement ms, String tableName) {
		JdbcTemplate jdbcTemplate = jdbcTemplate(ms);

		// jdbcTemplate.execute("drop table test");

	}
	
	
	/**
	 * 创建表
	 * @param ms
	 * @param tableName
	 */
	public static void createTable(MappedStatement ms, Map<String, List<Object>> newTableMap) {
		JdbcTemplate jdbcTemplate = jdbcTemplate(ms);
		if (newTableMap.size() > 0) {
			for (Entry<String, List<Object>> entry : newTableMap.entrySet()){
				Map<String, List<Object>> map = new HashMap<String, List<Object>>();
				map.put(entry.getKey(), entry.getValue());
				String sql = createTableSql(entry.getKey(), entry.getValue());
				jdbcTemplate.execute(sql);
				System.out.println("");
			}
		}
	}
	
	private static String createTableSql(String tableName,
			List<Object> columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("create table `" + tableName + "` (");
		for (int i=0;i<columns.size();i++ ) {
			if(i!=0){
				sb.append(",");
			}
			CreateTableParam colum = (CreateTableParam) columns.get(i);
			if (colum.getFileTypeLength() == 0) {
				sb.append(" `" + colum.getFieldName() + "` "
						+ colum.getFieldType());
			} else if (colum.getFileTypeLength() == 1) {
				sb.append(" `" + colum.getFieldName() + "` "
						+ colum.getFieldType() + "(" + colum.getFieldLength()
						+ ")");
			} else if (colum.getFileTypeLength() == 2) {
				sb.append(" `" + colum.getFieldName() + "` "
						+ colum.getFieldType() + "(" + colum.getFieldLength()
						+ "," + colum.getFieldDecimalLength() + ")");
			}
			if (colum.isFieldIsNull()) {
				sb.append(" NULL ");
			} else {
				sb.append(" NOT NULL ");
			}
			if (colum.isFieldIsAutoIncrement()) {// 是否自增长
				sb.append(" AUTO_INCREMENT ");
			}
			if (!colum.isFieldIsAutoIncrement()
					&& !"NULL".equals(colum.getFieldDefaultValue())) {
				sb.append(" DEFAULT " + colum.getFieldDefaultValue());
			}
			if(colum.isFieldIsKey()){
				sb.append(",PRIMARY KEY (`"+colum.getFieldName()+"`)");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	

	private static JdbcTemplate jdbcTemplate(MappedStatement ms) {
		DataSource dataSource = ms.getConfiguration().getEnvironment()
				.getDataSource();
		return new JdbcTemplate(dataSource);
	}

	public static boolean isNotExistTable(MappedStatement ms,
			String tableName) {
		JdbcTemplate jdbcTemplate = jdbcTemplate(ms);
//		String sql = "select * from SYSOBJECTS where name='" + tableName + "' ";
		String sql = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME='" + tableName + "' ";
		List list = jdbcTemplate.queryForList(sql);
		if (list != null && list.size() > 0) {
			return false;
		} else {
			return true;
		}
	}

}
