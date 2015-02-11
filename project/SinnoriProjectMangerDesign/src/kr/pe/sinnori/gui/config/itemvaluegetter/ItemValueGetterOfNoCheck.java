package kr.pe.sinnori.gui.config.itemvaluegetter;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.gui.config.AbstractItemValueGetter;

public class ItemValueGetterOfNoCheck extends AbstractItemValueGetter {

	@Override
	public Object getItemValueWithValidation(String value) throws ConfigValueInvalidException {
		if (null == value) {
			String errorMessage = "parameter value is null";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		String trimValue = value.trim();
		if (!trimValue.equals(value)) {
			String errorMessage = "parameter value needs to call String.trim() method";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		return value;
	}

	@Override
	public String toDescription() {
		return null;
	}	

}
