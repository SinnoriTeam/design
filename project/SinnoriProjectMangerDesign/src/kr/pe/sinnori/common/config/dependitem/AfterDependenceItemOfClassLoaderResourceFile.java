package kr.pe.sinnori.common.config.dependitem;

import java.io.File;

import kr.pe.sinnori.common.config.AbstractAfterDependenceItem;
import kr.pe.sinnori.common.config.AbstractItemValidator;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class AfterDependenceItemOfClassLoaderResourceFile extends AbstractAfterDependenceItem {

	public AfterDependenceItemOfClassLoaderResourceFile(String keyOfDependenceItem, AbstractItemValidator itemCheckerOfDependenceItem) {
		super(keyOfDependenceItem, itemCheckerOfDependenceItem);
	}

	@Override
	public void validate(String valueOfDependenceItem, Object paramNativeValue) throws ConfigValueInvalidException {
		if (null == valueOfDependenceItem) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)			
			.append("] errormessage=parameter valueOfDependenceItem is null").toString();			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (null == paramNativeValue) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)			
			.append("] errormessage=parameter nativeValue is null").toString();			
			throw new ConfigValueInvalidException(errorMessage);
		}

		if (!(paramNativeValue instanceof String)) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)			
			.append("] errormessage=parameter nativeValue type")
			.append(paramNativeValue.getClass().getName())
			.append("] is not a string type").toString();
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		String nativeValue = (String)paramNativeValue;
		
		String realFilePathString = null;
		if (nativeValue.startsWith("/")) {
			realFilePathString = new StringBuilder(valueOfDependenceItem)
			.append(nativeValue.replaceAll("/", File.separator)).toString();
		} else {
			realFilePathString = new StringBuilder(valueOfDependenceItem)
			.append(File.separator).append(nativeValue.replaceAll("/", File.separator)).toString();
		}
		
		File realFile = new File(realFilePathString);
		
		if (!realFile.exists()) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)			
			.append("] errormessage=real classloader resource file path[")
			.append(nativeValue)
			.append("][")
			.append(realFilePathString)
			.append("] not exist").toString();
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (!realFile.isFile()) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)			
			.append("] errormessage=real classloader resource file path[")
			.append(nativeValue)
			.append("][")
			.append(realFilePathString)
			.append("] cannot be a normal file").toString();
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (!realFile.canRead()) {
			String errorMessage = new StringBuilder("depence item key[")
			.append(keyOfDependenceItem)			
			.append("] errormessage=real classloader resource file path[")
			.append(nativeValue)
			.append("][")
			.append(realFilePathString)
			.append("] cannot be read").toString();
			
			throw new ConfigValueInvalidException(errorMessage);
		}		
	}
}
