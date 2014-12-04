package kr.pe.sinnori.common.config.itemvalidator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kr.pe.sinnori.common.config.AbstractItemValidator;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

/**
 * jdbc connection url 항목 유효성 검사기  
 * @author "Won Jonghoon"
 *
 */
public class ItemValidatorOfMultiStringSet extends AbstractItemValidator {
	private Set<String> stringValueSet = new HashSet<String>();
	
	public ItemValidatorOfMultiStringSet(String ... parmValueSet) throws ConfigValueInvalidException {		
		if (parmValueSet.length == 0) {
			String errorMessage = "parameter parmValueSet is empty";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		for (String value : parmValueSet) {
			stringValueSet.add(value);
		}
	}
	
	@Override
	public Object validateItem(String value) throws ConfigValueInvalidException {
		if (null == value) {
			String errorMessage = "parameter value is null";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (value.equals("")) {
			String errorMessage = "parameter value is empty";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		String tokens[] = value.split("\\s*,\\s*");
		for (String token : tokens) {
			if (! stringValueSet.contains(token)) {
				String errorMessage = new StringBuilder("parameter value[")
				.append(value)
				.append("] do not consist of the elememets of multi set[")
				.append(stringValueSet.toString())
				.append("]").toString();
				throw new ConfigValueInvalidException(errorMessage);
			}
		}
		
		return value;
	}
	
	@Override
	public String toDescription() {
		StringBuilder descriptionBuilder = new StringBuilder("multi set {");
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
