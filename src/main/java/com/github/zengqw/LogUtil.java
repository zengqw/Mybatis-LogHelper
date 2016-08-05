package com.github.zengqw;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.springframework.jdbc.core.JdbcTemplate;

public class LogUtil extends Constants {

	private static final ThreadLocal<LogBean> LOG_BEAN = new ThreadLocal<LogBean>();
	private static ReentrantLock lock = new ReentrantLock();
	// 是否需要建表,只请求一次
	private static boolean NEED_CREATED = true;

	private static boolean createTable = false;

	public static LogBean getLogBean() {
		return LOG_BEAN.get();
	}

	private Invocation invocation;

	public LogUtil(Invocation invocation) {
		this.invocation = invocation;
	}

	public static void setLogBean(LogBean logBean) {
		LOG_BEAN.set(logBean);
	}

	public static void remove() {
		LOG_BEAN.remove();
	}

	public void createTable() {
		if (NEED_CREATED) {
			try { // 这里需要加锁,否则会两次创建表
				lock.lock();
				MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
				DataSource dataSource = ms.getConfiguration().getEnvironment()
						.getDataSource();
				JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
				String sql = "select * from SYSOBJECTS where name='"
						+ LOG_TABLE_NAME + "' ";
				List list = jdbcTemplate.queryForList(sql);
				if (list != null && list.size() > 0) {
					if (createTable) {

					}
				}
				NEED_CREATED = false;
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * 加载插件配置属性
	 * 
	 * @param p
	 */
	public static void setProperties(Properties p) {
		// 默认是false 表示 只更新日志数据库表，不会破坏原有的日志数据
		// 如果为true 则会删除原有的日志表，再新建
		createTable = Boolean.parseBoolean(p.getProperty("createTable"));

	}

}
