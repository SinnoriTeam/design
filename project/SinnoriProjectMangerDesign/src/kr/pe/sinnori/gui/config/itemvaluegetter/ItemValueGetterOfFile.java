package kr.pe.sinnori.gui.config.itemvaluegetter;

import java.io.File;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.gui.config.AbstractItemValueGetter;

public class ItemValueGetterOfFile extends AbstractItemValueGetter {	
	@Override
	public Object getItemValueWithValidation(String value) throws ConfigValueInvalidException {
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
