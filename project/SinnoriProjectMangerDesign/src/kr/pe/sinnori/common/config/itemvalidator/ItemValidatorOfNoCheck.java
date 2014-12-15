package kr.pe.sinnori.common.config.itemvalidator;

import kr.pe.sinnori.common.config.AbstractItemValidator;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class ItemValidatorOfNoCheck extends AbstractItemValidator {

	@Override
	public Object validateItem(String value) throws ConfigValueInvalidException {
		if (null == value) {
			String errorMessage = "parameter value is null";
			throw new ConfigValueInvalidException(errorMessage);
		}
		return value;
	}

	@Override
	public String toDescription() {
		return null;
	}	

}
