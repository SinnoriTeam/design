package kr.pe.sinnori.common.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import kr.pe.sinnori.common.config.dependitem.AfterDependenceItemOfClassLoaderResourceFile;
import kr.pe.sinnori.common.config.dependitem.AfterDependenceItemOfIntegerTypeMaxValue;
import kr.pe.sinnori.common.config.dependitem.BeforeDependenceItemOfWantedMatchValue;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfBoolean;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfByteOrder;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfCharset;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfConnectionType;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfFile;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfMessageInfoXMLPath;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfMessageProtocol;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfMinMaxInteger;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfMinMaxLong;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfNoCheck;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfNoNullAndEmptyString;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfPath;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfSessionKeyRSAKeypairPath;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfSessionkeyPrivateKeyEncoding;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfSingleIntegerSet;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfSingleStringSet;
import kr.pe.sinnori.common.config.itemvalidator.ItemValidatorOfUpDownFileBlockMaxSize;
import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigKeyNotFoundException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.common.util.SequencedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SinnoriProjectConfig {
	private Logger log = LoggerFactory.getLogger(SinnoriProjectConfig.class);
	
	private String projectName;
	private String projectPathString;
	// private String projectConfigFilePathString;
	// private SequencedProperties sourceSequencedProperties;

	private String projectConfigFilePathString;
	private List<ConfigItem> itemList = new ArrayList<ConfigItem>();
	private Hashtable<String, ConfigItem> itemHash = new Hashtable<String, ConfigItem>();
	
		
	private Hashtable<String, AbstractBeforeDependenceItem> beforeDependenceItemHash = new Hashtable<String, AbstractBeforeDependenceItem>();
	private Hashtable<String, AbstractAfterDependenceItem> afterDependenceItemHash = new Hashtable<String, AbstractAfterDependenceItem>();
	
	private List<String> dbcpConnectionPoolNameList = new LinkedList<String>();
	private List<ConfigItem> dbcpPartItemList = new ArrayList<ConfigItem>();
	private Hashtable<String, ConfigItem> dbcpPartItemHash = new Hashtable<String, ConfigItem>();
	
	private Hashtable<String, AbstractBeforeDependenceItem> beforeDependenceDBCPPartItemHash = new Hashtable<String, AbstractBeforeDependenceItem>();	
	private Hashtable<String, AbstractAfterDependenceItem> afterDependenceDBCPPartItemHash = new Hashtable<String, AbstractAfterDependenceItem>();
	
	
	private List<ConfigItem> projectPartItemList = new ArrayList<ConfigItem>();
	private Hashtable<String, ConfigItem> projectPartItemHash = new Hashtable<String, ConfigItem>();
		
	private Hashtable<String, AbstractBeforeDependenceItem> beforeDependenceProjectPartItemHash = new Hashtable<String, AbstractBeforeDependenceItem>();	
	private Hashtable<String, AbstractAfterDependenceItem> afterDependenceProjectPartItemHash = new Hashtable<String, AbstractAfterDependenceItem>();

	private List<String> projectNameList = new LinkedList<String>();
	
	public SinnoriProjectConfig(String projectName, String projectPathString) throws ConfigErrorException {
		this.projectName = projectName;
		this.projectPathString = projectPathString;		
		// this.sourceSequencedProperties = sourceSequencedProperties;		
		// this.projectConfigFilePathString = getProjectConfigFilePathString();
		
		projectConfigFilePathString = getProjectConfigFilePathString();
		
		projectNameList.add(projectName);
		
		initCommonPart();
		
		dbcpPart();
		
		initProjectPart();
		
		/*try {
			validAllKey();
		} catch(ConfigKeyNotFoundException e) {
			*//** 알 수 없는 키가 존재할 경우 *//*
			throw new ConfigErrorException(e.getMessage());
		}*/
	}
	
	private void dbcpPart() throws IllegalArgumentException, ConfigErrorException {
		ConfigItem item = null;
		// String keyOfDepenceItem = null;
		String key = null;		
		try {
			/** DBCP start */
			
			key = "connection_pool_name_list.value";
			item = new ConfigItem(key,
					"dbcp 연결 폴 이름 목록, 구분자 콤마",
					"", false, 
					new ItemValidatorOfNoCheck());
			dbcpPartItemList.add(item);
			dbcpPartItemHash.put(item.getKey(), item);
			
			key = "confige_file.value";
			item = new ConfigItem(key,
					"연결 폴의 설정 파일 경로명, 형식 : dbcp.<dbcp 연결 폴 이름>.config_file.value",
					"/home/madang01/gitsinnori/sinnori/project/sample_test/server_build/APP-INF/resources/kr/pe/sinnori/impl/mybatis/tw_sinnoridb.properties", false, 
					new ItemValidatorOfFile());
			dbcpPartItemList.add(item);
			dbcpPartItemHash.put(item.getKey(), item);

			/** DBCP end */
		} catch(ConfigValueInvalidException | IllegalArgumentException e) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)			
			.append("]::key[")
			.append(key)
			.append("] errrorMessage=")
			.append(e.getMessage()).toString();
			
			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		}
	}
	
	private void initCommonPart() throws IllegalArgumentException, ConfigErrorException {
		ConfigItem item = null;
		String keyOfDepenceItem = null;
		String key = null;		
		try {
			/*key = "jdbc.connection_uri.value";
			item = new ConfigItem(key, 
					"jdbc connection url",
					"jdbc:mysql://localhost:3306/sinnori", true,
					 new ItemValidatorOfJdbcConnectionURI());
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "jdbc.db_user_name.value";
			item = new ConfigItem(key,
					"DB user name",
					"dbmadangse", true, 
					new ItemValidatorOfJdbcDBUserName());
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "jdbc.db_user_password.value";
			item = new ConfigItem(key,
					"DB user password",
					"test1234", true, 
					new ItemValidatorOfJdbcDBUserPassword());
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "jdbc.driver_class_name.value";
			item = new ConfigItem(key,
					"JDBC driver class name",
					"com.mysql.jdbc.Driver", true, 
					new ItemValidatorOfJdbcDriverClassName());
			itemList.add(item);
			itemHash.put(item.getKey(), item);*/
			
			// dbcp.connection_pool_name_list.value
			
			
			
			key = "servlet_jsp.jdf_error_message_page.value";
			item = new ConfigItem(key,
					"JDF framework 에서 에러 발생시 에러 내용을 보여주는 사용자 친화적인 화면을 전담할 jsp",
					"/errorMessagePage.jsp", true, 
					new ItemValidatorOfNoNullAndEmptyString());
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "servlet_jsp.jdf_login_page.value";
			item = new ConfigItem(key,
					"로그인 처리 jsp",
					"/login.jsp", true, 
					new ItemValidatorOfNoNullAndEmptyString());
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "servlet_jsp.jdf_servlet_trace.value";
			item = new ConfigItem(key,
					"JDF framework에서 서블릿 경과시간 추적 여부",
					"true", true, 
					new ItemValidatorOfBoolean());
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "servlet_jsp.web_layout_control_page.value";
			item = new ConfigItem(key,
					"신놀이 웹 사이트의 레이아웃 컨트롤러 jsp",
					"/PageJump.jsp", true, 
					new ItemValidatorOfNoNullAndEmptyString());
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			keyOfDepenceItem = "sessionkey.rsa_keypair_source.value";
			key = "sessionkey.rsa_keypair_path.value";
			
			item = new ConfigItem(keyOfDepenceItem,
					"세션키에 사용되는 공개키 키쌍 생성 방법, API:자체 암호 lib 이용하여 RSA 키쌍 생성, File:외부 파일를 읽어와서 RSA  키쌍을 생성",
					"API", true, 
					new ItemValidatorOfSingleStringSet("API", "File"));
			itemList.add(item);
			itemHash.put(item.getKey(), item);			
			
			item = new ConfigItem(key,
					"세션키에 사용되는 공개키 키쌍 파일 경로, 세션키에 사용되는 공개키 키쌍 생성 방법이 File인 경우에 유효하다.",
					getSessionKeyRSAKeypairPathString(), true, 
					new ItemValidatorOfSessionKeyRSAKeypairPath());
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			beforeDependenceItemHash.put(key, 
					new BeforeDependenceItemOfWantedMatchValue(keyOfDepenceItem, 
					itemHash.get(keyOfDepenceItem).getItemValidator(), "File"));			
			
			
			key = "sessionkey.rsa_keysize.value";
			item = new ConfigItem(key,
					"세션키에 사용하는 공개키 크기, 단위 byte",
					"1024", true, 
					new ItemValidatorOfSingleIntegerSet("512", "1024", "2048"));
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "sessionkey.symmetric_key_algorithm.value";
			item = new ConfigItem(key,
					"세션키에 사용되는 대칭키 알고리즘",
					"AES", true, 
					new ItemValidatorOfSingleStringSet("AES", "DESede", "DES"));
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "sessionkey.symmetric_key_size.value";
			item = new ConfigItem(key,
					"세션키에 사용되는 대칭키 크기",
					"16", true, 
					new ItemValidatorOfSingleIntegerSet("8", "16", "24"));
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "sessionkey.iv_size.value";
			item = new ConfigItem(key,
					"세션키에 사용되는 대칭키와 같이 사용되는 IV 크기",
					"16", true, 
					new ItemValidatorOfSingleIntegerSet("8", "16", "24"));
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "sessionkey.private_key.encoding.value";
			item = new ConfigItem(key,
					"개인키를 인코딩 방법, 웹의 경우 이진데이터는 폼 전송이 불가하므로 base64 인코딩하여 전송한다.",
					"BASE64", true, 
					new ItemValidatorOfSessionkeyPrivateKeyEncoding());
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "common.updownfile.local_source_file_resource_cnt.value";
			item = new ConfigItem(key,
					"로컬 원본 파일 자원 갯수",
					"10", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "common.updownfile.local_target_file_resource_cnt.value";
			item = new ConfigItem(key,
					"로컬 목적지 파일 자원 갯수",
					"10", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			key = "common.updownfile.file_block_max_size.value";
			item = new ConfigItem(key,
					"파일 송수신 파일 블락 최대 크기, 1024 배수, 단위 byte",
					"1048576", true, 
					new ItemValidatorOfUpDownFileBlockMaxSize(1024, Integer.MAX_VALUE));
			itemList.add(item);
			itemHash.put(item.getKey(), item);
						
			key = "common.cached_object.max_size.value";
			item = new ConfigItem(key,
					"싱글턴 클래스 객체 캐쉬 관리자(LoaderAndName2ObjectManager) 에서 캐쉬로 관리할 객체의 최대 갯수. 주로 캐쉬되는 대상 객체는 xxxServerCodec, xxxClientCodec 이다.",
					"100", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			// common.projectlist.value
			key = "common.projectlist.value";
			item = new ConfigItem(key,
					"프로젝트 목록, 프로젝트 구분은 콤마",
					"", false, 
					new ItemValidatorOfNoCheck());
			itemList.add(item);
			itemHash.put(item.getKey(), item);
			
			/*itemValidatorHash.put("jdbc.connection_uri.value", new ItemValidatorOfJdbcConnectionURI("jdbc:mysql://localhost:3306/sinnori"));
			itemValidatorHash.put("jdbc.db_user_name.value", new ItemValidatorOfJdbcDBUserName("dbmadangse"));*/
			//itemValidatorHash.put("jdbc.db_user_password.value", new ItemValidatorOfJdbcDBUserPassword("test1234"));
			// itemValidatorHash.put("jdbc.driver_class_name.value", new ItemValidatorOfJdbcDriverClassName("com.mysql.jdbc.Driver"));
			// itemValidatorHash.put("servlet_jsp.jdf_error_message_page.value", new ItemValidatorOfNoNullAndEmptyString("/errorMessagePage.jsp"));
			// itemValidatorHash.put("servlet_jsp.jdf_login_page.value", new ItemValidatorOfNoNullAndEmptyString("/login.jsp"));
			// itemValidatorHash.put("servlet_jsp.jdf_servlet_trace.value", new ItemValidatorOfBoolean("true"));
			// itemValidatorHash.put("servlet_jsp.web_layout_control_page.value", new ItemValidatorOfNoNullAndEmptyString("/PageJump.jsp"));
			// itemValidatorHash.put("sessionkey.rsa_keypair_source.value", new ItemValidatorOfSingleStringSet("API", "File"));
			// itemValidatorHash.put("sessionkey.rsa_keypair_path.value", new ItemValidatorOfSessionKeyRSAKeypairPath(getDefaultValueOfSessionKeyRSAKeypairPath()));			
			// itemValidatorHash.put("sessionkey.rsa_keysize.value", new ItemValidatorOfSingleIntegerSet("1024", "512", "1024", "2048"));			
			// itemValidatorHash.put("sessionkey.symmetric_key_algorithm.value", new ItemValidatorOfSingleStringSet("ASE", "ASE", "DESede", "DES"));			
			// itemValidatorHash.put("sessionkey.symmetric_key_size.value", new ItemValidatorOfSingleIntegerSet("16", "8", "16", "24"));			
			// itemValidatorHash.put("sessionkey.private_key.encoding.value", new ItemValidatorOfSessionkeyPrivateKeyEncoding("BASE64"));
			
			// itemValidatorHash.put("common.updownfile.local_source_file_resource_cnt.value", new ItemValidatorOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
			// itemValidatorHash.put("common.updownfile.local_target_file_resource_cnt.value", new ItemValidatorOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
			//itemValidatorHash.put("common.updownfile.file_block_max_size.value", new ItemValidatorOfUpDownFileBlockMaxSize("1048576", 1024, Integer.MAX_VALUE));		
		} catch(ConfigValueInvalidException | IllegalArgumentException e) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)			
			.append("]::key[")
			.append(key)
			.append("] errrorMessage=")
			.append(e.getMessage()).toString();
			
			log.warn(errorMessage);
			
			throw new ConfigErrorException(errorMessage);
		}
	}
	
	private void initProjectPart() throws ConfigErrorException {
		String projectPartSubKeyOfDepenceItem = null;
		String projectPartSubkey = null;
		ConfigItem item = null;
		try {			
			/** 프로젝트 공통 설정 부분 */
			projectPartSubkey = getProjectCommonPartSubKeyName("message_info.xmlpath");
			item = new ConfigItem(projectPartSubkey,
					"메시지 정보 파일 경로",
					getDefaultValueOfMessageInfoXMLPath(), true, 
					new ItemValidatorOfMessageInfoXMLPath());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectCommonPartSubKeyName("host");
			item = new ConfigItem(projectPartSubkey,
					"클라이언트에서 접속할 서버 주소",
					"localhost", true, 
					new ItemValidatorOfNoNullAndEmptyString());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectCommonPartSubKeyName("port");
			item = new ConfigItem(projectPartSubkey,
					"포트 번호",
					"9090", true, 
					new ItemValidatorOfMinMaxInteger(1024, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectCommonPartSubKeyName("byteorder");
			item = new ConfigItem(projectPartSubkey,
					"바이트 오더",
					"LITTLE_ENDIAN", true, 
					new ItemValidatorOfByteOrder());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectCommonPartSubKeyName("charset");
			item = new ConfigItem(projectPartSubkey,
					"문자셋",
					"UTF-8", true, 
					new ItemValidatorOfCharset());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectCommonPartSubKeyName("data_packet_buffer_max_cnt_per_message");
			item = new ConfigItem(projectPartSubkey,
					"1개 메시지당 할당 받을 수 있는 데이터 패킷 버퍼 최대수",
					"1000", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectCommonPartSubKeyName("data_packet_buffer_size");
			item = new ConfigItem(projectPartSubkey,
					"데이터 패킷 버퍼 크기, 단위 byte",
					"4096", true, 
					new ItemValidatorOfMinMaxInteger(1024, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			/** 메시지 식별자 크기의 최소 크기는 내부적으로 사용하는 SelfExn 메시지를 기준으로 정했음. */
			projectPartSubkey = getProjectCommonPartSubKeyName("message_id_fixed_size");
			item = new ConfigItem(projectPartSubkey,
					"메시지 식별자 크기의 최소 크기",
					"50", true, 
					new ItemValidatorOfMinMaxInteger(7, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectCommonPartSubKeyName("message_protocol");
			item = new ConfigItem(projectPartSubkey,
					"메시지 프로토콜, DHB:교차 md5 헤더+바디, DJSON:길이+존슨문자열, THB:길이+바디",
					"DHB", true, 
					new ItemValidatorOfMessageProtocol());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			/** 변경전 : classloader.class.package_prefix_name, 변경후 : classloader.class_package_prefix_name*/
			projectPartSubkey = getProjectCommonPartSubKeyName("classloader.class_package_prefix_name");
			item = new ConfigItem(projectPartSubkey,
					"동적 클래스 패키지명 접두어, 동적 클래스 여부를 판단하는 기준",
					"kr.pe.sinnori.impl.", true, 
					new ItemValidatorOfNoNullAndEmptyString());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			// itemValidatorHash.put(getCommonKeyName("message_info.xmlpath"), new ItemValidatorOfMessageInfoXMLPath(getDefaultValueOfMessageInfoXMLPath()));
			// itemValidatorHash.put(getCommonKeyName("host"), new ItemValidatorOfNoNullAndEmptyString("localhost"));
			// itemValidatorHash.put(getCommonKeyName("port"), new ItemValidatorOfMinMaxInteger("9090", 1024, Integer.MAX_VALUE));
			// itemValidatorHash.put(getCommonKeyName("byteorder"), new ItemValidatorOfByteOrder("LITTLE_ENDIAN"));
			// itemValidatorHash.put(getCommonKeyName("charset"), new ItemValidatorOfCharset("UTF-8"));
			// itemValidatorHash.put(getCommonKeyName("data_packet_buffer_max_cnt_per_message"), new ItemValidatorOfMinMaxInteger("1000", 1, Integer.MAX_VALUE));
			/** 메시지 식별자 크기의 최소 크기는 내부적으로 사용하는 SelfExn 메시지를 기준으로 정했음. */
			// itemValidatorHash.put(getCommonKeyName("message_id_fixed_size"), new ItemValidatorOfMinMaxInteger("50", 7, Integer.MAX_VALUE));
			// itemValidatorHash.put(getCommonKeyName("message_protocol"), new ItemValidatorOfSingleStringSet("DHB", "DHB", "DJSON", "THB"));
			/** 변경전 : classloader.class.package_prefix_name, 변경후 : classloader.class_package_prefix_name*/
			// itemValidatorHash.put(getCommonKeyName("classloader.class_package_prefix_name"), new ItemValidatorOfNoNullAndEmptyString("kr.pe.sinnori.impl."));
			
			
			/** 프로젝트 클라이언트 설정 부분 */
			projectPartSubkey = getProjectClientPartSubKeyName("connection.type");
			item = new ConfigItem(projectPartSubkey,
					"소캣 랩퍼 클래스인 연결 종류, NoShareAsyn:비공유+비동기, ShareAsyn:공유+비동기, NoShareSync:비공유+동기",
					"NoShareAsyn", true, 
					new ItemValidatorOfConnectionType());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			
			projectPartSubkey = getProjectClientPartSubKeyName("connection.socket_timeout");
			item = new ConfigItem(projectPartSubkey,
					"소켓 타임아웃, 단위 ms",
					"5000", true, 
					new ItemValidatorOfMinMaxLong(1000, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectClientPartSubKeyName("connection.whether_to_auto_connect");
			item = new ConfigItem(projectPartSubkey,
					"연결 생성시 자동 접속 여부",
					"false", true, 
					new ItemValidatorOfBoolean());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectClientPartSubKeyName("connection.count");
			item = new ConfigItem(projectPartSubkey,
					"연결 갯수",
					"4", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectClientPartSubKeyName("data_packet_buffer_cnt");
			item = new ConfigItem(projectPartSubkey,
					"클라이언트 프로젝트가 가지는 데이터 패킷 버퍼 갯수",
					"1000", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectClientPartSubKeyName("asyn.finish_connect.max_call");
			item = new ConfigItem(projectPartSubkey,
					"클라이언트 비동기 소켓 채널의 연결 확립 최대 시도 횟수",
					"10", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectClientPartSubKeyName("asyn.finish_connect.waitting_time");
			item = new ConfigItem(projectPartSubkey,
					"클라이언트 비동기 소켓 채널의 연결 확립을 재 시도 간격",
					"10", true, 
					new ItemValidatorOfMinMaxLong(0, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectClientPartSubKeyName("asyn.output_message_executor_thread_cnt");
			item = new ConfigItem(projectPartSubkey,
					"비동기 출력 메시지 처리자 쓰레드 갯수",
					"1", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectClientPartSubKeyName("asyn.share.mailbox_cnt");
			item = new ConfigItem(projectPartSubkey,
					"비동기+공유 연결 클래스(ShareAsynConnection)의 메일함 갯수",
					"2", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectClientPartSubKeyName("asyn.input_message_queue_size");
			item = new ConfigItem(projectPartSubkey,
					"클라이언트 비동기 입출력 지원용 입력 메시지 큐 크기",
					"10", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			/// itemValidatorHash.put(getClientKeyName("connection.type"), new ItemValidatorOfSingleStringSet("NoShareAsyn", "NoShareAsyn", "ShareAsyn", "NoShareSync"));
			// itemValidatorHash.put(getClientKeyName("connection.socket_timeout"), new ItemValidatorOfMinMaxLong("5000", 1000, Integer.MAX_VALUE));
			// itemValidatorHash.put(getClientKeyName("connection.whether_to_auto_connect"), new ItemValidatorOfBoolean("false"));			
			// itemValidatorHash.put(getClientKeyName("connection.count"), new ItemValidatorOfMinMaxInteger("4", 1, Integer.MAX_VALUE));			
			// itemValidatorHash.put(getProjectClientPartSubKeyName("data_packet_buffer_cnt"), new ItemValidatorOfMinMaxInteger("1000", 1, Integer.MAX_VALUE));			
			// itemValidatorHash.put(getProjectClientPartSubKeyName("asyn.finish_connect.max_call"), new ItemValidatorOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
			// itemValidatorHash.put(getProjectClientPartSubKeyName("asyn.finish_connect.waitting_time"), new ItemValidatorOfMinMaxInteger("10", 0, Integer.MAX_VALUE));
			// itemValidatorHash.put(getProjectClientPartSubKeyName("asyn.output_message_executor_thread_cnt"), new ItemValidatorOfMinMaxInteger("1", 1, Integer.MAX_VALUE));
			// itemValidatorHash.put(getProjectClientPartSubKeyName("asyn.share.mailbox_cnt"), new ItemValidatorOfMinMaxInteger("2", 1, Integer.MAX_VALUE));
			// itemValidatorHash.put(getProjectClientPartSubKeyName("asyn.input_message_queue_size"), new ItemValidatorOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
			
			projectPartSubKeyOfDepenceItem = getProjectClientPartSubKeyName("asyn.input_message_writer.max_size");
			projectPartSubkey = getProjectClientPartSubKeyName("asyn.input_message_writer.size");
			// itemValidatorHash.put(getProjectClientPartSubKeyName(projectPartSubKeyOfDepenceItem), new ItemValidatorOfMinMaxInteger("1", 1, Integer.MAX_VALUE));
			item = new ConfigItem(projectPartSubKeyOfDepenceItem,
					"클라이언트 비동기 입출력 지원용 입력 메시지 소켓 쓰기 담당 쓰레드 최대 갯수",
					"2", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			item = new ConfigItem(projectPartSubkey,
					"클라이언트 비동기 입출력 지원용 입력 메시지 소켓 쓰기 담당 쓰레드 갯수",
					"2", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			afterDependenceProjectPartItemHash.put(projectPartSubkey, 
					new  AfterDependenceItemOfIntegerTypeMaxValue(projectPartSubKeyOfDepenceItem, 
							projectPartItemHash.get(projectPartSubKeyOfDepenceItem).getItemValidator()));	
			// itemValidatorHash.put(getProjectClientPartSubKeyName(projectPartSubkey), new ItemValidatorOfMinMaxInteger("1", 1, Integer.MAX_VALUE));
			
			projectPartSubkey = getProjectClientPartSubKeyName("asyn.output_message_queue_size");
			item = new ConfigItem(projectPartSubkey,
					"출력 메시지 큐 크기",
					"10", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			// itemValidatorHash.put(getProjectClientPartSubKeyName("asyn.output_message_queue_size"), new ItemValidatorOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
					
			projectPartSubKeyOfDepenceItem = getProjectClientPartSubKeyName("asyn.output_message_reader.max_size");
			projectPartSubkey = getProjectClientPartSubKeyName("asyn.output_message_reader.size");
			item = new ConfigItem(projectPartSubKeyOfDepenceItem,
					"클라이언트 비동기 입출력 지원용 입력 메시지 소켓 읽기 담당 쓰레드 최대 갯수",
					"4", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			item = new ConfigItem(projectPartSubkey,
					"클라이언트 비동기 입출력 지원용 입력 메시지 소켓 읽기 담당 쓰레드 갯수",
					"4", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			afterDependenceProjectPartItemHash.put(projectPartSubkey, 
					new  AfterDependenceItemOfIntegerTypeMaxValue(projectPartSubKeyOfDepenceItem, 
							projectPartItemHash.get(projectPartSubKeyOfDepenceItem).getItemValidator()));	
			/*itemValidatorHash.put(getProjectClientPartSubKeyName(projectPartSubKeyOfDepenceItem), new ItemValidatorOfMinMaxInteger("2", 1, Integer.MAX_VALUE));		
			afterDependenceProjectPartItemHash.put(projectPartSubkey, new  AfterDependenceItemOfIntegerTypeMaxValue(projectPartSubKeyOfDepenceItem, itemValidatorHash.get(projectPartSubKeyOfDepenceItem)));	
			itemValidatorHash.put(getProjectClientPartSubKeyName(projectPartSubkey), new ItemValidatorOfMinMaxInteger("2", 1, Integer.MAX_VALUE));*/
			
			projectPartSubkey = getProjectClientPartSubKeyName("asyn.read_selector_wakeup_interval");
			item = new ConfigItem(projectPartSubkey,
					"클라이언트 비동기 입출력 지원용 출력 메시지 소켓 읽기 담당 쓰레드에서 블락된 읽기 이벤트 전용 selector 를 깨우는 주기. 단위 ms",
					"10", true, 
					new ItemValidatorOfMinMaxLong(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			// itemValidatorHash.put(getProjectClientPartSubKeyName("asyn.read_selector_wakeup_interval"), new ItemValidatorOfMinMaxInteger("10", 1, Integer.MAX_VALUE));
			
			projectPartSubkey = getProjectClientPartSubKeyName("monitor.time_interval");
			item = new ConfigItem(projectPartSubkey,
					"모니터링 주기, 단위 ms",
					"5000", true, 
					new ItemValidatorOfMinMaxLong(1000, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			/** 변경전:sample_test.client.monitor.request_timeout.value, 변경후:sample_test.client.monitor.reception_timeout.value*/
			projectPartSubkey = getProjectClientPartSubKeyName("connection.socket_timeout");
			projectPartSubKeyOfDepenceItem = getProjectClientPartSubKeyName("monitor.reception_timeout");
			item = new ConfigItem(projectPartSubKeyOfDepenceItem,
					"데이터를 수신하지 않고 기다려주는 최대 시간, 권장 값은 소켓 타임 아웃 시간*2, 단위 ms",
					"20000", true, 
					new ItemValidatorOfMinMaxLong(1000, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			afterDependenceProjectPartItemHash.put(projectPartSubkey, 
					new  AfterDependenceItemOfIntegerTypeMaxValue(projectPartSubKeyOfDepenceItem, 
							projectPartItemHash.get(projectPartSubKeyOfDepenceItem).getItemValidator()));	
			// itemValidatorHash.put(getClientKeyName("monitor.time_interval"), new ItemValidatorOfMinMaxLong("5000", 1000, Integer.MAX_VALUE));			
			// itemValidatorHash.put(getProjectClientPartSubKeyName("monitor.request_timeout"), new ItemValidatorOfMinMaxLong("20000", 1000, Integer.MAX_VALUE));
			
			/** 프로젝트 서버 설정 부분 */
			projectPartSubkey = getProjectServerPartSubKeyName("monitor.time_interval");
			item = new ConfigItem(projectPartSubkey,
					"모니터링 주기, 단위 ms",
					"5000", true, 
					new ItemValidatorOfMinMaxLong(1000, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectServerPartSubKeyName("monitor.reception_timeout");
			item = new ConfigItem(projectPartSubkey,
					"데이터를 수신하지 않고 기다려주는 최대 시간, 권장 값은 소켓 타임 아웃 시간*2, 단위 ms",
					"20000", true, 
					new ItemValidatorOfMinMaxLong(1000, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectServerPartSubKeyName("max_clients");
			item = new ConfigItem(projectPartSubkey,
					"서버로 접속할 수 있는 최대 클라이언트 수",
					"5", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectServerPartSubKeyName("data_packet_buffer_cnt");
			item = new ConfigItem(projectPartSubkey,
					"서버 프로젝트가 가지는 데이터 패킷 버퍼 수",
					"1000", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectServerPartSubKeyName("pool.accept_queue_size");
			item = new ConfigItem(projectPartSubkey,
					"접속 승인 큐 크기",
					"10", true, 
					new ItemValidatorOfMinMaxInteger(10, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectServerPartSubKeyName("pool.input_message_queue_size");
			item = new ConfigItem(projectPartSubkey,
					"입력 메시지 큐 크기",
					"10", true, 
					new ItemValidatorOfMinMaxInteger(10, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectServerPartSubKeyName("pool.output_message_queue_size");
			item = new ConfigItem(projectPartSubkey,
					"출력 메시지 큐 크기",
					"10", true, 
					new ItemValidatorOfMinMaxInteger(10, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectServerPartSubKeyName("accept_selector_timeout");
			item = new ConfigItem(projectPartSubkey,
					"접속 이벤트 전용 selector 에서 접속 이벤트 최대 대기 시간, 단위 ms",
					"10", true, 
					new ItemValidatorOfMinMaxLong(10, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			projectPartSubkey = getProjectServerPartSubKeyName("pool.read_selector_wakeup_interval");
			item = new ConfigItem(projectPartSubkey,
					"입력 메시지 소켓 읽기 담당 쓰레드에서 블락된 읽기 이벤트 전용 selector 를 깨우는 주기. 단위 ms",
					"10", true, 
					new ItemValidatorOfMinMaxLong(10, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);	
			
			
			// itemValidatorHash.put(getProjectServerPartSubKeyName("max_clients"), new ItemValidatorOfMinMaxInteger("5", 1, Integer.MAX_VALUE));
			// itemValidatorHash.put(getProjectServerPartSubKeyName("data_packet_buffer_cnt"), new ItemValidatorOfMinMaxInteger("1000", 1, Integer.MAX_VALUE));
			// itemValidatorHash.put(getProjectServerPartSubKeyName("accept_selector_timeout"), new ItemValidatorOfMinMaxLong("10", 10, Integer.MAX_VALUE));
			
			projectPartSubKeyOfDepenceItem = getProjectServerPartSubKeyName("pool.accept_processor.max_size");
			projectPartSubkey = getProjectServerPartSubKeyName("pool.accept_processor.size");
			item = new ConfigItem(projectPartSubKeyOfDepenceItem,
					"접속 요청이 승락된 클라이언트의 등록을 담당하는 쓰레드 최대 갯수",
					"1", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			item = new ConfigItem(projectPartSubkey,
					"접속 요청이 승락된 클라이언트의 등록을 담당하는 쓰레드 갯수",
					"1", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			afterDependenceProjectPartItemHash.put(projectPartSubkey, 
					new  AfterDependenceItemOfIntegerTypeMaxValue(projectPartSubKeyOfDepenceItem, 
							projectPartItemHash.get(projectPartSubKeyOfDepenceItem).getItemValidator()));	
			
			/*itemValidatorHash.put(getProjectServerPartSubKeyName(projectPartSubKeyOfDepenceItem), new ItemValidatorOfMinMaxInteger("1", 1, Integer.MAX_VALUE));		
			afterDependenceProjectPartItemHash.put(projectPartSubkey, new  AfterDependenceItemOfIntegerTypeMaxValue(projectPartSubKeyOfDepenceItem, itemValidatorHash.get(projectPartSubKeyOfDepenceItem)));	
			itemValidatorHash.put(getProjectClientPartSubKeyName(projectPartSubkey), new ItemValidatorOfMinMaxInteger("1", 1, Integer.MAX_VALUE));*/			
			
			// itemValidatorHash.put(getProjectServerPartSubKeyName("pool.accept_queue_size"), new ItemValidatorOfMinMaxLong("10", 1, Integer.MAX_VALUE));
			
			/*projectPartSubKeyOfDepenceItem = "pool.executor_processor.max_size";
			projectPartSubkey = "pool.executor_processor.size";
			itemValidatorHash.put(getProjectServerPartSubKeyName(projectPartSubKeyOfDepenceItem), new ItemValidatorOfMinMaxInteger("3", 1, Integer.MAX_VALUE));		
			afterDependenceProjectPartItemHash.put(projectPartSubkey, new  AfterDependenceItemOfIntegerTypeMaxValue(projectPartSubKeyOfDepenceItem, itemValidatorHash.get(projectPartSubKeyOfDepenceItem)));	
			itemValidatorHash.put(getProjectClientPartSubKeyName(projectPartSubkey), new ItemValidatorOfMinMaxInteger("3", 1, Integer.MAX_VALUE));*/
			
			projectPartSubKeyOfDepenceItem = getProjectServerPartSubKeyName("pool.executor_processor.max_size");
			projectPartSubkey = getProjectServerPartSubKeyName("pool.executor_processor.size");
			item = new ConfigItem(projectPartSubKeyOfDepenceItem,
					"서버 비지니스 로직 수행 담당 쓰레드 최대 갯수",
					"1", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			item = new ConfigItem(projectPartSubkey,
					"서버 비지니스 로직 수행 담당 쓰레드 갯수",
					"1", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			afterDependenceProjectPartItemHash.put(projectPartSubkey, 
					new  AfterDependenceItemOfIntegerTypeMaxValue(projectPartSubKeyOfDepenceItem, 
							projectPartItemHash.get(projectPartSubKeyOfDepenceItem).getItemValidator()));
			
			
			
			projectPartSubKeyOfDepenceItem = getProjectServerPartSubKeyName("pool.input_message_reader.max_size");
			projectPartSubkey = getProjectServerPartSubKeyName("pool.input_message_reader.size");
			item = new ConfigItem(projectPartSubKeyOfDepenceItem,
					"입력 메시지 소켓 읽기 담당 쓰레드 최대 갯수",
					"1", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			item = new ConfigItem(projectPartSubkey,
					"입력 메시지 소켓 읽기 담당 쓰레드 갯수",
					"1", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			afterDependenceProjectPartItemHash.put(projectPartSubkey, 
					new  AfterDependenceItemOfIntegerTypeMaxValue(projectPartSubKeyOfDepenceItem, 
							projectPartItemHash.get(projectPartSubKeyOfDepenceItem).getItemValidator()));
			
			
			projectPartSubKeyOfDepenceItem = getProjectServerPartSubKeyName("pool.output_message_writer.max_size");
			projectPartSubkey = getProjectServerPartSubKeyName("pool.output_message_writer.size");
			item = new ConfigItem(projectPartSubKeyOfDepenceItem,
					"출력 메시지 소켓 쓰기 담당 쓰레드 최대 갯수",
					"1", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			item = new ConfigItem(projectPartSubkey,
					"출력 메시지 소켓 쓰기 담당 쓰레드 갯수",
					"1", true, 
					new ItemValidatorOfMinMaxInteger(1, Integer.MAX_VALUE));
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);
			
			afterDependenceProjectPartItemHash.put(projectPartSubkey, 
					new  AfterDependenceItemOfIntegerTypeMaxValue(projectPartSubKeyOfDepenceItem, 
							projectPartItemHash.get(projectPartSubKeyOfDepenceItem).getItemValidator()));
			
			
			/*projectPartSubkey = getProjectServerPartSubKeyName("classloader.appinf.path");			
			item = new ConfigItem(projectPartSubkey,
					"서버 동적 클래스 APP-INF 경로",
					getAPPINFPathString(), false, 
					new ItemValidatorOfPath());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);	
			
			projectPartSubkey = getProjectServerPartSubKeyName("mybatis.config_file");			
			item = new ConfigItem(projectPartSubkey,
					"mybatis 설정 파일 이름. SinnoriClassLoader.getResourceAsStream 통해서 접근된다.",
					"kr/pe/sinnori/impl/mybatis/mybatisConfig.xml", false, 
					new ItemValidatorOfNoCheck());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);	*/
			
			
			projectPartSubKeyOfDepenceItem = getProjectServerPartSubKeyName("classloader.appinf.path");
			projectPartSubkey = getProjectServerPartSubKeyName("mybatis.config_file");
			item = new ConfigItem(projectPartSubKeyOfDepenceItem,
					"서버 동적 클래스 APP-INF 경로",
					getAPPINFPathString(), false, 
					new ItemValidatorOfPath());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);	
			
			item = new ConfigItem(projectPartSubkey,
					"mybatis 설정 파일 이름. SinnoriClassLoader.getResourceAsStream 통해서 접근된다.",
					"kr/pe/sinnori/impl/mybatis/mybatisConfig.xml", false, 
					new ItemValidatorOfNoCheck());
			projectPartItemList.add(item);
			projectPartItemHash.put(item.getKey(), item);	
			
			afterDependenceProjectPartItemHash.put(projectPartSubkey, 
					new  AfterDependenceItemOfClassLoaderResourceFile(projectPartSubKeyOfDepenceItem, 
							projectPartItemHash.get(projectPartSubKeyOfDepenceItem).getItemValidator()));
			
		} catch(ConfigValueInvalidException | IllegalArgumentException e) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)
			.append("]::project part sub key[")
			.append(projectPartSubkey)
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
	
	public String getSessionKeyRSAKeypairPathString() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);
		strBuilder.append(File.separator);
		strBuilder.append("rsa_keypair");		
		return strBuilder.toString();
	}
	
	public String getAPPINFPathString() {
		StringBuilder strBuilder = new StringBuilder(projectPathString);
		strBuilder.append(File.separator);
		strBuilder.append("server_build");
		strBuilder.append(File.separator);
		strBuilder.append("APP-INF");
		return strBuilder.toString();
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
	
	private String getProjectCommonPartSubKeyName(String subkey) {
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
	
	public void combind(Properties sourceProperties) throws ConfigErrorException {
		makeDBCPCOnnectionPoolNameSetFromSourceProperties(sourceProperties);
		makeProjectNameSetFromSourceProperties(sourceProperties);
		
		try {
			checkOnlyValidationForAllKeys(sourceProperties);
		} catch (ConfigKeyNotFoundException e) {
			String errorMessage = e.getMessage();
			log.warn(errorMessage);
			throw new ConfigErrorException(errorMessage);
		}
	}
	
	private void makeDBCPCOnnectionPoolNameSetFromSourceProperties(Properties sourceProperties) throws ConfigErrorException {
		String dbcpConnectionPoolNameListValue = sourceProperties.getProperty("dbcp.connection_pool_name_list.value");
		
		if (null == dbcpConnectionPoolNameListValue) {
			/** DBCP 연결 폴 이름 목록을 지정하는 키가 없을 경우 */
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("] has no a dbcp connection pool name list").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		String[] dbcpConnectionPoolNameArrray = dbcpConnectionPoolNameListValue.split(",");
		if (0 == dbcpConnectionPoolNameArrray.length) {
			dbcpConnectionPoolNameList.clear();
			return;
		}
		
		Set<String> tempNameSet = new HashSet<String>();
		
		for (String dbcpConnectionPoolNameOfList : dbcpConnectionPoolNameArrray) {
			dbcpConnectionPoolNameOfList = dbcpConnectionPoolNameOfList.trim();
			
			tempNameSet.add(dbcpConnectionPoolNameOfList);
		}		
		
		if (tempNameSet.size() != dbcpConnectionPoolNameArrray.length) {
			/** DBCP 연결 폴 이름 목록의 이름들중 중복된 것이 있는 경우 */
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)					
			.append("]:: the project list has no main project").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		dbcpConnectionPoolNameList.clear();
		dbcpConnectionPoolNameList.addAll(tempNameSet);
	}
		
	private void makeProjectNameSetFromSourceProperties(Properties sourceProperties) throws ConfigErrorException {
		String projectNameListValue = sourceProperties.getProperty("common.projectlist.value");
		
		
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
			throw new ConfigErrorException(errorMessage);
		}

		Set<String> tempNameSet = new HashSet<String>();
		
		boolean isMainProject = false;
		for (String projectNameOfList : projectNameArrray) {
			projectNameOfList = projectNameOfList.trim();
			
			if (projectNameOfList.equals(projectName)) {
				isMainProject = true;
			}
			tempNameSet.add(projectNameOfList);
		}
		
		if (!isMainProject) {
			/** 프로젝트 목록에 지정된 메인 프로젝트에 대한 정보가 없는 경우 */
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("]:: the project list has no main project").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		if (tempNameSet.size() != projectNameArrray.length) {
			/** 프로젝트 목록의 이름들중 중복된 것이 있는 경우 */
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)					
			.append("]:: the project list has no main project").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		projectNameList.clear();
		projectNameList.addAll(tempNameSet);
	}
	
	public void addSubProjectName(String subProjectName) throws ConfigErrorException {
		if (null == subProjectName) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("]::parameter subProjectName is null").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		if (subProjectName.equals(projectName)) {
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
		
		if (subProjectName.equals(projectName)) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)						
			.append("]::the main project cannot be deleted").toString();
			throw new ConfigErrorException(errorMessage);
		}
		
		projectNameList.remove(subProjectName);
	}
	
	public List<String> getProjectNameIterator() {
		return projectNameList;
	}	
	
	public SequencedProperties getSequencedProperties() {
		SequencedProperties sequencedProperties = new SequencedProperties();
		for (ConfigItem configItem : itemList) {
			String key = configItem.getKey();
			int len = key.length();
			String descKey = new StringBuilder(key.subSequence(0, len - ".value".length())).append(".desc").toString();
			sequencedProperties.put(descKey, configItem.toDescription());
			sequencedProperties.put(key, configItem.getDefaultValue());
		}
		
		for (String projectName : projectNameList) {
			for (ConfigItem configItem : projectPartItemList) {
				String key = new StringBuilder(projectName)
				.append(".")
				.append(configItem.getKey()).toString();
				int len = key.length();
				String descKey = new StringBuilder(key.subSequence(0, len - ".value".length())).append(".desc").toString();
				sequencedProperties.put(descKey, configItem.toDescription());
				sequencedProperties.put(key, configItem.getDefaultValue());
			}
		}
		return sequencedProperties;
	}
	
	// FIXME!
	public boolean isValidate(String key, Properties sourceProperties) throws ConfigValueInvalidException {
		boolean isValidation= true;
		AbstractBeforeDependenceItem beforeDependenceItem = null;
		
		StringTokenizer tokens = new StringTokenizer(key, "."); 
		if (tokens.hasMoreTokens()) {
			String firstToken = tokens.nextToken();
			if (firstToken.equals("dbcp")) {
				if (tokens.hasMoreTokens()) {
					String secondToken = tokens.nextToken();
					
					StringBuilder subKeyBuilder = new StringBuilder();
					
					if (dbcpConnectionPoolNameList.contains(secondToken)) {
						if (tokens.hasMoreTokens()) {
							subKeyBuilder.append(tokens.nextToken());
						}
						while (tokens.hasMoreTokens()) {
							subKeyBuilder.append(".");
							subKeyBuilder.append(tokens.nextToken());
						}
					} else {								
						subKeyBuilder.append(secondToken);
						
						while (tokens.hasMoreTokens()) {
							subKeyBuilder.append(".");
							subKeyBuilder.append(tokens.nextToken());
						}
					}
					
					// FIXME!
					log.info("dbcp subkey={}", subKeyBuilder.toString());
					beforeDependenceItem = beforeDependenceDBCPPartItemHash.get(subKeyBuilder.toString());
				}
			} else if (projectNameList.contains(firstToken)) {
				StringBuilder subKeyBuilder = new StringBuilder();
				if (tokens.hasMoreTokens()) {
					subKeyBuilder.append(tokens.nextToken());
				}
				
				while (tokens.hasMoreTokens()) {
					subKeyBuilder.append(".");
					subKeyBuilder.append(tokens.nextToken());
				}
				
				String subKey = subKeyBuilder.toString();					
				beforeDependenceItem = beforeDependenceProjectPartItemHash.get(subKey);			
			} else {
				beforeDependenceItem = beforeDependenceItemHash.get(key);
			}
		}
		
		
		if (null != beforeDependenceItem) {
			isValidation = beforeDependenceItem.isValidation(sourceProperties.getProperty(beforeDependenceItem.getKeyOfDependenceItem()));
		}
		return isValidation;
	}
	
	
	/*public Object getNativeValue(String key, Properties sourceProperties) throws ConfigValueInvalidException, ConfigKeyNotFoundException {
		ConfigItem itemConfig = null;
		Object nativeValue = null;
		int inx = key.indexOf(".");
		if (inx > 0) {
			int len = key.length();
			if (inx+1 < len) {
				String firstToken = key.substring(0, inx);
				
				if (projectNameSet.contains(firstToken)) {
					// String projectNameOfConfig = firstToken;				
					String subKey = key.substring(inx+1);
					
					itemConfig = projectPartItemHash.get(subKey);
					
				} else {
					itemConfig = itemHash.get(key);
				}
			} else {
				itemConfig = itemHash.get(key);
			}
		} else {
			itemConfig = itemHash.get(key);
		}
		
		if (null == itemConfig) {
			String errorMessage = new StringBuilder("project[")
			.append(projectName)			
			.append("]::parameter key[")
			.append(key)
			.append("] not exist").toString();
			
			log.warn(errorMessage);
			
			throw new ConfigKeyNotFoundException(errorMessage);
		}
		
		try {
			nativeValue = itemConfig.getItemValidator().validateItem(sourceProperties.getProperty(key));
		} catch(ConfigValueInvalidException e) {
			String errorMessage = new StringBuilder("project[")
			.append(projectName)			
			.append("]::key[")
			.append(key)
			.append("] errrorMessage=")
			.append(e.getMessage()).toString();
			
			log.warn(errorMessage);
			
			throw new ConfigValueInvalidException(errorMessage);
		}		
		
		return nativeValue;
	}	*/
	
	public Object getNativeValueWithAfterValidate(String key, Properties sourceProperties) throws ConfigValueInvalidException, ConfigKeyNotFoundException {
		Object nativeValue = null;
		ConfigItem itemConfig = null;
		
		AbstractAfterDependenceItem afterDependenceItem = null;
		StringTokenizer tokens = new StringTokenizer(key, "."); 
		if (tokens.hasMoreTokens()) {
			String firstToken = tokens.nextToken();
			if (firstToken.equals("dbcp")) {
				if (tokens.hasMoreTokens()) {
					String secondToken = tokens.nextToken();
					
					StringBuilder subKeyBuilder = new StringBuilder();
					
					if (dbcpConnectionPoolNameList.contains(secondToken)) {
						if (tokens.hasMoreTokens()) {
							subKeyBuilder.append(tokens.nextToken());
						}
						while (tokens.hasMoreTokens()) {
							subKeyBuilder.append(".");
							subKeyBuilder.append(tokens.nextToken());
						}
					} else {								
						subKeyBuilder.append(secondToken);
						
						while (tokens.hasMoreTokens()) {
							subKeyBuilder.append(".");
							subKeyBuilder.append(tokens.nextToken());
						}
					}
					
					// FIXME!
					log.info("dbcp subkey={}", subKeyBuilder.toString());
					itemConfig = dbcpPartItemHash.get(subKeyBuilder.toString());
					afterDependenceItem = afterDependenceDBCPPartItemHash.get(subKeyBuilder.toString());
				}
			} else if (projectNameList.contains(firstToken)) {
				StringBuilder subKeyBuilder = new StringBuilder();
				if (tokens.hasMoreTokens()) {
					subKeyBuilder.append(tokens.nextToken());
				}
				
				while (tokens.hasMoreTokens()) {
					subKeyBuilder.append(".");
					subKeyBuilder.append(tokens.nextToken());
				}
				
				String subKey = subKeyBuilder.toString();					
				itemConfig = projectPartItemHash.get(subKey);
				afterDependenceItem = afterDependenceProjectPartItemHash.get(subKey);
			} else {
				itemConfig = itemHash.get(key);
				afterDependenceItem = afterDependenceItemHash.get(key);
			}
		}
		
		if (null == itemConfig) {
			String errorMessage = new StringBuilder("1.project config file[")
			.append(projectConfigFilePathString)			
			.append("]::parameter key[")
			.append(key)
			.append("] not exist at SinnoriProjectConfig").toString();
			
			log.warn(errorMessage);
			
			throw new ConfigKeyNotFoundException(errorMessage);
		}
		
		String value = sourceProperties.getProperty(key);
		try {
			nativeValue = itemConfig.getItemValidator().validateItem(value);
		} catch(ConfigValueInvalidException e) {
			String errorMessage = new StringBuilder("project config file[")
			.append(projectConfigFilePathString)			
			.append("]::key[")
			.append(key)
			.append("] errrorMessage=")
			.append(e.getMessage()).toString();
			
			log.warn(errorMessage);
			
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (null != afterDependenceItem) {
			String valueOfDependenceItem = sourceProperties.getProperty(afterDependenceItem.getKeyOfDependenceItem());
			try {
				afterDependenceItem.validate(valueOfDependenceItem, nativeValue);
			} catch(ConfigValueInvalidException e) {
				String errorMessage = new StringBuilder("project config file[")
				.append(projectConfigFilePathString)			
				.append("]::key[")
				.append(key)
				.append("] errrorMessage=")
				.append(e.getMessage()).toString();
				
				log.warn(errorMessage);
				
				throw new ConfigValueInvalidException(errorMessage);
			}
		}
		
		return nativeValue;
	}
	
	/*public void afterValidate(String key, Object nativeValue, Properties sourceProperties) throws ConfigValueInvalidException {
		AbstractAfterDependenceItem afterDependenceItem = null;
		int inx = key.indexOf(".");
		if (inx > 0) {
			int len = key.length();
			if (inx+1 < len) {
				String firstToken = key.substring(0, inx);				
				if (projectNameSet.contains(firstToken)) {
					// String projectNameOfConfig = firstToken;				
					String subKey = key.substring(inx+1);
					
					afterDependenceItem = afterDependenceProjectPartItemHash.get(subKey);
				} else {				
					afterDependenceItem = afterDependenceItemHash.get(key);
				}
			} else {
				afterDependenceItem = afterDependenceItemHash.get(key);
			}
		} else {
			afterDependenceItem = afterDependenceItemHash.get(key);
		}
		if (null != afterDependenceItem) {
			String valueOfDependenceItem = sourceProperties.getProperty(afterDependenceItem.getKeyOfDependenceItem());
			afterDependenceItem.validate(valueOfDependenceItem, nativeValue);
		}
	}*/
	
	/**
	 * <pre>
	 * 환경 변수 전체 키 값들이 유효한 키인지 검사한다.
	 * 유효한 키란 환경 변수 값을 검사하기 위한 정보에 등록된 키를 말한다.
	 * </pre>
	 * 
	 * @throws ConfigKeyNotFoundException 환경 변수 값을 검사하기 위한 정보에 등록된 키가 없을 경우 던지는 예외
	 */
	private void checkOnlyValidationForAllKeys(Properties sourceProperties) throws ConfigKeyNotFoundException {
		ConfigItem itemConfig = null;
		
		Enumeration<Object> enumKey = sourceProperties.keys();
		while (enumKey.hasMoreElements()) {
			String key = (String)enumKey.nextElement();
			if (key.endsWith(".value")) {
				StringTokenizer tokens = new StringTokenizer(key, "."); 
				if (tokens.hasMoreTokens()) {
					String firstToken = tokens.nextToken();
					if (firstToken.equals("dbcp")) {
						if (tokens.hasMoreTokens()) {
							String secondToken = tokens.nextToken();
							
							StringBuilder subKeyBuilder = new StringBuilder();
							
							if (dbcpConnectionPoolNameList.contains(secondToken)) {
								if (tokens.hasMoreTokens()) {
									subKeyBuilder.append(tokens.nextToken());
								}
								while (tokens.hasMoreTokens()) {
									subKeyBuilder.append(".");
									subKeyBuilder.append(tokens.nextToken());
								}
							} else {								
								subKeyBuilder.append(secondToken);
								
								while (tokens.hasMoreTokens()) {
									subKeyBuilder.append(".");
									subKeyBuilder.append(tokens.nextToken());
								}
							}
							
							// FIXME!
							log.info("dbcp subkey={}", subKeyBuilder.toString());
							itemConfig = dbcpPartItemHash.get(subKeyBuilder.toString());
						}
					} else if (projectNameList.contains(firstToken)) {
						StringBuilder subKeyBuilder = new StringBuilder();
						if (tokens.hasMoreTokens()) {
							subKeyBuilder.append(tokens.nextToken());
						}
						
						while (tokens.hasMoreTokens()) {
							subKeyBuilder.append(".");
							subKeyBuilder.append(tokens.nextToken());
						}
						
						String subKey = subKeyBuilder.toString();					
						itemConfig = projectPartItemHash.get(subKey);
					} else {
						itemConfig = itemHash.get(key);
					}
				}
				
				if (null == itemConfig) {
					String errorMessage = new StringBuilder("2.project config file[")
					.append(projectConfigFilePathString)
					.append("]::parameter key[")
					.append(key)
					.append("] not exist at SinnoriProjectConfig").toString();
					
					// log.warn(errorMessage);
					
					throw new ConfigKeyNotFoundException(errorMessage);
				}
			}
		}
	}
	
	/*public void validAllCheck() throws ConfigException {		
		@SuppressWarnings("unchecked")
				
		Enumeration<String> enumProject = sourceSequencedProperties.keys();
		while (enumProject.hasMoreElements()) {
			String key = enumProject.nextElement();
			String projectNameOfList = isProjectPart(key);
			if (null == projectNameOfList) {
				if (key.endsWith(".value")) {
					ItemOfConfig itemOfConfig = itemHash.get(key);
					if (null == itemOfConfig) {
						String errorMessage = new StringBuilder("project[")
						.append(projectName)
						.append("] Config[")
						.append(projectConfigFilePathString)
						.append("]::undefined key[")
						.append(key)
						.append("]  error").toString();
						
						throw new ConfigException(errorMessage);
					}
					
					ItemValidator itemValidator = itemOfConfig.getItemValidator();
					
					AbstractBeforeDependenceItem beforeDependenceItem = beforeDependenceItemHash.get(key);
					boolean isValidation= true;
					if (null != beforeDependenceItem) {
						try {
							isValidation = beforeDependenceItem.isValidation(sourceSequencedProperties.getProperty(beforeDependenceItem.getKeyOfDependenceItem()));
						} catch(ConfigException e) {
							String errorMessage = new StringBuilder("project[")
							.append(projectName)
							.append("] Config[")
							.append(projectConfigFilePathString)
							.append("]::key[")
							.append(key)
							.append("] before depencence errrorMessage=")
							.append(e.getMessage()).toString();
							
							log.warn(errorMessage);
							
							throw new ConfigException(errorMessage);
						}
					}

					if (isValidation) {
						Object nativeValue = null;
						try {
							nativeValue = itemValidator.validateItem(sourceSequencedProperties.getProperty(key));
						} catch(ConfigException e) {
							String errorMessage = new StringBuilder("project[")
							.append(projectName)
							.append("] Config[")
							.append(projectConfigFilePathString)
							.append("]::key[")
							.append(key)
							.append("] errrorMessage=")
							.append(e.getMessage()).toString();
							
							log.warn(errorMessage);
							
							throw new ConfigException(errorMessage);
						}
						
						AbstractAfterDependenceItem afterDependenceItem = afterDependenceItemHash.get(key);
						if (null != afterDependenceItem) {
							try {
								afterDependenceItem.validate(sourceSequencedProperties.getProperty(afterDependenceItem.getKeyOfDependenceItem()), nativeValue);
							} catch(ConfigException e) {
								String errorMessage = new StringBuilder("project[")
								.append(projectName)
								.append("] Config[")
								.append(projectConfigFilePathString)
								.append("]::key[")
								.append(key)
								.append("] after dependence errrormessage=")
								.append(e.getMessage()).toString();
								
								log.warn(errorMessage);
								
								throw new ConfigException(errorMessage);
							}
						}
					}
				}
			} else {
				ProjectPartConfig projectConfig = projectConfigHash.get(projectNameOfList);
				projectConfig.validAllCheck();
			}
		}
	}*/
}
