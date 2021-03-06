package kr.pe.sinnori.gui.config.itemvaluegetter;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.gui.config.AbstractItemValueGetter;

public class ItemValueGetterOfMinMaxInteger extends AbstractItemValueGetter {
	private int min;
	private int max;
	
	public ItemValueGetterOfMinMaxInteger(int min, int max)
			throws ConfigValueInvalidException {	
		if (min > max) {
			String errorMessage = new StringBuilder("parameter min[")
			.append(min)
			.append("] is greater than parameter max[")
			.append(max)
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}		
		this.min = min;
		this.max = max;		
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
		
		int nativeValue;
		try {
			nativeValue = Integer.parseInt(value);
		} catch(NumberFormatException e) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not integer type").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (nativeValue < min) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is less than min[")
			.append(min)
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (nativeValue > max) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is greater than max[")
			.append(max)
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		return nativeValue;	
	}

	@Override
	public String toDescription() {
		return new StringBuilder("min[").append(min).append("], max[").append(max).append("]").toString();
	}
}
