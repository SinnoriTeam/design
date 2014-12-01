package kr.pe.sinnori.common.config.common;

import kr.pe.sinnori.common.config.ItemValidator;
import kr.pe.sinnori.common.exception.ConfigException;

/**
 * jdbc connection url 항목 유효성 검사기  
 * @author "Won Jonghoon"
 *
 */
public class ItemValidatorOfNoNullAndEmptyString extends ItemValidator {
	public ItemValidatorOfNoNullAndEmptyString(String defaultValue)
			throws ConfigException {
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
