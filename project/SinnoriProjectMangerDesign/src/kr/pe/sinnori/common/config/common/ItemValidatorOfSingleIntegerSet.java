package kr.pe.sinnori.common.config.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kr.pe.sinnori.common.config.ItemValidator;
import kr.pe.sinnori.common.exception.ConfigException;

/**
 * jdbc connection url 항목 유효성 검사기  
 * @author "Won Jonghoon"
 *
 */
public class ItemValidatorOfSingleIntegerSet extends ItemValidator {
	private Set<String> stringValueSet = new HashSet<String>();
	public ItemValidatorOfSingleIntegerSet(String defaultValue, String ... parmValueSet) throws ConfigException {
		super(defaultValue);

		if (parmValueSet.length == 0) {
			String errorMessage = "parameter parmValueSet is empty";
			throw new ConfigException(errorMessage);
		}
		
		for (String value : parmValueSet) {
			try {
				Integer.parseInt(value);
			} catch(NumberFormatException e) {
				String errorMessage = new StringBuilder("the elemment[")
				.append(value)
				.append("] of parmValueSet is not integer type").toString();
				throw new ConfigException(errorMessage);
			}
			
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
		
		if (! stringValueSet.contains(value)) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not a element of set[")
			.append(stringValueSet.toString())
			.append("]").toString();
			throw new ConfigException(errorMessage);
		}
		
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not integer type").toString();
			throw new ConfigException(errorMessage);
		}
	}
	
	public String getStringValueSet() {
		StringBuilder strBuilder = new StringBuilder();
		Iterator<String> iter = stringValueSet.iterator();
		if (iter.hasNext()) {
			strBuilder.append(iter.next());
		}
		
		while (iter.hasNext()) {
			strBuilder.append(", ");
			strBuilder.append(iter.next());
		}
		return strBuilder.toString();
	}
}
