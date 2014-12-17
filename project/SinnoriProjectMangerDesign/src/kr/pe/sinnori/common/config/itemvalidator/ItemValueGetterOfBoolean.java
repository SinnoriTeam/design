package kr.pe.sinnori.common.config.itemvalidator;

import java.util.HashSet;
import java.util.Set;

import kr.pe.sinnori.common.config.AbstractItemValueGetter;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class ItemValueGetterOfBoolean extends AbstractItemValueGetter {	
	private Set<String> stringValueSet = new HashSet<String>();
	public ItemValueGetterOfBoolean() throws ConfigValueInvalidException {
		stringValueSet.add("true");
		stringValueSet.add("false");
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
		
		value = value.toLowerCase();
		
		if (! stringValueSet.contains(value)) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not a element of set[")
			.append(stringValueSet.toString())
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (value.equals("true")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toDescription() {
		return "single set {true, false}";
	}
}
