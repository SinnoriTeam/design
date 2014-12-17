package kr.pe.sinnori.common.config.itemvalidator;

import kr.pe.sinnori.common.config.AbstractItemValueGetter;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class ItemValueGetterOfNoCheck extends AbstractItemValueGetter {

	@Override
	public Object getItemValueWithValidation(String value) throws ConfigValueInvalidException {
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
