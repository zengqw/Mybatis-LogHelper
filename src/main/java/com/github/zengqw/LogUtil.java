package com.github.zengqw;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.zengqw.store.CreateTableParam;
import com.github.zengqw.store.annotation.Column;
import com.github.zengqw.store.annotation.LengthCount;
import com.github.zengqw.store.annotation.Table;
import com.github.zengqw.store.util.ClassTools;
import com.github.zengqw.store.util.MySqlTypeConstant;
import com.github.zengqw.store.util.SQLUtil;

public class LogUtil extends Constants {

	private static final ThreadLocal<LogBean> LOG_BEAN = new ThreadLocal<LogBean>();
	private static ReentrantLock lock = new ReentrantLock();
	// 是否需要建表,服务器启动的时候请求一次,之后再不需要建表了
	private static boolean NEED_CREATED = true;

	private static boolean createTable = false;
	// 要扫描的model所在的pack
	private static String pack;

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
				// -------------获取数据源-----------------
				MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
				Map<String, List<Object>> newTableMap = newTableMap(ms);
				
				SQLUtil.createTable(ms, newTableMap);
				System.out.println("");
//				NEED_CREATED = false;
			} finally {
				lock.unlock();
			}
		}
	}



	private Map<String, List<Object>> newTableMap(MappedStatement ms) {
		Map<String, Object> mySqlTypeAndLengthMap = mySqlTypeAndLengthMap();
		Set<Class<?>> classes = ClassTools.getClasses(pack);
		// 用于存需要创建的表名+结构
		Map<String, List<Object>> newTableMap = new HashMap<String, List<Object>>();
		for (Class<?> clas : classes) {
			Table table = clas.getAnnotation(Table.class);

			if (createTable) {
				SQLUtil.dropTable(ms, table.name());
			}

			// 用于存新增表的字段
			List<Object> newFieldList = new ArrayList<Object>();
			tableFieldsConstruct(mySqlTypeAndLengthMap, clas, newFieldList);
			if (SQLUtil.isNotExistTable(ms, table.name())) {
				newTableMap.put(table.name(), newFieldList);
			}
		}

		return newTableMap;

	}

	/**
	 * 迭代出所有model的所有fields存到newFieldList中
	 * 
	 * @param mySqlTypeAndLengthMap
	 *            mysql数据类型和对应几个长度的map
	 * @param clas
	 *            准备做为创建表依据的class
	 * @param newFieldList
	 */
	private void tableFieldsConstruct(Map<String, Object> mySqlTypeAndLengthMap,
			Class<?> clas, List<Object> newFieldList) {
		Field[] fields = clas.getDeclaredFields();

		for (Field field : fields) {
			// 判断方法中是否有指定注解类型的注解
			boolean hasAnnotation = field.isAnnotationPresent(Column.class);
			if (hasAnnotation) {
				// 根据注解类型返回方法的指定类型注解
				Column column = field.getAnnotation(Column.class);
				CreateTableParam param = new CreateTableParam();
				param.setFieldName(column.name());
				param.setFieldType(column.type().toLowerCase());
				param.setFieldLength(column.length());
				param.setFieldDecimalLength(column.decimalLength());
				param.setFieldIsNull(column.isNull());
				param.setFieldIsKey(column.isKey());
				param.setFieldIsAutoIncrement(column.isAutoIncrement());
				param.setFieldDefaultValue(column.defaultValue());
				int length = (Integer) mySqlTypeAndLengthMap.get(column.type());
				param.setFileTypeLength(length);
				newFieldList.add(param);
			}
		}
	}

	/**
	 * 获取Mysql的类型，以及类型需要设置几个长度，这里构建成map的样式
	 * 构建Map(字段名(小写),需要设置几个长度(0表示不需要设置，1表示需要设置一个，2表示需要设置两个))
	 */
	private Map<String, Object> mySqlTypeAndLengthMap() {
		Field[] fields = MySqlTypeConstant.class.getDeclaredFields();
		Map<String, Object> map = new HashMap<String, Object>();
		for (Field field : fields) {
			LengthCount lengthCount = field.getAnnotation(LengthCount.class);
			map.put(field.getName().toLowerCase(), lengthCount.LengthCount());
		}
		return map;
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
		pack = p.getProperty("pack");

	}
}
