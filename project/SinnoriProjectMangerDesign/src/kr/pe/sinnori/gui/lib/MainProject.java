package kr.pe.sinnori.gui.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.util.SequencedProperties;
import kr.pe.sinnori.gui.config.ConfigItem;
import kr.pe.sinnori.gui.config.SinnoriConfigInfo;

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
	
	// public static final String NEWLINE = System.getProperty("line.separator");
	
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
	// private String servletEnginLibPathString = null;
	
	
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
		
		
				
		/** <project home>/config/sinnori.properties */
		SequencedProperties newSinnoriConfig = makeNewSinnoriConfigUsingSinnoriConfigInfo();
		
		saveConfigFile(newSinnoriConfig);
		
		/** <project home>/config/logback.xml */
		String logbackFilePathString = new StringBuilder(projectPathString)
		.append(File.separator).append("config")
		.append(File.separator).append("logback.xml").toString();
		
		createFile("logback config file", FileContents.getContentsOfLogback(), logbackFilePathString);
		
		/** <project home>/impl/message/info/Echo.xml */
		String echoMessageXMLFilePathString = new StringBuilder(projectPathString)
		.append(File.separator).append("impl")
		.append(File.separator).append("message")
		.append(File.separator).append("info")
		.append(File.separator).append("Echo.xml").toString();
		
		createFile("echo message", FileContents.getEchoMessageInfoContents(), echoMessageXMLFilePathString);		
		
		createSeverAntEnvironment();
		createClientAntEnvironment();
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
		
		if (!serverBuildPath.exists()) {
			boolean isSuccess = serverBuildPath.mkdir();
			if (!isSuccess) {
				String errorMessage = String.format("fail to make a new server build path[%s]", serverBuildPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
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
		createFile("server build.xml", FileContents.getServerAntBuildXMLFileContent(mainProjectName, SERVER_EXECUTABLE_JAR_SHORT_FILE_NAME), serverBuildXMLFilePathString);
					
		String relativeExecutabeJarFileName = new StringBuilder("dist")
		.append(File.separator).append(SERVER_EXECUTABLE_JAR_SHORT_FILE_NAME).toString();
		
		/** <main project name>Server.bat */
		String dosShellFilePathString = new StringBuilder(serverBuildPathString)
		.append(File.separator).append(mainProjectName).append("Server.bat").toString();
		
		createFile("dos shell of server", FileContents.getDosShellContents(
				mainProjectName,
				projectPathString,
				
				"-Xmx1024m -Xms1024m", "server", 
				serverBuildPathString, relativeExecutabeJarFileName,
				
				SINNORI_LOGBACK_LOG_FILE_NAME,
				SINNORI_CONFIG_FILE_NAME), dosShellFilePathString);
		
		/** <main project name>Server.sh */
		String unixShellFilePathString = new StringBuilder(serverBuildPathString)
		.append(File.separator).append(mainProjectName).append("Server.sh").toString();
		createFile("unix shell of server", FileContents.getUnixShellContents(
				mainProjectName,
				projectPathString,
				
				"-Xmx1024m -Xms1024m", "server", 
				serverBuildPathString, relativeExecutabeJarFileName,
				
				SINNORI_LOGBACK_LOG_FILE_NAME,
				SINNORI_CONFIG_FILE_NAME), unixShellFilePathString);
		
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
		
		
		
		createFile("main class source of server", 
				FileContents.getDefaultServerMainClassContents(DEFAULT_SERVER_MAIN_CLASS_NAME), 
				serverMainSrcFilePathString);
		
		/** server_build/src/kr/pe/sinnori/impl/servertask/EchoServerTask.java */		
		String echoServerTaskSrcFilePathString = new StringBuilder(serverBuildPathString)
		.append(File.separator).append("src")
		.append(File.separator).append("kr")
		.append(File.separator).append("pe")
		.append(File.separator).append("sinnori")
		.append(File.separator).append("impl")
		.append(File.separator).append("servertask")
		.append(File.separator).append("EchoServerTask.java").toString();
		
		createFile("echo server task source file", FileContents.getEchoServerTaskContents(), echoServerTaskSrcFilePathString);
	}
	/** server_build/src/main/SinnoriServerMain.java */
	
	
	

	private void createClientAntEnvironment() throws ConfigErrorException {
		String clientBuildBasePathString = getClinetBuildBasePathString();
		
		File clientBuildBasePath = new File(clientBuildBasePathString);
		if (!clientBuildBasePath.exists()) {
			boolean isSuccess = clientBuildBasePath.mkdir();
			if (!isSuccess) {
				String errorMessage = String.format("fail to make a new client build base path[%s]", clientBuildBasePathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
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
		setWebClient(false, "D:\\apache-tomcat-7.0.57\\lib");
	}
	
	/** client_build/web_build/build.xml */
	
	
	
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

	
	private SequencedProperties makeNewSinnoriConfigUsingSinnoriConfigInfo() {
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
			String conents = FileContents.getAppClientAntBuildXMLFileContents(mainProjectName, APPCLIENT_EXECUTABLE_JAR_SHORT_FILE_NAME);
			createFile("app client build.xml", conents, appClientBuildXMLFilePathString);
			
			String relativeExecutabeJarFileName = new StringBuilder("dist")
			.append(File.separator).append(APPCLIENT_EXECUTABLE_JAR_SHORT_FILE_NAME).toString();
			
			/** <main project name>Client.bat */			
			String dosShellFilePathString = new StringBuilder(appClinetBuildPathString)
			.append(File.separator).append(mainProjectName).append("AppClient.bat").toString();
			createFile("dos shell of client", FileContents.getDosShellContents(
					mainProjectName,
					projectPathString,
					
					"-Xmx1024m -Xms1024m", "client", 
					appClinetBuildPathString, relativeExecutabeJarFileName,
					
					SINNORI_LOGBACK_LOG_FILE_NAME,
					SINNORI_CONFIG_FILE_NAME), dosShellFilePathString);
			
			/** <main project name>Client.sh */
			String unixShellFilePathString = new StringBuilder(appClinetBuildPathString)
			.append(File.separator).append(mainProjectName).append("AppClient.sh").toString();
			createFile("unix shell of client", FileContents.getUnixShellContents(
					mainProjectName,
					projectPathString,
					
					"-Xmx1024m -Xms1024m", "client", 
					appClinetBuildPathString, relativeExecutabeJarFileName,
					
					SINNORI_LOGBACK_LOG_FILE_NAME,
					SINNORI_CONFIG_FILE_NAME), unixShellFilePathString);
			
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
					FileContents.getDefaultAppClientMainClassContents(DEFAULT_APPCLIENT_MAIN_CLASS_NAME), appClientMainSrcFilePathString);
			
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

	public void setWebClient(boolean isWebClient, String servletEnginLibPathString) throws ConfigErrorException {
		this.isWebClient = isWebClient;
		
		String antPropertiesFilePathString = new StringBuilder(projectPathString)
		.append(File.separator).append("ant.properties").toString();
		File antPropertiesFile = new File(antPropertiesFilePathString);
		SequencedProperties antPropeties = null;
		if (antPropertiesFile.exists()) {			
			antPropeties = loadAntProperties();			
			antPropeties.put("is.tomcat", String.valueOf(isWebClient));
			antPropeties.put("tomcat.servletlib", servletEnginLibPathString);
		} else {
			antPropeties = new SequencedProperties();
			antPropeties.put("is.tomcat", String.valueOf(isWebClient));
			antPropeties.put("tomcat.servletlib", servletEnginLibPathString);
			antPropeties.put("java.debug", "true");			
			antPropeties.put("server.main.class", DEFAULT_SERVER_MAIN_CLASS_NAME);
			antPropeties.put("appclient.main.class", DEFAULT_APPCLIENT_MAIN_CLASS_NAME);
		}		
		
		saveAntProperties(antPropeties);
		
		
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
			
			String conents = FileContents.getWebClientAntBuildXMLFileContents(mainProjectName);
			createFile("web client build.xml", conents, webClientBuildXMLFilePathString);
			
			String relativeDirectories[] = {"src"};
			createDirectories(webClinetBuildPathString, relativeDirectories);			
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
	
	public SequencedProperties loadConfigFile() throws ConfigErrorException {
		SequencedProperties projectConfig = getSequencedPropertiesFromFile(projectConfigFilePathString);
		
		return projectConfig;
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
	
	public SequencedProperties loadAntProperties() throws ConfigErrorException {
		String antPropertiesFilePathString = new StringBuilder(projectPathString)
		.append(File.separator).append("ant.properties").toString();
		
		SequencedProperties antProperties = getSequencedPropertiesFromFile(antPropertiesFilePathString);
		
		return antProperties;
	}
	
	public void saveAntProperties(SequencedProperties antProperties) throws ConfigErrorException {	
		String antPropertiesFilePathString = new StringBuilder(projectPathString)
		.append(File.separator).append("ant.properties").toString();
		
		
		File antPropertiesFile = new File(antPropertiesFilePathString);
		FileOutputStream fos = null;
		try {
			fos = FileUtils.openOutputStream(antPropertiesFile);			
			antProperties.store(fos, new StringBuilder("Project[")
			.append(mainProjectName).append("]'s ant properties file").toString());
			
		} catch (Exception e) {
			String errorMessage = String.format("fail to create the ant properties file[%s], errorMessage=%s", projectConfigFilePathString, e.getMessage());
			log.warn(errorMessage);			
			throw new ConfigErrorException(errorMessage);
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch(Exception e) {
					log.warn("fail to close the file output stream of the ant properties file[{}], errorMessage={}", projectConfigFilePathString, e.getMessage());
				}
			}
		}
	}
	
	public String getServletEnginLibPathString() throws ConfigErrorException {
		SequencedProperties antProperties = loadAntProperties();
		return antProperties.getProperty("tomcat.servletlib");
	}
	
	public void renewDevEnvBasedOnInstalledPath() throws ConfigErrorException {
		// FIXME!
		SequencedProperties sinnoriConfig = loadConfigFile();
		
		@SuppressWarnings("unchecked")
		Enumeration<Object> enumKey = sinnoriConfig.keys();
		while (enumKey.hasMoreElements()) {
			String key = (String)enumKey.nextElement();
			
			if (key.equals(SinnoriConfigInfo.DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING)) {
				continue;
			} else if (key.equals(SinnoriConfigInfo.PROJECT_NAME_LIST_KEY_STRING)) {
				continue;
			} else if (key.endsWith(".value")) {
				String itemID = sinnoriConfigInfo.getItemIDFromKey(key);				
				if (null == itemID) {
					String errorMessage = new StringBuilder("project config file[")
					.append(projectConfigFilePathString)
					.append("]::source key[")
					.append(key)
					.append("] is bad, itemID is null").toString();
					
					log.warn(errorMessage);
					
					throw new ConfigErrorException(errorMessage);
				}
				
				if (itemID.equals("confige_file.value")) {
					// DBCP
					StringTokenizer tokens = new StringTokenizer(key, ".");
					tokens.nextToken();
					String dbcpConnPoolName = tokens.nextToken();
					String value = sinnoriConfigInfo.getDefaultValueOfDBCPConnPoolConfigFile(dbcpConnPoolName);
					sinnoriConfig.put(key, value);
				} else if (itemID.equals("sessionkey.rsa_keypair_path.value")) {
					String value = sinnoriConfigInfo.getConfigItem(itemID).getDefaultValue();
					sinnoriConfig.put(key, value);
				} else if (itemID.equals("common.message_info.xmlpath.value")) {
					String value = sinnoriConfigInfo.getConfigItem(itemID).getDefaultValue();
					sinnoriConfig.put(key, value);
				} else if (itemID.equals("server.classloader.appinf.path.value")) {
					String value = sinnoriConfigInfo.getConfigItem(itemID).getDefaultValue();
					sinnoriConfig.put(key, value);
				}				
			}
		}
		
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
		
		createSeverAntEnvironment();
		createClientAntEnvironment();
	}
}
