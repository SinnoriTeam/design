package kr.pe.sinnori.common.config.itemvalidator;

import java.util.HashSet;
import java.util.Set;

import kr.pe.sinnori.common.config.AbstractItemValueGetter;
import kr.pe.sinnori.common.config.SingleSetValueGetterIF;
import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.common.lib.CommonType;

/**
 * 소캣 랩퍼 클래스인 연결 종류 항목 값 유효성 검사기, NoShareAsyn:비공유+비동기, ShareAsyn:공유+비동기, NoShareSync:비공유+동기.
 * @author "Won Jonghoon"
 *
 */
public class ItemValueGetterOfConnectionType extends AbstractItemValueGetter 
implements SingleSetValueGetterIF {	
	private Set<String> stringValueSet = new HashSet<String>();
	
	/**
	 * 소캣 랩퍼 클래스인 연결 종류 항목 값 유효성 검사기 생성자, NoShareAsyn:비공유+비동기, ShareAsyn:공유+비동기, NoShareSync:비공유+동기
	 * @throws ConfigValueInvalidException
	 */
	public ItemValueGetterOfConnectionType() throws ConfigValueInvalidException {
		stringValueSet.add("NoShareAsyn");
		stringValueSet.add("ShareAsyn");
		stringValueSet.add("NoShareSync");
	}
	
	@Override
	public Object getItemValueWithValidation(String value) throws ConfigValueInvalidException {
		if (null == value) {
			String errorMessage = "parameter value is null";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (value.equals("")) {
			String errorMessage = "parameter value is empty";
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		// value = value.toLowerCase();
		
		if (! stringValueSet.contains(value)) {
			String errorMessage = new StringBuilder("parameter value[")
			.append(value)
			.append("] is not a element of set[")
			.append(stringValueSet.toString())
			.append("]").toString();
			throw new ConfigValueInvalidException(errorMessage);
		}
		
		if (value.equals("NoShareAsyn")) {
			return CommonType.CONNECTION_TYPE.NoShareAsyn;
		} else if (value.equals("ShareAsyn")) {
			return CommonType.CONNECTION_TYPE.ShareAsyn;
		} else {
			return CommonType.CONNECTION_TYPE.NoShareSync;
		}
	}

	@Override
	public String toDescription() {
		return "single set {NoShareAsyn, ShareAsyn, NoShareSync}";
	}

	@Override
	public Set<String> getStringTypeValueSet() {
		return stringValueSet;
	}
}
