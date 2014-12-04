package kr.pe.sinnori.common.config.itemvalidator;

import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

import kr.pe.sinnori.common.config.AbstractItemValidator;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class ItemValidatorOfByteOrder extends AbstractItemValidator {
	private Set<String> stringValueSet = new HashSet<String>();
	
	public ItemValidatorOfByteOrder() throws ConfigValueInvalidException {		
		stringValueSet.add("LITTLE_ENDIAN");
		stringValueSet.add("BIG_ENDIAN");		
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
		
		if (! stringValueSet.contains(value)) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not a element of set[")
			.append(stringValueSet.toString())
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		ByteOrder nativeValue = null;
		
		if (value.equals("LITTLE_ENDIAN")) {
			nativeValue = ByteOrder.LITTLE_ENDIAN;
		} else {
			nativeValue = ByteOrder.BIG_ENDIAN;
		}
		
		return nativeValue;
	}

	@Override
	public String toDescription() {
		return "single set {LITTLE_ENDIAN, BIG_ENDIAN}";
	}

}
