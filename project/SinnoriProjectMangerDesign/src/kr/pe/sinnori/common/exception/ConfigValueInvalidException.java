package kr.pe.sinnori.common.exception;

@SuppressWarnings("serial")
public class ConfigValueInvalidException extends Exception {
	/**
	 * 생성자
	 * 
	 * @param errorMessage
	 *            에러 내용
	 */
	public ConfigValueInvalidException(String errorMessage) {
		super(errorMessage);
	}
}
