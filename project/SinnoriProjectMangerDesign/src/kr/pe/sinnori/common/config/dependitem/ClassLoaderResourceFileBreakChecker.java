package kr.pe.sinnori.common.config.dependitem;

import java.io.File;
import java.util.Properties;

import kr.pe.sinnori.common.config.AbstractBreakChecker;
import kr.pe.sinnori.common.config.SinnoriProjectConfig;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class ClassLoaderResourceFileBreakChecker extends AbstractBreakChecker {

	public ClassLoaderResourceFileBreakChecker(
			String targetItemID, 			
			String dependenceItemID,
			SinnoriProjectConfig sinnoriProjectConfig) throws ConfigErrorException {
		super(targetItemID, dependenceItemID, sinnoriProjectConfig);
	}

	@Override
	public void validate(Properties sourceProperties, 
			String targetItemKey) throws ConfigValueInvalidException {
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
		
		/*if (null == dependenceItemKey) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=parameter dependenceItemKey is null").toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}*/
		
		String localTargetItemID = sinnoriProjectConfig.getItemIDFromKey(targetItemKey);
		if (null == localTargetItemID) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(this.targetItemID)
			.append("] dependenceItemID=[")	
			.append(this.dependenceItemID)	
			.append("] errormessage=parameter targetItemKey=[")
			.append(targetItemKey)	
			.append("] is a unknown key. localMinItemID is null")
			.toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (!localTargetItemID.equals(this.targetItemID)) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(this.targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=localTargetItemID[")
			.append(targetItemKey)	
			.append("][")
			.append(localTargetItemID)
			.append("] is not equals to targetItemID")
			.toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		String dependenceItemKey = sinnoriProjectConfig.getKeyOfItemIDFromKey(targetItemKey, dependenceItemID);
		
		String localDependenceItemID = sinnoriProjectConfig.getItemIDFromKey(dependenceItemKey);
		if (null == localDependenceItemID) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(this.targetItemID)
			.append("] dependenceItemID=[")	
			.append(this.dependenceItemID)	
			.append("] errormessage=parameter dependenceItemKey=[")
			.append(dependenceItemKey)	
			.append("] is a unknown key. localMaxItemID is null")
			.toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (!localDependenceItemID.equals(this.dependenceItemID)) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(this.targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=localDependenceItemID[")
			.append(dependenceItemKey)	
			.append("][")
			.append(localDependenceItemID)
			.append("] is not equals to dependenceItemID")
			.toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
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
