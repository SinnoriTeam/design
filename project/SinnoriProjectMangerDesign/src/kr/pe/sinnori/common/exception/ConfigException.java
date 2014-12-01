package kr.pe.sinnori.common.exception;

@SuppressWarnings("serial")
public class ConfigException extends Exception {
	/**
	 * 생성자
	 * 
	 * @param errorMessage
	 *            에러 내용
	 */
	public ConfigException(String errorMessage) {
		super(errorMessage);
	}
}
