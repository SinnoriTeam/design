package kr.pe.sinnori.gui.config.dependencebreaker;

import java.io.File;
import java.util.Properties;

import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.gui.config.AbstractDependenceBreaker;
import kr.pe.sinnori.gui.config.SinnoriConfigInfo;

public class ClassLoaderResourceFileDependenceBreaker extends AbstractDependenceBreaker {

	public ClassLoaderResourceFileDependenceBreaker(
			String targetItemID, 			
			String dependenceItemID,
			SinnoriConfigInfo sinnoriConfigInfo) throws ConfigErrorException {
		super(targetItemID, dependenceItemID, sinnoriConfigInfo);
	}

	@Override
	public void validate(Properties sourceProperties, 
			String targetItemKey, String dependenceItemKey) throws ConfigValueInvalidException {
		if (null == sourceProperties) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=parameter sourceProperties is null").toString();
			log.warn(errorMessage);			
			throw new ConfigValueInvalidException(errorMessage);
		}
		if (null == targetItemKey) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=parameter targetItemKey is null").toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (null == dependenceItemKey) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=parameter dependenceItemKey is null").toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
				
		
		String classloaderResourceFileRelativePathString = null;
		try {
			String targetItemValue = sourceProperties.getProperty(targetItemKey);
			Object valueObject = 
					targetItemValueGetter.
					getItemValueWithValidation(targetItemValue);			
			
			if (!(valueObject instanceof String)) {
				String errorMessage = new StringBuilder("targetItemID[")
				.append(this.targetItemID)
				.append("] dependenceItemID=[")	
				.append(dependenceItemID)	
				.append("] errormessage=target item[")
				.append(targetItemKey)
				.append("] value type[")
				.append(valueObject.getClass().getName())
				.append("] is not a string type")
				.toString();
				log.warn(errorMessage);
				throw new ConfigValueInvalidException(errorMessage);
			}
			classloaderResourceFileRelativePathString = (String)valueObject;
		} catch (ConfigValueInvalidException e) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(this.targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=fail to get a target item[")
			.append(targetItemKey)
			.append("] value, ")
			.append(e.getMessage()).toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (classloaderResourceFileRelativePathString.equals("")) {
			return;
		}
		
		File classloaderAPPINFPath = null;
		
		try {
			String dependenceItemValue = sourceProperties.getProperty(dependenceItemKey);
			Object valueObject = 
					dependenceItemValueGetter.
					getItemValueWithValidation(dependenceItemValue);			
			
			if (!(valueObject instanceof File)) {
				String errorMessage = new StringBuilder("targetItemID[")
				.append(this.targetItemID)
				.append("] dependenceItemID=[")	
				.append(dependenceItemID)	
				.append("] errormessage=dependence item[")
				.append(dependenceItemKey)
				.append("] value type[")
				.append(valueObject.getClass().getName())
				.append("] is not a File type")
				.toString();
				log.warn(errorMessage);
				throw new ConfigValueInvalidException(errorMessage);
			}
			classloaderAPPINFPath = (File)valueObject;
		} catch (ConfigValueInvalidException e) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(this.targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=fail to get a dependence item[")
			.append(dependenceItemKey)
			.append("] value, ")
			.append(e.getMessage()).toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		
		String classloaderAPPINFPathString = classloaderAPPINFPath.getAbsolutePath();
		String classloaderResourceFileRealPathString = null;
		
		
		if (classloaderResourceFileRelativePathString.startsWith("/")) {
			if (File.separator.equals("/")) {
				classloaderResourceFileRealPathString = new StringBuilder(classloaderAPPINFPathString)
				.append(File.separator)
				.append("resources")				
				.append(classloaderResourceFileRelativePathString)
				.toString();
			} else {
				classloaderResourceFileRealPathString = new StringBuilder(classloaderAPPINFPathString)
				.append(File.separator)
				.append("resources")
				.append(classloaderResourceFileRelativePathString.replaceAll("/", "\\\\"))
				.toString();
			}
			
			
		} else {
			if (File.separator.equals("/")) {
				classloaderResourceFileRealPathString = new StringBuilder(classloaderAPPINFPathString)
				.append(File.separator)
				.append("resources")
				.append(File.separator)
				.append(classloaderResourceFileRelativePathString)
				.toString();
			} else {
				classloaderResourceFileRealPathString = new StringBuilder(classloaderAPPINFPathString)
				.append(File.separator)
				.append("resources")
				.append(File.separator)
				.append(classloaderResourceFileRelativePathString.replaceAll("/", "\\\\"))
				.toString();
			}			
		}		
		
		File realFile = new File(classloaderResourceFileRealPathString);
		
		if (!realFile.exists()) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(this.targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=mybatis config file[")
			.append(classloaderResourceFileRealPathString)
			.append("] is not found").toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (!realFile.isFile()) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(this.targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=mybatis config file[")
			.append(classloaderResourceFileRealPathString)
			.append("] is not a normal file").toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (!realFile.canRead()) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(this.targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=mybatis config file[")
			.append(classloaderResourceFileRealPathString)
			.append("] cannot be read").toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}		
	}
}
