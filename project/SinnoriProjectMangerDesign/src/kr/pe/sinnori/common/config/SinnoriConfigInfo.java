package kr.pe.sinnori.common.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import kr.pe.sinnori.common.config.dependitem.MinMaxBreakChecker;
import kr.pe.sinnori.common.config.dependitem.ValueEqualConditionChecker;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfBoolean;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfByteOrder;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfCharset;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfConnectionType;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfFile;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfMessageInfoXMLPath;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfMessageProtocol;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfMinMaxInteger;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfMinMaxLong;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfNoCheck;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfNoNullAndEmptyString;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfPath;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfSessionKeyRSAKeypairPath;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfSessionkeyPrivateKeyEncoding;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfSingleIntegerSet;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfSingleStringSet;
import kr.pe.sinnori.common.config.itemvalidator.ItemValueGetterOfUpDownFileBlockMaxSize;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigKeyNotFoundException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.common.util.SequencedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SinnoriConfigInfo {
	private Logger log = LoggerFactory.getLogger(SinnoriConfigInfo.class);
	
	public final static String DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING = "dbcp.connection_pool_name_list.value";
	public final static String PROJECT_NAME_LIST_KEY_STRING = "project.name_list.value";
	
	private String mainProjectName;
	private String projectPathString;
	// private String projectConfigFilePathString;
	// private SequencedProperties sourceSequencedProperties;

	private String projectConfigFilePathString;
	private List<ConfigItem> configItemList = new ArrayList<ConfigItem>();
	
	private Hashtable<String, ConfigItem> configItemHash = new Hashtable<String, ConfigItem>();
	
		
	private Hashtable<String, AbstractConditionChecker> conditionCheckerHash = new Hashtable<String, AbstractConditionChecker>();
	private Hashtable<String, AbstractBreakChecker> breakCheckerHash = new Hashtable<String, AbstractBreakChecker>();
	
		
	/*private List<String> dbcpConnectionPoolNameList = new LinkedList<String>();
	private List<String> projectNameList = new LinkedList<String>();*/
	
	private List<ConfigItem> dbcpPartConfigItemList = new ArrayList<ConfigItem>();
	private List<ConfigItem> commonPartConfigItemList = new ArrayList<ConfigItem>();
	private List<ConfigItem> projectPartConfigItemList = new ArrayList<ConfigItem>();
	
	public SinnoriConfigInfo(String mainProjectName, String projectPathString) throws ConfigErrorException {
		this.mainProjectName = mainProjectName;
		this.projectPathString = projectPathString;		
		// this.sourceSequencedProperties = sourceSequencedProperties;		
		// this.projectConfigFilePathString = getProjectConfigFilePathString();
		
		projectConfigFilePathString = getProjectConfigFilePathString();
		
		//projectNameList.add(mainProjectName);
		
		log.info("projectName={}", mainProjectName);
		
		initCommonPart();
		
		dbcpPart();
		
		initProjectPart();
		
		/*try {
			validAllKey();
		} catch(ConfigKeyNotFoundException e) {
			*//** 알 수 없는 키가 존재할 경우 *//*
			throw new ConfigErrorException(e.getMessage());
		}*/
		
		for (ConfigItem configItem : configItemList) {
			ConfigItem.ConfigPart itemConfigPart = configItem.getConfigPart();
			if (ConfigItem.ConfigPart.DBCP == itemConfigPart) {				
				dbcpPartConfigItemList.add(configItem);
			} else if (ConfigItem.ConfigPart.COMMON == itemConfigPart) {
				commonPartConfigItemList.add(configItem);
			} else {
				projectPartConfigItemList.add(configItem);
			}
		}
	}
	
	private void dbcpPart() throws IllegalArgumentException, ConfigErrorException {
		ConfigItem item = null;
		// String keyOfDepenceItem = null;
		String dbcpID = null;		
		try {
			/** DBCP start */	
			dbcpID = "confige_file.value";		
			
			item = new ConfigItem(ConfigItem.ConfigPart.DBCP, 
					ConfigItem.ConfigItemViewType.FILE,					
					dbcpID,
					"연결 폴의 설정 파일 경로명, 형식 : dbcp.<dbcp 연결 폴 이름>.config_file.value",
					getDefaultValueOfDBCPConnPoolConfigFile(), 
					false, 
					new ItemValueGetterOfFile());
			addConfigItem(item);

			/** DBCP end */
		} catch(ConfigValueInvalidException | IllegalArgumentException e) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)			
			.append("]::dbcpID[")
			.append(dbcpID)
			.append("] errrorMessage=")
			.append(e.getMessage()).toString();
			
			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		}
	}
	
	private void initCommonPart() throws IllegalArgumentException, ConfigErrorException {
		ConfigItem item = null;
		String depenceItemID = null;
		String targetItemID = null;		
		try {			
			targetItemID = "servlet_jsp.jdf_error_message_page.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON,
					ConfigItem.ConfigItemViewType.TEXT,	
					targetItemID,					
					"JDF framework 에서 에러 발생시 에러 내용을 보여주는 사용자 친화적인 화면을 전담할 jsp",
					"/errorMessagePage.jsp", true, 
					new ItemValueGetterOfNoNullAndEmptyString());			
			addConfigItem(item);
			
			targetItemID = "servlet_jsp.jdf_login_page.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,	targetItemID,
					"로그인 처리 jsp",
					"/login.jsp", true, 
					new ItemValueGetterOfNoNullAndEmptyString());
			addConfigItem(item);
			
			targetItemID = "servlet_jsp.jdf_servlet_trace.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.SINGLE_SET,	
					targetItemID,
					"JDF framework에서 서블릿 경과시간 추적 여부",
					"true", true, 
					new ItemValueGetterOfBoolean());
			addConfigItem(item);
			
			targetItemID = "servlet_jsp.web_layout_control_page.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,	targetItemID,
					"신놀이 웹 사이트의 레이아웃 컨트롤러 jsp",
					"/PageJump.jsp", true, 
					new ItemValueGetterOfNoNullAndEmptyString());
			addConfigItem(item);
			
			depenceItemID = "sessionkey.rsa_keypair_source.value";
			targetItemID = "sessionkey.rsa_keypair_path.value";
			
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.SINGLE_SET,	depenceItemID,
					"세션키에 사용되는 공개키 키쌍 생성 방법, API:자체 암호 lib 이용하여 RSA 키쌍 생성, File:외부 파일를 읽어와서 RSA  키쌍을 생성",
					"API", true, 
					new ItemValueGetterOfSingleStringSet("API", "File"));
			addConfigItem(item);			
			
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.PATH,	
					targetItemID,
					"세션키에 사용되는 공개키 키쌍 파일 경로, 세션키에 사용되는 공개키 키쌍 생성 방법이 File인 경우에 유효하다.",
					getDefaultValueOfSessionKeyRSAKeypairPath(), true, 
					new ItemValueGetterOfSessionKeyRSAKeypairPath());
			addConfigItem(item);
			
			conditionCheckerHash.put(targetItemID, 
					new ValueEqualConditionChecker(targetItemID, 
							depenceItemID, "File", this));
			
			
			targetItemID = "sessionkey.rsa_keysize.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"세션키에 사용하는 공개키 크기, 단위 byte",
					"1024", true, 
					new ItemValueGetterOfSingleIntegerSet("512", "1024", "2048"));
			addConfigItem(item);
			
			targetItemID = "sessionkey.symmetric_key_algorithm.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"세션키에 사용되는 대칭키 알고리즘",
					"AES", true, 
					new ItemValueGetterOfSingleStringSet("AES", "DESede", "DES"));
			addConfigItem(item);
			
			targetItemID = "sessionkey.symmetric_key_size.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"세션키에 사용되는 대칭키 크기",
					"16", true, 
					new ItemValueGetterOfSingleIntegerSet("8", "16", "24"));
			addConfigItem(item);
			
			targetItemID = "sessionkey.iv_size.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"세션키에 사용되는 대칭키와 같이 사용되는 IV 크기",
					"16", true, 
					new ItemValueGetterOfSingleIntegerSet("8", "16", "24"));
			addConfigItem(item);
			
			targetItemID = "sessionkey.private_key.encoding.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON,
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"개인키를 인코딩 방법, 웹의 경우 이진데이터는 폼 전송이 불가하므로 base64 인코딩하여 전송한다.",
					"BASE64", true, 
					new ItemValueGetterOfSessionkeyPrivateKeyEncoding());
			addConfigItem(item);
			
			targetItemID = "common.updownfile.local_source_file_resource_cnt.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"로컬 원본 파일 자원 갯수",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(item);
			
			targetItemID = "common.updownfile.local_target_file_resource_cnt.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"로컬 목적지 파일 자원 갯수",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(item);
			
			targetItemID = "common.updownfile.file_block_max_size.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"파일 송수신 파일 블락 최대 크기, 1024 배수, 단위 byte",
					"1048576", true, 
					new ItemValueGetterOfUpDownFileBlockMaxSize(1024, Integer.MAX_VALUE));
			addConfigItem(item);
						
			targetItemID = "common.cached_object.max_size.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"싱글턴 클래스 객체 캐쉬 관리자(LoaderAndName2ObjectManager) 에서 캐쉬로 관리할 객체의 최대 갯수. 주로 캐쉬되는 대상 객체는 xxxServerCodec, xxxClientCodec 이다.",
					"100", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(item);
			
			// common.projectlist.value
			/*targetItemID = "common.projectlist.value";
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, targetItemID,
					"프로젝트 목록, 프로젝트 구분은 콤마",
					"", false, 
					new ItemValueGetterOfNoCheck());
			addConfigItem(item);*/
			
			/*ItemValueGetterHash.put("jdbc.connection_uri.value", new ItemValueGetterOfJdbcConnectionURI("jdbc:mysql://localhost:3306/sinnori"));
			ItemValueGetterHash.put("jdbc.db_user_name.value", new ItemValueGetterOfJdbcDBUserName("dbmadangse"));*/
			//ItemValueGetterHash.put("jdbc.db_user_password.value", new ItemValueGetterOfJdbcDBUserPassword("test1234"));
			// ItemValueGetterHash.put("jdbc.driver_class_name.value", new ItemValueGetterOfJdbcDriverClassName("com.mysql.jdbc.Driver"));
			// ItemValueGetterHash.put("servlet_jsp.jdf_error_message_page.value", new ItemValueGetterOfNoNullAndEmptyString("/errorMessagePage.jsp"));
			// ItemValueGetterHash.put("servlet_jsp.jdf_login_page.value", new ItemValueGetterOfNoNullAndEmptyString("/login.jsp"));
			// ItemValueGetterHash.put("servlet_jsp.jdf_servlet_trace.value", new ItemValueGetterOfBoolean("true"));
			// ItemValueGetterHash.put("servlet_jsp.web_layout_control_page.value", new ItemValueGetterOfNoNullAndEmptyString("/PageJump.jsp"));
			// ItemValueGetterHash.put("sessionkey.rsa_keypair_source.value", new ItemValueGetterOfSingleStringSet("API", "File"));
			// ItemValueGetterHash.put("sessionkey.rsa_keypair_path.value", new ItemValueGetterOfSessionKeyRSAKeypairPath(getDefaultValueOfSessionKeyRSAKeypairPath()));			
			// ItemValueGetterHash.put("sessionkey.rsa_keysize.value", new ItemValueGetterOfSingleIntegerSet("1024", "512", "1024", "2048"));			
			// ItemValueGetterHash.put("sessionkey.symmetric_key_algorithm.value", new ItemValueGetterOfSingleStringSet("ASE", "ASE", "DESede", "DES"));			
			// ItemValueGetterHash.put("sessionkey.symmetric_key_size.value", new ItemValueGetterOfSingleIntegerSet("16", "8", "16", "24"));			
			// ItemValueGetterHash.put("sessionkey.private_key.encoding.value", new ItemValueGetterOfSessionkeyPrivateKeyEncoding("BASE64"));
			
			// ItemValueGetterHash.put("common.updownfile.local_source_file_resource_cnt.value", new ItemValueGetterOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
			// ItemValueGetterHash.put("common.updownfile.local_target_file_resource_cnt.value", new ItemValueGetterOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
			//ItemValueGetterHash.put("common.updownfile.file_block_max_size.value", new ItemValueGetterOfUpDownFileBlockMaxSize("1048576", 1024, Integer.MAX_VALUE));		
		} catch(ConfigValueInvalidException | IllegalArgumentException e) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)			
			.append("]::key[")
			.append(targetItemID)
			.append("] errrorMessage=")
			.append(e.getMessage()).toString();
			
			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		}
	}
	
	private void initProjectPart() throws ConfigErrorException {
		String depenceItemID = null;
		String targetItemID = null;
		ConfigItem configItem = null;
		try {			
			/** 프로젝트 공통 설정 부분 */
			targetItemID = getProjectCommonPartID("message_info.xmlpath");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.PATH,
					targetItemID,
					"메시지 정보 파일 경로",
					getDefaultValueOfMessageInfoXMLPath(), true, 
					new ItemValueGetterOfMessageInfoXMLPath());
			addConfigItem(configItem);
			
			targetItemID = getProjectCommonPartID("host");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트에서 접속할 서버 주소",
					"localhost", true, 
					new ItemValueGetterOfNoNullAndEmptyString());
			addConfigItem(configItem);
			
			targetItemID = getProjectCommonPartID("port");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"포트 번호",
					"9090", true, 
					new ItemValueGetterOfMinMaxInteger(1024, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectCommonPartID("byteorder");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"바이트 오더",
					"LITTLE_ENDIAN", true, 
					new ItemValueGetterOfByteOrder());
			addConfigItem(configItem);
			
			targetItemID = getProjectCommonPartID("charset");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"문자셋",
					"UTF-8", true, 
					new ItemValueGetterOfCharset());
			addConfigItem(configItem);
			
			targetItemID = getProjectCommonPartID("data_packet_buffer_max_cnt_per_message");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"1개 메시지당 할당 받을 수 있는 데이터 패킷 버퍼 최대수",
					"1000", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectCommonPartID("data_packet_buffer_size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"데이터 패킷 버퍼 크기, 단위 byte",
					"4096", true, 
					new ItemValueGetterOfMinMaxInteger(1024, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			/** 메시지 식별자 크기의 최소 크기는 내부적으로 사용하는 SelfExn 메시지를 기준으로 정했음. */
			targetItemID = getProjectCommonPartID("message_id_fixed_size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"메시지 식별자 크기의 최소 크기",
					"50", true, 
					new ItemValueGetterOfMinMaxInteger(7, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectCommonPartID("message_protocol");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"메시지 프로토콜, DHB:교차 md5 헤더+바디, DJSON:길이+존슨문자열, THB:길이+바디",
					"DHB", true, 
					new ItemValueGetterOfMessageProtocol());
			addConfigItem(configItem);
			
			/** 변경전 : classloader.class.package_prefix_name, 변경후 : classloader.class_package_prefix_name*/
			targetItemID = getProjectCommonPartID("classloader.class_package_prefix_name");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"동적 클래스 패키지명 접두어, 동적 클래스 여부를 판단하는 기준",
					"kr.pe.sinnori.impl.", true, 
					new ItemValueGetterOfNoNullAndEmptyString());
			addConfigItem(configItem);
			
			// ItemValueGetterHash.put(getCommonKeyName("message_info.xmlpath"), new ItemValueGetterOfMessageInfoXMLPath(getDefaultValueOfMessageInfoXMLPath()));
			// ItemValueGetterHash.put(getCommonKeyName("host"), new ItemValueGetterOfNoNullAndEmptyString("localhost"));
			// ItemValueGetterHash.put(getCommonKeyName("port"), new ItemValueGetterOfMinMaxInteger("9090", 1024, Integer.MAX_VALUE));
			// ItemValueGetterHash.put(getCommonKeyName("byteorder"), new ItemValueGetterOfByteOrder("LITTLE_ENDIAN"));
			// ItemValueGetterHash.put(getCommonKeyName("charset"), new ItemValueGetterOfCharset("UTF-8"));
			// ItemValueGetterHash.put(getCommonKeyName("data_packet_buffer_max_cnt_per_message"), new ItemValueGetterOfMinMaxInteger("1000", 1, Integer.MAX_VALUE));
			/** 메시지 식별자 크기의 최소 크기는 내부적으로 사용하는 SelfExn 메시지를 기준으로 정했음. */
			// ItemValueGetterHash.put(getCommonKeyName("message_id_fixed_size"), new ItemValueGetterOfMinMaxInteger("50", 7, Integer.MAX_VALUE));
			// ItemValueGetterHash.put(getCommonKeyName("message_protocol"), new ItemValueGetterOfSingleStringSet("DHB", "DHB", "DJSON", "THB"));
			/** 변경전 : classloader.class.package_prefix_name, 변경후 : classloader.class_package_prefix_name*/
			// ItemValueGetterHash.put(getCommonKeyName("classloader.class_package_prefix_name"), new ItemValueGetterOfNoNullAndEmptyString("kr.pe.sinnori.impl."));
			
			
			/** 프로젝트 클라이언트 설정 부분 */
			targetItemID = getProjectClientPartSubKeyName("connection.type");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"소캣 랩퍼 클래스인 연결 종류, NoShareAsyn:비공유+비동기, ShareAsyn:공유+비동기, NoShareSync:비공유+동기",
					"NoShareAsyn", true, 
					new ItemValueGetterOfConnectionType());
			addConfigItem(configItem);
			
			
			targetItemID = getProjectClientPartSubKeyName("connection.socket_timeout");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"소켓 타임아웃, 단위 ms",
					"5000", true, 
					new ItemValueGetterOfMinMaxLong(1000, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectClientPartSubKeyName("connection.whether_to_auto_connect");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"연결 생성시 자동 접속 여부",
					"false", true, 
					new ItemValueGetterOfBoolean());
			addConfigItem(configItem);
			
			targetItemID = getProjectClientPartSubKeyName("connection.count");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"연결 갯수",
					"4", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectClientPartSubKeyName("data_packet_buffer_cnt");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 프로젝트가 가지는 데이터 패킷 버퍼 갯수",
					"1000", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectClientPartSubKeyName("asyn.finish_connect.max_call");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 비동기 소켓 채널의 연결 확립 최대 시도 횟수",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectClientPartSubKeyName("asyn.finish_connect.waitting_time");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 비동기 소켓 채널의 연결 확립을 재 시도 간격",
					"10", true, 
					new ItemValueGetterOfMinMaxLong(0, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectClientPartSubKeyName("asyn.output_message_executor_thread_cnt");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"비동기 출력 메시지 처리자 쓰레드 갯수",
					"1", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectClientPartSubKeyName("asyn.share.mailbox_cnt");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"비동기+공유 연결 클래스(ShareAsynConnection)의 메일함 갯수",
					"2", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectClientPartSubKeyName("asyn.input_message_queue_size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 비동기 입출력 지원용 입력 메시지 큐 크기",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			depenceItemID = getProjectClientPartSubKeyName("asyn.input_message_writer.max_size");
			targetItemID = getProjectClientPartSubKeyName("asyn.input_message_writer.size");
			// ItemValueGetterHash.put(getProjectClientPartSubKeyName(projectPartSubKeyOfDepenceItem), new ItemValueGetterOfMinMaxInteger("1", 1, Integer.MAX_VALUE));
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					depenceItemID,
					"클라이언트 비동기 입출력 지원용 입력 메시지 소켓 쓰기 담당 쓰레드 최대 갯수",
					"2", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 비동기 입출력 지원용 입력 메시지 소켓 쓰기 담당 쓰레드 갯수",
					"2", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			breakCheckerHash.put(targetItemID, 
					new  MinMaxBreakChecker(
							targetItemID,
							depenceItemID, 
							this));	
			// ItemValueGetterHash.put(getProjectClientPartSubKeyName(projectPartSubkey), new ItemValueGetterOfMinMaxInteger("1", 1, Integer.MAX_VALUE));
			
			targetItemID = getProjectClientPartSubKeyName("asyn.output_message_queue_size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"출력 메시지 큐 크기",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			// ItemValueGetterHash.put(getProjectClientPartSubKeyName("asyn.output_message_queue_size"), new ItemValueGetterOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
					
			depenceItemID = getProjectClientPartSubKeyName("asyn.output_message_reader.max_size");
			targetItemID = getProjectClientPartSubKeyName("asyn.output_message_reader.size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					depenceItemID,
					"클라이언트 비동기 입출력 지원용 입력 메시지 소켓 읽기 담당 쓰레드 최대 갯수",
					"4", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 비동기 입출력 지원용 입력 메시지 소켓 읽기 담당 쓰레드 갯수",
					"4", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			breakCheckerHash.put(targetItemID, 
					new  MinMaxBreakChecker(targetItemID, depenceItemID, this));	

			targetItemID = getProjectClientPartSubKeyName("asyn.read_selector_wakeup_interval");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 비동기 입출력 지원용 출력 메시지 소켓 읽기 담당 쓰레드에서 블락된 읽기 이벤트 전용 selector 를 깨우는 주기. 단위 ms",
					"10", true, 
					new ItemValueGetterOfMinMaxLong(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			// ItemValueGetterHash.put(getProjectClientPartSubKeyName("asyn.read_selector_wakeup_interval"), new ItemValueGetterOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
			
			targetItemID = getProjectClientPartSubKeyName("monitor.time_interval");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"모니터링 주기, 단위 ms",
					"5000", true, 
					new ItemValueGetterOfMinMaxLong(1000, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			/** 변경전:sample_test.client.monitor.request_timeout.value, 변경후:sample_test.client.monitor.reception_timeout.value*/
			targetItemID = getProjectClientPartSubKeyName("connection.socket_timeout");
			depenceItemID = getProjectClientPartSubKeyName("monitor.reception_timeout");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					depenceItemID,
					"데이터를 수신하지 않고 기다려주는 최대 시간, 권장 값은 소켓 타임 아웃 시간*2, 단위 ms",
					"20000", true, 
					new ItemValueGetterOfMinMaxLong(1000, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			breakCheckerHash.put(targetItemID, 
					new  MinMaxBreakChecker(targetItemID, depenceItemID, this));	
			// ItemValueGetterHash.put(getClientKeyName("monitor.time_interval"), new ItemValueGetterOfMinMaxLong("5000", 1000, Integer.MAX_VALUE));			
			// ItemValueGetterHash.put(getProjectClientPartSubKeyName("monitor.request_timeout"), new ItemValueGetterOfMinMaxLong("20000", 1000, Integer.MAX_VALUE));
			
			/** 프로젝트 서버 설정 부분 */
			targetItemID = getProjectServerPartSubKeyName("monitor.time_interval");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"모니터링 주기, 단위 ms",
					"5000", true, 
					new ItemValueGetterOfMinMaxLong(1000, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectServerPartSubKeyName("monitor.reception_timeout");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"데이터를 수신하지 않고 기다려주는 최대 시간, 권장 값은 소켓 타임 아웃 시간*2, 단위 ms",
					"20000", true, 
					new ItemValueGetterOfMinMaxLong(1000, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectServerPartSubKeyName("max_clients");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"서버로 접속할 수 있는 최대 클라이언트 수",
					"5", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectServerPartSubKeyName("data_packet_buffer_cnt");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"서버 프로젝트가 가지는 데이터 패킷 버퍼 수",
					"1000", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectServerPartSubKeyName("pool.accept_queue_size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"접속 승인 큐 크기",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(10, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectServerPartSubKeyName("pool.input_message_queue_size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"입력 메시지 큐 크기",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(10, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectServerPartSubKeyName("pool.output_message_queue_size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"출력 메시지 큐 크기",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(10, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectServerPartSubKeyName("accept_selector_timeout");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"접속 이벤트 전용 selector 에서 접속 이벤트 최대 대기 시간, 단위 ms",
					"10", true, 
					new ItemValueGetterOfMinMaxLong(10, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = getProjectServerPartSubKeyName("pool.read_selector_wakeup_interval");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"입력 메시지 소켓 읽기 담당 쓰레드에서 블락된 읽기 이벤트 전용 selector 를 깨우는 주기. 단위 ms",
					"10", true, 
					new ItemValueGetterOfMinMaxLong(10, Integer.MAX_VALUE));
			addConfigItem(configItem);	
			
			
			// ItemValueGetterHash.put(getProjectServerPartSubKeyName("max_clients"), new ItemValueGetterOfMinMaxInteger("5", 1, Integer.MAX_VALUE));
			// ItemValueGetterHash.put(getProjectServerPartSubKeyName("data_packet_buffer_cnt"), new ItemValueGetterOfMinMaxInteger("1000", 1, Integer.MAX_VALUE));
			// ItemValueGetterHash.put(getProjectServerPartSubKeyName("accept_selector_timeout"), new ItemValueGetterOfMinMaxLong("10", 10, Integer.MAX_VALUE));
			
			depenceItemID = getProjectServerPartSubKeyName("pool.accept_processor.max_size");
			targetItemID = getProjectServerPartSubKeyName("pool.accept_processor.size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					depenceItemID,
					"접속 요청이 승락된 클라이언트의 등록을 담당하는 쓰레드 최대 갯수",
					"1", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"접속 요청이 승락된 클라이언트의 등록을 담당하는 쓰레드 갯수",
					"1", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			breakCheckerHash.put(targetItemID, 
					new  MinMaxBreakChecker(targetItemID, depenceItemID, this));	
			
			depenceItemID = getProjectServerPartSubKeyName("pool.executor_processor.max_size");
			targetItemID = getProjectServerPartSubKeyName("pool.executor_processor.size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 					
					ConfigItem.ConfigItemViewType.TEXT,
					depenceItemID,
					"서버 비지니스 로직 수행 담당 쓰레드 최대 갯수",
					"1", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"서버 비지니스 로직 수행 담당 쓰레드 갯수",
					"1", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			breakCheckerHash.put(targetItemID, 
					new  MinMaxBreakChecker(targetItemID, depenceItemID, this));
			
			
			
			depenceItemID = getProjectServerPartSubKeyName("pool.input_message_reader.max_size");
			targetItemID = getProjectServerPartSubKeyName("pool.input_message_reader.size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					depenceItemID,
					"입력 메시지 소켓 읽기 담당 쓰레드 최대 갯수",
					"1", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"입력 메시지 소켓 읽기 담당 쓰레드 갯수",
					"1", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			breakCheckerHash.put(targetItemID, 
					new  MinMaxBreakChecker(targetItemID, depenceItemID, this));
			
			
			depenceItemID = getProjectServerPartSubKeyName("pool.output_message_writer.max_size");
			targetItemID = getProjectServerPartSubKeyName("pool.output_message_writer.size");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					depenceItemID,
					"출력 메시지 소켓 쓰기 담당 쓰레드 최대 갯수",
					"1", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"출력 메시지 소켓 쓰기 담당 쓰레드 갯수",
					"1", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			breakCheckerHash.put(targetItemID, 
					new  MinMaxBreakChecker(targetItemID, depenceItemID, this));
						
			
			targetItemID = getProjectServerPartSubKeyName("classloader.appinf.path");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER,
					ConfigItem.ConfigItemViewType.PATH,
					targetItemID,
					"서버 동적 클래스 APP-INF 경로",
					getDefaultValueOfAPPINFPath(), false, 
					new ItemValueGetterOfPath());
			addConfigItem(configItem);
			
			targetItemID = getProjectServerPartSubKeyName("classloader.mybatis_config_file_relative_path");
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"ClassLoader#getResourceAsStream 의 구현 이며 <APP-INF>/resources 경로 기준으로 읽어오며 구별자가 '/' 문자로된 상대 경로로 기술되어야 한다.",
					"kr/pe/sinnori/impl/mybatis/mybatisConfig.xml", false, 
					new ItemValueGetterOfNoCheck());
			addConfigItem(configItem);
			/*
			breakCheckerHash.put(targetItemID, 
					new  ClassLoaderResourceFileBreakChecker(targetItemID, 
							depenceItemID, this));*/
			
		} catch(ConfigValueInvalidException | IllegalArgumentException e) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)
			.append("]::project part sub key[")
			.append(targetItemID)
			.append("] errrorMessage=")
			.append(e.getMessage()).toString();
			
			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		}
	}
	public String getProjectConfigFilePathString() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);		
		strBuilder.append(File.separator);
		strBuilder.append("config");
		strBuilder.append(File.separator);
		strBuilder.append("sinnori.properties");
		
		return strBuilder.toString();
	}
	
	private String getDefaultValueOfDBCPConnPoolConfigFile() {
		return getDefaultValueOfDBCPConnPoolConfigFile("tw_sinnoridb");
	}
	
	private String getDefaultValueOfSessionKeyRSAKeypairPath() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);
		strBuilder.append(File.separator);
		strBuilder.append("rsa_keypair");		
		return strBuilder.toString();
	}
	
	private String getDefaultValueOfAPPINFPath() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);
		strBuilder.append(File.separator);
		strBuilder.append("server_build");
		strBuilder.append(File.separator);
		strBuilder.append("APP-INF");
		return strBuilder.toString();
	}
	
	public String getDefaultValueOfDBCPConnPoolConfigFile(String dbcpConnPoolName) {
		StringBuilder strBuilder = new StringBuilder(getDefaultValueOfAPPINFPath());
		strBuilder.append(File.separator);
		strBuilder.append("resources");
		strBuilder.append(File.separator);
		strBuilder.append("kr");
		strBuilder.append(File.separator);
		strBuilder.append("pe");		
		strBuilder.append(File.separator);
		strBuilder.append("sinnori");
		strBuilder.append(File.separator);
		strBuilder.append("impl");
		strBuilder.append(File.separator);
		strBuilder.append("mybatis");
		strBuilder.append(File.separator);
		strBuilder.append(dbcpConnPoolName);
		strBuilder.append(".properties");
		return strBuilder.toString();
	}
	
	public ConfigItem getConfigItem(String itemID) {
		return configItemHash.get(itemID);
	}
	
	private void addConfigItem(ConfigItem configItem) throws ConfigErrorException {
		ConfigItem oldConfigItem = configItemHash.get(configItem.getItemID());
		if (null != oldConfigItem) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)		
			.append("] errrorMessage=config item[")
			.append(configItem.getItemID())
			.append("] is registed").toString();
			
			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		}
		
		configItemHash.put(configItem.getItemID(), configItem);
		configItemList.add(configItem);
	}
	
	
	private String getProjectCommonPartID(String subkey) {
		/*StringBuffer strBuff = new StringBuffer(projectName);
		strBuff.append(".common.");
		strBuff.append(subkey);
		strBuff.append(".value");*/
		
		StringBuffer strBuff = new StringBuffer("common.");
		strBuff.append(subkey);
		strBuff.append(".value");
		
		return strBuff.toString();
	}
	
	private String getProjectServerPartSubKeyName(String subkey) {
		StringBuffer strBuff = new StringBuffer("server.");
		strBuff.append(subkey);
		strBuff.append(".value");		
		return strBuff.toString();
	}
		
	private String getProjectClientPartSubKeyName(String subkey) {
		StringBuffer strBuff = new StringBuffer("client.");
		strBuff.append(subkey);
		strBuff.append(".value");		
		return strBuff.toString();
	}
	
	public String getDefaultValueOfMessageInfoXMLPath() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);
		strBuilder.append(File.separator);
		strBuilder.append("impl");
		strBuilder.append(File.separator);
		strBuilder.append("message");
		strBuilder.append(File.separator);
		strBuilder.append("info");
		return strBuilder.toString();
	}
	/*
	public void combind(Properties sourceProperties) throws ConfigErrorException {
		makeDBCPCOnnectionPoolNameSetFromSourceProperties(sourceProperties);
		makeProjectNameSetFromSourceProperties(sourceProperties);
		
		try {
			checkOnlyAllKeyValidation(sourceProperties);
		} catch (ConfigKeyNotFoundException e) {
			String errorMessage = e.getMessage();
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
	}
	
	private void makeDBCPCOnnectionPoolNameSetFromSourceProperties(Properties sourceProperties) throws ConfigErrorException {
		String dbcpConnectionPoolNameListValue = sourceProperties.getProperty(DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING);
		
		if (null == dbcpConnectionPoolNameListValue) {
			*//** DBCP 연결 폴 이름 목록을 지정하는 키가 없을 경우 *//*
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("] has no a dbcp connection pool name list").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		String[] dbcpConnectionPoolNameArrray = dbcpConnectionPoolNameListValue.split(",");
		
		dbcpConnectionPoolNameList.clear();
		
		Set<String> tempNameSet = new HashSet<String>();
		
		for (String dbcpConnectionPoolNameOfList : dbcpConnectionPoolNameArrray) {
			dbcpConnectionPoolNameOfList = dbcpConnectionPoolNameOfList.trim();
			
			if (dbcpConnectionPoolNameOfList.equals("")) continue;
			
			tempNameSet.add(dbcpConnectionPoolNameOfList);
			dbcpConnectionPoolNameList.add(dbcpConnectionPoolNameOfList);
		}		
		
		if (tempNameSet.size() != dbcpConnectionPoolNameList.size()) {
			*//** DBCP 연결 폴 이름 목록의 이름들중 중복된 것이 있는 경우 *//*
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)					
			.append("]::dbcp connection pool name list has one more same thing").toString();
			
			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		}
		
	}
		
	private void makeProjectNameSetFromSourceProperties(Properties sourceProperties) throws ConfigErrorException {
		String projectNameListValue = sourceProperties.getProperty(PROJECT_NAME_LIST_KEY_STRING);
		
		log.info("mainProjectName={}, projectNameListValue={}", 
				mainProjectName, projectNameListValue);
		
		
		if (null == projectNameListValue) {
			*//** 프로젝트 목록을 지정하는 키가 없을 경우 *//*
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("] has no a project list").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		String[] projectNameArrray = projectNameListValue.split(",");
		if (0 == projectNameArrray.length) {
			*//** 프로젝트 목록 값으로 부터 프로젝트 목록을 추출할 수 없는 경우 *//*
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)					
			.append("]:: the project list is empty").toString();
			
			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		}

		projectNameList.clear();
		Set<String> tempNameSet = new HashSet<String>();
		
		
		for (String projectNameOfList : projectNameArrray) {
			projectNameOfList = projectNameOfList.trim();
			
			if (projectNameOfList.equals("")) continue;
			
			
			tempNameSet.add(projectNameOfList);
			projectNameList.add(projectNameOfList);
		}
		
		if (! tempNameSet.contains(mainProjectName)) {
			*//** 프로젝트 목록에 지정된 메인 프로젝트에 대한 정보가 없는 경우 *//*
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("]:: the project list has no main project").toString();
			
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
		
		if (tempNameSet.size() != projectNameList.size()) {
			*//** 프로젝트 목록의 이름들중 중복된 것이 있는 경우 *//*
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)					
			.append("]::project name list has one more same thing").toString();
			
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}		
	}
	*/
	
	/*public void addSubProjectName(String subProjectName) throws ConfigErrorException {
		if (null == subProjectName) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("]::parameter subProjectName is null").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		if (subProjectName.equals(mainProjectName)) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("]::parameter subProjectName is same to the main proejct name").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		if (projectNameList.contains(subProjectName)) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("]::already registered project name[")
			.append(subProjectName)
			.append("]").toString();
			throw new ConfigErrorException(errorMessage);
		}
		projectNameList.add(subProjectName);
	}
	
	public void deleteSubProjectName(String subProjectName) throws ConfigErrorException {
		if (null == subProjectName) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("]::parameter subProjectName is null").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		if (subProjectName.equals(mainProjectName)) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("]::the main project cannot be deleted").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		projectNameList.remove(subProjectName);
	}
	
	public List<String> getProjectNameList() {
		return projectNameList;
	}
	
	public List<String> getDBCPConnectionPoolNameList() {
		return dbcpConnectionPoolNameList;
	}
	
	public void addDBCPConnectionPoolName(String newDBCPConnPoolName) {
		if (dbcpConnectionPoolNameList.contains(newDBCPConnPoolName)) {
			String errorMessage = String.format("중복된 이름[%s]을 가진 DBCP 연결 폴 이름이 있습니다.", newDBCPConnPoolName);
			log.warn(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		dbcpConnectionPoolNameList.add(newDBCPConnPoolName);
	}
	
	public void removeDBCPConnectionPoolName(String selectedDBCPConnPoolName) {
		dbcpConnectionPoolNameList.remove(selectedDBCPConnPoolName);
	}
	*/
	public SequencedProperties getSequencedProperties() {
		SequencedProperties sequencedProperties = new SequencedProperties();
		
		String key = null;
		
		/** DBCP */
		{			
			List<String> dbcpConnectionPoolNameList = new ArrayList<String>();
			
			StringBuilder dbcpConnectionPoolNameListBuilder = new StringBuilder();
			int dbcpConnectionPoolNameListSize = dbcpConnectionPoolNameList.size();
			int i=0;
			if (i < dbcpConnectionPoolNameListSize) {
				String dbcpConnectionPoolName = dbcpConnectionPoolNameList.get(i);
				dbcpConnectionPoolNameListBuilder.append(dbcpConnectionPoolName);
				i++;
			}
			
			for (; i < dbcpConnectionPoolNameListSize; i++) {	
				String dbcpConnectionPoolName = dbcpConnectionPoolNameList.get(i);
				dbcpConnectionPoolNameListBuilder.append(", ");
				dbcpConnectionPoolNameListBuilder.append(dbcpConnectionPoolName);
			}
			
			sequencedProperties.setProperty(DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING, dbcpConnectionPoolNameListBuilder.toString());
			
			for (String dbcpConnectionPoolName : dbcpConnectionPoolNameList) {				
				for (ConfigItem configItem : dbcpPartConfigItemList) {
					String itemID = configItem.getItemID();
					
					key = new StringBuilder("dbcp.")
					.append(dbcpConnectionPoolName)
					.append(".")
					.append(itemID).toString();
					
					int len = key.length();
					String descKey = new StringBuilder(key.subSequence(0, len - ".value".length())).append(".desc").toString();
					
					// FIXME!
					log.info("key=[{}], descKey=[{}]", key, descKey);
					
					sequencedProperties.put(descKey, configItem.toDescription());
					sequencedProperties.put(key, configItem.getDefaultValue());
				}		
			}
		}
		
		
		/** common */
		for (ConfigItem configItem : commonPartConfigItemList) {
			key = configItem.getItemID();
			int len = key.length();
			String descKey = new StringBuilder(key.subSequence(0, len - ".value".length())).append(".desc").toString();
			
			// FIXME!
			log.info("key=[{}], descKey=[{}]", key, descKey);
			
			sequencedProperties.put(descKey, configItem.toDescription());
			sequencedProperties.put(key, configItem.getDefaultValue());
		}
		
		/** project */
		{
			List<String> projectNameList = new ArrayList<String>();
			projectNameList.add(this.mainProjectName);
			
			StringBuilder projectNameListBuilder = new StringBuilder();
			int projectNameListSize = projectNameList.size();
			int i=0;
			if (i < projectNameListSize) {
				String projectName = projectNameList.get(i);
				projectNameListBuilder.append(projectName);
				i++;
			}
			
			for (; i < projectNameListSize; i++) {	
				String projectName = projectNameList.get(i);
				projectNameListBuilder.append(", ");
				projectNameListBuilder.append(projectName);
			}
			
			sequencedProperties.setProperty(PROJECT_NAME_LIST_KEY_STRING, projectNameListBuilder.toString());
			
			for (String projectName : projectNameList) {
				for (ConfigItem configItem : projectPartConfigItemList) {
					key = new StringBuilder("project.")
					.append(projectName)
					.append(".")
					.append(configItem.getItemID()).toString();
					
					int len = key.length();
					String descKey = new StringBuilder(key.subSequence(0, len - ".value".length())).append(".desc").toString();
					
					// FIXME!
					log.info("key=[{}], descKey=[{}]", key, descKey);
					
					sequencedProperties.put(descKey, configItem.toDescription());
					sequencedProperties.put(key, configItem.getDefaultValue());
				}
			}
		}		
		
		return sequencedProperties;
	}
	
	// FIXME!
	public boolean isValidation(String targetKey, Properties sourceProperties) throws ConfigValueInvalidException {
		boolean isValidation= true;
		String itemID = getItemIDFromKey(targetKey);
		if (null == itemID) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)
			.append("]::parameter targetKey[")
			.append(targetKey)
			.append("] is bad, itemID is null").toString();
			
			log.warn(errorMessage);
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		int inx = targetKey.indexOf(itemID);
		String prefixOfDomain = targetKey.substring(0, inx);
		// FIXME!
		log.info("targetKey={}, itemID={}, inx={}, prefix={}", targetKey, itemID, inx, prefixOfDomain);
		
		AbstractConditionChecker conditionChecker = conditionCheckerHash.get(itemID);
				
		if (null != conditionChecker) {
			isValidation = conditionChecker.isValidation(sourceProperties, prefixOfDomain);
		}
		return isValidation;
	}
	
	
	public String getItemIDFromKey(String key) {
		StringTokenizer tokens = new StringTokenizer(key, "."); 
		if (tokens.hasMoreTokens()) {
			String firstToken = tokens.nextToken();
			if (firstToken.equals("dbcp")) {
				if (tokens.hasMoreTokens()) {
					String secondToken = tokens.nextToken();
					if (secondToken.equals("connection_pool_name_list")) {
						return null;
					}
					
					StringBuilder itemIDBuilder = new StringBuilder();
					
					/*if (dbcpConnectionPoolNameList.contains(secondToken)) {
						if (tokens.hasMoreTokens()) {
							itemIDBuilder.append(tokens.nextToken());
						}
						while (tokens.hasMoreTokens()) {
							itemIDBuilder.append(".");
							itemIDBuilder.append(tokens.nextToken());
						}
					} else {								
						itemIDBuilder.append(secondToken);
						
						while (tokens.hasMoreTokens()) {
							itemIDBuilder.append(".");
							itemIDBuilder.append(tokens.nextToken());
						}
					}*/
					
					if (tokens.hasMoreTokens()) {
						itemIDBuilder.append(tokens.nextToken());
					}
					while (tokens.hasMoreTokens()) {
						itemIDBuilder.append(".");
						itemIDBuilder.append(tokens.nextToken());
					}
					
					// FIXME!
					// log.info("dbcp subkey={}", itemIDBuilder.toString());
					return itemIDBuilder.toString();
				}
			} else if (firstToken.equals("project")) {
				if (tokens.hasMoreTokens()) {
					String secondToken = tokens.nextToken();
					if (secondToken.equals("name_list")) {
						return null;
					}
					
					/*if (projectNameList.contains(secondToken)) {
						StringBuilder itemIDBuilder = new StringBuilder();
						if (tokens.hasMoreTokens()) {
							itemIDBuilder.append(tokens.nextToken());
						}
						
						while (tokens.hasMoreTokens()) {
							itemIDBuilder.append(".");
							itemIDBuilder.append(tokens.nextToken());
						}
						
						return itemIDBuilder.toString();
					}*/
					
					StringBuilder itemIDBuilder = new StringBuilder();
					if (tokens.hasMoreTokens()) {
						itemIDBuilder.append(tokens.nextToken());
					}
					
					while (tokens.hasMoreTokens()) {
						itemIDBuilder.append(".");
						itemIDBuilder.append(tokens.nextToken());
					}
					
					return itemIDBuilder.toString();
				}			
				
			} else {
				return key;
			}
		}
		
		return null;
	}
		
	
	public Object getNativeValueAfterBreakChecker(String targetKey, Properties sourceProperties) throws ConfigValueInvalidException {		
		String itemID = getItemIDFromKey(targetKey);
		if (null == itemID) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)
			.append("]::parameter targetKey[")
			.append(targetKey)
			.append("] is bad, itemID is null").toString();
			
			log.warn(errorMessage);
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		ConfigItem configItem = configItemHash.get(itemID);	
		if (null == configItem) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)
			.append("]::parameter targetKey[")
			.append(targetKey)
			.append("] is bad, itemConfig is null").toString();
			
			log.warn(errorMessage);
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		int inx = targetKey.indexOf(itemID);
		String prefix = targetKey.substring(0, inx);
		
		// FIXME!
		log.info("targetKey={}, itemID={}, inx={}, prefix={}", targetKey, itemID, inx, prefix);		
		
		AbstractBreakChecker breakChecker = breakCheckerHash.get(itemID);		

		if (null != breakChecker) {
			// String valueOfDependenceItem = sourceProperties.getProperty(afterDependenceItem.getKeyOfDependenceItem());
			try {
				breakChecker.validate(sourceProperties, prefix);
			} catch(ConfigValueInvalidException e) {
				String errorMessage = new StringBuilder("project config file[")
				.append(projectConfigFilePathString)			
				.append("]::targetKey[")
				.append(targetKey)
				.append("] errrorMessage=")
				.append(e.getMessage()).toString();
				
				log.warn(errorMessage);
				
				throw new ConfigValueInvalidException(errorMessage);
			}
		}
		
		Object targetNativeValue = null;
		String targetValue = sourceProperties.getProperty(targetKey);
		try {
			targetNativeValue = configItem.getItemValueGetter().getItemValueWithValidation(targetValue);
		} catch(ConfigValueInvalidException e) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)			
			.append("]::targetKey[")
			.append(targetKey)
			.append("] errrorMessage=")
			.append(e.getMessage()).toString();
			
			log.warn(errorMessage);
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		return targetNativeValue;
	}
	
	
	/**
	 * <pre>
	 * 환경 변수 전체 키 값들이 유효한 키인지 검사한다.
	 * 유효한 키란 환경 변수 값을 검사하기 위한 정보에 등록된 키를 말한다.
	 * </pre>
	 * 
	 * @throws ConfigKeyNotFoundException 환경 변수 값을 검사하기 위한 정보에 등록된 키가 없을 경우 던지는 예외
	 */
	@SuppressWarnings("unused")
	private void checkOnlyAllKeyValidation(Properties sourceProperties,
			List<String> dbcpConnectionPoolNameList,
			List<String> projectNameList) throws ConfigKeyNotFoundException {
		ConfigItem itemConfig = null;
		
		/**
		 * 소스 프로퍼터티 키들이 설정 정보에 등록된 키인지 검사
		 */
		Enumeration<Object> enumKey = sourceProperties.keys();
		while (enumKey.hasMoreElements()) {
			String key = (String)enumKey.nextElement();
			
			if (key.equals(DBCP_CONNECTION_POOL_NAME_LIST_KEY_STRING)) {
				continue;
			} else if (key.equals(PROJECT_NAME_LIST_KEY_STRING)) {
				continue;
			} else if (key.endsWith(".value")) {
				String itemID = getItemIDFromKey(key);
				
				if (null == itemID) {
					String errorMessage = new StringBuilder("project config file[")
					.append(projectConfigFilePathString)
					.append("]::source key[")
					.append(key)
					.append("] is bad, itemID is null").toString();
					
					log.warn(errorMessage);
					
					throw new ConfigKeyNotFoundException(errorMessage);
				}
				
				itemConfig = configItemHash.get(itemID);			
					
				if (null == itemConfig) {
					String errorMessage = new StringBuilder("project config file[")
					.append(projectConfigFilePathString)
					.append("]::source key[")
					.append(key)
					.append("] is bad, itemConfig is null").toString();
					
					log.warn(errorMessage);
					
					throw new ConfigKeyNotFoundException(errorMessage);
				}
			}
		}
		
		/**
		 * 설정 정보에 등록된 키들이 소스 프로퍼티에 있는지 검사
		 */
		for (String dbcpConnectionPoolName : dbcpConnectionPoolNameList) {		
			for (ConfigItem configItem : dbcpPartConfigItemList) {
				String itemID = configItem.getItemID();
				
				String key = new StringBuilder("dbcp.")
				.append(dbcpConnectionPoolName)
				.append(".")
				.append(itemID).toString();
				
				log.info("key=[{}]", key);
				
				Object value = sourceProperties.get(key);
				if (null == value) {
					String errorMessage = new StringBuilder("project config file[")
					.append(projectConfigFilePathString)
					.append("]::config key[")
					.append(key)
					.append("] is bad, value is null").toString();
					
					log.warn(errorMessage);
					
					throw new ConfigKeyNotFoundException(errorMessage);
				}
			}		
		}
		
		for (ConfigItem configItem : commonPartConfigItemList) {
			String key = configItem.getItemID();			
			
			log.info("key=[{}]", key);
			
			Object value = sourceProperties.get(key);
			if (null == value) {
				String errorMessage = new StringBuilder("project config file[")
				.append(projectConfigFilePathString)
				.append("]::config key[")
				.append(key)
				.append("] is bad, value is null").toString();
				
				log.warn(errorMessage);
				
				throw new ConfigKeyNotFoundException(errorMessage);
			}
		}
		
		for (String projectName : projectNameList) {
			for (ConfigItem configItem : projectPartConfigItemList) {
				String key = new StringBuilder("project.")
				.append(projectName)
				.append(".")
				.append(configItem.getItemID()).toString();

				// FIXME!
				log.info("key=[{}]", key);
				
				Object value = sourceProperties.get(key);
				if (null == value) {
					String errorMessage = new StringBuilder("project config file[")
					.append(projectConfigFilePathString)
					.append("]::config key[")
					.append(key)
					.append("] is bad, value is null").toString();
					
					log.warn(errorMessage);
					
					throw new ConfigKeyNotFoundException(errorMessage);
				}
			}
		}
	}

	public List<ConfigItem> getDBCPPartConfigItemList() {
		return dbcpPartConfigItemList;
	}

	public List<ConfigItem> getCommonPartConfigItemList() {
		return commonPartConfigItemList;
	}

	public List<ConfigItem> getProjectPartConfigItemList() {
		return projectPartConfigItemList;
	}
	
	
	
}
