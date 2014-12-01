package kr.pe.sinnori.common.config.common;

import java.util.HashSet;
import java.util.Set;

import kr.pe.sinnori.common.config.ItemValidator;
import kr.pe.sinnori.common.exception.ConfigException;

public class ItemValidatorOfJDFServletTrace extends ItemValidator {
	private Set<String> stringValueSet = new HashSet<String>();
	public ItemValidatorOfJDFServletTrace(String defaultValue) throws ConfigException {
		super(defaultValue);

		stringValueSet.add("true");
		stringValueSet.add("false");
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
		
		value = value.toLowerCase();
		
		if (! stringValueSet.contains(value)) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not a element of set[")
			.append(stringValueSet.toString())
			.append("]").toString();
			throw new ConfigException(errorMessage);
		}
		
		if (value.equals("true")) {
			return true;
		} else {
			return false;
		}
	}
}
