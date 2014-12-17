package kr.pe.sinnori.common.config;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public abstract class AbstractItemValueGetter {	
	public abstract Object getItemValueWithValidation(String value) throws ConfigValueInvalidException;	
	public abstract String toDescription();
}
