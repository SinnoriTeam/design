package kr.pe.sinnori.gui.config;

import java.util.Properties;

import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDependenceBreaker {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected String targetItemID;
	// protected AbstractItemValueGetter itemCheckerOfDependenceItem;
	// protected Object wantedNativeValue;
	protected String dependenceItemID = null;
	protected SinnoriConfigInfo sinnoriConfigInfo = null;
	
	protected AbstractItemValueGetter targetItemValueGetter = null;
	protected AbstractItemValueGetter dependenceItemValueGetter = null;
	
	public AbstractDependenceBreaker(String targetItemID, 			
			String dependenceItemID,
			SinnoriConfigInfo sinnoriConfigInfo) throws ConfigErrorException {
		this.targetItemID = targetItemID;		
		this.dependenceItemID = dependenceItemID;
		this.sinnoriConfigInfo = sinnoriConfigInfo;
		
		ConfigItem targetConfigItem = sinnoriConfigInfo.getConfigItem(targetItemID);
		
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
		
		ConfigItem dependConfigItem = sinnoriConfigInfo.getConfigItem(dependenceItemID);
		
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
		
		validate(sourceProperties, targetItemKey, dependenceItemKey);
	}
	
	
	
	public abstract void validate(Properties sourceProperties, 
			String targetItemKey, String dependenceItemKey) throws ConfigValueInvalidException;
	
	public String getTargetItemID() {
		return targetItemID;
	}	
}
