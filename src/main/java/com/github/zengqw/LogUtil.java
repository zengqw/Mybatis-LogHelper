package com.github.zengqw;

public class LogUtil {

	private static final ThreadLocal<LogBean> LOG_BEAN = new ThreadLocal<LogBean>();

	public static LogBean getLogBean() {
		return LOG_BEAN.get();
	}

	public static void setLogBean(LogBean logBean) {
		LOG_BEAN.set(logBean);
	}

	public static void remove() {
		LOG_BEAN.remove();
	}

}
