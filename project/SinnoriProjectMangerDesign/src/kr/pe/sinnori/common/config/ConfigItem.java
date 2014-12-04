package kr.pe.sinnori.common.config;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;

/**
 * 환경 변수 값을 검사하기 위한 정보 클래스
 * @author "Won Jonghoon"
 *
 */
public class ConfigItem {
	private String key;	
	private String description;
	private String defaultValue;
	private boolean isDefaultValidation;
	private AbstractItemValidator itemValidator;
	
	/**
	 * 환경 변수 값을 검사하기 위한 정보 클래스 생성자
	 * @param key 환경 변수 키, 단 프로젝트 파트의 경우 프로젝트명이 생략된 형태의 서브 키이다.
	 * @param description 환경 변수 설명
	 * @param defaultValue 디폴트 값
	 * @param isDefaultValidation 객체 생성시 디폴트 값 검사 수행 여부, 
	 * @param itemValidator 환경 변수 값 검사기
	 * @throws IllegalArgumentException 잘못된 파라미터 입력시 던지는 예외
	 * @throws ConfigValueInvalidException 디폴트 값 검사 수행시 디폴트 값이 잘못된 경우 던지는 예외
	 */
	public ConfigItem(String key, 
			String description, 
			String defaultValue,
			boolean isDefaultValidation,
			AbstractItemValidator itemValidator) throws IllegalArgumentException, ConfigValueInvalidException {
		if (null == key) {
			String errorMessage = "parameter key is null";
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (key.equals("")) {
			String errorMessage = "parameter key is empty";
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
		
		if (defaultValue.equals("")) {
			String errorMessage = "parameter defaultValue is empty";
			throw new IllegalArgumentException(errorMessage);
		}
		
		
		this.key = key;		
		this.description = description;
		this.defaultValue = defaultValue;
		this.isDefaultValidation = isDefaultValidation;
		this.itemValidator = itemValidator;

		if (isDefaultValidation) {			
			itemValidator.validateItem(defaultValue);
		}
	}

	public String getKey() {
		return key;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public AbstractItemValidator getItemValidator() {
		return itemValidator;
	}

	public String toDescription() {
		StringBuilder descriptBuilder = new StringBuilder(description);
				
		descriptBuilder.append(", defaultValue[");
		descriptBuilder.append(defaultValue);
		descriptBuilder.append("]");
		
		String descriptionOfItemValidator = itemValidator.toDescription();
		if (null != descriptionOfItemValidator && !descriptionOfItemValidator.equals("")) {
			descriptBuilder.append(", ");
			descriptBuilder.append(descriptionOfItemValidator);
		}
		
		return descriptBuilder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ItemOfConfig [key=");
		builder.append(key);
		builder.append(", description=");
		builder.append(description);
		builder.append(", defaultValue=");
		builder.append(defaultValue);
		builder.append(", isDefaultValidation=");
		builder.append(isDefaultValidation);
		builder.append(", itemValidator=");
		builder.append(itemValidator);
		builder.append("]");
		return builder.toString();
	}
}
