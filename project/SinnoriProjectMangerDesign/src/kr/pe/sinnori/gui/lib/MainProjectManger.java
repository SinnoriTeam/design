package kr.pe.sinnori.gui.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.util.SequencedProperties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainProjectManger {
	private Logger log = LoggerFactory.getLogger(MainProjectManger.class);
	
	private String projectBasePathString = null;
	private Hashtable<String, MainProject> mainProjectHash = new Hashtable<String, MainProject>();
	private List<MainProject> mainProjectList = new ArrayList<MainProject>();
	
	public MainProjectManger(String projectBasePathString) throws ConfigErrorException {
		if (null == projectBasePathString) {
			String errorMessage = "parameter projectBasePathString is null"; 
			log.info(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		
		this.projectBasePathString = projectBasePathString;
		
		File projectBasePath = new File(projectBasePathString);
		if (!projectBasePath.exists()) {
			String errorMessage = String.format("신놀이 프로젝트 기본 경로[%s] 가 존재하지 않습니다.", projectBasePathString);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!projectBasePath.isDirectory()) {
			String errorMessage = String.format("신놀이 프로젝트 기본 경로[%s] 가 디렉토리가 아닙니다.", projectBasePathString);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!projectBasePath.canRead()) {
			String errorMessage = String.format("신놀이 프로젝트 기본 경로[%s] 에 대한 읽기 권한이 없습니다.", projectBasePathString);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!projectBasePath.canWrite()) {
			String errorMessage = String.format("신놀이 프로젝트 기본 경로[%s] 에 대한 쓰기 권한이 없습니다.", projectBasePathString);
			throw new ConfigErrorException(errorMessage);
		}
		
		
		List<String> tempProjectNameList = new ArrayList<String>();
		// projectNameList = new ArrayList<String>();		
		
		for (File fileOfList : projectBasePath.listFiles()) {
			if (fileOfList.isDirectory()) {
				if (!fileOfList.canRead()) {
					String errorMessage = String.format("신놀이 프로젝트 경로[%s] 에 대한 읽기 권한이 없습니다.", fileOfList.getAbsolutePath());
					throw new ConfigErrorException(errorMessage);
				}
				
				if (!fileOfList.canWrite()) {
					String errorMessage = String.format("신놀이 프로젝트 경로[%s] 에 대한 쓰기 권한이 없습니다.", fileOfList.getAbsolutePath());
					throw new ConfigErrorException(errorMessage);
				}
				
				tempProjectNameList.add(fileOfList.getName());
			}
		}
		
		// HashMap<String, Project> tempProjectHash = new HashMap<String, Project>(); 
		for (String mainProjectName : tempProjectNameList) {
			String projectPathString = new StringBuilder(projectBasePathString)
			.append(File.separator).append(mainProjectName).toString();
			
			String projectConfigFilePathString = getProjectConfigFilePathStringFromProjectPathString(projectPathString);
			
			SequencedProperties sourceProperties = null;
			try {
				sourceProperties = getSourceSequencedProperties(projectConfigFilePathString);
			} catch (ConfigErrorException e1) {
				String errorMessage = String.format("project[%s][%s] errormessage=%s", projectBasePathString, mainProjectName, e1.getMessage());
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
						
			MainProject project = new MainProject(mainProjectName, projectPathString, sourceProperties);
				
			// tempProjectHash.put(projectName, project);
			mainProjectList.add(project);
			mainProjectHash.put(mainProjectName, project);
		}
		
		
	}	
	
	private String getProjectConfigFilePathStringFromProjectPathString(String projectPathString) {
		StringBuilder configBuilder = new StringBuilder(projectPathString);		
		configBuilder.append(File.separator);
		configBuilder.append("config");
		configBuilder.append(File.separator);
		configBuilder.append("sinnori.properties");
		
		return configBuilder.toString();
	}
	
	public static  SequencedProperties getSourceSequencedProperties(String properteisFilePathString) throws ConfigErrorException {
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

	public String getMainProjectBasePathString() {
		return projectBasePathString;
	}
	
	public MainProject getMainProject(String mainProjectName) {
		return mainProjectHash.get(mainProjectName);
	}

	public List<MainProject> getMainProjectList() {
		return mainProjectList;
	}
	
	public void addMainProject(String newMainProjectName) {
		if (null == newMainProjectName) {
			String errorMessage = "신규 메인 프로젝트 이름을 넣어 주세요.";
			log.warn(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (newMainProjectName.equals("")) {
			String errorMessage = "신규 메인 프로젝트 이름을 다시 넣어 주세요.";
			log.warn(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		
		String newMainProjectNameTrim = newMainProjectName.trim();
		
		if (!newMainProjectName.equals(newMainProjectNameTrim)) {
			String errorMessage = "신규 메인 프로젝트 이름에 앞뒤로 공백을 넣을 수 없습니다.";
			log.warn(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		
		
		String projectPathString = new StringBuilder(projectBasePathString)
		.append(File.separator).append(newMainProjectName).toString();
		
		File projectPath = new File(projectPathString);
		boolean isSuccess = projectPath.mkdir();
		if (!isSuccess) {
			if (!projectPath.exists()) {
				String errorMessage = String.format("신규 메인 프로젝트 디렉토리[%s] 생성 실패", projectPathString);
				log.warn(errorMessage);
				throw new RuntimeException(errorMessage);
			}			
		}
		
		if (!projectPath.canRead()) {
			String errorMessage = String.format("신규 메인 프로젝트 디렉토리[%s] 읽기 권한 없음", projectPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		if (!projectPath.canWrite()) {
			String errorMessage = String.format("신규 메인 프로젝트 디렉토리[%s] 쓰기 권한 없음", projectPathString);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
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
			antPropeties.put("tomcat.servletlib", "D:\\apache-tomcat-7.0.57\\lib");
			antPropeties.put("java.debug", "true");
			
			antPropeties.store(fos, String.format("Project[%s]'s ant properties file", newMainProjectName));
		} catch (Exception e) {
			log.warn("fail to create the ant properties file[{}], errorMessage={}", antFilePathString, e.getMessage());
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch(Exception e) {
					log.warn("fail to close the file output stream of the ant properties file[{}], errorMessage={}", antFilePathString, e.getMessage());
				}
			}
		}
		
		String projectConfigFilePathString = getProjectConfigFilePathStringFromProjectPathString(projectPathString);
		
		//FileOutputStream fos = null;
		try {
			fos = FileUtils.openOutputStream(antFile);
			
			SequencedProperties antPropeties = new SequencedProperties();
			antPropeties.put("is.tomcat", "false");
			antPropeties.put("tomcat.servletlib", "D:\\apache-tomcat-7.0.57\\lib");
			antPropeties.put("java.debug", "true");
			
			antPropeties.store(fos, String.format("Project[%s]'s ant properties file", newMainProjectName));
		} catch (Exception e) {
			log.warn("fail to create the ant properties file[{}], errorMessage={}", antFilePathString, e.getMessage());
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch(Exception e) {
					log.warn("fail to close the file output stream of the ant properties file[{}], errorMessage={}", antFilePathString, e.getMessage());
				}
			}
		}
		
	}
}
