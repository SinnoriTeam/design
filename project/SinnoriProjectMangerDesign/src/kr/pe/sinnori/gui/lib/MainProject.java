package kr.pe.sinnori.gui.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import kr.pe.sinnori.common.config.ConfigItem;
import kr.pe.sinnori.common.config.SinnoriConfigInfo;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.util.SequencedProperties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainProject {
	private Logger log = LoggerFactory.getLogger(MainProject.class);
	
	private String mainProjectName;
	private String projectPathString;
	// private SequencedProperties sourceSequencedProperties;
	
	private String projectConfigFilePathString = null;
	
	
	private SinnoriConfigInfo sinnoriConfigInfo = null;
	private boolean isAppClient = false;
	private boolean isWebClient = false;
	private String servletEnginLibPathString = null;
	
	
	private List<String> subProjectNameList = new ArrayList<String>();
	private List<String> dbcpConnPoolNameList = new ArrayList<String>();

	/**
	 * 기존에 생성된 메인 프로젝트 추가시 호출되는 생성자
	 * @param projectName
	 * @param projectPathString
	 * @param sourceSequencedProperties
	 * @throws ConfigErrorException
	 */
	public MainProject(String mainProjectName, String projectPathString, 
			SequencedProperties sourceSequencedProperties) throws ConfigErrorException {
		this.mainProjectName = mainProjectName;
		this.projectPathString = projectPathString;
		this.sinnoriConfigInfo = new SinnoriConfigInfo(mainProjectName, projectPathString);
		this.projectConfigFilePathString = sinnoriConfigInfo.getProjectConfigFilePathString();
		
		// sinnoriConfigInfo.combind(sourceSequencedProperties);		
		
		String antPropertiesFilePathString = getAntPropertiesFilePath();
		
		SequencedProperties antProperties = MainProjectManger.getSourceSequencedProperties(antPropertiesFilePathString);
		
		checkAntProperties(antProperties);
		
		servletEnginLibPathString = antProperties.getProperty("tomcat.servletlib");
		
		checkAntEnvironment();	
		
		makeProjectNameSetFromSourceProperties(sourceSequencedProperties);
		makeDBCPCOnnectionPoolNameSetFromSourceProperties(sourceSequencedProperties);		
	}
	
	/**
	 * 메인 프로젝트 신규 추가시 호출되는 생성자
	 * @param mainProjectName
	 * @param projectPathString
	 * @throws ConfigErrorException
	 */
	public MainProject(String mainProjectName, String projectPathString) throws ConfigErrorException {
		this.mainProjectName = mainProjectName;
		this.projectPathString = projectPathString;
		this.isAppClient = true;
		this.isWebClient = false;
		
		String relativeDirectories[] = {"rsa_keypair", 
				"log/apache", "log/client", "log/server", "log/servlet", 
				"impl/message/info"};
		createDirectories(projectPathString, relativeDirectories);
		
		this.sinnoriConfigInfo = new SinnoriConfigInfo(mainProjectName, projectPathString);		
		this.projectConfigFilePathString = sinnoriConfigInfo.getProjectConfigFilePathString();
		this.servletEnginLibPathString = "D:\\apache-tomcat-7.0.57\\lib";
		
		/** ant.properties */
		String antFilePathString = new StringBuilder(projectPathString)
		.append(File.separator).append("ant.properties").toString();
		
		// FileUtils fileUtils = new FileUtils();		
		
		File antFile = new File(antFilePathString);
				
		FileOutputStream fos = null;
		try {
			fos = FileUtils.openOutputStream(antFile);
			
			SequencedProperties antPropeties = new SequencedProperties();
			antPropeties.put("is.tomcat", "false");
			antPropeties.put("tomcat.servletlib", servletEnginLibPathString);
			antPropeties.put("java.debug", "true");
			
			antPropeties.store(fos, new StringBuilder("Project[")
			.append(mainProjectName).append("]'s ant properties file").toString());
		} catch (Exception e) {
			String errorMessage = String.format("fail to create the ant properties file[%s], errorMessage=%s", antFilePathString, e.getMessage());
			log.warn(errorMessage);			
			throw new ConfigErrorException(errorMessage);
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch(Exception e) {
					log.warn("fail to close the file output stream of the ant properties file[{}], errorMessage={}", antFilePathString, e.getMessage());
				}
			}
		}
		
		createAntEnvironment();
		
		SequencedProperties newSinnoriConfig = getNewSinnoriConfig();
		
		// sinnoriConfigInfo.getProjectConfigFilePathString();
		File projectConfigFile = new File(projectConfigFilePathString);
		try {
			fos = FileUtils.openOutputStream(projectConfigFile);
			
			// Project[sample_fileupdown]'s Config File
			/*newSinnoriConfig.storeToXML(fos, new StringBuilder("Project[")
			.append(mainProjectName).append("]'s Config File").toString(), "UTF-8");*/
			
			newSinnoriConfig.store(fos, new StringBuilder("Project[")
			.append(mainProjectName).append("]'s Config File").toString());
			
		} catch (Exception e) {
			String errorMessage = String.format("fail to create the project config file[%s], errorMessage=%s", antFilePathString, e.getMessage());
			log.warn(errorMessage);			
			throw new ConfigErrorException(errorMessage);
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch(Exception e) {
					log.warn("fail to close the file output stream of the project config file[{}], errorMessage={}", projectConfigFilePathString, e.getMessage());
				}
			}
		}
		
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
	
	public void checkAntEnvironment() throws ConfigErrorException {
		checkSeverAntEnvironment();
		checkClientAntEnvironment();
	}
	
	private void checkSeverAntEnvironment() throws ConfigErrorException {
		String serverBuildPathString = getServerBuildPathString();
		String serverBuildFilePathString = getServerBuildXMLFilePathString(serverBuildPathString);
		
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
	
	private void checkClientAntEnvironment() throws ConfigErrorException {
		String clientBuildBasePathString = getClinetBuildBasePathString();
		File clientBuildPath = new File(clientBuildBasePathString);
		if (!clientBuildPath.exists()) {
			String errorMessage = String.format("client build base path[%s] is not found", clientBuildBasePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!clientBuildPath.isDirectory()) {
			String errorMessage = String.format("client build base path[%s] is not directory", clientBuildBasePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!clientBuildPath.canRead()) {
			String errorMessage = String.format("client build base path[%s] cannot be read", clientBuildBasePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!clientBuildPath.canWrite()) {
			String errorMessage = String.format("client build base path[%s] cannot be written", clientBuildBasePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		
		String appClinetBuildPathString = getAppClinetBuildPathString(clientBuildBasePathString);	
				
		String appClientBuildXMLFilePathString = getAppClientBuildXMLFilePathString(appClinetBuildPathString);
		File appClientBuildFile = new File(appClientBuildXMLFilePathString);
		if (appClientBuildFile.exists()) {
			if (!appClientBuildFile.canRead()) {
				String errorMessage = String.format("app client build.xml[%s]  cannot be read", appClientBuildXMLFilePathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!appClientBuildFile.canWrite()) {
				String errorMessage = String.format("app client build.xml[%s]  cannot be written", appClientBuildXMLFilePathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			isAppClient = true;
		} else {
			isAppClient = false;
		}
		
		String  webClinetBuildPathString = getWebClinetBuildPathString(clientBuildBasePathString);
		
		String webClientBuildXMLFilePathString = getWebClientBuildXMLFilePathString(webClinetBuildPathString);
		File webClientBuildFile = new File(webClientBuildXMLFilePathString);
		if (webClientBuildFile.exists()) {
			if (!webClientBuildFile.canRead()) {
				String errorMessage = String.format("web client build.xml[%s]  cannot be read", webClientBuildXMLFilePathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!webClientBuildFile.canWrite()) {
				String errorMessage = String.format("web client build.xml[%s]  cannot be written", webClientBuildXMLFilePathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			isWebClient = true;
		} else {
			isWebClient = false;
		}
		
		if (!isAppClient && !isWebClient) {
			String errorMessage = String.format("app client build.xml[%s] and web client build.xml[%s] cannot be found, one more client need", 
					appClientBuildXMLFilePathString, webClientBuildXMLFilePathString);
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
	
	private void createAntEnvironment() throws ConfigErrorException {
		// FIXME!
		createSeverAntEnvironment();
		createClientAntEnvironment();
	}
	
	private void createSeverAntEnvironment() throws ConfigErrorException {
		String serverBuildPathString = getServerBuildPathString();
		File serverBuildPath = new File(serverBuildPathString);
		boolean isSuccess = serverBuildPath.mkdir();
		if (!isSuccess) {
			String errorMessage = String.format("fail to make a new server build path[%s]", serverBuildPathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!serverBuildPath.isDirectory()) {
			String errorMessage = String.format("server build path[%s] is not directory", serverBuildPathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!serverBuildPath.canRead()) {
			String errorMessage = String.format("server build path[%s] cannot be read", serverBuildPathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!serverBuildPath.canWrite()) {
			String errorMessage = String.format("server build path[%s] cannot be written", serverBuildPathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		String serverBuildXMLFilePathString = getServerBuildXMLFilePathString(serverBuildPathString);
		String conents = getServerAntBuildXMLFileContent();
		createFile("서버 build.xml", conents, serverBuildXMLFilePathString);
		
		// FIXME!
		String relativeDirectories[] = {"src", "APP-INF/lib", "APP-INF/resources"};
		createDirectories(serverBuildPathString, relativeDirectories);
	}
	
	
		
	private String getServerAntBuildXMLFileContent() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<project name=\"");
		stringBuilder.append(mainProjectName);
		stringBuilder.append("_server\" default=\"compile.appinf\" basedir=\".\">");				
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<!-- set global properties for this build -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"src\" location=\"src\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"build\" location=\"build\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"APP-INF\" location=\"APP-INF\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"dist\" location=\"dist\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"corelib\" location=\"corelib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"lib\" location=\"lib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"core.build\" location=\"../../../core_build\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property file=\"../ant.properties\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<condition property=\"is.windows.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<os family=\"windows\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<condition property=\"is.unix.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<os family=\"unix\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<condition property=\"is.debug.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<istrue value=\"${java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.debug\" if=\"is.debug.yes\" description=\"it sets the value of the variable 'core.java.debug' to 'debug' in order that the sinnori core compile including debug info\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"core.java.debug\" value=\"debug\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.nodebug\" unless=\"is.debug.yes\" description=\"it sets the value of the variable 'core.java.debug' to 'nodebug' in order that the sinnori core compile excluding debug info\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"core.java.debug\" value=\"nodebug\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.dos\" if=\"is.windows.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"weblib\" location=\"${dos.weblib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.unix\" if=\"is.unix.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"weblib\" location=\"${unix.weblib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.var\" depends=\"init.nodebug, init.debug, init.dos, init.unix\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"java.debug=${java.debug}, core.java.debug=${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"is.debug.yes=${is.debug.yes}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the time stamp -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<tstamp />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.unixcore\" if=\"is.unix.yes\" depends=\"init.var\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"make sinnori core jar file in Unix OS\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<exec dir=\"${core.build}\" executable=\"ant\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</exec>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.doscore\" if=\"is.windows.yes\" depends=\"init.var\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"make sinnori core jar file in Microsoft Windows OS\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<exec dir=\"${core.build}\" executable=\"cmd\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"/c\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"ant.bat\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</exec>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.core\" depends=\"make.doscore, make.unixcore\" description=\"make sinnori core jar file\" />\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.directory\" depends=\"make.core\" description=\"directory init for ant compile envoroment\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"directory init for ant compile envoroment\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- core directory -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${corelib}\" />\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${corelib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${corelib}/in\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- lib directory is a user define directory -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${lib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${lib}/in\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- APP-INF/classes directory is a user define directory that has the server dynamic classes -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${APP-INF}/classes\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${APP-INF}/classes\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- core APP-INF/lib direcotry -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${APP-INF}/lib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${APP-INF}/lib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- APP-INF/resoruces directory is a user define directory -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${APP-INF}/resources\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"copy.core\" description=\"sinnori core copy\" depends=\"init.directory\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${corelib}/in\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset file=\"${core.build}/dist/sinnori-core.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${corelib}/ex\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset dir=\"${core.build}/lib/ex/\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"copy.appinf\" depends=\"copy.core\" description=\"copy server APP-INF's library, common source files and resources\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${APP-INF}/lib\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset dir=\"${core.build}/APP-INF/lib/\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("    <!-- Except for the sample class file that inherits the UnpooledDataSourceFactory class and gets a db connection pool from the DBCPManager class -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${src}/kr/pe/sinnori/impl/server/mybatis/\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t  <!-- the SqlSessionFactoryManger class depends on the server dynimic class loader -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset file=\"${core.build}/APP-INF/src/kr/pe/sinnori/impl/server/mybatis/SqlSessionFactoryManger.java\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"sinnori\" depends=\"copy.appinf\" description=\"copy sinnori core after making the sinnori core jar file\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"copy the sinnori core jar file after making the sinnori core jar file\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"clean.main\" depends=\"sinnori\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Delete the ${build} and ${dist} directory trees -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${build}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.main\" depends=\"clean.main\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the time stamp -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<tstamp />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the build directory structure used by compile -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${build}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"compile.main\" depends=\"init.main\" description=\"compile server main class\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Compile the java code from ${src} into ${build} -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<javac debug=\"${java.debug}\" debuglevel=\"lines,vars,source\" encoding=\"UTF-8\" includeantruntime=\"false\" srcdir=\"${src}\" destdir=\"${build}\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<include name=\"main/**\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}/ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}/in\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${APP-INF}/lib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</javac>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"compile.main\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<path id=\"build.classpath\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<fileset dir=\"${basedir}\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<include name=\"corelib/ex/*.jar\"/>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<include name=\"lib/ex/*.jar\"/>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</path>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<pathconvert property=\"manifest.classpath\" pathsep=\" \">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t  <path refid=\"build.classpath\"/>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t  <mapper>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<chainedmapper>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t   <flattenmapper/>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t   <globmapper from=\"*.jar\" to=\"lib/*.jar\"/>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</chainedmapper>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t  </mapper>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</pathconvert>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"copy.jarlib\" depends=\"compile.main\" description=\"copy jar library\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${dist}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${dist}/lib\" />\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${dist}/lib\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset dir=\"${corelib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset dir=\"${lib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("<target name=\"clean.serverlib\" depends=\"copy.jarlib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Delete the ${build} and ${dist} directory trees -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${build}/serverlib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.serverlib\" depends=\"clean.main\">\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the build directory structure used by compile -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${build}/serverlib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"compile.serverlib\" depends=\"init.serverlib\" description=\"compile the server library\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<javac debug=\"${java.debug}\" debuglevel=\"lines,vars,source\" encoding=\"UTF-8\" includeantruntime=\"false\" srcdir=\"${src}\" destdir=\"${build}/serverlib\" >\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<include name=\"kr/pe/sinnori/common/serverlib/**\" />\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}/ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}/in\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}/ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}/in\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${APP-INF}/lib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<!-- pathelement path=\"${APP-INF}/classes\" / -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</javac>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.serverlib\" depends=\"compile.serverlib\" description=\"make the server library jar file\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"${build}/serverlib\" />\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<jar jarfile=\"${lib}/in/sinnori-serverlib.jar\" basedir=\"${build}/serverlib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<restrict>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<name name=\"**/*.class\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<archives>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<!-- the server library does not need a extern jar library because the server library is indenpent -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</archives>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</restrict>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</jar>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.main\" depends=\"make.serverlib\" description=\"make the server executable jar file\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<jar destfile=\"${dist}/SinnoriServerMain.jar\" basedir=\"${build}\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<include name=\"main/**\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<restrict>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<name name=\"**/*.class\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<archives>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<zips>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t\t<fileset dir=\"${corelib}/in\" includes=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t\t<fileset dir=\"${lib}/in\" includes=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t</zips>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</archives>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</restrict>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<manifest>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<attribute name=\"Main-Class\" value=\"main.SinnoriServerMain\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<attribute name=\"Class-Path\" value=\"${manifest.classpath}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</manifest>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</jar>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"clear.appinf\" if=\"is.web.yes\" depends=\"init.var\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t  <echo message=\"delete ${APP-INF}/classes\" />\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${APP-INF}/classes\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.appinf\" if=\"is.web.yes\" depends=\"clear.appinf\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t  <echo message=\"mkdir ${APP-INF}/classes\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${APP-INF}/classes\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"compile.appinf\" depends=\"init.var\" description=\"only compile the server business logic classes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<javac debug=\"${java.debug}\" debuglevel=\"lines,vars,source\" encoding=\"UTF-8\" includeantruntime=\"false\" srcdir=\"${src}\" destdir=\"${APP-INF}/classes\" >");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<include name=\"kr/pe/sinnori/impl/**\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}/ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}/in\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}/ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}/in\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${APP-INF}/lib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</javac>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"compile.appinf, is.windows.yes=${is.windows.yes}, is.unix.yes=${is.unix.yes}, java.debug=${java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"all\" depends=\"make.main, compile.appinf\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"is.windows.yes=${is.windows.yes}, is.unix.yes=${is.unix.yes}, java.debug=${java.debug}, core.java.debug=${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("</project>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.toString();
		return stringBuilder.toString();
	}
	
	// FIXME!
	private void createClientAntEnvironment() throws ConfigErrorException {
		String clientBuildBasePathString = getClinetBuildBasePathString();
		
		File clientBuildBasePath = new File(clientBuildBasePathString);
		boolean isSuccess = clientBuildBasePath.mkdir();
		if (!isSuccess) {
			String errorMessage = String.format("fail to make a new client build base path[%s]", clientBuildBasePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!clientBuildBasePath.isDirectory()) {
			String errorMessage = String.format("client build base path[%s] is not directory", clientBuildBasePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!clientBuildBasePath.canRead()) {
			String errorMessage = String.format("client build base path[%s] cannot be read", clientBuildBasePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!clientBuildBasePath.canWrite()) {
			String errorMessage = String.format("client build base path[%s] cannot be written", clientBuildBasePathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (isAppClient) {
			String appClinetBuildPathString = getAppClinetBuildPathString(clientBuildBasePathString);			
			
			File appClientBuildPath = new File(appClinetBuildPathString);
			isSuccess = appClientBuildPath.mkdir();
			if (!isSuccess) {
				String errorMessage = String.format("fail to make a new app client build path[%s]", appClinetBuildPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!appClientBuildPath.isDirectory()) {
				String errorMessage = String.format("app client build path[%s] is not directory", appClinetBuildPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!appClientBuildPath.canRead()) {
				String errorMessage = String.format("app client build path[%s] cannot be read", appClinetBuildPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!appClientBuildPath.canWrite()) {
				String errorMessage = String.format("app client build path[%s] cannot be written", appClinetBuildPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			String appClientBuildXMLFilePathString = getAppClientBuildXMLFilePathString(appClinetBuildPathString);
			String conents = getAppClientAntBuildXMLFileContents();
			createFile("app client build.xml", conents, appClientBuildXMLFilePathString);
			
			// FIXME!
			String relativeDirectories[] = {"src"};
			createDirectories(appClinetBuildPathString, relativeDirectories);
		} else {
			String appClinetBuildPathString = getAppClinetBuildPathString(clientBuildBasePathString);			
			
			File appClientBuildPath = new File(appClinetBuildPathString);
			
			if (appClientBuildPath.exists()) {
				try {
					FileUtils.forceDelete(appClientBuildPath);
				} catch (IOException e) {
					String errorMessage = String.format("fail to delete app client build path[%s], errormessage=%s", 
							appClinetBuildPathString, e.getMessage());
					log.warn(errorMessage);
					throw new ConfigErrorException(errorMessage);
				}
				
				log.info("deleting app client build path[{}] is success", appClinetBuildPathString);
			}			
		}
		
		if (isWebClient) {
			String  webClinetBuildPathString = getWebClinetBuildPathString(clientBuildBasePathString);			
			
			File webClientBuildPath = new File(webClinetBuildPathString);
			isSuccess = webClientBuildPath.mkdir();
			if (!isSuccess) {
				String errorMessage = String.format("fail to make a new web client build path[%s]", webClinetBuildPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!webClientBuildPath.isDirectory()) {
				String errorMessage = String.format("web client build path[%s] is not directory", webClinetBuildPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!webClientBuildPath.canRead()) {
				String errorMessage = String.format("web client build path[%s] cannot be read", webClinetBuildPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!webClientBuildPath.canWrite()) {
				String errorMessage = String.format("web client build path[%s] cannot be written", webClinetBuildPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			String webClientBuildXMLFilePathString = getWebClientBuildXMLFilePathString(webClinetBuildPathString);
			
			String conents = getWebClientAntBuildXMLFileContents();
			createFile("web client build.xml", conents, webClientBuildXMLFilePathString);
			
			// FIXME!
			String relativeDirectories[] = {"src"};
			createDirectories(webClinetBuildPathString, relativeDirectories);
			
			/** 
			 * web root directory struect
			 */
			String webRootRelativeDirectories[] = {"web_app_base/ROOT/WEB-INF/lib", "web_app_base/upload"};
			createDirectories(projectPathString, webRootRelativeDirectories);
		} else {
			String  webClinetBuildPathString = getWebClinetBuildPathString(clientBuildBasePathString);
			File webClientBuildPath = new File(webClinetBuildPathString);
			if (webClientBuildPath.exists()) {
				try {
					FileUtils.forceDelete(webClientBuildPath);
				} catch (IOException e) {
					String errorMessage = String.format("fail to delete web client build path[%s], errormessage=%s", 
							webClinetBuildPathString, e.getMessage());
					log.warn(errorMessage);
					throw new ConfigErrorException(errorMessage);
				}
				
				log.info("deleting app client build path[{}] is success", webClinetBuildPathString);
			}	
		}
	}
	
	private String getAppClientAntBuildXMLFileContents() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<project name=\"");
		stringBuilder.append(mainProjectName);
		stringBuilder.append("_appclient\" default=\"make.main.only\" basedir=\".\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<!-- set global properties for this build -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"src\" location=\"src\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"build\" location=\"build\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"dist\" location=\"dist\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"corelib\" location=\"corelib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"lib\" location=\"lib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"framework.build\" location=\"../../../../core_build\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property file=\"../../ant.properties\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<condition property=\"is.windows.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<os family=\"windows\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<condition property=\"is.unix.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<os family=\"unix\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<condition property=\"is.debug.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<istrue value=\"${java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.debug\" if=\"is.debug.yes\" description=\"framework with debug info\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"core.java.debug\" value=\"debug\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.nodebug\" unless=\"is.debug.yes\" description=\"framework with no debug info\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"core.java.debug\" value=\"nodebug\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.dos\" if=\"is.windows.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"weblib\" location=\"${dos.weblib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.unix\" if=\"is.unix.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"weblib\" location=\"${unix.weblib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.var\" depends=\"init.nodebug, init.debug, init.dos, init.unix\" description=\"var init\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"java.debug=${java.debug}, core.java.debug=${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"is.debug.yes=${is.debug.yes}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.unixcore\" if=\"is.unix.yes\" depends=\"init.var\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<exec dir=\"${framework.build}\" executable=\"ant\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</exec>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.doscore\" if=\"is.windows.yes\" depends=\"init.var\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<exec dir=\"${framework.build}\" executable=\"cmd\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"/c\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"ant.bat\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</exec>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.core\" depends=\"make.doscore, make.unixcore\" description=\"make sinnori core jar file\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.directory\" depends=\"make.core\" description=\"directory init for ant compile envoroment\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"directory init for ant compile envoroment\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${corelib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${corelib}/in\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${lib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${lib}/in\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"copy.core\" depends=\"init.directory\" description=\"sinnori core copy\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${corelib}/in\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset file=\"${framework.build}/dist/sinnori-core.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${corelib}/ex\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset dir=\"${framework.build}/lib/ex/\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"sinnori\" depends=\"copy.core\" description=\"copy sinnori core after making the sinnori core jar file\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"copy the sinnori core jar file after making the sinnori core jar file\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"clean.main\" depends=\"sinnori\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Delete the ${build} and ${dist} directory trees -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${build}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.main\" depends=\"clean.main\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the time stamp -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<tstamp />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the build directory structure used by compile -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${build}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"compile.main\" depends=\"init.main\" description=\"compile application main class\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Compile the java code from ${src} into ${build} -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<javac debug=\"${java.debug}\" debuglevel=\"lines,vars,source\" encoding=\"UTF-8\" includeantruntime=\"false\" srcdir=\"${src}\" destdir=\"${build}\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}/ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}/in\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}/ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}/in\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</javac>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<path id=\"build.classpath\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<fileset dir=\"${basedir}\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<include name=\"corelib/ex/*.jar\"/>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<include name=\"lib/ex/*.jar\"/>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</path>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<pathconvert property=\"manifest.classpath\" pathsep=\" \">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t  <path refid=\"build.classpath\"/>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t  <mapper>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<chainedmapper>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t   <flattenmapper/>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t   <globmapper from=\"*.jar\" to=\"lib/*.jar\"/>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</chainedmapper>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t  </mapper>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</pathconvert>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"copy.jarlib\" depends=\"compile.main\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${dist}\" />\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${dist}/lib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${dist}/lib\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset dir=\"${corelib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset dir=\"${lib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.main\" depends=\"copy.jarlib\">\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<jar destfile=\"${dist}/SinnoriAppClientMain.jar\" basedir=\"${build}\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<restrict>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<name name=\"**/*.class\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<archives>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<zips>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t\t<fileset dir=\"${corelib}/in\" includes=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t\t<fileset dir=\"${lib}/in\" includes=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t</zips>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</archives>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</restrict>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<manifest>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<attribute name=\"Main-Class\" value=\"main.SinnoriAppClientMain\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<attribute name=\"Class-Path\" value=\"${manifest.classpath}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</manifest>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</jar>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"compile.main.only\" depends=\"init.var\" description=\"complie client sources without sinnori core work\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Compile the java code from ${src} into ${build} -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<javac debug=\"${java.debug}\" debuglevel=\"lines,vars,source\" encoding=\"UTF-8\" includeantruntime=\"false\" srcdir=\"${src}\" destdir=\"${build}\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}/ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}/in\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}/ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}/in\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</javac>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.main.only\" depends=\"compile.main.only\" description=\"make executable jar file without sinnori core work\">\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<jar destfile=\"${dist}/SinnoriAppClientMain.jar\" basedir=\"${build}\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<restrict>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<name name=\"**/*.class\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<archives>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<zips>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t\t<fileset dir=\"${corelib}/in\" includes=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t\t<fileset dir=\"${lib}/in\" includes=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t</zips>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</archives>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</restrict>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<manifest>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<attribute name=\"Main-Class\" value=\"main.SinnoriAppClientMain\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<attribute name=\"Class-Path\" value=\"${manifest.classpath}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</manifest>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</jar>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"all\" depends=\"make.main\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"is.windows.yes=${is.windows.yes}, is.unix.yes=${is.unix.yes}, java.debug=${java.debug}, core.java.debug=${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("</project>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		return stringBuilder.toString();
	}
	
	private String getWebClientAntBuildXMLFileContents() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<project name=\"");
		stringBuilder.append(mainProjectName);
		stringBuilder.append("_webclient\" default=\"compile.webclass\" basedir=\".\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<!-- set global properties for this build -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"src\" location=\"src\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"build\" location=\"build\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"dist\" location=\"dist\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"corelib\" location=\"corelib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"lib\" location=\"lib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"framework.build\" location=\"../../../../core_build\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"webclass\" location=\"../../web_app_base/ROOT/WEB-INF/classes\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"weblib\" location=\"../../web_app_base/ROOT/WEB-INF/lib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property file=\"../../ant.properties\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<property name=\"servletlib\" location=\"${tomcat.servletlib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<condition property=\"is.web.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<istrue value=\"${is.tomcat}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<condition property=\"is.windows.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<os family=\"windows\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<condition property=\"is.unix.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<os family=\"unix\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<condition property=\"is.debug.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<istrue value=\"${java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.debug\" if=\"is.debug.yes\" description=\"framework with debug info\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"core.java.debug\" value=\"debug\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.nodebug\" unless=\"is.debug.yes\" description=\"framework with no debug info\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"core.java.debug\" value=\"nodebug\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.dos\" if=\"is.windows.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"weblib\" location=\"${dos.weblib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.unix\" if=\"is.unix.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"weblib\" location=\"${unix.weblib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.var\" depends=\"init.nodebug, init.debug, init.dos, init.unix\" description=\"var init\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"java.debug=${java.debug}, core.java.debug=${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"is.web.yes=${is.web.yes}, servletlib=${servletlib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the time stamp -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<tstamp />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.unixcore\" if=\"is.unix.yes\" depends=\"init.var\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<exec dir=\"${framework.build}\" executable=\"ant\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</exec>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.doscore\" if=\"is.windows.yes\" depends=\"init.var\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<exec dir=\"${framework.build}\" executable=\"cmd\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"/c\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"ant.bat\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<arg value=\"${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</exec>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.core\" depends=\"make.doscore, make.unixcore\" description=\"make sinnori core jar file\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.directory\" depends=\"make.core\" description=\"directory init for ant compile envoroment\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"directory init for ant compile envoroment\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- core directory -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${corelib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${corelib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("    <!-- lib directory is a user define directory -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${lib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- webclass directory is a user define directory that has the web-application dynamic classes -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${webclass}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${webclass}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"copy.core\" depends=\"init.directory\" description=\"sinnori core copy\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- web-application has only extern lib jar files. not including jar lib file. -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${corelib}/ex\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset file=\"${framework.build}/dist/sinnori-core.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${corelib}/ex\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset dir=\"${framework.build}/lib/ex/\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"sinnori\" depends=\"copy.core\" description=\"copy sinnori core after making the sinnori core jar file\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"copy the sinnori core jar file after making the sinnori core jar file\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"clean.weblib\" depends=\"sinnori\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${build}/weblib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${dist}\" />\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${weblib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.weblib\" depends=\"clean.weblib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the build directory structure used by compile -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${build}/weblib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${dist}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${weblib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"compile.weblib\" if=\"is.web.yes\" depends=\"init.weblib\">\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"compile.weblib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Compile the java code from ${src} into ${build}/weblib -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<javac debug=\"${java.debug}\" debuglevel=\"lines,vars,source\" encoding=\"UTF-8\" includeantruntime=\"false\" srcdir=\"${src}\" destdir=\"${build}/weblib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<include name=\"kr/pe/sinnori/common/weblib/**\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<classpath>\t\t    ");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<pathelement location=\"${webclass}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${servletlib}\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*-api.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}\\ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}\\ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</javac>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.weblib\" depends=\"compile.weblib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"${build}/weblib\" />\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<jar jarfile=\"${dist}/sinnori-weblib.jar\" basedir=\"${build}/weblib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<restrict>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<name name=\"**/*.class\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<archives>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<!-- web-application do not include extern jar files -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</archives>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</restrict>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</jar>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"dist.weblib\" if=\"is.web.yes\" depends=\"make.weblib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${weblib}\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset file=\"${dist}/sinnori-weblib.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset file=\"${lib}/ex/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"clear.webclass\" if=\"is.web.yes\" depends=\"init.var\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t  <echo message=\"delete ${webclass}\" />\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${webclass}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.webclass\" if=\"is.web.yes\" depends=\"clear.webclass\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t  <echo message=\"mkdir ${webclass}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${webclass}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"compile.webclass\" if=\"is.web.yes\" depends=\"init.var\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"compile.webclass\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Compile the java code from ${src}/impl/** into ${webclass} -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<javac debug=\"${java.debug}\" debuglevel=\"lines,vars,source\" encoding=\"UTF-8\" ");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tincludeantruntime=\"false\" srcdir=\"${src}\" destdir=\"${webclass}\"");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\texcludes=\"kr/pe/sinnori/common/**\" >");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${dist}\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"sinnori-weblib.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${servletlib}/\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"*-api.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}\\ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}\\ex\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t</classpath>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</javac>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"all\" depends=\"dist.weblib, compile.webclass\" description=\"all work\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"is.windows.yes=${is.windows.yes}, is.unix.yes=${is.unix.yes}, java.debug=${java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("</project>");
		stringBuilder.append(System.getProperty("line.separator"));
		return stringBuilder.toString();
	}
	private String getServerBuildPathString() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("server_build");		
		return strBuilder.toString();
	}
	
	private String getServerBuildXMLFilePathString(String serverBuildPathString) {
		StringBuilder strBuilder = new StringBuilder(serverBuildPathString);
		strBuilder.append(File.separator);
		strBuilder.append("build.xml");		
		return strBuilder.toString();
	}
	
	private String getClinetBuildBasePathString() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("client_build");
		
		return strBuilder.toString();
	}
	
	
	/*private String getAppClinetBuildPathString() {
		StringBuilder strBuilder = new StringBuilder(getClinetBuildPathString());		
		strBuilder.append(File.separator);
		strBuilder.append("app_build");		
		return strBuilder.toString();
	}*/
	
	private String getAppClinetBuildPathString(String clientBuildPathString) {
		StringBuilder strBuilder = new StringBuilder(clientBuildPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("app_build");		
		return strBuilder.toString();
	}
	
	private String getAppClientBuildXMLFilePathString(String appClinetBuildPathString) {
		StringBuilder strBuilder = new StringBuilder(appClinetBuildPathString);
		strBuilder.append(File.separator);
		strBuilder.append("build.xml");		
		return strBuilder.toString();
	}
	
	/*private String geAppClientBuildFilePathString() {
		StringBuilder strBuilder = new StringBuilder(getAppClinetBuildPathString());
		strBuilder.append(File.separator);
		strBuilder.append("build.xml");
		
		return strBuilder.toString();
	}*/
	
	
	
	
	private String getWebClinetBuildPathString(String clientBuildPathString) {
		StringBuilder strBuilder = new StringBuilder(clientBuildPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("web_build");		
		return strBuilder.toString();
	}
	
	private String getWebClientBuildXMLFilePathString(String webClinetBuildPathString) {
		StringBuilder strBuilder = new StringBuilder(webClinetBuildPathString);
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

	public SequencedProperties getNewSinnoriConfig() {
		SequencedProperties sourceSequencedProperties = new SequencedProperties();
		
		List<ConfigItem> dbcpPartConfigItemList = sinnoriConfigInfo.getDBCPPartConfigItemList();
		List<ConfigItem> commonPartConfigItemList = sinnoriConfigInfo.getCommonPartConfigItemList();
		List<ConfigItem> projectPartConfigItemList = sinnoriConfigInfo.getProjectPartConfigItemList();
		
		// dbcpConnPoolNameList
		// subProjectNameList
		int dbcpConnPoolNameListSize = dbcpConnPoolNameList.size();
		
		if (dbcpConnPoolNameListSize > 0) {
			
			
			StringBuilder dbcpConnPoolNameListValueBuilder = new StringBuilder();
			String firstDBCPConnPoolName = dbcpConnPoolNameList.get(0);
			dbcpConnPoolNameListValueBuilder.append(", ");
			dbcpConnPoolNameListValueBuilder.append(firstDBCPConnPoolName);
			
			for (int i=1; i < dbcpConnPoolNameListSize; i++) {
				String dbcpConnPoolName = dbcpConnPoolNameList.get(i);
				dbcpConnPoolNameListValueBuilder.append(", ");
				dbcpConnPoolNameListValueBuilder.append(dbcpConnPoolName);
			}
			
			int inx = SinnoriConfigInfo.DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING.lastIndexOf("value");
			String descKey = new StringBuilder(SinnoriConfigInfo.DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING.substring(0, inx))
			.append("desc").toString();
			
			sourceSequencedProperties.setProperty(descKey, "dbcp 연결 폴 이름 목록, 구분자 콤마");
			sourceSequencedProperties.setProperty(SinnoriConfigInfo.DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING, dbcpConnPoolNameListValueBuilder.toString());
			
			for (String dbcpConnPoolName : dbcpConnPoolNameList) {
				
				for (ConfigItem configItem : dbcpPartConfigItemList) {
					String itemID = configItem.getItemID();
					
					String key = new StringBuilder("dbcp.")
					.append(dbcpConnPoolName)
					.append(".").append(itemID).toString();
					
					String value = null;
					
					if ("confige_file.value".equals(itemID)) {
						value = sinnoriConfigInfo.getDefaultValueOfDBCPConnPoolConfigFile(dbcpConnPoolName);
					} else {
						value = configItem.getDefaultValue();
					}
					
					inx = key.lastIndexOf("value");
					descKey = new StringBuilder(key.substring(0, inx))
					.append("desc").toString();
					String descVAlue = configItem.toDescription();
					sourceSequencedProperties.setProperty(descKey, descVAlue);
					sourceSequencedProperties.setProperty(key, value);
				}
			}
		} else {
			int inx = SinnoriConfigInfo.DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING.lastIndexOf("value");
			String descKey = new StringBuilder(SinnoriConfigInfo.DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING.substring(0, inx))
			.append("desc").toString();
			
			sourceSequencedProperties.setProperty(descKey, "dbcp 연결 폴 이름 목록, 구분자 콤마");
			sourceSequencedProperties.setProperty(SinnoriConfigInfo.DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING, "");			
		}
		
		for (ConfigItem configItem : commonPartConfigItemList) {
			String itemID = configItem.getItemID();
			String key = itemID;
			String value = configItem.getDefaultValue();
			
			int inx = key.lastIndexOf("value");
			String descKey = new StringBuilder(key.substring(0, inx))
			.append("desc").toString();
			String descVAlue = configItem.toDescription();
			sourceSequencedProperties.setProperty(descKey, descVAlue);
			sourceSequencedProperties.setProperty(key, value);
		}
		
		// FIXME!
		// int subProjectNameListSize = subProjectNameList.size();
		
		StringBuilder projectNameListValueBuilder = new StringBuilder();		
		projectNameListValueBuilder.append(mainProjectName);		
		for (String subProjectName : subProjectNameList) {
			projectNameListValueBuilder.append(", ");
			projectNameListValueBuilder.append(subProjectName);
		}
		sourceSequencedProperties.setProperty(SinnoriConfigInfo.PROJECT_NAME_LIST_KEY_STRING, projectNameListValueBuilder.toString());
		
		for (ConfigItem configItem : projectPartConfigItemList) {
			String itemID = configItem.getItemID();
			String key = new StringBuilder("project.")
			.append(mainProjectName)
			.append(".").append(itemID).toString();
			
			String value = configItem.getDefaultValue();
			
			int inx = key.lastIndexOf("value");
			String descKey = new StringBuilder(key.substring(0, inx))
			.append("desc").toString();
			String descVAlue = configItem.toDescription();
			sourceSequencedProperties.setProperty(descKey, descVAlue);
			sourceSequencedProperties.setProperty(key, value);
		}
		
		for (String subProjectName : subProjectNameList) {
			for (ConfigItem configItem : projectPartConfigItemList) {
				String itemID = configItem.getItemID();
				String key = new StringBuilder("project.")
				.append(subProjectName)
				.append(".").append(itemID).toString();
				
				String value = configItem.getDefaultValue();
				
				int inx = key.lastIndexOf("value");
				String descKey = new StringBuilder(key.substring(0, inx))
				.append("desc").toString();
				String descVAlue = configItem.toDescription();
				sourceSequencedProperties.setProperty(descKey, descVAlue);
				
				sourceSequencedProperties.setProperty(key, value);
			}
		}

		// log.info(sourceSequencedProperties.toString());
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
	
	
	public void setAppClient(boolean isAppClient) {
		this.isAppClient = isAppClient;
	}

	public void setWebClient(boolean isWebClient) {
		this.isWebClient = isWebClient;
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
	
		
	private void createFile(String title, String contents, String filePathString) throws ConfigErrorException {
		File serverBuildXMLFile = new File(filePathString);
		FileOutputStream fos = null;
		try {
			fos = FileUtils.openOutputStream(serverBuildXMLFile);			
			
			
			fos.write(contents.getBytes("UTF-8"));
			
			log.info("title={}, filePathString={} UTF8 file creation success", title, filePathString);
			
		} catch(UnsupportedEncodingException  e) {
			String errorMessage = new StringBuilder(title)
			.append("[")
			.append(filePathString)
			.append("] 생성중 문자셋 에러::")
			.append(e.getMessage()).toString();

			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		} catch(IOException e) {
			String errorMessage = new StringBuilder("서버 build.xml 파일[")
			.append(filePathString)
			.append("] 생성중 IO 에러::")
			.append(e.getMessage()).toString();

			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch(Exception e) {
					String errorMessage = new StringBuilder("서버 build.xml 파일[")
					.append(filePathString)
					.append("] 의 쓰기 쓰트림 닫기 에러::")
					.append(e.getMessage()).toString();

					log.warn(errorMessage);
				}
			}
		}
	}
	
	private void createDirectories(String basePath, String[] relativeDirectories) throws ConfigErrorException {
		for (int i=0; i < relativeDirectories.length; i++) {			
			String relativeDir = relativeDirectories[i];
			
			log.info("relativeDir[{}]=[{}]", i, relativeDir);
			
			String subDir = null;
			if (File.separator.equals("/")) {
				subDir = relativeDir;
			} else {
				subDir = relativeDir.replaceAll("/", "\\\\");
			}
			
			
			String wantedPathString = new StringBuilder(basePath).append(File.separator).append(subDir).toString();			
			
			File wantedPath = new File(wantedPathString);
			try {
				FileUtils.forceMkdir(wantedPath);
			} catch (IOException e) {
				String errorMessage = String.format("fail to make a new path[%s]", wantedPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!wantedPath.isDirectory()) {
				String errorMessage = String.format("path[%s] is not directory", wantedPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!wantedPath.canRead()) {
				String errorMessage = String.format("path[%s] cannot be read", wantedPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			if (!wantedPath.canWrite()) {
				String errorMessage = String.format("path[%s] cannot be written", wantedPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
			
			log.info("direcotry[{}] creation success", wantedPathString);
		}
	}
	
	public void removeProjectDirectory() throws ConfigErrorException {
		// projectPathString
		File projectPath = new File(projectPathString);
		try {
			FileUtils.forceDelete(projectPath);
		} catch (IOException e) {
			String errorMessage = String.format("fail to delete the project path[%s], errormessage=%s", 
					projectPathString, e.getMessage());
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
	}
}
