package kr.pe.sinnori.common.config.dependitem;

import java.io.File;
import java.util.Properties;

import kr.pe.sinnori.common.config.AbstractBreakChecker;
import kr.pe.sinnori.common.config.SinnoriConfigInfo;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class ClassLoaderResourceFileBreakChecker extends AbstractBreakChecker {

	public ClassLoaderResourceFileBreakChecker(
			String targetItemID, 			
			String dependenceItemID,
			SinnoriConfigInfo sinnoriConfigInfo) throws ConfigErrorException {
		super(targetItemID, dependenceItemID, sinnoriConfigInfo);
	}

	@Override
	public void validate(Properties sourceProperties, 
			String prefixOfDomain) throws ConfigValueInvalidException {
		if (null == sourceProperties) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=parameter sourceProperties is null").toString();
			log.warn(errorMessage);			
			throw new ConfigValueInvalidException(errorMessage);
		}
		if (null == prefixOfDomain) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=parameter prefixOfDomain is null").toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		String targetItemKey = new StringBuilder(prefixOfDomain).append(targetItemID).toString();
		String dependenceItemKey = new StringBuilder(prefixOfDomain).append(dependenceItemID).toString();
				
		
		String classloaderResourceFileRelativePathString = null;
		try {
			String sourceItemValue = sourceProperties.getProperty(targetItemKey);
			Object valueObject = 
					targetItemValueGetter.
					getItemValueWithValidation(sourceItemValue);			
			
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
		
		String classloaderAPPINFPathString = null;
		try {
			String sourceItemValue = sourceProperties.getProperty(dependenceItemKey);
			Object valueObject = 
					dependenceItemValueGetter.
					getItemValueWithValidation(sourceItemValue);			
			
			if (!(valueObject instanceof String)) {
				String errorMessage = new StringBuilder("targetItemID[")
				.append(this.targetItemID)
				.append("] dependenceItemID=[")	
				.append(dependenceItemID)	
				.append("] errormessage=dependence item[")
				.append(dependenceItemKey)
				.append("] value type[")
				.append(valueObject.getClass().getName())
				.append("] is not a string type")
				.toString();
				log.warn(errorMessage);
				throw new ConfigValueInvalidException(errorMessage);
			}
			classloaderAPPINFPathString = (String)valueObject;
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
		
		String classloaderResourceFileRealPathString = null;
		
		if (classloaderResourceFileRelativePathString.startsWith("/")) {
			classloaderResourceFileRealPathString = new StringBuilder(classloaderAPPINFPathString)
			.append(classloaderResourceFileRelativePathString.replaceAll("/", File.separator))
			.toString();
		} else {
			classloaderResourceFileRealPathString = new StringBuilder(classloaderAPPINFPathString)
			.append(File.separator)
			.append(classloaderResourceFileRelativePathString.replaceAll("/", File.separator))
			.toString();
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
