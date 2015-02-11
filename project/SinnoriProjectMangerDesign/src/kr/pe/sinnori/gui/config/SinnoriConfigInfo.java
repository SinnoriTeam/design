package kr.pe.sinnori.gui.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.common.exception.ConfigKeyNotFoundException;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.common.util.SequencedProperties;
import kr.pe.sinnori.gui.config.conditionchecker.ValueEqualConditionChecker;
import kr.pe.sinnori.gui.config.dependencebreaker.ClassLoaderResourceFileDependenceBreaker;
import kr.pe.sinnori.gui.config.dependencebreaker.MinMaxIntegerDependenceBreaker;
import kr.pe.sinnori.gui.config.dependencebreaker.MinMaxLongDependenceBreaker;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfBoolean;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfByteOrder;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfCharset;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfConnectionType;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfFile;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfMessageInfoXMLPath;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfMessageProtocol;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfMinMaxInteger;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfMinMaxLong;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfNoCheck;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfNoNullAndEmptyString;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfPath;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfSessionKeyRSAKeypairPath;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfSessionkeyPrivateKeyEncoding;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfSingleIntegerSet;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfSingleStringSet;
import kr.pe.sinnori.gui.config.itemvaluegetter.ItemValueGetterOfUpDownFileBlockMaxSize;
import kr.pe.sinnori.gui.lib.MainProject;

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
	private Hashtable<String, AbstractDependenceBreaker> breakCheckerHash = new Hashtable<String, AbstractDependenceBreaker>();
	
		
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
		
		projectConfigFilePathString = MainProject.getProjectConfigFilePathStringFromProjectPath(projectPathString);
		
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
					"세션키에 사용되는 공개키 키쌍 생성 방법, API:자체 암호 lib 이용하여 RSA 키쌍 생성, \nFile:외부 파일를 읽어와서 RSA  키쌍을 생성",
					"API", true, 
					new ItemValueGetterOfSingleStringSet("API", "File"));
			addConfigItem(item);			
			
			item = new ConfigItem(ConfigItem.ConfigPart.COMMON, 
					ConfigItem.ConfigItemViewType.PATH,	
					targetItemID,
					"세션키에 사용되는 공개키 키쌍 파일 경로, \n세션키에 사용되는 공개키 키쌍 생성 방법이 File인 경우에 유효하다.",
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
					"싱글턴 클래스 객체 캐쉬 관리자(LoaderAndName2ObjectManager) 에서 캐쉬로 관리할 객체의 최대 갯수.\n 주로 캐쉬되는 대상 객체는 xxxServerCodec, xxxClientCodec 이다.",
					"100", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(item);				
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
			targetItemID = "common.message_info.xmlpath.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.PATH,
					targetItemID,
					"메시지 정보 파일 경로",
					getDefaultValueOfMessageInfoXMLPath(), true, 
					new ItemValueGetterOfMessageInfoXMLPath());
			addConfigItem(configItem);
			
			targetItemID = "common.host.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트에서 접속할 서버 주소",
					"localhost", true, 
					new ItemValueGetterOfNoNullAndEmptyString());
			addConfigItem(configItem);
			
			targetItemID = "common.port.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"포트 번호",
					"9090", true, 
					new ItemValueGetterOfMinMaxInteger(1024, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "common.byteorder.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"바이트 오더",
					"LITTLE_ENDIAN", true, 
					new ItemValueGetterOfByteOrder());
			addConfigItem(configItem);
			
			targetItemID = "common.charset.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"문자셋",
					"UTF-8", true, 
					new ItemValueGetterOfCharset());
			addConfigItem(configItem);
			
			targetItemID = "common.data_packet_buffer_max_cnt_per_message.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"1개 메시지당 할당 받을 수 있는 데이터 패킷 버퍼 최대수",
					"1000", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "common.data_packet_buffer_size.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"데이터 패킷 버퍼 크기, 단위 byte",
					"4096", true, 
					new ItemValueGetterOfMinMaxInteger(1024, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			/** 메시지 식별자 크기의 최소 크기는 내부적으로 사용하는 SelfExn 메시지를 기준으로 정했음. */
			targetItemID = "common.message_id_fixed_size.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"메시지 식별자 크기의 최소 크기",
					"50", true, 
					new ItemValueGetterOfMinMaxInteger(7, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "common.message_protocol.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"메시지 프로토콜, DHB:교차 md5 헤더+바디, DJSON:길이+존슨문자열, THB:길이+바디",
					"DHB", true, 
					new ItemValueGetterOfMessageProtocol());
			addConfigItem(configItem);
			
			
			targetItemID = "common.classloader.class_package_prefix_name.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_COMMON, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"동적 클래스 패키지명 접두어, 동적 클래스 여부를 판단하는 기준",
					"kr.pe.sinnori.impl.", true, 
					new ItemValueGetterOfNoNullAndEmptyString());
			addConfigItem(configItem);
			
						
			/** 프로젝트 클라이언트 설정 부분 */
			targetItemID = "client.monitor.time_interval.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"모니터링 주기, 단위 ms",
					"5000", true, 
					new ItemValueGetterOfMinMaxLong(1000, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
					
			targetItemID = "client.monitor.reception_timeout.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"데이터를 수신하지 않고 기다려주는 최대 시간, 권장 값은 소켓 타임 아웃 시간*2, 단위 ms",
					"20000", true, 
					new ItemValueGetterOfMinMaxLong(1000, Integer.MAX_VALUE));
			addConfigItem(configItem);	
			
			targetItemID = "client.connection.type.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"소캣 랩퍼 클래스인 연결 종류, NoShareAsyn:비공유+비동기, \nShareAsyn:공유+비동기, NoShareSync:비공유+동기",
					"NoShareAsyn", true, 
					new ItemValueGetterOfConnectionType());
			addConfigItem(configItem);					
			
			depenceItemID = "client.monitor.reception_timeout.value";
			targetItemID = "client.connection.socket_timeout.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"소켓 타임아웃, 단위 ms",
					"5000", true, 
					new ItemValueGetterOfMinMaxLong(1000, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			breakCheckerHash.put(targetItemID, 
					new  MinMaxLongDependenceBreaker(targetItemID, depenceItemID, this));
			
			targetItemID = "client.connection.whether_to_auto_connect.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.SINGLE_SET,
					targetItemID,
					"연결 생성시 자동 접속 여부",
					"false", true, 
					new ItemValueGetterOfBoolean());
			addConfigItem(configItem);
			
			targetItemID = "client.connection.count.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"연결 갯수",
					"4", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "client.data_packet_buffer_cnt.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 프로젝트가 가지는 데이터 패킷 버퍼 갯수",
					"1000", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "client.asyn.finish_connect.max_call.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 비동기 소켓 채널의 연결 확립 최대 시도 횟수",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "client.asyn.finish_connect.waitting_time.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 비동기 소켓 채널의 연결 확립을 재 시도 간격",
					"10", true, 
					new ItemValueGetterOfMinMaxLong(0, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "client.asyn.output_message_executor_thread_cnt.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"비동기 출력 메시지 처리자 쓰레드 갯수",
					"1", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "client.asyn.share.mailbox_cnt.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"비동기+공유 연결 클래스(ShareAsynConnection)의 메일함 갯수",
					"2", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "client.asyn.input_message_queue_size.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 비동기 입출력 지원용 입력 메시지 큐 크기",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			depenceItemID = "client.asyn.input_message_writer.max_size.value";
			targetItemID = "client.asyn.input_message_writer.size.value";
			
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
					new  MinMaxIntegerDependenceBreaker(
							targetItemID,
							depenceItemID, 
							this));	
						
			targetItemID = "client.asyn.output_message_queue_size.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"출력 메시지 큐 크기",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);			
			
					
			depenceItemID = "client.asyn.output_message_reader.max_size.value";
			targetItemID = "client.asyn.output_message_reader.size.value";
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
					new  MinMaxIntegerDependenceBreaker(targetItemID, depenceItemID, this));	

			targetItemID = "client.asyn.read_selector_wakeup_interval.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_CLIENT,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"클라이언트 비동기 입출력 지원용 출력 메시지 소켓 읽기 담당 쓰레드에서 블락된 읽기 이벤트 전용 selector 를 깨우는 주기. 단위 ms",
					"10", true, 
					new ItemValueGetterOfMinMaxLong(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			/** 프로젝트 서버 설정 부분 */
			targetItemID = "server.monitor.time_interval.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"모니터링 주기, 단위 ms",
					"5000", true, 
					new ItemValueGetterOfMinMaxLong(1000, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "server.monitor.reception_timeout.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"데이터를 수신하지 않고 기다려주는 최대 시간, 권장 값은 소켓 타임 아웃 시간*2, 단위 ms",
					"20000", true, 
					new ItemValueGetterOfMinMaxLong(1000, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "server.max_clients.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"서버로 접속할 수 있는 최대 클라이언트 수",
					"5", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "server.data_packet_buffer_cnt.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"서버 프로젝트가 가지는 데이터 패킷 버퍼 수",
					"1000", true, 
					new ItemValueGetterOfMinMaxInteger(1, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "server.pool.accept_queue_size.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"접속 승인 큐 크기",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(10, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "server.pool.input_message_queue_size.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"입력 메시지 큐 크기",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(10, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "server.pool.output_message_queue_size.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"출력 메시지 큐 크기",
					"10", true, 
					new ItemValueGetterOfMinMaxInteger(10, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "server.accept_selector_timeout.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"접속 이벤트 전용 selector 에서 접속 이벤트 최대 대기 시간, 단위 ms",
					"10", true, 
					new ItemValueGetterOfMinMaxLong(10, Integer.MAX_VALUE));
			addConfigItem(configItem);
			
			targetItemID = "server.pool.read_selector_wakeup_interval.value";
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER, 
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"입력 메시지 소켓 읽기 담당 쓰레드에서 블락된 읽기 이벤트 전용 selector 를 깨우는 주기. 단위 ms",
					"10", true, 
					new ItemValueGetterOfMinMaxLong(10, Integer.MAX_VALUE));
			addConfigItem(configItem);	
			
			depenceItemID = "server.pool.accept_processor.max_size.value";
			targetItemID = "server.pool.accept_processor.size.value";
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
					new  MinMaxIntegerDependenceBreaker(targetItemID, depenceItemID, this));	
			
			depenceItemID = "server.pool.executor_processor.max_size.value";
			targetItemID = "server.pool.executor_processor.size.value";
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
					new  MinMaxIntegerDependenceBreaker(targetItemID, depenceItemID, this));
			
			
			
			depenceItemID = "server.pool.input_message_reader.max_size.value";
			targetItemID = "server.pool.input_message_reader.size.value";
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
					new  MinMaxIntegerDependenceBreaker(targetItemID, depenceItemID, this));
			
			
			depenceItemID = "server.pool.output_message_writer.max_size.value";
			targetItemID = "server.pool.output_message_writer.size.value";
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
					new  MinMaxIntegerDependenceBreaker(targetItemID, depenceItemID, this));
						
			
			depenceItemID = "server.classloader.appinf.path.value";
			targetItemID = "server.classloader.mybatis_config_file_relative_path.value";
			
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER,
					ConfigItem.ConfigItemViewType.PATH,
					depenceItemID,
					"서버 동적 클래스 APP-INF 경로",
					getDefaultValueOfAPPINFPath(), false, 
					new ItemValueGetterOfPath());
			addConfigItem(configItem);			
			
			configItem = new ConfigItem(ConfigItem.ConfigPart.PROJECT_SERVER,
					ConfigItem.ConfigItemViewType.TEXT,
					targetItemID,
					"ClassLoader#getResourceAsStream 의 구현 이며 \n<APP-INF>/resources 경로 기준으로 읽어오며 구별자가 '/' 문자로된 상대 경로로 기술되어야 한다.\nex) kr/pe/sinnori/impl/mybatis/mybatisConfig.xml",
					"", false, 
					new ItemValueGetterOfNoCheck());
			addConfigItem(configItem);
			
			breakCheckerHash.put(targetItemID, 
					new  ClassLoaderResourceFileDependenceBreaker(targetItemID, 
							depenceItemID, this));
			
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
				
		
		AbstractDependenceBreaker breakChecker = breakCheckerHash.get(itemID);		

		if (null != breakChecker) {
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
