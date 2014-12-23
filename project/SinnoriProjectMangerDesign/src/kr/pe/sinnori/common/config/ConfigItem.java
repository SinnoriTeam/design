package kr.pe.sinnori.common.config;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

/**
 * 환경 변수 값을 검사하기 위한 정보 클래스
 * @author "Won Jonghoon"
 *
 */
public class ConfigItem {
	public enum ConfigPart {DBCP, COMMON, PROJECT_COMMON, PROJECT_SERVER, PROJECT_CLIENT};
	public enum ConfigItemViewType {TEXT, FILE, PATH, SINGLE_SET};
	
	private ConfigPart configPart;
	private ConfigItemViewType configItemViewType;
	private String itemID;
	private String description;
	

	private String defaultValue;
	private boolean isDefaultValidation;
	private AbstractItemValueGetter itemValueGetter;
	
	
	
	
	/**
	 * 환경 변수 값을 검사하기 위한 정보 클래스 생성자
	 * @param configPart
	 * @param configItemType
	 * @param itemID 항목 식별자
	 * @param description 환경 변수 설명
	 * @param defaultValue 디폴트 값
	 * @param isDefaultValidation 객체 생성시 디폴트 값 검사 수행 여부
	 * @param itemValidator 환경 변수 값 검사기
	 * @throws IllegalArgumentException 잘못된 파라미터 입력시 던지는 예외
	 * @throws ConfigValueInvalidException 디폴트 값 검사 수행시 디폴트 값이 잘못된 경우 던지는 예외
	 */
	public ConfigItem(ConfigPart configPart, 
			ConfigItemViewType configItemViewType,
			String itemID, 
			String description, 
			String defaultValue,
			boolean isDefaultValidation,
			AbstractItemValueGetter itemValidator) throws IllegalArgumentException, ConfigValueInvalidException {
		if (null == configPart) {
			String errorMessage = "parameter configPart is null";
			throw new IllegalArgumentException(errorMessage);
		}
		if (null == configItemViewType) {
			String errorMessage = "parameter configItemViewType is null";
			throw new IllegalArgumentException(errorMessage);
		}
		
		
		if (null == itemID) {
			String errorMessage = "parameter itemID is null";
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (itemID.equals("")) {
			String errorMessage = "parameter itemID is empty";
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (null == description) {
			String errorMessage = "parameter description is null";
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (description.equals("")) {
			String errorMessage = "parameter description is empty";
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (null == defaultValue) {
			String errorMessage = "parameter defaultValue is null";
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (configItemViewType == ConfigItemViewType.SINGLE_SET) {
			if (!(itemValidator instanceof SingleSetValueGetterIF)) {
				String errorMessage = "parameter configItemViewType is a signle view type  " +
						"but parameter itemValidator object is not a instance of  a SingleSetValueGetterIF type";
				throw new IllegalArgumentException(errorMessage);
			}
		}
		
		/*if (defaultValue.equals("")) {
			String errorMessage = "parameter defaultValue is empty";
			throw new IllegalArgumentException(errorMessage);
		}*/
		
		this.configPart = configPart;
		this.configItemViewType = configItemViewType;
		this.itemID = itemID;		
		this.description = description;
		this.defaultValue = defaultValue;
		this.isDefaultValidation = isDefaultValidation;
		this.itemValueGetter = itemValidator;

		if (this.isDefaultValidation) {			
			itemValidator.getItemValueWithValidation(defaultValue);
		}
	}

	public ConfigItemViewType getConfigItemViewType() {
		return configItemViewType;
	}
	
	public ConfigPart getConfigPart() {
		return configPart;
	}

	public String getItemID() {
		return itemID;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public AbstractItemValueGetter getItemValueGetter() {
		return itemValueGetter;
	}
	
	
	public boolean isDefaultValidation() {
		return isDefaultValidation;
	}

	public String toDescription() {
		StringBuilder descriptBuilder = new StringBuilder(description);
				
		descriptBuilder.append(", defaultValue[");
		descriptBuilder.append(defaultValue);
		descriptBuilder.append("]");
		
		String descriptionOfItemValidator = itemValueGetter.toDescription();
		if (null != descriptionOfItemValidator && !descriptionOfItemValidator.equals("")) {
			descriptBuilder.append(", ");
			descriptBuilder.append(descriptionOfItemValidator);
		}
		
		return descriptBuilder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConfigItem [configPart=");
		builder.append(configPart);
		builder.append(", configItemType=");
		builder.append(configItemViewType);
		builder.append(", itemID=");
		builder.append(itemID);
		builder.append(", description=");
		builder.append(description);
		builder.append(", defaultValue=");
		builder.append(defaultValue);
		builder.append(", isDefaultValidation=");
		builder.append(isDefaultValidation);
		builder.append(", itemValueGetter=");
		builder.append(itemValueGetter);
		builder.append("]");
		return builder.toString();
	}	
}
