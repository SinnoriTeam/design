package kr.pe.sinnori.common.config.common;

import java.io.File;

import kr.pe.sinnori.common.config.ItemValidator;
import kr.pe.sinnori.common.exception.ConfigException;

public class ItemValidatorOfSessionKeyRSAKeypairPath extends ItemValidator {	

	public ItemValidatorOfSessionKeyRSAKeypairPath(String defaultValue) throws ConfigException {
		super(defaultValue);
	}

	@Override
	public Object validateItem(String value) throws ConfigException {
		if (null == value) {
			String errorMessage = "parameter value is null";
			throw new ConfigException(errorMessage);
		}
		
		if (value.equals("")) {
			String errorMessage = "parameter value is empty";
			throw new ConfigException(errorMessage);
		}
		
		File f = new File(value);
		
		if (!f.exists()) {
			String errorMessage = new StringBuilder("file[")
			.append(value)
			.append("] not exist").toString();
			throw new ConfigException(errorMessage);
		}
		
		if (!f.isDirectory()) {
			String errorMessage = new StringBuilder("file[")
			.append(value)
			.append("] is not directory").toString();
			throw new ConfigException(errorMessage);
		}
		
		if (!f.canRead()) {
			String errorMessage = new StringBuilder("can't read direcotry[")
			.append(value)
			.append("]").toString();
			throw new ConfigException(errorMessage);
		}
		
		return f;
	}

}
