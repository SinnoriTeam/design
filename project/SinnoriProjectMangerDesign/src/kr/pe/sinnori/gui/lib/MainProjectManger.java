package kr.pe.sinnori.gui.lib;

import java.io.File;
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
			
			String projectConfigFilePathString = getProjectConfigFilePathStringFromProjectPath(projectPathString);
			
			SequencedProperties sourceProperties = null;
			try {
				sourceProperties = MainProject.getSequencedPropertiesFromFile(projectConfigFilePathString);
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
	
	
	public static String getProjectConfigFilePathStringFromProjectPath(String projectPathString) {
		StringBuilder strBuilder = new StringBuilder(projectPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("config");
		strBuilder.append(File.separator);
		strBuilder.append(MainProject.SINNORI_CONFIG_FILE_NAME);
		
		return strBuilder.toString();
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
	
	public void addMainProject(String newMainProjectName) throws ConfigErrorException {
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
		
		// FIXME!
		boolean isOver = mainProjectHash.containsKey(newMainProjectName);	
		if (isOver) {
			String errorMessage = String.format("프록제트[%s] 중복 에러", newMainProjectName);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		
		String projectPathString = new StringBuilder(projectBasePathString)
		.append(File.separator).append(newMainProjectName).toString();
		
		File projectPath = new File(projectPathString);
		boolean isSuccess = projectPath.mkdir();
		if (!isSuccess) {
			if (!projectPath.exists()) {
				String errorMessage = String.format("신규 메인 프로젝트 디렉토리[%s] 생성 실패", projectPathString);
				log.warn(errorMessage);
				throw new ConfigErrorException(errorMessage);
			}			
		}
		
		if (!projectPath.canRead()) {
			String errorMessage = String.format("신규 메인 프로젝트 디렉토리[%s] 읽기 권한 없음", projectPathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (!projectPath.canWrite()) {
			String errorMessage = String.format("신규 메인 프로젝트 디렉토리[%s] 쓰기 권한 없음", projectPathString);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		MainProject mainProject = null;
		try {
			mainProject = new MainProject(newMainProjectName, projectPathString);
		} catch(ConfigErrorException e) {
			log.info("ConfigErrorException", e);
			try {
				FileUtils.forceDelete(projectPath);
			} catch (IOException e1) {
				String errorMessage = String.format("프로젝트 신규 생성시 에러 발생으로 신규 프로젝 디렉토리 삭제 실패, errormessage=%s", e1.getMessage());
				log.warn(errorMessage);
			}
			throw e;
		}
		mainProjectList.add(mainProject);
		mainProjectHash.put(newMainProjectName, mainProject);		
	}
	
	public void removeMainProject(String selectedMainProjectName) throws ConfigErrorException {
		MainProject selectedMainProject = mainProjectHash.get(selectedMainProjectName);
		
		if (null == selectedMainProject) {
			String errorMessage = String.format("삭제할 메인 프로젝트[%s]가 존재하지 않습니다.", selectedMainProjectName);
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		selectedMainProject.removeProjectDirectory();
		mainProjectList.remove(selectedMainProjectName);
		mainProjectHash.remove(selectedMainProjectName);
	}
}
