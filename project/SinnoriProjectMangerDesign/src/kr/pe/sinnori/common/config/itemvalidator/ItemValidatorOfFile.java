package kr.pe.sinnori.common.config.itemvalidator;

import java.io.File;

import kr.pe.sinnori.common.config.AbstractItemValidator;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class ItemValidatorOfFile extends AbstractItemValidator {	
	@Override
	public Object validateItem(String value) throws ConfigValueInvalidException {
		if (null == value) {
			String errorMessage = "parameter value is null";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (value.equals("")) {
			String errorMessage = "parameter value is empty";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		File f = new File(value);
		
		if (!f.exists()) {
			String errorMessage = new StringBuilder("file[")
			.append(value)
			.append("] not exist").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (!f.isFile()) {
			String errorMessage = new StringBuilder("file[")
			.append(value)
			.append("] is not a normal file").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (!f.canRead()) {
			String errorMessage = new StringBuilder("can't read file[")
			.append(value)
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		return f;
	}

	@Override
	public String toDescription() {
		return null;
	}
}
