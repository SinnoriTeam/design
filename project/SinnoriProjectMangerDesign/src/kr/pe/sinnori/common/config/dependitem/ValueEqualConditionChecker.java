package kr.pe.sinnori.common.config.dependitem;

import java.util.Properties;

import kr.pe.sinnori.common.config.AbstractConditionChecker;
import kr.pe.sinnori.common.config.SinnoriConfigInfo;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class ValueEqualConditionChecker extends AbstractConditionChecker {
	
	public ValueEqualConditionChecker(String targetItemID, 			
			String dependenceItemID, Object conditionValue,
			SinnoriConfigInfo sinnoriConfigInfo) throws ConfigErrorException {
		super(targetItemID, dependenceItemID, conditionValue, sinnoriConfigInfo);
	}
	
	public boolean isValidation(Properties sourceProperties, String prefixOfDomain) throws ConfigValueInvalidException {
		if (null == sourceProperties) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=parameter sourceProperties is null").toString();
			
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (null == prefixOfDomain) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=parameter prefixOfDomain is null").toString();
			
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		String dependenceItemKey = new StringBuilder(prefixOfDomain).append(dependenceItemID).toString();
		
		
		String dependenceSourceValue = 
		sourceProperties.getProperty(dependenceItemKey);
		
		/*if (null == dependenceSourceValue) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=parameter dependenceItemKey[")
			.append(dependenceItemKey)
			.append("] is not found at sourceProperties").toString();
			
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}	*/
		
		Object dependNativeValue = 
				dependItemValueGetter.getItemValueWithValidation
				(dependenceSourceValue);
		
		
		return dependNativeValue.equals(conditionValue);
	}	
}
