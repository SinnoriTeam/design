package kr.pe.sinnori.gui.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.util.SequencedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProjectManger {
	private Logger log = LoggerFactory.getLogger(ProjectManger.class);
	
	private String projectBasePathString = null;
	private Hashtable<String, MainProject> projectHash = new Hashtable<String, MainProject>();
	private List<MainProject> projectList = new ArrayList<MainProject>();
	
	public ProjectManger(String projectBasePathString) throws ConfigErrorException {
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
		for (String projectName : tempProjectNameList) {
			String projectPathString = new StringBuilder(projectBasePathString)
			.append(File.separator).append(projectName).toString();
			
			String projectConfigFilePathString = getProjectConfigFilePathStringFromProjectPathString(projectPathString);
			
			SequencedProperties sourceProperties = null;
			try {
				sourceProperties = getSourceSequencedProperties(projectConfigFilePathString);
			} catch (ConfigErrorException e1) {
				String errorMessage = String.format("project[%s][%s] errormessage=%s", projectBasePathString, projectName, e1.getMessage());
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}
						
			MainProject project = new MainProject(projectName, projectPathString, sourceProperties);
				
			// tempProjectHash.put(projectName, project);
			projectList.add(project);
			projectHash.put(projectName, project);
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

	public String getProjectBasePathString() {
		return projectBasePathString;
	}
	
	public MainProject getProject(String projectName) {
		return projectHash.get(projectName);
	}

	public List<MainProject> getProjectList() {
		return projectList;
	}
}
