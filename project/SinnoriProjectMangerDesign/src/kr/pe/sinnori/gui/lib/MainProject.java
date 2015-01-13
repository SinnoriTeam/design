package kr.pe.sinnori.gui.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	
	public static final String SINNORI_LOGBACK_LOG_FILE_NAME = "logback.xml";
	public static final String SINNORI_CONFIG_FILE_NAME = "sinnori.properties";
	
	public static final String SERVER_EXECUTABLE_JAR_SHORT_FILE_NAME = "SinnoriServerRun.jar";
	public static final String APPCLIENT_EXECUTABLE_JAR_SHORT_FILE_NAME = "SinnoriAppClientRun.jar";
		
	/** server main class file's package and class name is free */
	public static final String DEFAULT_SERVER_MAIN_CLASS_NAME = "main.SinnoriServerMain";
	/** general application client main class file's package and class name is free */
	public static final String DEFAULT_APPCLIENT_MAIN_CLASS_NAME= "main.SinnoriAppClientMain";
	
	public static final String NEWLINE = System.getProperty("line.separator");
	
	private String mainProjectName;
	private String projectPathString;
	// private SequencedProperties sourceSequencedProperties;
	
	private String projectConfigFilePathString = null;
	// private String appClinetBuildPathString = null;
	private File appClientBuildPath = null;
	// private String  webClinetBuildPathString = null;
	private File  webClientBuildPath = null;
	
	
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
		this.projectConfigFilePathString = getProjectConfigFilePathStringFromProjectPath(projectPathString);
		
		
		String antPropertiesFilePathString = getAntPropertiesFilePath();
		
		SequencedProperties antProperties = getSequencedPropertiesFromFile(antPropertiesFilePathString);
		
		checkAntProperties(antProperties);
		
		servletEnginLibPathString = antProperties.getProperty("tomcat.servletlib");
		
		checkSeverAntEnvironment();
		checkClientAntEnvironment();
		
		makeProjectNameSetFromSourceProperties(sourceSequencedProperties);
		makeDBCPCOnnectionPoolNameSetFromSourceProperties(sourceSequencedProperties);		
	}
	
	public static  SequencedProperties getSequencedPropertiesFromFile(String properteisFilePathString) throws ConfigErrorException {
		SequencedProperties sourceSequencedProperties = new SequencedProperties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(properteisFilePathString);
			sourceSequencedProperties.load(fis);
		} catch (FileNotFoundException e) {
			String errorMessage = String.format("prop file[%s] not found", properteisFilePathString); 
			// log.info(errorMessage);
			throw new ConfigErrorException(errorMessage);
		} catch (IOException e) {
			String errorMessage = String.format("prop file[%s] IOException[%s]", 
					properteisFilePathString, e.getMessage());
			// log.info(errorMessage);
			throw new ConfigErrorException(errorMessage);
		} finally {
			if (null != fis) {
				try {
					fis.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return sourceSequencedProperties;
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
		
		String relativeDirectories[] = {"config", "impl/message/info", "rsa_keypair", 
				"log/apache", "log/client", "log/server", "log/servlet"
				};

		createDirectories(projectPathString, relativeDirectories);
		
		this.sinnoriConfigInfo = new SinnoriConfigInfo(mainProjectName, projectPathString);		
		this.projectConfigFilePathString = getProjectConfigFilePathStringFromProjectPath(projectPathString);
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
			antPropeties.put("server.main.class", DEFAULT_SERVER_MAIN_CLASS_NAME);
			antPropeties.put("appclient.main.class", DEFAULT_APPCLIENT_MAIN_CLASS_NAME);
			
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
		
		/** <project home>/config/sinnori.properties */
		SequencedProperties newSinnoriConfig = getNewSinnoriConfigFromSinnoriConfigInfo();
		
		saveConfigFile(newSinnoriConfig);
		
		/** <project home>/config/logback.xml */
		String logbackFilePathString = new StringBuilder(projectPathString)
		.append(File.separator).append("config")
		.append(File.separator).append("logback.xml").toString();
		
		createFile("logback config file", getContentsOfLogback(), logbackFilePathString);
		
		/** <project home>/impl/message/info/Echo.xml */
		String echoMessageXMLFilePathString = new StringBuilder(projectPathString)
		.append(File.separator).append("impl")
		.append(File.separator).append("message")
		.append(File.separator).append("info")
		.append(File.separator).append("Echo.xml").toString();
		
		createFile("echo message", getEchoMessageInfoContents(), echoMessageXMLFilePathString);
		
		
		createSeverAntEnvironment();
		createClientAntEnvironment();
	}
	
	private String getContentsOfLogback() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("<configuration>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<appender name=\"logfile\" class=\"ch.qos.logback.core.rolling.RollingFileAppender\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<file>${sinnori.logPath}/logger.log</file>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<encoder>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</pattern>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</encoder>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<rollingPolicy class=\"ch.qos.logback.core.rolling.TimeBasedRollingPolicy\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<fileNamePattern>${sinnori.logPath}/logFile.%d{yyyy-MM-dd}.log</fileNamePattern>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<maxHistory>15</maxHistory>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</rollingPolicy>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</appender>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<appender name=\"console\" class=\"ch.qos.logback.core.ConsoleAppender\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<encoder>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<pattern>%d %-5level [%thread] %msg\\(%F:%L\\)%n</pattern>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</encoder>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</appender>\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<root level=\"INFO\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<appender-ref ref=\"console\"/>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</root>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<logger name=\"kr.pe.sinnori\" level=\"INFO\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<appender-ref ref=\"logfile\"/>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</logger>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<!-- \"mapper\" tag's attribute namespace in the mybatis mapper xml file -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<logger name=\"kr.pr.sinnori.testweb\" level=\"DEBUG\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<appender-ref ref=\"logfile\"/>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</logger>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("</configuration>");
		stringBuilder.append(NEWLINE);
		return stringBuilder.toString();
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
		appClientBuildPath = new File(appClinetBuildPathString);
		
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
		
		String webClinetBuildPathString = getWebClinetBuildPathString(clientBuildBasePathString);
		webClientBuildPath = new File(webClinetBuildPathString);
		
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
		
		/** build.xml */
		String serverBuildXMLFilePathString = getServerBuildXMLFilePathString(serverBuildPathString);
		createFile("server build.xml", getServerAntBuildXMLFileContent(), serverBuildXMLFilePathString);
					
		String relativeExecutabeJarFileName = new StringBuilder("dist")
		.append(File.separator).append(SERVER_EXECUTABLE_JAR_SHORT_FILE_NAME).toString();
		
		/** <main project name>Server.bat */
		String dosShellFilePathString = new StringBuilder(serverBuildPathString)
		.append(File.separator).append(mainProjectName).append("Server.bat").toString();
		
		createFile("dos shell of server", getDosShellContents("-Xmx1024m -Xms1024m", "server", 
				serverBuildPathString, relativeExecutabeJarFileName), dosShellFilePathString);
		
		/** <main project name>Server.sh */
		String unixShellFilePathString = new StringBuilder(serverBuildPathString)
		.append(File.separator).append(mainProjectName).append("Server.sh").toString();
		createFile("unix shell of server", getUnixShellContents("-Xmx1024m -Xms1024m", "server", 
				serverBuildPathString, relativeExecutabeJarFileName), unixShellFilePathString);
		
		String relativeDirectories[] = {"src", "src/main", 
				"src/kr/pe/sinnori/common/serverlib", "src/kr/pe/sinnori/impl/message",
				"src/kr/pe/sinnori/impl/server/mybatis", "src/kr/pe/sinnori/impl/servertask",
				"APP-INF/lib", "APP-INF/resources"};
		createDirectories(serverBuildPathString, relativeDirectories);
			
		/**
		 * create source file having DEFAULT_SERVER_MAIN_CLASS_NAME
		 * ex) server_build/src/main/SinnoriServerMain.java
		 */
		String serverMainSrcFilePathString = null;
		if (File.separator.equals("/")) {
			String subStr = DEFAULT_SERVER_MAIN_CLASS_NAME.replaceAll("\\.", "/");
			serverMainSrcFilePathString = new StringBuilder(serverBuildPathString)
			.append(File.separator).append("src")
			.append(File.separator).append(subStr)
			.append(".java").toString();
			
			log.info("subStr=[{}], serverMainSrcFilePathString=[{}]", subStr, serverMainSrcFilePathString);
		} else {
			String subStr = DEFAULT_SERVER_MAIN_CLASS_NAME.replaceAll("\\.", "\\\\");
			
			serverMainSrcFilePathString = new StringBuilder(serverBuildPathString)
			.append(File.separator).append("src")
			.append(File.separator).append(subStr)
			.append(".java").toString();
			
			log.info("subStr=[{}], serverMainSrcFilePathString=[{}]", subStr, serverMainSrcFilePathString);
		}
		
		
		
		createFile("main class source of server", getDefaultServerMainClassContents(), serverMainSrcFilePathString);
		
		/** server_build/src/kr/pe/sinnori/impl/servertask/EchoServerTask.java */		
		String echoServerTaskSrcFilePathString = new StringBuilder(serverBuildPathString)
		.append(File.separator).append("src")
		.append(File.separator).append("kr")
		.append(File.separator).append("pe")
		.append(File.separator).append("sinnori")
		.append(File.separator).append("impl")
		.append(File.separator).append("servertask")
		.append(File.separator).append("EchoServerTask.java").toString();
		
		createFile("echo server task source file", getEchoServerTaskContents(), echoServerTaskSrcFilePathString);
	}
	
	/** server_build/src/kr/pe/sinnori/impl/servertask/EchoServerTask.java */	
	private String getEchoServerTaskContents() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("/*");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * Licensed to the Apache Software Foundation (ASF) under one or more");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * contributor license agreements.  See the NOTICE file distributed with");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * this work for additional information regarding copyright ownership.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * The ASF licenses this file to You under the Apache License, Version 2.0");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * (the \"License\"); you may not use this file except in compliance with");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * the License.  You may obtain a copy of the License at");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * ");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" *      http://www.apache.org/licenses/LICENSE-2.0");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * ");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * Unless required by applicable law or agreed to in writing, software");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * distributed under the License is distributed on an \"AS IS\" BASIS,");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * See the License for the specific language governing permissions and");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * limitations under the License.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" */");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("package kr.pe.sinnori.impl.servertask;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.configuration.ServerProjectConfig;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.message.AbstractMessage;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.impl.message.Echo.Echo;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.server.LoginManagerIF;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.server.executor.AbstractServerTask;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.server.executor.LetterSender;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("/**");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * Echo \uC11C\uBC84 \uD0C0\uC2A4\uD06C");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" * @author \"Won Jonghoon\"");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" *");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append(" */");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("public final class EchoServerTask extends AbstractServerTask {\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t@Override");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\tpublic void doTask(ServerProjectConfig serverProjectConfig,");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tLoginManagerIF loginManager,");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tLetterSender letterSender, AbstractMessage messageFromClient)");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tthrows Exception {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tdoWork(serverProjectConfig, letterSender, (Echo)messageFromClient);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\tprivate void doWork(ServerProjectConfig serverProjectConfig,");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tLetterSender letterSender, Echo echoInObj)");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tthrows Exception {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t// FIXME!");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t// log.info(\"echoInObj={}\", echoInObj.toString());\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tEcho echoOutObj = new Echo();");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\techoOutObj.setRandomInt(echoInObj.getRandomInt());");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\techoOutObj.setStartTime(echoInObj.getStartTime());");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tletterSender.addSyncMessage(echoOutObj);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t}\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("}");
		stringBuilder.append(System.getProperty("line.separator"));
		return stringBuilder.toString();
	}
	
	/** server_build/src/main/SinnoriServerMain.java */
	private String getDefaultServerMainClassContents() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("package main;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.exception.NotFoundProjectException;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.lib.CommonRootIF;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.lib.CommonStaticFinalVars;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.server.ServerProject;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.server.ServerProjectManager;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("public class SinnoriServerMain implements CommonRootIF {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\tpublic static void main(String argv[]) throws NotFoundProjectException {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tString projectName = System.getProperty(CommonStaticFinalVars.SINNORI_PROJECT_NAME_JAVA_SYSTEM_VAR_NAME);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tif (null == projectName) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.error(\"\uC790\uBC14 \uC2DC\uC2A4\uD15C \uD658\uACBD \uBCC0\uC218[{}] \uAC00 \uC815\uC758\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4.\", CommonStaticFinalVars.SINNORI_PROJECT_NAME_JAVA_SYSTEM_VAR_NAME);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tSystem.exit(1);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tString trimProjectName = projectName.trim();");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tif (trimProjectName.length() == 0) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.error(\"\uC790\uBC14 \uC2DC\uC2A4\uD15C \uD658\uACBD \uBCC0\uC218[{}] \uAC12[{}]\uC774 \uBE48 \uBB38\uC790\uC5F4 \uC788\uC2B5\uB2C8\uB2E4.\", CommonStaticFinalVars.SINNORI_PROJECT_NAME_JAVA_SYSTEM_VAR_NAME, projectName);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tSystem.exit(1);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tif (! projectName.equals(trimProjectName)) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.error(\"\uC790\uBC14 \uC2DC\uC2A4\uD15C \uD658\uACBD \uBCC0\uC218[{}] \uAC12[{}] \uC55E\uB4A4\uB85C \uACF5\uBC31 \uBB38\uC790\uC5F4\uC774 \uC874\uC7AC\uD569\uB2C8\uB2E4.\", ");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\tCommonStaticFinalVars.SINNORI_PROJECT_NAME_JAVA_SYSTEM_VAR_NAME, projectName);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tSystem.exit(1);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t/*try {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tSqlSessionFactory sqlSessionFactory = SqlSessionFactoryManger.getInstance().getSqlSessionFactory(\"tw_sinnoridb\");");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tsqlSessionFactory.openSession();");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t} catch (DBNotReadyException e) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t// TODO Auto-generated catch block");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\te.printStackTrace();");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t}*/");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t/** FIXME! */");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tServerProject serverProject = ServerProjectManager.getInstance().getServerProject(projectName);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tserverProject.startServer();");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("}");
		stringBuilder.append(System.getProperty("line.separator"));
		return stringBuilder.toString();
	}
	
	/** impl/message/info/Echo.xml */
	private String getEchoMessageInfoContents() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("<!--");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\titem type : (unsigned) byte, (unsigned) short, (unsigned) integer, long,");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\tfixed length string, ");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\tub pascal string, us pascal string, si pascal string, ");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\tfixed length byte[], variable length byte[] ");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\tarray counter type : reference 변수참조, direct 직접입력");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\tdirection : FROM_NONE_TO_NONE, FROM_SERVER_TO_CLINET, FROM_CLIENT_TO_SERVER, FROM_ALL_TO_ALL");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t(1) FROM_NONE_TO_NONE : 메시지는 서버에서 클라이언트로 혹은 클라이언트에서 서버로 양쪽 모두에서 전송되지 않는다.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t(2) FROM_SERVER_TO_CLINET : 메시지는 서버에서 클라이언트로만 전송된다.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t(3) FROM_CLIENT_TO_SERVER : 메시지는 클라이언트에서 서버로만 전송된다.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t(4) FROM_ALL_TO_ALL : 메시지는 서버에서 클라이언트로도 혹은 클라이언트에서 서버로 양쪽 모두에서 전송된다.");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("-->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("<sinnori_message>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("<messageID>Echo</messageID>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("<direction>FROM_ALL_TO_ALL</direction>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("<desc>에코 메시지</desc>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("<singleitem name=\"randomInt\" type=\"integer\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("<singleitem name=\"startTime\" type=\"long\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("</sinnori_message>");
		stringBuilder.append(System.getProperty("line.separator"));
		return stringBuilder.toString();
	}
	
	/** server_build/<main project name>.sh or client_build/app_build/<main project name>Client.sh */
	private String getDosShellContents(String jvmOptions, String logName, String workingPathString, String relativeExecutabeJarFileName) {
		String commonPartOfShellContents = getCommonPartOfShellContents(jvmOptions, "^", logName, relativeExecutabeJarFileName);
		
		StringBuilder shellContentsBuilder = new StringBuilder();
		shellContentsBuilder.append("set OLDPWD=%CD%");
		shellContentsBuilder.append(NEWLINE);
		
		shellContentsBuilder.append("cd /D ");
		shellContentsBuilder.append(workingPathString);
		shellContentsBuilder.append(NEWLINE);
		
		shellContentsBuilder.append(commonPartOfShellContents);
		shellContentsBuilder.append(NEWLINE);
		
		shellContentsBuilder.append("cd /D %OLDPWD%");
		return shellContentsBuilder.toString();
	}
	
	/** server_build/<main project name>Server.bat or client_build/app_build/<main project name>Client.bat */
	private String getUnixShellContents(String jvmOptions, String logName, String workingPathString, String relativeExecutabeJarFileName) {
		String commonPartOfShellContents = getCommonPartOfShellContents(jvmOptions, "\\", logName, relativeExecutabeJarFileName);
		
		StringBuilder shellContentsBuilder = new StringBuilder();
		shellContentsBuilder.append("cd ");
		shellContentsBuilder.append(workingPathString);
		shellContentsBuilder.append(NEWLINE);
		
		shellContentsBuilder.append(commonPartOfShellContents);
		shellContentsBuilder.append(NEWLINE);
		
		shellContentsBuilder.append("cd -");
		return shellContentsBuilder.toString();
	}
	
	private String getCommonPartOfShellContents(String jvmOptions, String shellLineSeparator, 
			String logName, String relativeExecutabeJarFileName) {
		StringBuilder commandPartBuilder = new StringBuilder();
		
		commandPartBuilder.append("java ");
		// -Xmx1024m -Xms1024m
		commandPartBuilder.append(jvmOptions);
		commandPartBuilder.append(" ").append(shellLineSeparator).append(NEWLINE);		
		
		commandPartBuilder.append("-Dlogback.configurationFile=").append(projectPathString)
		.append(File.separator)
		.append("config")
		.append(File.separator)
		.append(SINNORI_LOGBACK_LOG_FILE_NAME);
		commandPartBuilder.append(" ").append(shellLineSeparator).append(NEWLINE);
		
		commandPartBuilder.append("-Dsinnori.logPath=")
		.append(projectPathString)
		.append(File.separator).append("log")
		.append(File.separator).append(logName);
		commandPartBuilder.append(" ").append(shellLineSeparator).append(NEWLINE);
		
		commandPartBuilder.append("-Dsinnori.configurationFile=")
		.append(projectPathString)
		.append(File.separator)
		.append("config")
		.append(File.separator)
		.append(SINNORI_CONFIG_FILE_NAME);
		commandPartBuilder.append(" ").append(shellLineSeparator).append(NEWLINE);
		
		commandPartBuilder.append("-Dsinnori.projectName=")
		.append(mainProjectName);
		commandPartBuilder.append(" ").append(shellLineSeparator).append(NEWLINE);
		
		// -jar /home/madang01/gitsinnori/sinnori/project/sample_test/server_build/dist/SinnoriServerMain.jar
		commandPartBuilder.append("-jar ")
		.append(relativeExecutabeJarFileName);
		
		return commandPartBuilder.toString();
	}
	
	/** server build.xml */
	
	/** server_build/build.xml */
	private String getServerAntBuildXMLFileContent() {
		StringBuilder stringBuilder = new StringBuilder();
		// stringBuilder.append("<project name=\"sample_base_server\" default=\"compile.appinf\" basedir=\".\">");
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
		stringBuilder.append("\t<target name=\"copy.jarlib\" depends=\"sinnori\" description=\"copy jar library\">");
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
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"clean.main\" depends=\"copy.jarlib\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Delete the ${build} and ${dist} directory trees -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<delete dir=\"${build}/main\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"init.main\" depends=\"clean.main\">\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<!-- Create the build directory structure used by compile -->");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<mkdir dir=\"${build}/main\" />");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t</target>");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"compile.main\" depends=\"init.main\" description=\"compile the server library\">");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t<javac debug=\"${java.debug}\" debuglevel=\"lines,vars,source\" encoding=\"UTF-8\" includeantruntime=\"false\" srcdir=\"${src}\" destdir=\"${build}/main\" >");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t<exclude name=\"kr/pe/sinnori/impl/**\" />");
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
		stringBuilder.append("\t");
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
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"make.main\" depends=\"compile.main\" description=\"make the server executable jar file\">");
		stringBuilder.append(System.getProperty("line.separator"));
		
		// stringBuilder.append("\t\t<jar destfile=\"${dist}/SinnoriServerRun.jar\" basedir=\"${build}/main\">");
		stringBuilder.append("\t\t<jar destfile=\"${dist}/");
		stringBuilder.append(SERVER_EXECUTABLE_JAR_SHORT_FILE_NAME);
		stringBuilder.append("\" basedir=\"${build}\">");
		
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
		stringBuilder.append("\t\t\t\t<attribute name=\"Main-Class\" value=\"${server.main.class}\" />");
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
		return stringBuilder.toString();
	}

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
		
		String appClinetBuildPathString = getAppClinetBuildPathString(clientBuildBasePathString);
		this.appClientBuildPath = new File(appClinetBuildPathString);
		this.setAppClient(true);
		
		
		String webClinetBuildPathString = getWebClinetBuildPathString(clientBuildBasePathString);	
		webClientBuildPath = new File(webClinetBuildPathString);
		this.setWebClient(false);
		// FIXME!	
	}
	
	/** client_build/app_build/src/main/SinnoriAppClientMain.java */
	private String getDefaultAppClientMainClassContents() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("package main;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import java.net.SocketTimeoutException;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.client.ClientProject;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.client.ClientProjectManager;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.exception.BodyFormatException;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.exception.DynamicClassCallException;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.exception.NoMoreDataPacketBufferException;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.exception.NotFoundProjectException;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.exception.NotLoginException;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.exception.ServerNotReadyException;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.exception.ServerTaskException;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.lib.CommonStaticFinalVars;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.common.message.AbstractMessage;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import kr.pe.sinnori.impl.message.Echo.Echo;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import org.slf4j.Logger;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("import org.slf4j.LoggerFactory;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("public class SinnoriAppClientMain {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\tpublic static void main(String[] args) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tLogger log = LoggerFactory.getLogger(\"kr.pe.sinnori\");");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tlog.info(\"start\");");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tString projectName = System.getProperty(CommonStaticFinalVars.SINNORI_PROJECT_NAME_JAVA_SYSTEM_VAR_NAME);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tif (null == projectName) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.error(\"\uC790\uBC14 \uC2DC\uC2A4\uD15C \uD658\uACBD \uBCC0\uC218[{}] \uAC00 \uC815\uC758\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4.\", CommonStaticFinalVars.SINNORI_PROJECT_NAME_JAVA_SYSTEM_VAR_NAME);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tSystem.exit(1);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tString trimProjectName = projectName.trim();");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tif (trimProjectName.length() == 0) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.error(\"\uC790\uBC14 \uC2DC\uC2A4\uD15C \uD658\uACBD \uBCC0\uC218[{}] \uAC12[{}]\uC774 \uBE48 \uBB38\uC790\uC5F4 \uC788\uC2B5\uB2C8\uB2E4.\", CommonStaticFinalVars.SINNORI_PROJECT_NAME_JAVA_SYSTEM_VAR_NAME, projectName);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tSystem.exit(1);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tif (! projectName.equals(trimProjectName)) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.error(\"\uC790\uBC14 \uC2DC\uC2A4\uD15C \uD658\uACBD \uBCC0\uC218[{}] \uAC12[{}] \uC55E\uB4A4\uB85C \uACF5\uBC31 \uBB38\uC790\uC5F4\uC774 \uC874\uC7AC\uD569\uB2C8\uB2E4.\", ");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\tCommonStaticFinalVars.SINNORI_PROJECT_NAME_JAVA_SYSTEM_VAR_NAME, projectName);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tSystem.exit(1);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tClientProject clientProject = null;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\ttry {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tclientProject = ClientProjectManager.getInstance().getClientProject(projectName);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t} catch (NotFoundProjectException e) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.error(\"NotFoundProjectException\", e);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tSystem.exit(1);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tjava.util.Random random = new java.util.Random();");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tEcho echoInObj = new Echo();");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\techoInObj.setRandomInt(random.nextInt());");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\techoInObj.setStartTime(new java.util.Date().getTime());");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\tAbstractMessage messageFromServer = null;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\ttry {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tmessageFromServer = clientProject.sendSyncInputMessage(echoInObj);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tif (messageFromServer instanceof Echo) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\tEcho echoOutObj = (Echo)messageFromServer;");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\tif ((echoInObj.getRandomInt() == echoOutObj.getRandomInt()) && (echoInObj.getStartTime() == echoOutObj.getStartTime())) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\tlog.info(\"\uC131\uACF5::echo \uBA54\uC2DC\uC9C0 \uC785\uB825/\uCD9C\uB825 \uB3D9\uC77C\uD568\");");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t} else {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t\tlog.info(\"\uC2E4\uD328::echo \uBA54\uC2DC\uC9C0 \uC785\uB825/\uCD9C\uB825 \uB2E4\uB984\");");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t} else {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t\tlog.warn(\"messageFromServer={}\", messageFromServer.toString());");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t} catch (SocketTimeoutException e) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.warn(\"SocketTimeoutException\", e);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t} catch (ServerNotReadyException e) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.warn(\"ServerNotReadyException\", e);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t} catch (NoMoreDataPacketBufferException e) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.warn(\"NoMoreDataPacketBufferException\", e);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t} catch (BodyFormatException e) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.warn(\"BodyFormatException\", e);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t} catch (DynamicClassCallException e) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.warn(\"DynamicClassCallException\", e);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t} catch (ServerTaskException e) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.warn(\"ServerTaskException\", e);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t} catch (NotLoginException e) {");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t\tlog.warn(\"NotLoginException\", e);");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t\t}\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t}");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("}");
		stringBuilder.append(System.getProperty("line.separator"));
		return stringBuilder.toString();
	}
	
	/** general application client build.xml */
	/** client_build/app_build/build.xml */
	private String getAppClientAntBuildXMLFileContents() {		
		// FIXME!
		StringBuilder stringBuilder = new StringBuilder();
		// stringBuilder.append("<project name=\"sample_base_appclient\" default=\"make.main.only\" basedir=\".\">");
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
		stringBuilder.append("\t\t<delete dir=\"${corelib}\" />");
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
		
		// stringBuilder.append("\t\t<jar destfile=\"${dist}/SinnoriAppClientRun.jar\" basedir=\"${build}\">");
		stringBuilder.append("\t\t<jar destfile=\"${dist}/");
		stringBuilder.append(APPCLIENT_EXECUTABLE_JAR_SHORT_FILE_NAME);
		stringBuilder.append("\" basedir=\"${build}\">");
		
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
		stringBuilder.append("\t\t\t\t<attribute name=\"Main-Class\" value=\"${appclient.main.class}\" />");
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
		stringBuilder.append("\t");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("\t<target name=\"copy.jarlib.only\" depends=\"compile.main.only\">");
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
		stringBuilder.append("\t<target name=\"make.main.only\" depends=\"copy.jarlib.only\" description=\"make executable jar file without sinnori core work\">\t\t");
		stringBuilder.append(System.getProperty("line.separator"));
		
		// stringBuilder.append("\t\t<jar destfile=\"${dist}/SinnoriAppClientRun.jar\" basedir=\"${build}\">");
		stringBuilder.append("\t\t<jar destfile=\"${dist}/");
		stringBuilder.append(APPCLIENT_EXECUTABLE_JAR_SHORT_FILE_NAME);
		stringBuilder.append("\" basedir=\"${build}\">");
		
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
		stringBuilder.append("\t\t\t\t<attribute name=\"Main-Class\" value=\"${appclient.main.class}\" />");
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
	
	/** client_build/web_build/build.xml */
	private String getWebClientAntBuildXMLFileContents() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<project name=\"");
		stringBuilder.append(mainProjectName);
		stringBuilder.append("_webclient\" default=\"compile.webclass\" basedir=\".\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<!-- set global properties for this build -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<property name=\"src\" location=\"src\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<property name=\"build\" location=\"build\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<property name=\"dist\" location=\"dist\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<property name=\"corelib\" location=\"corelib\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<property name=\"lib\" location=\"lib\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<property name=\"framework.build\" location=\"../../../../core_build\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<property name=\"webclass\" location=\"../../web_app_base/ROOT/WEB-INF/classes\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<property name=\"weblib\" location=\"../../web_app_base/ROOT/WEB-INF/lib\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<property file=\"../../ant.properties\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<property name=\"servletlib\" location=\"${tomcat.servletlib}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<condition property=\"is.web.yes\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<istrue value=\"${is.tomcat}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</condition>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<condition property=\"is.windows.yes\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<os family=\"windows\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</condition>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<condition property=\"is.unix.yes\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<os family=\"unix\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</condition>\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<condition property=\"is.debug.yes\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<istrue value=\"${java.debug}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</condition>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"init.debug\" if=\"is.debug.yes\" description=\"framework with debug info\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<property name=\"core.java.debug\" value=\"debug\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"init.nodebug\" unless=\"is.debug.yes\" description=\"framework with no debug info\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<property name=\"core.java.debug\" value=\"nodebug\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"init.dos\" if=\"is.windows.yes\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<property name=\"weblib\" location=\"${dos.weblib}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"init.unix\" if=\"is.unix.yes\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<property name=\"weblib\" location=\"${unix.weblib}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"init.var\" depends=\"init.nodebug, init.debug, init.dos, init.unix\" description=\"var init\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<echo message=\"java.debug=${java.debug}, core.java.debug=${core.java.debug}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<echo message=\"is.web.yes=${is.web.yes}, servletlib=${servletlib}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<!-- Create the time stamp -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<tstamp />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"make.unixcore\" if=\"is.unix.yes\" depends=\"init.var\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<exec dir=\"${framework.build}\" executable=\"ant\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<arg value=\"${core.java.debug}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</exec>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"make.doscore\" if=\"is.windows.yes\" depends=\"init.var\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<exec dir=\"${framework.build}\" executable=\"cmd\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<arg value=\"/c\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<arg value=\"ant.bat\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<arg value=\"${core.java.debug}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</exec>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"make.core\" depends=\"make.doscore, make.unixcore\" description=\"make sinnori core jar file\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"init.directory\" depends=\"make.core\" description=\"directory init for ant compile envoroment\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<echo message=\"directory init for ant compile envoroment\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<!-- core directory -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<delete dir=\"${corelib}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<mkdir dir=\"${corelib}/ex\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("    <!-- lib directory is a user define directory -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<mkdir dir=\"${lib}/ex\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<!-- webclass directory is a user define directory that has the web-application dynamic classes -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<delete dir=\"${webclass}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<mkdir dir=\"${webclass}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"copy.core\" depends=\"init.directory\" description=\"sinnori core copy\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<!-- web-application has only extern lib jar files. not including jar lib file. -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<copy todir=\"${corelib}/ex\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<fileset file=\"${framework.build}/dist/sinnori-core.jar\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<copy todir=\"${corelib}/ex\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<fileset dir=\"${framework.build}/lib/ex/\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</copy>\t\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"sinnori\" depends=\"copy.core\" description=\"copy sinnori core after making the sinnori core jar file\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<echo message=\"copy the sinnori core jar file after making the sinnori core jar file\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"clean.weblib\" depends=\"sinnori\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<delete dir=\"${build}/weblib\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<delete dir=\"${dist}\" />\t\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<delete dir=\"${weblib}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"init.weblib\" depends=\"clean.weblib\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<!-- Create the build directory structure used by compile -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<mkdir dir=\"${build}/weblib\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<mkdir dir=\"${dist}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<mkdir dir=\"${weblib}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"compile.weblib\" if=\"is.web.yes\" depends=\"init.weblib\">\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<echo message=\"compile.weblib\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<!-- Compile the java code from ${src} into ${build}/weblib -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<javac debug=\"${java.debug}\" debuglevel=\"lines,vars,source\" encoding=\"UTF-8\" includeantruntime=\"false\" srcdir=\"${src}\" destdir=\"${build}/weblib\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<include name=\"kr/pe/sinnori/common/weblib/**\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<classpath>\t\t    ");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t<pathelement location=\"${webclass}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t<fileset dir=\"${servletlib}\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*-api.jar\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}\\ex\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}\\ex\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t</classpath>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</javac>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"make.weblib\" depends=\"compile.weblib\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<echo message=\"${build}/weblib\" />\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<jar jarfile=\"${dist}/sinnori-weblib.jar\" basedir=\"${build}/weblib\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<restrict>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t<name name=\"**/*.class\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t<archives>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t\t<!-- web-application do not include extern jar files -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t</archives>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t</restrict>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</jar>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"dist.weblib\" if=\"is.web.yes\" depends=\"make.weblib\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<copy todir=\"${weblib}\" verbose=\"true\" overwrite=\"true\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<fileset file=\"${dist}/sinnori-weblib.jar\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<fileset file=\"${lib}/ex/*.jar\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</copy>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"clear.webclass\" if=\"is.web.yes\" depends=\"init.var\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t  <echo message=\"delete ${webclass}\" />\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<delete dir=\"${webclass}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"init.webclass\" if=\"is.web.yes\" depends=\"clear.webclass\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t  <echo message=\"mkdir ${webclass}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<mkdir dir=\"${webclass}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"compile.webclass\" if=\"is.web.yes\" depends=\"init.var\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<echo message=\"compile.webclass\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<!-- Compile the java code from ${src}/impl/** into ${webclass} -->");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<javac debug=\"${java.debug}\" debuglevel=\"lines,vars,source\" encoding=\"UTF-8\" ");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\tincludeantruntime=\"false\" srcdir=\"${src}\" destdir=\"${webclass}\"");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\texcludes=\"kr/pe/sinnori/common/**\" >");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t<classpath>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t<fileset dir=\"${dist}\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t\t<include name=\"sinnori-weblib.jar\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t<fileset dir=\"${servletlib}/\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t\t<include name=\"*-api.jar\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t<fileset dir=\"${corelib}\\ex\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t<fileset dir=\"${lib}\\ex\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t\t<include name=\"**/*.jar\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t\t</fileset>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t\t</classpath>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t</javac>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t<target name=\"all\" depends=\"dist.weblib, compile.webclass\" description=\"all work\">");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t\t<echo message=\"is.windows.yes=${is.windows.yes}, is.unix.yes=${is.unix.yes}, java.debug=${java.debug}\" />");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("\t</target>");
		stringBuilder.append(NEWLINE);
		stringBuilder.append("</project>");
		stringBuilder.append(NEWLINE);
		return stringBuilder.toString();
	}
	
	
	public static String getProjectConfigFilePathStringFromProjectPath(String projectPathString) {
		StringBuilder strBuilder = new StringBuilder(projectPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("config");
		strBuilder.append(File.separator);
		strBuilder.append(MainProject.SINNORI_CONFIG_FILE_NAME);
		
		return strBuilder.toString();
	}
	
	public String getProjectConfigFilePathString() {
		return projectConfigFilePathString;
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

	public SequencedProperties getNewSinnoriConfigFromSinnoriConfigInfo() {
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
	
	
	public void setAppClient(boolean isAppClient) throws ConfigErrorException {
		// FIXME!
		this.isAppClient = isAppClient;
		
		String appClinetBuildPathString = appClientBuildPath.getAbsolutePath();
		
		if (isAppClient) {
			if (appClientBuildPath.exists()) {
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
				return;
			}
			
			boolean isSuccess = appClientBuildPath.mkdir();
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
			
			String relativeExecutabeJarFileName = new StringBuilder("dist")
			.append(File.separator).append(APPCLIENT_EXECUTABLE_JAR_SHORT_FILE_NAME).toString();
			
			/** <main project name>Client.bat */			
			String dosShellFilePathString = new StringBuilder(appClinetBuildPathString)
			.append(File.separator).append(mainProjectName).append("AppClient.bat").toString();
			createFile("dos shell of client", getDosShellContents("-Xmx1024m -Xms1024m", "client", 
					appClinetBuildPathString, relativeExecutabeJarFileName), dosShellFilePathString);
			
			/** <main project name>Client.sh */
			String unixShellFilePathString = new StringBuilder(appClinetBuildPathString)
			.append(File.separator).append(mainProjectName).append("AppClient.sh").toString();
			createFile("unix shell of client", getUnixShellContents("-Xmx1024m -Xms1024m", "client", 
					appClinetBuildPathString, relativeExecutabeJarFileName), unixShellFilePathString);
			
			String relativeDirectories[] = {"src", "src/main",
					"src/kr/pe/sinnori/common/clientlib", "src/kr/pe/sinnori/impl/message"};
			createDirectories(appClinetBuildPathString, relativeDirectories);
			
			/**
			 * create source file having DEFAULT_APPCLIENT_MAIN_CLASS_NAME
			 * ex) client_build/app_build/src/main/SinnoriAppClientMain.java
			 */
			String appClientMainSrcFilePathString = null;
			if (File.separator.equals("/")) {
				String subStr = DEFAULT_APPCLIENT_MAIN_CLASS_NAME.replaceAll("\\.", "/");
				
				appClientMainSrcFilePathString = new StringBuilder(appClinetBuildPathString)
				.append(File.separator).append("src")
				.append(File.separator).append(subStr)
				.append(".java").toString();
				
				log.info("subStr=[{}], serverMainSrcFilePathString=[{}]", subStr, appClientMainSrcFilePathString);
			} else {
				String subStr = DEFAULT_APPCLIENT_MAIN_CLASS_NAME.replaceAll("\\.", "\\\\");
				
				appClientMainSrcFilePathString = new StringBuilder(appClinetBuildPathString)
				.append(File.separator).append("src")
				.append(File.separator).append(subStr)
				.append(".java").toString();
				
				log.info("subStr=[{}], serverMainSrcFilePathString=[{}]", subStr, appClientMainSrcFilePathString);
			}
			
			createFile("main class source of app client", 
					getDefaultAppClientMainClassContents(), appClientMainSrcFilePathString);
			
		} else {			
			if (appClientBuildPath.exists()) {
				if (!appClientBuildPath.isDirectory()) {
					String errorMessage = String.format("app client build path[%s] is not directory", appClinetBuildPathString);
					log.warn(errorMessage);
					throw new ConfigErrorException(errorMessage);
				}
				
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
	}

	public void setWebClient(boolean isWebClient) throws ConfigErrorException {
		this.isWebClient = isWebClient;
		
		String webClinetBuildPathString = webClientBuildPath.getAbsolutePath();
		
		if (isWebClient) {	
			if (webClientBuildPath.exists()) {
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
				
				return;
			}
			
			boolean isSuccess = webClientBuildPath.mkdir();
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
			
			String relativeDirectories[] = {"src"};
			createDirectories(webClinetBuildPathString, relativeDirectories);
			
			/** 
			 * web root directory struect
			 */
			String webRootRelativeDirectories[] = {"web_app_base/ROOT/WEB-INF/lib", "web_app_base/upload"};
			createDirectories(projectPathString, webRootRelativeDirectories);
		} else {
			if (webClientBuildPath.exists()) {
				if (!webClientBuildPath.isDirectory()) {
					String errorMessage = String.format("web client build path[%s] is not directory", webClinetBuildPathString);
					log.warn(errorMessage);
					throw new ConfigErrorException(errorMessage);
				}
				
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
			if (!wantedPath.exists()) {
				try {
					FileUtils.forceMkdir(wantedPath);
				} catch (IOException e) {
					String errorMessage = String.format("fail to make a new path[%s]", wantedPathString);
					log.warn(errorMessage);
					throw new ConfigErrorException(errorMessage);
				}
				
				log.info("direcotry[{}] creation success", wantedPathString);
			} else {
				log.info("direcotry[{}] exist", wantedPathString);
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
	
	public void saveConfigFile(SequencedProperties sinnoriConfig) throws ConfigErrorException {		
		File projectConfigFile = new File(projectConfigFilePathString);
		FileOutputStream fos = null;
		try {
			fos = FileUtils.openOutputStream(projectConfigFile);			
			sinnoriConfig.store(fos, new StringBuilder("Project[")
			.append(mainProjectName).append("]'s Config File").toString());
			
		} catch (Exception e) {
			String errorMessage = String.format("fail to create the project config file[%s], errorMessage=%s", projectConfigFilePathString, e.getMessage());
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
}
