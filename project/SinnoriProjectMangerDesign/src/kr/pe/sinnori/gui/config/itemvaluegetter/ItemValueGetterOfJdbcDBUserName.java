package kr.pe.sinnori.gui.config.itemvaluegetter;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.gui.config.AbstractItemValueGetter;

public class ItemValueGetterOfJdbcDBUserName extends AbstractItemValueGetter {

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
		
		return value;
	}

	@Override
	public String toDescription() {
		return null;
	}
}
