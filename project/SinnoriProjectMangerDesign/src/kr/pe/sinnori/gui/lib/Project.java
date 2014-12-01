package kr.pe.sinnori.gui.lib;

import java.util.Properties;

import kr.pe.sinnori.common.config.SinnoriConfig;
import kr.pe.sinnori.common.exception.ConfigException;

public class Project {
	private String projectName;
	private String projectPathStr;
	private Properties projectProperteis;
	
	private SinnoriConfig projectConfig = null;
	
	
	public Project(String projectName, String projectPathStr, Properties projectProperteis) throws ConfigException {
		this.projectName = projectName;
		this.projectPathStr = projectPathStr;
		this.projectProperteis = projectProperteis;
		
		projectConfig = new SinnoriConfig(projectName, projectPathStr);
	}
	
	public void validCheck() throws ConfigException {
		projectConfig.validCheck(projectProperteis); 
	}

	public String getProjectName() {
		return projectName;
	}

	public String getProjectPathStr() {
		return projectPathStr;
	}	
}
