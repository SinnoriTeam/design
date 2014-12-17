package kr.pe.sinnori.common.config.itemvalidator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kr.pe.sinnori.common.config.AbstractItemValueGetter;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

/**
 * jdbc connection url 항목 유효성 검사기  
 * @author "Won Jonghoon"
 *
 */
public class ItemValueGetterOfSingleIntegerSet extends AbstractItemValueGetter {
	private Set<String> stringValueSet = new HashSet<String>();
	public ItemValueGetterOfSingleIntegerSet(String ... parmValueSet) throws ConfigValueInvalidException {
		if (parmValueSet.length == 0) {
			String errorMessage = "parameter parmValueSet is empty";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		for (String value : parmValueSet) {
			try {
				Integer.parseInt(value);
			} catch(NumberFormatException e) {
				String errorMessage = new StringBuilder("the elemment[")
				.append(value)
				.append("] of parmValueSet is not integer type").toString();
				throw new ConfigValueInvalidException(errorMessage);
			}
			
			stringValueSet.add(value);
		}
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
		
		if (! stringValueSet.contains(value)) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not a element of set[")
			.append(stringValueSet.toString())
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not integer type").toString();
			throw new ConfigValueInvalidException(errorMessage);
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

	@Override
	public String toDescription() {
		StringBuilder descriptionBuilder = new StringBuilder("single set {");
		Iterator<String> iter =  stringValueSet.iterator();
		if (iter.hasNext()) descriptionBuilder.append(iter.next());
		while (iter.hasNext()) {
			descriptionBuilder.append(", ");
			descriptionBuilder.append(iter.next());
		}
		descriptionBuilder.append("}");
		return descriptionBuilder.toString();
	}
}
