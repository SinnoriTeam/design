package kr.pe.sinnori.common.config.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kr.pe.sinnori.common.config.ItemValidator;
import kr.pe.sinnori.common.exception.ConfigException;
import kr.pe.sinnori.common.lib.CommonType;

public class ItemValidatorOfSessionkeyPrivateKeyEncoding extends ItemValidator {
	private Set<String> stringValueSet = new HashSet<String>();

	public ItemValidatorOfSessionkeyPrivateKeyEncoding(String defaultValue)
			throws ConfigException {
		super(defaultValue);
		
		stringValueSet.add("NONE");
		stringValueSet.add("BASE64");
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

}
