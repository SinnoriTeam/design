package kr.pe.sinnori.gui.config.itemvaluegetter;

import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.gui.config.AbstractItemValueGetter;
import kr.pe.sinnori.gui.config.SingleSetValueGetterIF;

public class ItemValueGetterOfByteOrder extends AbstractItemValueGetter implements SingleSetValueGetterIF {
	private Set<String> stringValueSet = new HashSet<String>();
	
	public ItemValueGetterOfByteOrder() throws ConfigValueInvalidException {		
		stringValueSet.add("LITTLE_ENDIAN");
		stringValueSet.add("BIG_ENDIAN");		
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

	@Override
	public Set<String> getStringTypeValueSet() {
		return stringValueSet;
	}

}
