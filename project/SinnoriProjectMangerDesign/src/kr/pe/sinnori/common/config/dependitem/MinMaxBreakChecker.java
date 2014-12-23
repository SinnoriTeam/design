package kr.pe.sinnori.common.config.dependitem;

import java.util.Properties;

import kr.pe.sinnori.common.config.AbstractBreakChecker;
import kr.pe.sinnori.common.config.SinnoriConfigInfo;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

public class MinMaxBreakChecker extends AbstractBreakChecker {

	public MinMaxBreakChecker(
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
		
		
		int min=Integer.MIN_VALUE;
		try {
			String sourceItemValue = sourceProperties.getProperty(targetItemKey);
			Object valueObject = 
					targetItemValueGetter.
					getItemValueWithValidation(sourceItemValue);			
			
			if (!(valueObject instanceof Integer)) {
				String errorMessage = new StringBuilder("targetItemID[")
				.append(this.targetItemID)
				.append("] dependenceItemID=[")	
				.append(dependenceItemID)	
				.append("] errormessage=target item[")
				.append(targetItemKey)
				.append("] value type[")
				.append(valueObject.getClass().getName())
				.append("] is not a integer type")
				.toString();
				log.warn(errorMessage);
				throw new ConfigValueInvalidException(errorMessage);
			}
			min = (Integer)valueObject;
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
		
		int max = Integer.MAX_VALUE;
		try {
			String sourceItemValue = sourceProperties.getProperty(dependenceItemKey);
			Object valueObject = 
					dependenceItemValueGetter.
					getItemValueWithValidation(sourceItemValue);			
			
			if (!(valueObject instanceof Integer)) {
				String errorMessage = new StringBuilder("targetItemID[")
				.append(this.targetItemID)
				.append("] dependenceItemID=[")	
				.append(dependenceItemID)	
				.append("] errormessage=dependence item[")
				.append(dependenceItemKey)
				.append("] value type[")
				.append(valueObject.getClass().getName())
				.append("] is not a integer type")
				.toString();
				log.warn(errorMessage);
				throw new ConfigValueInvalidException(errorMessage);
			}
			max = (Integer)valueObject;
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
		
		if (min > max) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(this.targetItemID)
			.append("] dependenceItemID=[")	
			.append(dependenceItemID)	
			.append("] errormessage=min[")
			.append(targetItemKey)
			.append("][")
			.append(min)
			.append("] is greater than max[")
			.append(dependenceItemKey)
			.append("][")
			.append(max)
			.append("]").toString();
			log.warn(errorMessage);
			throw new ConfigValueInvalidException(errorMessage);
		}
	}

}
