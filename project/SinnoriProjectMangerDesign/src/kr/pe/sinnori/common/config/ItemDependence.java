package kr.pe.sinnori.common.config;

import kr.pe.sinnori.common.exception.ConfigException;

public class ItemDependence {
	private String key;
	private ItemValidator itemChecker;
	private Object wantedNativeValue;
	
	public ItemDependence(String key, ItemValidator itemChecker, Object wantedNativeValue) {
		this.key = key;
		this.itemChecker = itemChecker;
		this.wantedNativeValue = wantedNativeValue;
	}
	
	public boolean isValidation(String value) throws ConfigException {
		Object nativeValue = itemChecker.validateItem(value);
		return nativeValue.equals(wantedNativeValue);
	}
	
	public String getKey() {
		return key;
	}
}
