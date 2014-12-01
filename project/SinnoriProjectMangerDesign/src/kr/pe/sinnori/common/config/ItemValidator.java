package kr.pe.sinnori.common.config;

import kr.pe.sinnori.common.exception.ConfigException;

public abstract class ItemValidator {
	protected String defaultValue;
	
	public ItemValidator(String defaultValue) throws ConfigException {
		this.defaultValue = defaultValue;
	}
	public abstract Object validateItem(String value) throws ConfigException;
	
	public String getDefaultValue() {
		return defaultValue;
	}
}
