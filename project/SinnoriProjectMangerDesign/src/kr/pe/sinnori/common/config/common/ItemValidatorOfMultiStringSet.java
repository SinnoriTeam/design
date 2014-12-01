package kr.pe.sinnori.common.config.common;

import java.util.HashSet;
import java.util.Set;

import kr.pe.sinnori.common.config.ItemValidator;
import kr.pe.sinnori.common.exception.ConfigException;

/**
 * jdbc connection url 항목 유효성 검사기  
 * @author "Won Jonghoon"
 *
 */
public class ItemValidatorOfMultiStringSet extends ItemValidator {
	private Set<String> stringValueSet = new HashSet<String>();
	
	public ItemValidatorOfMultiStringSet(String defaultValue, String ... parmValueSet) throws ConfigException {
		super(defaultValue);
		
		if (parmValueSet.length == 0) {
			String errorMessage = "parameter parmValueSet is empty";
			throw new ConfigException(errorMessage);
		}
		
		for (String value : parmValueSet) {
			stringValueSet.add(value);
		}
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
		
		String tokens[] = value.split("\\s*,\\s*");
		for (String token : tokens) {
			if (! stringValueSet.contains(token)) {
				String errorMessage = new StringBuilder("parameter value[")
				.append(value)
				.append("] do not consist of the elememets of multi set[")
				.append(stringValueSet.toString())
				.append("]").toString();
				throw new ConfigException(errorMessage);
			}
		}
		
		return value;
	}
}
