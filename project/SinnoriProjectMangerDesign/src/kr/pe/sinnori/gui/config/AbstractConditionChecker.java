package kr.pe.sinnori.gui.config;

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
	
	
	protected SinnoriConfigInfo sinnoriConfigInfo = null;
	
	// protected AbstractItemValueGetter targetItemValueGetter = null;
	// protected ConfigItem dependConfigItem = null;
	protected AbstractItemValueGetter dependItemValueGetter = null;
	
	
	public AbstractConditionChecker(String targetItemID, 			
			String dependenceItemID, Object conditionValue,
			SinnoriConfigInfo sinnoriConfigInfo) throws ConfigErrorException {
		this.targetItemID = targetItemID;
		// this.itemCheckerOfDependenceItem = itemCheckerOfDependenceItem;
		// this.wantedNativeValue = wantedNativeValue;
		
		this.dependenceItemID = dependenceItemID;
		this.conditionValue = conditionValue;
		this.sinnoriConfigInfo = sinnoriConfigInfo;
		
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
		
		ConfigItem dependConfigItem = sinnoriConfigInfo.getConfigItem(dependenceItemID);
				
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
	
	public abstract boolean isValidation(Properties sourceProperties, String prefixOfDomain) throws ConfigValueInvalidException;
	
	public String getTargetItemID() {
		return targetItemID;
	}
}
