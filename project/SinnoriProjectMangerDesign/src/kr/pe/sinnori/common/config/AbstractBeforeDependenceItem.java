package kr.pe.sinnori.common.config;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public abstract class AbstractBeforeDependenceItem {
	protected String keyOfDependenceItem;
	protected AbstractItemValidator itemCheckerOfDependenceItem;
	protected Object wantedNativeValue;
	
	public AbstractBeforeDependenceItem(String keyOfDependenceItem, AbstractItemValidator itemCheckerOfDependenceItem, Object wantedNativeValue) {
		this.keyOfDependenceItem = keyOfDependenceItem;
		this.itemCheckerOfDependenceItem = itemCheckerOfDependenceItem;
		this.wantedNativeValue = wantedNativeValue;
	}
	
	public abstract boolean isValidation(String valueOfDependenceItem) throws ConfigValueInvalidException;
	
	public String getKeyOfDependenceItem() {
		return keyOfDependenceItem;
	}
}
