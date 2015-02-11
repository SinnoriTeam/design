package kr.pe.sinnori.gui.table;

import java.awt.Dimension;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import kr.pe.sinnori.gui.config.AbstractItemValueGetter;
import kr.pe.sinnori.gui.config.ConfigItem;
import kr.pe.sinnori.gui.config.SingleSetValueGetterIF;
import kr.pe.sinnori.gui.config.SinnoriConfigInfo;
import kr.pe.sinnori.gui.config.ConfigItem.ConfigItemViewType;
import kr.pe.sinnori.gui.util.PathSwingAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@SuppressWarnings("serial")
public class ConfigItemValue extends JPanel {
	private Logger log = LoggerFactory.getLogger(ConfigItemValue.class);
	
	private String targetKey;
	private ConfigItem.ConfigItemViewType configItemViewType=null;
	private JFrame mainFrame = null;
	
	private JComboBox<String> valueComboBox = null;
	private JTextField valueTextField = null;
	private JButton pathButton = null;
	
	public ConfigItemValue(String targetKey, 
			String targetItemValue, 
			SinnoriConfigInfo sinnoriConfigInfo, JFrame mainFrame) {		
		this.targetKey = targetKey;
		this.mainFrame = mainFrame;		
		
		String itemID = sinnoriConfigInfo.getItemIDFromKey(targetKey);
		ConfigItem configItem = sinnoriConfigInfo.getConfigItem(itemID);
		if (null == configItem) {
			log.error("configItem is null, targetKey={}, itemID={}", targetKey, itemID);
			System.exit(1);
		}
		configItemViewType = configItem.getConfigItemViewType();
		String defaultValue = configItem.getDefaultValue();
		
		if (null == targetItemValue) {
			targetItemValue = defaultValue;
			log.info("targetKey[{}] is not found so change deault value[{}]", targetKey, defaultValue);
		} else {
			/**
			 * 설정 파일의 항목 정보를 구성할때 디폴트 값 검사를 수행하는 항목만 값이 빈 문자열일 경우 디폴트 값으로 설정.
			 * 디폴트 값 검사를 수행한다는 의미는 2가지를 내포한다.
			 * 첫번째 항목 정보를 구성할때의 지정한 디폴트 값이 올바른지 검사를 수행할 수 있다는것이며
			 * 마지막 두번째 의미 있는 값으로 지정하겠다는 의도를 갖는다.
			 * 
			 * 즉 디폴트 값 검사를 수행할 경우에는 의미있는 값 지정하기 바라기 때문에 빈문자열을 허용하지 않는다.
			 * 반면에 디폴트 값 검사를 수행하지 않는 경우 빈문자열은 의도한건지 의도하지 않는건지 불명확하다.
			 */
			if (targetItemValue.equals("") && configItem.isDefaultValidation()) {
				targetItemValue = defaultValue;
				log.info("targetKey[{}]'s value is a empty string and the default validation is true so change deault value[{}]", targetKey, defaultValue);
			}
		}
		
		/*if (targetKey.equals("dbcp.tw_sinnoridb.confige_file.value")) {
			log.info("targetKey={}, configItemViewType={}, targetItemValue={}", 
					targetKey, configItemViewType, targetItemValue);
		}*/
		
		if (configItemViewType == ConfigItem.ConfigItemViewType.FILE) {
			valueTextField = new JTextField();
			valueTextField.setText(targetItemValue);
			valueTextField.setPreferredSize(new Dimension(310,20));
			add(valueTextField);
			
			pathButton = new JButton("파일 선택");
			
			UIManager.put("FileChooser.readOnly", Boolean.TRUE);
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			PathSwingAction pathAction = new PathSwingAction(this.mainFrame, chooser, valueTextField);
			pathButton.setAction(pathAction);
			add(pathButton);
		} else if (configItemViewType == ConfigItem.ConfigItemViewType.PATH) {
			valueTextField = new JTextField();
			valueTextField.setText(targetItemValue);
			valueTextField.setPreferredSize(new Dimension(310,20));
			add(valueTextField);
			
			pathButton = new JButton("경로 선택");
			
			UIManager.put("FileChooser.readOnly", Boolean.TRUE); 
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);			
			
			PathSwingAction pathAction = new PathSwingAction(this.mainFrame, chooser, valueTextField);
			pathButton.setAction(pathAction);
			add(pathButton);
		} else if (configItemViewType == ConfigItemViewType.SINGLE_SET) {
			AbstractItemValueGetter itemValueGetter = configItem.getItemValueGetter();			
			int selectedIndex = -1;
			SingleSetValueGetterIF singleSetValueGetter = (SingleSetValueGetterIF)itemValueGetter;
			Set<String> singleSet = singleSetValueGetter.getStringTypeValueSet();
			int inx=0;
			valueComboBox = new JComboBox<String>();
			for (String value : singleSet) {
				valueComboBox.addItem(value);
				if (targetItemValue.equals(value)) {
					selectedIndex=inx;
				}
				inx++;
			}
			if (-1 == selectedIndex) {
				selectedIndex=0;
			}
			valueComboBox.setSelectedIndex(selectedIndex);
			add(valueComboBox);
		} else {
			valueTextField = new JTextField();
			valueTextField.setPreferredSize(new Dimension(400,20));
			valueTextField.setText(targetItemValue);
			add(valueTextField);
		}
	}

	public String getTargetKey() {
		return targetKey;
	}
	

	public String getValueOfComponent() {
		if (configItemViewType == ConfigItemViewType.SINGLE_SET) {
			return valueComboBox.getItemAt(valueComboBox.getSelectedIndex());
		} else {
			return valueTextField.getText();
		}		
	}
}
