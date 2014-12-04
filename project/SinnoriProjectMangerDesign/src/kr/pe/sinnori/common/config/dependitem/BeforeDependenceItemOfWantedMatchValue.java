package kr.pe.sinnori.common.config.dependitem;

import kr.pe.sinnori.common.config.AbstractBeforeDependenceItem;
import kr.pe.sinnori.common.config.AbstractItemValidator;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class BeforeDependenceItemOfWantedMatchValue extends AbstractBeforeDependenceItem {	
	public BeforeDependenceItemOfWantedMatchValue(String keyOfDependenceItem, AbstractItemValidator itemCheckerOfDependenceItem, Object wantedNativeValue) {
		super(keyOfDependenceItem, itemCheckerOfDependenceItem, wantedNativeValue);
	}
	
	public boolean isValidation(String valueOfDependenceItem) throws ConfigValueInvalidException {
		Object nativeValueOfDependenceItem = null;
		try {
			nativeValueOfDependenceItem = itemCheckerOfDependenceItem.validateItem(valueOfDependenceItem);
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
		return nativeValueOfDependenceItem.equals(wantedNativeValue);
	}	
}
