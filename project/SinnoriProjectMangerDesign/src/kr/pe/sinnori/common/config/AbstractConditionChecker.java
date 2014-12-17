package kr.pe.sinnori.common.config;

import java.util.Properties;

import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConditionChecker {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected String targetItemID;
	// protected AbstractItemValueGetter itemCheckerOfDependenceItem;
	// protected Object wantedNativeValue;
	protected String dependenceItemID = null;
	protected Object conditionValue = null;
	
	
	protected SinnoriProjectConfig sinnoriProjectConfig = null;
	
	// protected AbstractItemValueGetter targetItemValueGetter = null;
	// protected ConfigItem dependConfigItem = null;
	protected AbstractItemValueGetter dependItemValueGetter = null;
	
	
	public AbstractConditionChecker(String targetItemID, 			
			String dependenceItemID, Object conditionValue,
			SinnoriProjectConfig sinnoriProjectConfig) throws ConfigErrorException {
		this.targetItemID = targetItemID;
		// this.itemCheckerOfDependenceItem = itemCheckerOfDependenceItem;
		// this.wantedNativeValue = wantedNativeValue;
		
		this.dependenceItemID = dependenceItemID;
		this.conditionValue = conditionValue;
		this.sinnoriProjectConfig = sinnoriProjectConfig;
		
		/*ConfigItem targetConfigItem = configItemHash.get(targetItemID);
		if (null == targetConfigItem) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=target config item is not found").toString();
			
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}

		targetItemValueGetter = targetConfigItem.getItemValueGetter();*/
		
		ConfigItem dependConfigItem = sinnoriProjectConfig.getConfigItem(dependenceItemID);
				
		if (null == dependConfigItem) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=depend config item is not found").toString();
			
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		
		dependItemValueGetter = dependConfigItem.getItemValueGetter();
		
	}
	
	public abstract boolean isValidation(Properties sourceProperties, String dependenceItemKey) throws ConfigValueInvalidException;
	
	public String getTargetItemID() {
		return targetItemID;
	}
}
