package kr.pe.sinnori.gui.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import kr.pe.sinnori.common.exception.ConfigException;


public class ProjectManger {
	private String sinnoriInstalledPathStr = null;
	private Hashtable<String, Project> projectHash = new Hashtable<String, Project>();
	private List<Project> projectList = new ArrayList<Project>();
	private Logger log = Logger.getLogger(ProjectManger.class.getName());
	/**
	 * 동기화 쓰지 않고 싱글턴 구현을 위한 비공개 생성자.
	 */
	private ProjectManger() {
	}	
	
	/**
	 * 동기화 안쓰고 싱글턴 구현을 위한 내부 클래스
	 */
	private static final class ProjectMangerHolder {
		static final ProjectManger singleton = new ProjectManger();
	}

	/**
	 * 동기화 쓰지 않는 싱글턴 메소드
	 * 
	 * @return 싱글턴 객체
	 */
	public static ProjectManger getInstance() {
		return ProjectMangerHolder.singleton;
	}
	
	public void analyzeSinnoriInstalledPath(String sinnoriInstalledPathStr) {
		if (null == sinnoriInstalledPathStr) {
			String errorMessage = "parameter sinnoriInstalledPathStr is null"; 
			log.info(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		
		File sinnoriInstalledPath = new File(sinnoriInstalledPathStr);
		if (!sinnoriInstalledPath.exists()) {
			String errorMessage = String.format("parameter sinnoriInstalledPathStr[%s] do not exist", sinnoriInstalledPathStr); 
			log.info(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (!sinnoriInstalledPath.isDirectory()) {
			String errorMessage = String.format("parameter sinnoriInstalledPathStr[%s] is not directory", sinnoriInstalledPathStr); 
			log.info(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (!sinnoriInstalledPath.canRead()) {
			String errorMessage = String.format("can not read the path that is parameter sinnoriInstalledPathStr[%s]", sinnoriInstalledPathStr); 
			log.info(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (!sinnoriInstalledPath.canWrite()) {
			String errorMessage = String.format("can not write the path that is parameter sinnoriInstalledPathStr[%s]", sinnoriInstalledPathStr); 
			log.info(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		
		try {
			sinnoriInstalledPath = sinnoriInstalledPath.getCanonicalFile();
		} catch (IOException e) {
			String errorMessage = String.format("fail to call getCanonicalFile, parameter sinnoriInstalledPathStr[%s]", sinnoriInstalledPathStr); 
			log.info(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		String sinnoriInstalledAbsPathStr = sinnoriInstalledPath.getAbsolutePath();
				
		StringBuilder strBuilder = new StringBuilder(sinnoriInstalledPathStr);
		strBuilder.append(File.separator);
		strBuilder.append("project");
		
		String projectBasePathStr = strBuilder.toString();
		
		File projectPath = new File(projectBasePathStr);
				
		for (File f : projectPath.listFiles()) {
			if (f.isDirectory()) {
				String projectPathStr = f.getAbsolutePath();
				
				if (!f.canRead()) {
					String errorMessage = String.format("can not read the sub project path[%s]", projectPathStr); 
					log.info(errorMessage);
					throw new RuntimeException(errorMessage);
				}
				
				if (!f.canWrite()) {
					String errorMessage = String.format("can not write the sub project path[%s]", projectPathStr); 
					log.info(errorMessage);
					throw new RuntimeException(errorMessage);
				}
				
				String projectConfigFileStr = new StringBuilder(projectPathStr)
				.append(File.separator)
				.append("config")
				.append(File.separator)
				.append("sinnori.properties")
				.toString();
				
				Properties projectProperteis = new Properties();
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(projectConfigFileStr);
					projectProperteis.load(fis);
				} catch (FileNotFoundException e) {
					String errorMessage = String.format("project config file[%s] not found", projectConfigFileStr); 
					log.info(errorMessage);
					throw new RuntimeException(errorMessage);
				} catch (IOException e) {
					String errorMessage = String.format("IOException[%s]::project config file[%s]", e.getMessage(), projectConfigFileStr); 
					log.info(errorMessage);
					throw new RuntimeException(errorMessage);
				} finally {
					if (null != fis) {
						try {
							fis.close();
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}

				String projectName = f.getName();
				Project project = null;
				try {
					project = new Project(projectName, projectPathStr, projectProperteis);
				} catch(ConfigException e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}
				projectList.add(project);
				projectHash.put(projectName, project);
			}
		}
		
		
		this.sinnoriInstalledPathStr = sinnoriInstalledAbsPathStr;
	}
	
	public String getProjectBasePath(String projectName) {
		StringBuilder strBuilder = new StringBuilder(sinnoriInstalledPathStr);
		strBuilder.append(File.separator);
		strBuilder.append("project");
		strBuilder.append(File.separator);
		strBuilder.append(projectName);
		
		return strBuilder.toString(); 
	}
	
	public String getProjectConfigFileStr(String projectName) {
		StringBuilder strBuilder = new StringBuilder(getProjectBasePath(projectName));
		strBuilder.append(File.separator);
		strBuilder.append("config");
		strBuilder.append(File.separator);
		strBuilder.append("sinnori.properties");
		
		return strBuilder.toString(); 
	}
}
