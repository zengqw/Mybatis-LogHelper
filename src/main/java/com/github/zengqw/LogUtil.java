package com.github.zengqw;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.springframework.jdbc.core.JdbcTemplate;

public class LogUtil {

	private static final ThreadLocal<LogBean> LOG_BEAN = new ThreadLocal<LogBean>();

	//是否需要建表,只请求一次
	private static boolean NEED_CREATED= true;
	
	public static LogBean getLogBean() {
		return LOG_BEAN.get();
	}

	public static void setLogBean(LogBean logBean) {
		LOG_BEAN.set(logBean);
	}

	public static void remove() {
		LOG_BEAN.remove();
	}
	
	public static void createTable(Invocation invocation){
		if(NEED_CREATED){
			MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
	        DataSource dataSource = ms.getConfiguration().getEnvironment().getDataSource();
	        JdbcTemplate  jdbcTemplate = new JdbcTemplate(dataSource);
	        jdbcTemplate.execute("select * from workcode");
	        
			NEED_CREATED=false;
		}
	}
	
	/**
	 * 加载插件配置属性
	 * @param p
	 */
	public static void setProperties(Properties p) {
		
		
	}

}
