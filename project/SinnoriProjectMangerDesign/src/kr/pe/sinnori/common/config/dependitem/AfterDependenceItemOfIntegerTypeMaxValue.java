package kr.pe.sinnori.common.config.dependitem;

import kr.pe.sinnori.common.config.AbstractAfterDependenceItem;
import kr.pe.sinnori.common.config.AbstractItemValidator;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class AfterDependenceItemOfIntegerTypeMaxValue extends AbstractAfterDependenceItem {

	public AfterDependenceItemOfIntegerTypeMaxValue(String keyOfDependenceItem, AbstractItemValidator itemCheckerOfDependenceItem) {
		super(keyOfDependenceItem, itemCheckerOfDependenceItem);
	}

	@Override
	public void validate(String valueOfDependenceItem, Object paramNativeValue) throws ConfigValueInvalidException {
		if (null == paramNativeValue) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)			
			.append("] errormessage=parameter nativeValue is null").toString();			
			throw new ConfigValueInvalidException(errorMessage);
		}
		if (!(paramNativeValue instanceof Integer)) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)			
			.append("] errormessage=parameter nativeValue type")
			.append(paramNativeValue.getClass().getName())
			.append("] is not a integer type").toString();
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		int nativeValue = (Integer)paramNativeValue;
		
		int nativeValueOfDependenceItem;
		try {
			Object tempNativeValueOfDependenceItem = itemCheckerOfDependenceItem.validateItem(valueOfDependenceItem);
			if (!(tempNativeValueOfDependenceItem instanceof Integer)) {
				String errorMessage = new StringBuilder("depence item key[")
				.append(keyOfDependenceItem)			
				.append("] errormessage=value type")
				.append(tempNativeValueOfDependenceItem.getClass().getName())
				.append("] is not integer").toString();
				
				throw new ConfigValueInvalidException(errorMessage);
			}
			nativeValueOfDependenceItem = (Integer)tempNativeValueOfDependenceItem;
		} catch (ConfigValueInvalidException e) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)			
			.append("] errormessage=")
			.append(e.getMessage())
			.append("]").toString();
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (nativeValue > nativeValueOfDependenceItem) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)
			.append("] value[")
			.append(nativeValueOfDependenceItem)
			.append("] is a max value but is less than value[")
			.append(nativeValue)
			.append("]").toString();
			
			throw new ConfigValueInvalidException(errorMessage);
		}
	}

}
