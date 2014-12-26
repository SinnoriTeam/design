package kr.pe.sinnori.gui.table;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import kr.pe.sinnori.common.config.AbstractItemValueGetter;
import kr.pe.sinnori.common.config.ConfigItem;
import kr.pe.sinnori.common.config.ConfigItem.ConfigItemViewType;
import kr.pe.sinnori.common.config.SingleSetValueGetterIF;
import kr.pe.sinnori.common.config.SinnoriConfigInfo;
import kr.pe.sinnori.gui.util.PathSwingAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@SuppressWarnings("serial")
public class ConfigItemCellValue extends JPanel {
	private Logger log = LoggerFactory.getLogger(ConfigItemCellValue.class);
	
	private String targetKey;
	private ConfigItem.ConfigItemViewType configItemViewType=null;
	// private Properties sourceProperties = null;
	// private SinnoriConfigInfo sinnoriConfigInfo = null;
	private JFrame mainFrame = null;
	
	private JComboBox<String> valueComboBox = null;
	private JTextField valueTextField = null;
	private JButton pathButton = null;
	
	//private Object result=null;
	
	public ConfigItemCellValue() {
		super();
		// this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		//this.getParent().setLayout(arg0);
		LayoutManager parentLayoutManger = this.getParent().getLayout();
		log.info("parnet layout manger class name={}", parentLayoutManger.getClass().getName());
	}
		
	/*public ConfigItemCellValue(String targetKey, 
			Properties sourceProperties, 
			SinnoriConfigInfo sinnoriConfigInfo, JFrame mainFrame) {		
		this(targetKey, sourceProperties.getProperty(targetKey),
				sinnoriConfigInfo, mainFrame);
	}*/
	
	public ConfigItemCellValue(String targetKey, 
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
			if (targetItemValue.equals("")) {
				targetItemValue = defaultValue;
				log.info("targetKey[{}]'s value is a empty string so change deault value[{}]", targetKey, defaultValue);
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
