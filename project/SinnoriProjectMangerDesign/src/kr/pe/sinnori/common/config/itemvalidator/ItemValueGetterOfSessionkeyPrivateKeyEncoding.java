package kr.pe.sinnori.common.config.itemvalidator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kr.pe.sinnori.common.config.AbstractItemValueGetter;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.common.lib.CommonType;

public class ItemValueGetterOfSessionkeyPrivateKeyEncoding extends AbstractItemValueGetter {
	private Set<String> stringValueSet = new HashSet<String>();

	public ItemValueGetterOfSessionkeyPrivateKeyEncoding()
			throws ConfigValueInvalidException {		
		stringValueSet.add("NONE");
		stringValueSet.add("BASE64");
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
		
		if (value.equals("NONE")) {
			return CommonType.SymmetricKeyEncoding.NONE;
		} else {
			return CommonType.SymmetricKeyEncoding.BASE64;
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
