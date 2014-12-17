package kr.pe.sinnori.common.config.dependitem;

import java.util.Properties;

import kr.pe.sinnori.common.config.AbstractConditionChecker;
import kr.pe.sinnori.common.config.SinnoriProjectConfig;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class ValueEqualConditionChecker extends AbstractConditionChecker {
	
	public ValueEqualConditionChecker(String targetItemID, 			
			String dependenceItemID, Object conditionValue,
			SinnoriProjectConfig sinnoriProjectConfig) throws ConfigErrorException {
		super(targetItemID, dependenceItemID, conditionValue, sinnoriProjectConfig);
	}
	
	public boolean isValidation(Properties sourceProperties, String dependenceItemKey) throws ConfigValueInvalidException {
		if (null == sourceProperties) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=parameter sourceProperties is null").toString();
			
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		/*Object targetNativeValue = 
				targetItemValueGetter.getItemValueWithValidation
				(sourceProperties.getProperty(targetItemID));*/
		
		if (null == dependenceItemKey) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=parameter dependenceItemKey is null").toString();
			
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (!dependenceItemKey.endsWith(dependenceItemID)) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=parameter dependenceItemKey[")
			.append(dependenceItemKey)
			.append("] is bad").toString();
			
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		String dependenceItemValue = 
		sourceProperties.getProperty(dependenceItemKey);
		
		if (null == dependenceItemValue) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=parameter dependenceItemKey[")
			.append(dependenceItemKey)
			.append("] is not found at sourceProperties").toString();
			
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}	
		
		Object dependNativeValue = 
				dependItemValueGetter.getItemValueWithValidation
				(dependenceItemValue);
		
		
		return dependNativeValue.equals(conditionValue);
	}	
}
