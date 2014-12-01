package kr.pe.sinnori.common.config.common;

import kr.pe.sinnori.common.config.ItemValidator;
import kr.pe.sinnori.common.exception.ConfigException;

public class ItemValidatorOfJdbcDBUserPassword extends ItemValidator {

	public ItemValidatorOfJdbcDBUserPassword(String defaultValue) throws ConfigException {
		super(defaultValue);
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
		
		return value;
	}
}
