package kr.pe.sinnori.common.config;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import kr.pe.sinnori.common.config.common.ItemValidatorOfJDFServletTrace;
import kr.pe.sinnori.common.config.common.ItemValidatorOfJdbcConnectionURI;
import kr.pe.sinnori.common.config.common.ItemValidatorOfJdbcDBUserName;
import kr.pe.sinnori.common.config.common.ItemValidatorOfJdbcDBUserPassword;
import kr.pe.sinnori.common.config.common.ItemValidatorOfJdbcDriverClassName;
import kr.pe.sinnori.common.config.common.ItemValidatorOfMinMaxInteger;
import kr.pe.sinnori.common.config.common.ItemValidatorOfNoNullAndEmptyString;
import kr.pe.sinnori.common.config.common.ItemValidatorOfSessionKeyRSAKeypairPath;
import kr.pe.sinnori.common.config.common.ItemValidatorOfSessionkeyPrivateKeyEncoding;
import kr.pe.sinnori.common.config.common.ItemValidatorOfSingleIntegerSet;
import kr.pe.sinnori.common.config.common.ItemValidatorOfSingleStringSet;
import kr.pe.sinnori.common.config.common.ItemValidatorOfUpDownFileBlockMaxSize;
import kr.pe.sinnori.common.exception.ConfigException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SinnoriConfig {
	private Logger log = LoggerFactory.getLogger(SinnoriConfig.class);
	
	private String projectName;
	private String projectPathStr;

	private Hashtable<String, ItemValidator> itemCheckerHash = new Hashtable<String, ItemValidator>();	
	private Hashtable<String, ItemDependence> itemDependenceListHash = new Hashtable<String, ItemDependence>();
	
	
	public SinnoriConfig(String projectName, String projectPathStr) throws ConfigException {
		this.projectName = projectName;
		this.projectPathStr = projectPathStr;
		
		itemCheckerHash.put("jdbc.connection_uri.value", new ItemValidatorOfJdbcConnectionURI("jdbc:mysql://localhost:3306/sinnori"));
		itemCheckerHash.put("jdbc.db_user_name.value", new ItemValidatorOfJdbcDBUserName("dbmadangse"));
		itemCheckerHash.put("jdbc.db_user_password.value", new ItemValidatorOfJdbcDBUserPassword("test1234"));
		itemCheckerHash.put("jdbc.driver_class_name.value", new ItemValidatorOfJdbcDriverClassName("com.mysql.jdbc.Driver"));
		itemCheckerHash.put("servlet_jsp.jdf_error_message_page.value", new ItemValidatorOfNoNullAndEmptyString("/errorMessagePage.jsp"));
		itemCheckerHash.put("servlet_jsp.jdf_login_page.value", new ItemValidatorOfNoNullAndEmptyString("/login.jsp"));
		itemCheckerHash.put("servlet_jsp.jdf_servlet_trace.value", new ItemValidatorOfJDFServletTrace("true"));
		itemCheckerHash.put("servlet_jsp.web_layout_control_page.value", new ItemValidatorOfNoNullAndEmptyString("/PageJump.jsp"));
		itemCheckerHash.put("sessionkey.rsa_keypair_source.value", new ItemValidatorOfSingleStringSet("API", "File"));
		
		ItemDependence itemDepanedOfSessionkeyRSAKeypairPath = 
				new ItemDependence("sessionkey.rsa_keypair_source.value", 
						itemCheckerHash.get("sessionkey.rsa_keypair_source.value"), "File");		
		itemDependenceListHash.put("sessionkey.rsa_keypair_path.value", itemDepanedOfSessionkeyRSAKeypairPath);		
		itemCheckerHash.put("sessionkey.rsa_keypair_path.value", new ItemValidatorOfSessionKeyRSAKeypairPath(getDefaultValueOfSessionKeyRSAKeypairPath()));
		
		itemCheckerHash.put("sessionkey.rsa_keypair_path.value", new ItemValidatorOfSingleIntegerSet("1024", "512", "1024", "2048"));
		
		itemCheckerHash.put("sessionkey.symmetric_key_algorithm.value", new ItemValidatorOfSingleStringSet("ASE", "ASE", "DESede", "DES"));
		
		itemCheckerHash.put("sessionkey.symmetric_key_size.value", new ItemValidatorOfSingleIntegerSet("16", "8", "16", "24"));
		
		itemCheckerHash.put("sessionkey.private_key.encoding.value", new ItemValidatorOfSessionkeyPrivateKeyEncoding("BASE64"));
		
		itemCheckerHash.put("common.updownfile.local_source_file_resource_cnt.value", new ItemValidatorOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
		itemCheckerHash.put("common.updownfile.local_target_file_resource_cnt.value", new ItemValidatorOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
		itemCheckerHash.put("common.updownfile.file_block_max_size.value", new ItemValidatorOfUpDownFileBlockMaxSize("1048576", 1024, Integer.MAX_VALUE));
		
		
	}
	
	private String getDefaultValueOfSessionKeyRSAKeypairPath() {
		StringBuilder strBuilder = new StringBuilder(projectPathStr);
		strBuilder.append(File.separator);
		strBuilder.append("rsa_keypair");		
		return strBuilder.toString();
	}
	
	
	public void validCheck(Properties projectProperteis) throws ConfigException {
		Enumeration<Object> enumProject = projectProperteis.keys();
		while (enumProject.hasMoreElements()) {
			String key = (String)enumProject.nextElement();
			if (key.endsWith(".value")) {
				ItemValidator itemCheck = itemCheckerHash.get(key);
				if (null == itemCheck) {
					String errorMessage = new StringBuilder("project[")
					.append(projectName)
					.append("] Config::unknown key[")
					.append(key)
					.append("]  error").toString();
					
					throw new ConfigException(errorMessage);
				}
				
				ItemDependence itemDependence = itemDependenceListHash.get(key);
				boolean isValidation= true;
				if (null != itemDependence) {
					isValidation = itemDependence.isValidation(projectProperteis.getProperty(itemDependence.getKey()));
				}

				if (isValidation) {
					try {
						itemCheck.validateItem(projectProperteis.getProperty(key));
					} catch(ConfigException e) {
						String errorMessage = new StringBuilder("project[")
						.append(projectName)
						.append("] Config::key[")
						.append(key)
						.append("] errrorMessage=")
						.append(e.getMessage()).toString();
						
						log.warn(errorMessage);
					}
				}
			}
		}
	}
}
