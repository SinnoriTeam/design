package kr.pe.sinnori.common.config;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public abstract class AbstractAfterDependenceItem {
	protected String keyOfDependenceItem;
	protected AbstractItemValidator itemCheckerOfDependenceItem;
	
	public AbstractAfterDependenceItem(String keyOfDependenceItem, AbstractItemValidator itemCheckerOfDependenceItem) {
		this.keyOfDependenceItem = keyOfDependenceItem;
		this.itemCheckerOfDependenceItem = itemCheckerOfDependenceItem;
	}
	
	public abstract void validate(String valueOfDependenceItem, Object nativeValue) throws ConfigValueInvalidException;
	
	public String getKeyOfDependenceItem() {
		return keyOfDependenceItem;
	}
}
