package kr.pe.sinnori.common.config.itemvalidator;

import java.nio.charset.Charset;

import kr.pe.sinnori.common.config.AbstractItemValueGetter;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class ItemValueGetterOfCharset extends AbstractItemValueGetter {

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
		Charset nativeValue = null;
		
		try {
			nativeValue = Charset.forName(value);
		} catch(Exception e) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is a bad charset name, errormessage=")
			.append(e.getMessage()).toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		return nativeValue;
	}

	@Override
	public String toDescription() {
		return null;
	}

}
