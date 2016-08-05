package com.github.zengqw;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

@Intercepts({ @Signature(type = Executor.class, method = "update", args = {
		MappedStatement.class, Object.class }) })
public class LogHelper implements Interceptor {
	// 需要记录日志的操作表
	private static String[] TABLES = { "WorkcodeUser", "WorkcodeKey",
			"WorkcodeRules", "Workcode","WorkcodeRetreat" };

	private static final String INSERT_LOG_MAPPER_ID = "com.netease.workcode.mapper.WorkcodeLogMapper.insertSelective";
	// 只属于 update 操作的 标识,强行不记录日志,默认false
	private static final ThreadLocal<Boolean> NOT_LOG = new ThreadLocal<Boolean>();

	public static void setNotLog(Boolean notLog) {
		NOT_LOG.set(notLog);
	}
	
	private String opreator()  {
		return LogUtil.getLogBean().getOpreator();
	}
	
	private String ip()  {
		return LogUtil.getLogBean().getIp();
	}
	
	public Object intercept(Invocation invocation) throws Throwable {
		try {
			Boolean notLog = NOT_LOG.get();
			notLog = notLog == null ? false : notLog;
			if (notLog) {// 不需要记录日志
				return invocation.proceed();
			}
			return invocation.proceed();
//			return interceptLog(invocation);
		} finally {// NOT_LOG 标识只能用一次，执行完操作 之后清除掉
			NOT_LOG.remove();
		}
	}

//	/**
//	 * 构建 日志bean
//	 * @param invocation
//	 * @return
//	 * @throws Throwable
//	 */
//	private Object interceptLog(Invocation invocation) throws Throwable {
//		Object[] args = invocation.getArgs();
//		invocation.getTarget();
//		// 保存参数
//		MappedStatement ms = (MappedStatement) args[0];
//		Object arg1 = args[1];
//		Configuration c = ms.getConfiguration();
//		WorkcodeLog log = new WorkcodeLog();
//
//		if (checkLog(ms.getId(), log)) { // 检查是否该 记录日志,顺便把tablename拿到
//			return invocation.proceed();
//		}
//		log = initLog(log, ms, args);
//		args[0] = c.getMappedStatement(INSERT_LOG_MAPPER_ID);
//		args[1] = log;
//		invocation.proceed();
//		// 执行完log的ms 替换回原先的ms
//		args[0] = ms;
//		args[1] = arg1;
//		return invocation.proceed();
//	}
//
//	private WorkcodeLog initLog(WorkcodeLog log, MappedStatement ms,
//			Object[] args) throws Throwable {
//		Configuration c = ms.getConfiguration();
//		log.setSaveTime(new Date());
//		String api = getApiNmae();
//		if (api.startsWith(Constants.IS_API)) {
//			log.setType("1");
//			log.setName(LogUtil.getOsFrom());
//		} else {
//			log.setType("0");
//			log.setName(""+LogUtil.userId());
//		}
//		log.setApi(api);
//		log.setIp(getIp());
//		BoundSql boundSql = ms.getBoundSql(args[1]);
//		Object param = boundSql.getParameterObject();
//		MetaObject metaObject = c.newMetaObject(param);
//		log.setParam(param(metaObject.getGetterNames(), metaObject));
//		log.setOperate(ms.getSqlCommandType().toString());
//		return log;
//	}
//
//	private String keyFromExample(String key, Example ex) {
//		Iterator it = ex.getOredCriteria().iterator();
//		while (it.hasNext()) {
//			Example.Criteria o = (Criteria) it.next();
//			List<Example.Criterion> list = o.getAllCriteria();
//			for (Example.Criterion temp : list) {
//				if (temp.getCondition().equals(key.toUpperCase() + " =")) {
//					return "" + temp.getValue();
//				}
//			}
//		}
//		return "";
//	}
//
//	private String fromExample(Example ex) {
//		StringBuilder sb = new StringBuilder();
//		Iterator it = ex.getOredCriteria().iterator();
//		while (it.hasNext()) {
//			Example.Criteria o = (Criteria) it.next();
//			List<Example.Criterion> list = o.getAllCriteria();
//			for (Example.Criterion temp : list) {
//				sb.append(temp.getCondition()).append(temp.getValue());
//			}
//		}
//		return sb.toString();
//	}
//
//	private String recordId(String[] keys, MetaObject metaObject) {
//		String recordId = "";
//		if (metaObject.hasGetter("example")) {// 如果是example的更新或删除 需要解析example对象
//			Example ex = ((Example) metaObject.getValue("example"));
//			if (keys.length == 1) {
//				return "" + keyFromExample(keys[0], ex);
//			}
//			for (String key : keys) {
//				recordId += key + "=" + keyFromExample(keys[0], ex) + ",";
//			}
//		} else {
//			if (keys.length == 1) {
//				return "" + metaObject.getValue(keys[0]);
//			}
//			for (String key : keys) {
//				recordId += key + "=" + metaObject.getValue(key) + ",";
//			}
//		}
//		if (recordId.endsWith(",")) {
//			recordId = recordId.substring(0, recordId.length() - 1);
//		}
//		return recordId;
//	}
//
//	private String refilect(Object obj)
//			throws IllegalArgumentException, IllegalAccessException,
//			InvocationTargetException, NoSuchMethodException {
//		StringBuilder sb = new StringBuilder();
//		Field[] fields = obj.getClass().getDeclaredFields();
//		for (Field field : fields) {
//			field.setAccessible(true);// 设置访问权限
////			System.out.println("field.getName() is" + field.getName());
//			if("serialVersionUID".equals(field.getName())){
//				continue;
//			}
//			sb.append(field.getName() + "="
//					+ BeanUtils.getProperty(obj, field.getName()) + ",");
//		}
//		return sb.toString();
//	}
//
//	private String param(String[] names, MetaObject metaObject)
//			throws IllegalArgumentException, IllegalAccessException,
//			InvocationTargetException, NoSuchMethodException {
//		String remark = "";
//		if (metaObject.hasGetter("example")) {
//			Example ex = ((Example) metaObject.getValue("example"));
//			Object vo = metaObject.getValue("param1" + "");
//			remark += refilect(vo);
//			remark += fromExample(ex);
//		} else {
//			for (String name : names) {
//				remark += name + "=" + metaObject.getValue(name) + ",";
//			}
//
//		}
//		if (remark.endsWith(",")) {
//			remark = remark.substring(0, remark.length() - 1);
//		}
//		return remark;
//	}
//
//
//	private boolean checkLog(String mapperId, WorkcodeLog log) {
//		Pattern pattern = Pattern
//				.compile("^com.netease.workcode.mapper.[A-Za-z]+Mapper");
//		Matcher matcher = pattern.matcher(mapperId);
//		if (matcher.find()) {
//			String temp = matcher.group(0);
//			temp = temp.substring(28, temp.length() - 6);
//			if (Arrays.asList(TABLES).contains(temp)) {
//				log.setTableName(temp);
//				return false;
//			}
//		}
//		return true;
//	}

	public Object plugin(Object target) {
		if (target instanceof Executor) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	public void setProperties(Properties properties) {
	}

}
