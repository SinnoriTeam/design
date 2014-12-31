package kr.pe.sinnori.gui.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import kr.pe.sinnori.common.config.SinnoriConfigInfo;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.util.SequencedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainProject {
	private Logger log = LoggerFactory.getLogger(MainProject.class);
	
	private String mainProjectName;
	private String projectPathString;
	private SequencedProperties sourceSequencedProperties;
	
	private String projectConfigFilePathString = null;
	
	
	private SinnoriConfigInfo sinnoriConfigInfo = null;
	private boolean isAppClient = false;
	private boolean isWebClient = false;
	private String servletEnginLibPathString = null;
	
	
	private List<String> subProjectNameList = new ArrayList<String>();
	private List<String> dbcpConnPoolNameList = new ArrayList<String>();

	/**
	 * 기존 생성된 프로젝트 생성자
	 * @param projectName
	 * @param projectPathString
	 * @param sourceSequencedProperties
	 * @throws ConfigErrorException
	 */
	public MainProject(String mainProjectName, String projectPathString, 
			SequencedProperties sourceSequencedProperties) throws ConfigErrorException {
		this.mainProjectName = mainProjectName;
		this.projectPathString = projectPathString;
		this.sourceSequencedProperties = sourceSequencedProperties;		
		// projectConfigFilePathString = getProjectConfigFilePathString();
		
		sinnoriConfigInfo = new SinnoriConfigInfo(mainProjectName, projectPathString);
		
		projectConfigFilePathString = sinnoriConfigInfo.getProjectConfigFilePathString();
		
		// sinnoriConfigInfo.combind(sourceSequencedProperties);		
		
		String antPropertiesFilePathString = getAntPropertiesFilePath();
		
		SequencedProperties antProperties = MainProjectManger.getSourceSequencedProperties(antPropertiesFilePathString);
		
		checkAntProperties(antProperties);
		
		servletEnginLibPathString = antProperties.getProperty("tomcat.servletlib");
		
		checkSeverBuild();		
		checkClientBuild();
			
		
		makeProjectNameSetFromSourceProperties(sourceSequencedProperties);
		makeDBCPCOnnectionPoolNameSetFromSourceProperties(sourceSequencedProperties);		
	}
	
	private void makeDBCPCOnnectionPoolNameSetFromSourceProperties(Properties sourceProperties) throws ConfigErrorException {
		String dbcpConnPoolNameListValue = sourceProperties.getProperty(SinnoriConfigInfo.DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING);
		
		if (null == dbcpConnPoolNameListValue) {
			/** DBCP 연결 폴 이름 목록을 지정하는 키가 없을 경우 */
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("] has no a dbcp connection pool name list").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		String[] dbcpConnectionPoolNameArrray = dbcpConnPoolNameListValue.split(",");
		
		dbcpConnPoolNameList.clear();
		
		Set<String> tempNameSet = new HashSet<String>();
		
		for (String dbcpConnectionPoolNameOfList : dbcpConnectionPoolNameArrray) {
			dbcpConnectionPoolNameOfList = dbcpConnectionPoolNameOfList.trim();
			
			if (dbcpConnectionPoolNameOfList.equals("")) continue;
			
			tempNameSet.add(dbcpConnectionPoolNameOfList);
			dbcpConnPoolNameList.add(dbcpConnectionPoolNameOfList);
		}		
		
		if (tempNameSet.size() != dbcpConnPoolNameList.size()) {
			/** DBCP 연결 폴 이름 목록의 이름들중 중복된 것이 있는 경우 */
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)					
			.append("]::dbcp connection pool name list has one more same thing").toString();
			
			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		}
		
	}
	
	private void makeProjectNameSetFromSourceProperties(Properties sourceProperties) throws ConfigErrorException {
		String projectNameListValue = sourceProperties.getProperty(SinnoriConfigInfo.PROJECT_NAME_LIST_KEY_STRING);
		
		log.info("mainProjectName={}, projectNameListValue={}", 
				mainProjectName, projectNameListValue);		
		
		if (null == projectNameListValue) {
			/** 프로젝트 목록을 지정하는 키가 없을 경우 */
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("] has no a project list").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		String[] projectNameArrray = projectNameListValue.split(",");
		if (0 == projectNameArrray.length) {
			/** 프로젝트 목록 값으로 부터 프로젝트 목록을 추출할 수 없는 경우 */
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)					
			.append("]:: the project list is empty").toString();
			
			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		}

		subProjectNameList.clear();
		Set<String> tempNameSet = new HashSet<String>();		
		
		for (String projectNameOfList : projectNameArrray) {
			projectNameOfList = projectNameOfList.trim();
			
			if (projectNameOfList.equals("")) continue;			
			
			tempNameSet.add(projectNameOfList);
			subProjectNameList.add(projectNameOfList);
		}
		
		if (! tempNameSet.contains(mainProjectName)) {
			/** 프로젝트 목록에 지정된 메인 프로젝트에 대한 정보가 없는 경우 */
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("]:: the project list has no main project").toString();
			
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (tempNameSet.size() != subProjectNameList.size()) {
			/** 프로젝트 목록의 이름들중 중복된 것이 있는 경우 */
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)					
			.append("]::project name list has one more same thing").toString();
			
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		subProjectNameList.remove(mainProjectName);
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
	
	public void addSubProjectName(String newSubProjectName) {
		if (mainProjectName.equals(newSubProjectName)) {
			String errorMessage = String.format("메인 프로젝트 이름[%s]과 같은 이름입니다.", newSubProjectName);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		if (subProjectNameList.contains(newSubProjectName)) {
			String errorMessage = String.format("중복된 이름[%s]을 가진 서브 프로젝트 이름이 있습니다.", newSubProjectName);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		subProjectNameList.add(newSubProjectName);
	}
	public void removeSubProjectName(String selectedSubProjectName) {
		subProjectNameList.remove(selectedSubProjectName);
	}
	
	public List<String> getDBCPConnPoolNameList() {
		return dbcpConnPoolNameList;
	}
	
	public void addDBCPConnectionPoolName(String newDBCPConnPoolName) {
		if (dbcpConnPoolNameList.contains(newDBCPConnPoolName)) {
			String errorMessage = String.format("중복된 이름[%s]을 가진 DBCP 연결 폴 이름이 있습니다.", newDBCPConnPoolName);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		dbcpConnPoolNameList.add(newDBCPConnPoolName);
	}
	
	public void removeDBCPConnectionPoolName(String selectedDBCPConnPoolName) {
		dbcpConnPoolNameList.remove(selectedDBCPConnPoolName);
	}
}
