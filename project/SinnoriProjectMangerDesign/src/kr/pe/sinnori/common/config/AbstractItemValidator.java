package kr.pe.sinnori.common.config;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public abstract class AbstractItemValidator {	
	public abstract Object validateItem(String value) throws ConfigValueInvalidException;	
	public abstract String toDescription();
}
