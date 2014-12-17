package kr.pe.sinnori.common.config;

import java.util.Properties;

import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBreakChecker {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected String targetItemID;
	// protected AbstractItemValueGetter itemCheckerOfDependenceItem;
	// protected Object wantedNativeValue;
	protected String dependenceItemID = null;
	protected SinnoriProjectConfig sinnoriProjectConfig = null;
	
	protected AbstractItemValueGetter targetItemValueGetter = null;
	protected AbstractItemValueGetter dependenceItemValueGetter = null;
	
	public AbstractBreakChecker(String targetItemID, 			
			String dependenceItemID,
			SinnoriProjectConfig sinnoriProjectConfig) throws ConfigErrorException {
		this.targetItemID = targetItemID;
		// this.itemCheckerOfDependenceItem = itemCheckerOfDependenceItem;
		// this.wantedNativeValue = wantedNativeValue;
		
		this.dependenceItemID = dependenceItemID;
		this.sinnoriProjectConfig = sinnoriProjectConfig;
		
		ConfigItem targetConfigItem = sinnoriProjectConfig.getConfigItem(targetItemID);
		
		if (null == targetConfigItem) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=targetItemID is not found at sinnoriProjectConfig").toString();
			
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		
		targetItemValueGetter = targetConfigItem.getItemValueGetter();
		
		ConfigItem dependConfigItem = sinnoriProjectConfig.getConfigItem(dependenceItemID);
		
		if (null == dependConfigItem) {
			String errorMessage = new StringBuilder("targetItemID[")
			.append(targetItemID)
			.append("] dependenceItemID[")
			.append(dependenceItemID)
			.append("] errormessage=dependenceItemID is not found at sinnoriProjectConfig").toString();
			
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		
		dependenceItemValueGetter = dependConfigItem.getItemValueGetter();
	}
	
	public abstract void validate(Properties sourceProperties, 
			String targetItemKey) throws ConfigValueInvalidException;
	
	public String getTargetItemID() {
		return targetItemID;
	}	
}
