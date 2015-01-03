package kr.pe.sinnori.gui.lib;

import java.io.File;
import java.io.FileOutputStream;
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
		
		SequencedProperties sequencedProperties = getSourceSequencedProperties();
		
		// sinnoriConfigInfo.getProjectConfigFilePathString();
		File projectConfigFile = new File(projectConfigFilePathString);
		try {
			fos = FileUtils.openOutputStream(projectConfigFile);
			
			// Project[sample_fileupdown]'s Config File
			sequencedProperties.store(fos, new StringBuilder("Project[")
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
		String serverBuildFilePathString = getServerBuildFilePathString(serverBuildPathString);
		
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
		
		
		String appClinetBuildPathString = getAppClinetBuildPathString(clientBuildPathString);	
				
		String appClientBuildXMLFilePathString = geAppClientBuildXMLFilePathString(appClinetBuildPathString);
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
		
		String  webClinetBuildPathString = getWebClinetBuildPathString(clientBuildPathString);
		
		String webClientBuildXMLFilePathString = geWebClientBuildXMLFilePathString(webClinetBuildPathString);
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
			throw new RuntimeException(errorMessage);
		}
		
		if (!serverBuildPath.canRead()) {
			String errorMessage = String.format("server build path[%s] cannot be read", serverBuildPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		if (!serverBuildPath.canWrite()) {
			String errorMessage = String.format("server build path[%s] cannot be written", serverBuildPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		String serverBuildFilePathString = getServerBuildFilePathString(serverBuildPathString);
		
	}
	
	private String getServerAntBuildXMLFileContent() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<project name=\"");
		stringBuilder.append(mainProjectName);
		stringBuilder.append("\" default=\"compile.appinf\" basedir=\".\">");
		/*stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<description>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tSinnori");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</description>");*/
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
		stringBuilder.append("\t<condition property=\"java.debug.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<istrue value=\"${java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</condition>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"java.debug.yes.init\" if=\"java.debug.yes\" description=\"framework with debug info\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"core.java.debug\" value=\"debug\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"java.debug.no.init\" unless=\"java.debug.yes\" description=\"framework with no debug info\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"core.java.debug\" value=\"nodebug\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"dos.init\" if=\"is.windows.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"weblib\" location=\"${dos.weblib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"unix.init\" if=\"is.unix.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<property name=\"weblib\" location=\"${unix.weblib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"var.init\" depends=\"java.debug.no.init, java.debug.yes.init, dos.init, unix.init\" description=\"var init\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"java.debug=${java.debug}, core.java.debug=${core.java.debug}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"java.debug.yes=${java.debug.yes}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("  <target name=\"init.directory\" depends=\"var.init\" description=\"directory init for ant compile envoroment\">");
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
		stringBuilder.append("\t\t<mkdir dir=\"${APP-INF}/classes\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${APP-INF}/lib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${APP-INF}/resources\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"unix.make.core\" if=\"is.unix.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"make sinnori core jar file in the Unix OS\" />");
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
		stringBuilder.append("\t<target name=\"dos.make.core\" if=\"is.windows.yes\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"make sinnori core jar file in the Microsoft Windows OS\" />");
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
		stringBuilder.append("\t<target name=\"make.core\" depends=\"init.directory, dos.make.core, unix.make.core\" description=\"make sinnori core jar file\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"clean.corelib\" depends=\"make.core\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${corelib}\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.corelib\" depends=\"clean.corelib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the time stamp -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<tstamp />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${corelib}/ex\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${corelib}/in\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"copy.core\" description=\"sinnori framework core copy\" depends=\"init.corelib\">");
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
		stringBuilder.append("\t<target name=\"copy.appinf\" depends=\"copy.core\" description=\"copy from sinnori framework APP-INF/\">\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${APP-INF}/resources\" />\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${APP-INF}/lib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${APP-INF}/lib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${APP-INF}/lib\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset dir=\"${core.build}/APP-INF/lib/\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${APP-INF}/classes\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${APP-INF}/classes\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- copy todir=\"${APP-INF}/classes\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset dir=\"${core.build}/APP-INF/classes/\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<copy todir=\"${src}/kr/pe/sinnori/impl/server/mybatis/\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<fileset file=\"${core.build}/APP-INF/src/kr/pe/sinnori/impl/server/mybatis/SqlSessionFactoryManger.java\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"sinnori\" depends=\"copy.appinf\" description=\"\uC2E0\uB180\uC774 \uD504\uB808\uC784 \uC6CC\uD06C \uCEF4\uD30C\uC77C\uD6C4 jar \uB9CC\uB4E4\uC5B4 \uAC00\uC838\uC624\uAE30\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<echo message=\"\uC2E0\uB180\uC774 \uD504\uB808\uC784 \uC6CC\uD06C \uCEF4\uD30C\uC77C \uD558\uC5EC  jar \uB77C\uC774\uBE0C\uB7EC\uB9AC \uB9CC\uB4E4\uC5B4 \uAC00\uC838\uC624\uAE30\" />");
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
		stringBuilder.append("\t<target name=\"compile.main\" depends=\"init.main\" description=\"\uC11C\uBC84 \uC5B4\uD50C\uB9AC\uCF00\uC774\uC158 \uBA54\uC778 \uD074\uB798\uC2A4 \uCEF4\uD30C\uC77C\">");
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
		stringBuilder.append("\t\t\t\t<!-- pathelement path=\"${APP-INF}/classes\" / -->");
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
		stringBuilder.append("\t<target name=\"copy.lib\" depends=\"compile.main\" description=\"\uC11C\uBC84 \uC5B4\uD50C\uB9AC \uCF00\uC774\uC158 jar \uB77C\uC774\uBE0C\uB7EC\uB9AC \uBCF5\uC0AC\">");
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
		stringBuilder.append("<target name=\"clean.serverlib\" depends=\"copy.lib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Delete the ${build} and ${dist} directory trees -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${build}/serverlib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.serverlib\" depends=\"clean.main\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the time stamp -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<tstamp />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the build directory structure used by compile -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${build}/serverlib\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"compile.serverlib\" depends=\"init.serverlib\" description=\"\uC11C\uBC84 \uC804\uC6A9 \uB77C\uC774\uBE0C\uB7EC\uB9AC \uCEF4\uD30C\uC77C\">");
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
		stringBuilder.append("\t<target name=\"make.serverlib\" depends=\"compile.serverlib\" description=\"\uC11C\uBC84 \uC804\uC6A9 \uB77C\uC774\uBE0C\uB7EC\uB9AC jar \uB9CC\uB4E4\uAE30\">");
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
		stringBuilder.append("\t\t\t\t\t<!-- \uC11C\uBC84 \uB77C\uC774\uBE0C\uB7EC\uB9AC\uB294 \uB2E8\uB3C5\uC73C\uB85C \uC874\uC7AC\uD558\uBBC0\uB85C \uC678\uBD80 \uB77C\uC774\uBE0C\uB7EC\uB9AC \uD3EC\uD568\uB4F1 \uC5C6\uC74C -->");
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
		stringBuilder.append("\t<target name=\"make.main\" depends=\"make.serverlib\" description=\"\uC11C\uBC84 \uC5B4\uD50C\uB9AC\uCF00\uC774\uC158 \uBA54\uC778 \uD074\uB798\uC2A4 jar \uB9CC\uB4E4\uAE30\">\t\t\t\t");
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
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"compile.appinf\" depends=\"init.directory\" description=\"\uC624\uC9C1 \uC11C\uBC84 \uBE44\uC9C0\uB2C8\uC2A4 \uB85C\uC9C1 \uCEF4\uD30C\uC77C\uB9CC \uC218\uD589\">");
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
		stringBuilder.append("\t\t\t\t<!-- pathelement path=\"${APP-INF}/classes\" / -->");
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
		stringBuilder.append("\t<target name=\"all\" depends=\"make.main, compile.appinf\" description=\"only compile and then make jar\">");
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
	
	private void createClientAntEnvironment() {
		String clientBuildPathString = getClinetBuildPathString();
	}
	
	private String getServerBuildPathString() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("server_build");		
		return strBuilder.toString();
	}
	
	private String getServerBuildFilePathString(String serverBuildPathString) {
		StringBuilder strBuilder = new StringBuilder(serverBuildPathString);
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
	
	/*private String geAppClientBuildFilePathString() {
		StringBuilder strBuilder = new StringBuilder(getAppClinetBuildPathString());
		strBuilder.append(File.separator);
		strBuilder.append("build.xml");
		
		return strBuilder.toString();
	}*/
	
	private String geAppClientBuildXMLFilePathString(String appClinetBuildPathString) {
		StringBuilder strBuilder = new StringBuilder(appClinetBuildPathString);
		strBuilder.append(File.separator);
		strBuilder.append("build.xml");
		
		return strBuilder.toString();
	}
	
	
	private String getWebClinetBuildPathString(String clientBuildPathString) {
		StringBuilder strBuilder = new StringBuilder(clientBuildPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("web_build");		
		return strBuilder.toString();
	}
	
	private String geWebClientBuildXMLFilePathString(String webClinetBuildPathString) {
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

	public SequencedProperties getSourceSequencedProperties() {
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
					sourceSequencedProperties.setProperty(key, value);
				}
			}
		}		
		
		for (ConfigItem configItem : commonPartConfigItemList) {
			String itemID = configItem.getItemID();
			String key = itemID;
			String value = configItem.getDefaultValue();
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
			sourceSequencedProperties.setProperty(key, value);
		}
		
		for (String subProjectName : subProjectNameList) {
			for (ConfigItem configItem : projectPartConfigItemList) {
				String itemID = configItem.getItemID();
				String key = new StringBuilder("project.")
				.append(subProjectName)
				.append(".").append(itemID).toString();
				
				String value = configItem.getDefaultValue();
				sourceSequencedProperties.setProperty(key, value);
			}
		}

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
	
	/*public void rebuildAntBuild() throws RuntimeException {
		// FIXME!
		String clientBuildPathString = getClinetBuildPathString();
		String appClinetBuildPathString = getAppClinetBuildPathString(clientBuildPathString);
		String appClientBuildXMLFilePathString = geAppClientBuildXMLFilePathString(appClinetBuildPathString);
		
		String  webClinetBuildPathString = getWebClinetBuildPathString(clientBuildPathString);
		
		String webClientBuildXMLFilePathString = geWebClientBuildXMLFilePathString(webClinetBuildPathString);		
		
		File appClientBuildPath = new File(appClinetBuildPathString);
		if (!appClientBuildPath.exists()) {
			boolean isSuccess = appClientBuildPath.mkdir();
			
			if (!isSuccess) {
				String errorMessage = String.format("fail to make a new app client build path[%s]", appClinetBuildPathString);
				log.warn(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}
		
		if (!appClientBuildPath.isDirectory()) {
			String errorMessage = String.format("app client build path[%s] is not directory", appClinetBuildPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		if (!appClientBuildPath.canRead()) {
			String errorMessage = String.format("app client build path[%s] cannot be read", appClinetBuildPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		if (!appClientBuildPath.canWrite()) {
			String errorMessage = String.format("app client build path[%s] cannot be written", appClinetBuildPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		String appClientSrcPathString = new StringBuilder(appClinetBuildPathString)
		.append(File.separator).append("src").toString();
		
		File appClientSrcPath = new File(appClientSrcPathString);
		if (appClientSrcPath.exists()) {
			boolean isSuccess = appClientSrcPath.mkdir();
			
			if (!isSuccess) {
				String errorMessage = String.format("fail to make a new app client build src path[%s]", appClientSrcPathString);
				log.warn(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}
		if (!appClientSrcPath.isDirectory()) {
			String errorMessage = String.format("app client build' src path[%s] is not directory", appClientSrcPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		if (!appClientSrcPath.canRead()) {
			String errorMessage = String.format("app client build' src path[%s] cannot be read", appClientSrcPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		if (!appClientSrcPath.canWrite()) {
			String errorMessage = String.format("app client build' src path[%s] cannot be written", appClientSrcPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
				
		
		File appClientBuildFile = new File(appClientBuildXMLFilePathString);
		// FileUtils fileUtils = new FileUtils();
		FileOutputStream fos = null;
		
		try {
			fos = FileUtils.openOutputStream(appClientBuildFile);
			
			String appClientBuildXMLFileConents = getAppClientBuildXMLFileConents();
			
			fos.write(appClientBuildXMLFileConents.getBytes("UTF-8"));
			
		} catch(UnsupportedEncodingException  e) {
			String errorMessage = new StringBuilder("일반 응용 프로그램 build.xml 내용 쓰기 작업중 문자셋 에러::")
			.append(e.getMessage()).toString();

			log.warn(errorMessage);
			
			throw new RuntimeException(errorMessage);
		} catch(IOException e) {
			String errorMessage = new StringBuilder("일반 응용 프로그램 build.xml 내용 쓰기 작업중 IO 에러::")
			.append(e.getMessage()).toString();

			log.warn(errorMessage);
			
			throw new RuntimeException(errorMessage);
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch(Exception e) {
					log.warn("fail to close the appliction client build.xml file", e);
				}
			}
		}
		
		
		File webClientBuildPath = new File(webClinetBuildPathString);
		if (!webClientBuildPath.exists()) {
			boolean isSuccess = webClientBuildPath.mkdir();
			
			if (!isSuccess) {
				String errorMessage = String.format("fail to make a new web client build src path[%s]", webClinetBuildPathString);
				log.warn(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}
		
		if (!webClientBuildPath.isDirectory()) {
			String errorMessage = String.format("web client build path[%s] is not directory", webClinetBuildPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		if (!webClientBuildPath.canRead()) {
			String errorMessage = String.format("web client build path[%s] cannot be read", webClinetBuildPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		if (!webClientBuildPath.canWrite()) {
			String errorMessage = String.format("web client build path[%s] cannot be written", webClinetBuildPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}
	
	private String getAppClientBuildXMLFileConents() {
		return null;
	}*/
}
