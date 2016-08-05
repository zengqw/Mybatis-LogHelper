package com.github.zengqw.exception;

/**
 * @author zqwn3535
 * 
 */
public class LogException extends Throwable {
	private static final long serialVersionUID = 1L;
	private String code = null;
	private String msg = null;
	private String data = null;

	public LogException(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public LogException(String code, String msg, String data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
