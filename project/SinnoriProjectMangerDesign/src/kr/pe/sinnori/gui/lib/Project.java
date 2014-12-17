package kr.pe.sinnori.gui.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import kr.pe.sinnori.common.config.SinnoriConfigInfo;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.util.SequencedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Project {
	private Logger log = LoggerFactory.getLogger(Project.class);
	
	private String mainProjectName;
	private String projectPathString;
	private SequencedProperties sourceSequencedProperties;	
	
	
	private SinnoriConfigInfo sinnoriConfigInfo = null;
	private boolean isAppClient = false;
	private boolean isWebClient = false;
	private String servletEnginLibPathString = null;
	
	
	private List<String> subProjectNameList = new ArrayList<String>();

	/**
	 * 기존 생성된 프로젝트 생성자
	 * @param projectName
	 * @param projectPathString
	 * @param sourceSequencedProperties
	 * @throws ConfigErrorException
	 */
	public Project(String projectName, String projectPathString, SequencedProperties sourceSequencedProperties) throws ConfigErrorException {
		this.mainProjectName = projectName;
		this.projectPathString = projectPathString;
		this.sourceSequencedProperties = sourceSequencedProperties;		
		// projectConfigFilePathString = getProjectConfigFilePathString();
		
		sinnoriConfigInfo = new SinnoriConfigInfo(projectName, projectPathString);
		
		sinnoriConfigInfo.combind(sourceSequencedProperties);		
		
		String antPropertiesFilePathString = getAntPropertiesFilePath();
		
		SequencedProperties antProperties = ProjectManger.getSourceSequencedProperties(antPropertiesFilePathString);
		
		checkAntProperties(antProperties);
		
		servletEnginLibPathString = antProperties.getProperty("tomcat.servletlib");
		
		checkSeverBuild();		
		checkClientBuild();
		
		subProjectNameList.add("- 서브 프로젝트 -");
		
		List<String> projectNameListOfConfig = sinnoriConfigInfo.getProjectNameList();
		
		for (String projectNameOfConfig : projectNameListOfConfig) {
			if (!projectName.equals(projectNameOfConfig)) {
				subProjectNameList.add(projectNameOfConfig);
			}
		}
	}
	
	private void checkSeverBuild() throws ConfigErrorException {
		String serverBuildFilePathString = getServerBuildFilePathString();
		File serverBuildFile = new File(serverBuildFilePathString);
		if (!serverBuildFile.exists()) {
			String errorMessage = String.format("server build.xml[%s] is not found", serverBuildFilePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!serverBuildFile.isFile()) {
			String errorMessage = String.format("server build.xml[%s]  is not a normal file", serverBuildFilePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!serverBuildFile.canRead()) {
			String errorMessage = String.format("server build.xml[%s]  cannot be read", serverBuildFilePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!serverBuildFile.canWrite()) {
			String errorMessage = String.format("server build.xml[%s]  cannot be written", serverBuildFilePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
	}
	
	private void checkClientBuild() throws ConfigErrorException {
		String clientBuildPathString = getClinetBuildPathString();
		File clientBuildPath = new File(clientBuildPathString);
		if (!clientBuildPath.exists()) {
			String errorMessage = String.format("client build path[%s] is not found", clientBuildPathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!clientBuildPath.isDirectory()) {
			String errorMessage = String.format("client build path[%s] is not directory", clientBuildPathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!clientBuildPath.canRead()) {
			String errorMessage = String.format("client build path[%s] cannot be read", clientBuildPathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!clientBuildPath.canWrite()) {
			String errorMessage = String.format("client build path[%s] cannot be written", clientBuildPathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		String appClientBuildFilePathString = geAppClientBuildFilePathString(clientBuildPathString);
		File appClientBuildFile = new File(appClientBuildFilePathString);
		if (appClientBuildFile.exists()) {
			if (!appClientBuildFile.canRead()) {
				String errorMessage = String.format("app client build.xml[%s]  cannot be read", appClientBuildFilePathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!appClientBuildFile.canWrite()) {
				String errorMessage = String.format("app client build.xml[%s]  cannot be written", appClientBuildFilePathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			isAppClient = true;
		} else {
			isAppClient = false;
		}
		
		String webClientBuildFilePathString = geWebClientBuildFilePathString(clientBuildPathString);
		File webClientBuildFile = new File(webClientBuildFilePathString);
		if (webClientBuildFile.exists()) {
			if (!webClientBuildFile.canRead()) {
				String errorMessage = String.format("web client build.xml[%s]  cannot be read", webClientBuildFilePathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!webClientBuildFile.canWrite()) {
				String errorMessage = String.format("web client build.xml[%s]  cannot be written", webClientBuildFilePathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			isWebClient = true;
		} else {
			isWebClient = false;
		}
		
		if (!isAppClient && !isWebClient) {
			String errorMessage = String.format("app client build.xml[%s] and web client build.xml[%s] cannot be found, one more client need", 
					appClientBuildFilePathString, webClientBuildFilePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
	}
		
	
	private void checkAntProperties(Properties antProperties) throws ConfigErrorException {
		String key = "tomcat.servletlib";
		String propValue = antProperties.getProperty(key);
		
		if (null == propValue) {
			String errorMessage = String.format("project[%s]'s ant.properties is bad, the key '%s' not found", projectPathString, key);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		propValue = propValue.trim();
		
		if (propValue.equals("")) {
			String errorMessage = String.format("project[%s]'s ant.properties is bad, th key '%s' value is empty", projectPathString, key);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		antProperties.put(key, propValue);
		
		key = "java.debug";
		propValue = antProperties.getProperty(key);
		
		if (null == propValue) {
			String errorMessage = String.format("project[%s]'s ant.properties is bad, the key '%s' not found", projectPathString, key);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		propValue = propValue.trim();
		
		if (propValue.equals("")) {
			String errorMessage = String.format("project[%s]'s ant.properties is bad, th key '%s' value is empty", projectPathString, key);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		antProperties.put(key, propValue);
	}
	
	/*private String getProjectConfigFilePathString() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("config");
		strBuilder.append(File.separator);
		strBuilder.append("sinnori.properties");
		
		return strBuilder.toString();
	}*/
	
	private String getServerBuildFilePathString() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("server_build");
		strBuilder.append(File.separator);
		strBuilder.append("build.xml");
		
		return strBuilder.toString();
	}
	
	private String getClinetBuildPathString() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("client_build");
		
		return strBuilder.toString();
	}
	
	
	private String geAppClientBuildFilePathString(String clientBuildPathString) {
		StringBuilder strBuilder = new StringBuilder(clientBuildPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("app_build");
		strBuilder.append(File.separator);
		strBuilder.append("build.xml");
		
		return strBuilder.toString();
	}
	
	private String geWebClientBuildFilePathString(String clientBuildPathString) {
		StringBuilder strBuilder = new StringBuilder(clientBuildPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("web_build");
		strBuilder.append(File.separator);
		strBuilder.append("build.xml");
		
		return strBuilder.toString();
	}
	
	private String getAntPropertiesFilePath() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("ant.properties");
		
		return strBuilder.toString();
	}
	
	public String getMainProjectName() {
		return mainProjectName;
	}

	public String getProjectPathString() {
		return projectPathString;
	}

	public SequencedProperties getSourceSequencedProperties() {
		return sourceSequencedProperties;
	}

	public SinnoriConfigInfo getSinnoriConfigInfo() {
		return sinnoriConfigInfo;
	}

	public boolean isAppClient() {
		return isAppClient;
	}

	public boolean isWebClient() {
		return isWebClient;
	}

	public String getServletEnginLibPathString() {
		return servletEnginLibPathString;
	}

	public List<String> getSubProjectNameList() {
		return subProjectNameList;
	}
	
	
}
