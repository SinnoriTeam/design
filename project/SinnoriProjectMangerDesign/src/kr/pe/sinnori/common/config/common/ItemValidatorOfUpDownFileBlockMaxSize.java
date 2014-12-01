package kr.pe.sinnori.common.config.common;

import kr.pe.sinnori.common.config.ItemValidator;
import kr.pe.sinnori.common.exception.ConfigException;

public class ItemValidatorOfUpDownFileBlockMaxSize extends ItemValidator {
	private int min;
	private int max;
	
	public ItemValidatorOfUpDownFileBlockMaxSize(String defaultValue, int min, int max)
			throws ConfigException {
		super(defaultValue);
		
		if (min > max) {
			String errorMessage = new StringBuilder("parameter min[")
			.append(min)
			.append("] is greater than parameter max[")
			.append(max)
			.append("]").toString();
			throw new ConfigException(errorMessage);
		}
		this.min = min;
		this.max = max;
	}

	@Override
	public Object validateItem(String value) throws ConfigException {
		if (null == value) {
			String errorMessage = "parameter value is null";
			throw new ConfigException(errorMessage);
		}
		
		if (value.equals("")) {
			String errorMessage = "parameter value is empty";
			throw new ConfigException(errorMessage);
		}
		
		int nativeValue;
		try {
			nativeValue = Integer.parseInt(value);
		} catch(NumberFormatException e) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not integer type").toString();
			throw new ConfigException(errorMessage);
		}
		
		if (nativeValue < min) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is less than min[")
			.append(min)
			.append("]").toString();
			throw new ConfigException(errorMessage);
		}
		
		if (nativeValue > max) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is greater than max[")
			.append(max)
			.append("]").toString();
			throw new ConfigException(errorMessage);
		}
		
		if (nativeValue % 1024 != 0) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not multiple of 1024").toString();
			throw new ConfigException(errorMessage);
		}
		
		return nativeValue;
	}

}
