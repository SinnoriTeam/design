package kr.pe.sinnori.common.config.itemvalidator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kr.pe.sinnori.common.config.AbstractItemValidator;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.common.lib.CommonType;

/**
 * 메시지 프로토콜 항목 값 유효성 검사기, DHB:교차 md5 헤더+바디, DJSON:길이+존슨문자열, THB:길이+바디
 * @author "Won Jonghoon"
 *
 */
public class ItemValidatorOfMessageProtocol extends AbstractItemValidator {	
	private Set<String> stringValueSet = new HashSet<String>();
	
	/**
	 * 메시지 프로토콜 항목 값 유효성 검사기 생성자, DHB:교차 md5 헤더+바디, DJSON:길이+존슨문자열, THB:길이+바디
	 * @throws ConfigValueInvalidException
	 */
	public ItemValidatorOfMessageProtocol() throws ConfigValueInvalidException {		
		stringValueSet.add("DHB");
		stringValueSet.add("DJSON");
		stringValueSet.add("THB");
	}
	
	@Override
	public Object validateItem(String value) throws ConfigValueInvalidException {
		if (null == value) {
			String errorMessage = "parameter value is null";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (value.equals("")) {
			String errorMessage = "parameter value is empty";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (! stringValueSet.contains(value)) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not a element of set[")
			.append(stringValueSet.toString())
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (value.equals("DHB")) {
			return CommonType.MESSAGE_PROTOCOL.DHB;
		} else if (value.equals("DJSON")) {
			return CommonType.MESSAGE_PROTOCOL.DJSON;
		} else {
			return CommonType.MESSAGE_PROTOCOL.THB;
		}
	}

	@Override
	public String toDescription() {
		StringBuilder descriptionBuilder = new StringBuilder("single set {");
		Iterator<String> iter =  stringValueSet.iterator();
		if (iter.hasNext()) descriptionBuilder.append(iter.next());
		while (iter.hasNext()) {
			descriptionBuilder.append(", ");
			descriptionBuilder.append(iter.next());
		}
		descriptionBuilder.append("}");
		return descriptionBuilder.toString();
	}

}
