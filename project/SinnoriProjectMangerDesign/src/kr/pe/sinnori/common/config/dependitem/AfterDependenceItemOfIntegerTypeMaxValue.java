package kr.pe.sinnori.common.config.dependitem;

import kr.pe.sinnori.common.config.AbstractAfterDependenceItem;
import kr.pe.sinnori.common.config.AbstractItemValidator;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class AfterDependenceItemOfIntegerTypeMaxValue extends AbstractAfterDependenceItem {

	public AfterDependenceItemOfIntegerTypeMaxValue(String keyOfDependenceItem, AbstractItemValidator itemCheckerOfDependenceItem) {
		super(keyOfDependenceItem, itemCheckerOfDependenceItem);
	}

	@Override
	public void validate(String valueOfDependenceItem, Object nativeValue) throws ConfigValueInvalidException {
		Integer nativeValueOfDependenceItem = null;
		try {
			nativeValueOfDependenceItem = (Integer)itemCheckerOfDependenceItem.validateItem(valueOfDependenceItem);
		} catch (ConfigValueInvalidException e) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)
			.append("] value[")
			.append(nativeValueOfDependenceItem)
			.append("] errormessage=")
			.append(e.getMessage())
			.append("]").toString();
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if ((Integer)nativeValue > nativeValueOfDependenceItem) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)
			.append("] value[")
			.append(nativeValueOfDependenceItem)
			.append("] is a max value but is less than value[")
			.append((Integer)nativeValue)
			.append("]").toString();
			
			throw new ConfigValueInvalidException(errorMessage);
		}
	}

}
