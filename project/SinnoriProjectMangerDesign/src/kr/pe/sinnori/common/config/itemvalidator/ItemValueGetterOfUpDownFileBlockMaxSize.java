package kr.pe.sinnori.common.config.itemvalidator;

import kr.pe.sinnori.common.config.AbstractItemValueGetter;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

/**
 * 파일 송수신 파일 블락 최대 크기 항목의 값 유효성 검사기
 * @author "Won Jonghoon"
 *
 */
public class ItemValueGetterOfUpDownFileBlockMaxSize extends AbstractItemValueGetter {
	private int min;
	private int max;
	
	/**
	 * 파일 송수신 파일 블락 최대 크기 항목의 값 유효성 검사기 생성자
	 * @param min 최소값, 주) 1024 배수 검사 없음
	 * @param max 최대값, 주) 1024 배수 검사 없음
	 * @throws ConfigValueInvalidException 최소값이 최대값 보다 클때 던지는 예외
	 */
	public ItemValueGetterOfUpDownFileBlockMaxSize(int min, int max)
			throws ConfigValueInvalidException {	
		if (min > max) {
			String errorMessage = new StringBuilder("parameter min[")
			.append(min)
			.append("] is greater than parameter max[")
			.append(max)
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		/*
		if (min % 1024 != 0) {
			String errorMessage = new StringBuilder("parameter min[")
			.append(min)
			.append("] is not a multiple of 1024").toString();
			throw new ConfigException(errorMessage);
		}
		
		if (max % 1024 != 0) {
			String errorMessage = new StringBuilder("parameter max[")
			.append(max)
			.append("] is not a multiple of 1024").toString();
			throw new ConfigException(errorMessage);
		}*/
		
		this.min = min;
		this.max = max;		
	}

	@Override
	public Object getItemValueWithValidation(String value) throws ConfigValueInvalidException {
		if (null == value) {
			String errorMessage = "parameter value is null";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (value.equals("")) {
			String errorMessage = "parameter value is empty";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		int nativeValue;
		try {
			nativeValue = Integer.parseInt(value);
		} catch(NumberFormatException e) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not integer type").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (nativeValue < min) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is less than min[")
			.append(min)
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (nativeValue > max) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is greater than max[")
			.append(max)
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (nativeValue % 1024 != 0) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not a multiple of 1024").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		return nativeValue;	
	}

	@Override
	public String toDescription() {
		return new StringBuilder("min[").append(min).append("], max[").append(max).append("]").toString();
	}
}
