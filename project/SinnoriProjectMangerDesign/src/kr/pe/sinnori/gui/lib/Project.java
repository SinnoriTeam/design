package kr.pe.sinnori.gui.lib;

import kr.pe.sinnori.common.config.SinnoriProjectConfig;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.util.SequencedProperties;

public class Project {
	// private Logger log = LoggerFactory.getLogger(Project.class);
	
	private String projectName;
	private String projectPathString;
	private SequencedProperties sourceSequencedProperties;
	
	private SinnoriProjectConfig sinnoriProjectConfig = null;
		
	public Project(String projectName, String projectPathString, SequencedProperties sourceSequencedProperties) throws ConfigErrorException {
		this.projectName = projectName;
		this.projectPathString = projectPathString;
		this.sourceSequencedProperties = sourceSequencedProperties;
		
		sinnoriProjectConfig = new SinnoriProjectConfig(projectName, projectPathString, sourceSequencedProperties);
	}
	
	public String getProjectName() {
		return projectName;
	}

	public String getProjectPathStr() {
		return projectPathString;
	}

	public String getProjectPathString() {
		return projectPathString;
	}

	public SequencedProperties getSourceSequencedProperties() {
		return sourceSequencedProperties;
	}

	public SinnoriProjectConfig getSinnoriProjectConfig() {
		return sinnoriProjectConfig;
	}	
}
