package kr.pe.sinnori.gui.table;

import java.awt.LayoutManager;
import java.util.Properties;
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
		
	public ConfigItemCellValue(String targetKey, 
			Properties sourceProperties, 
			SinnoriConfigInfo sinnoriConfigInfo, JFrame mainFrame) {
		// this.setSize(200, 200);
		// this.setLayout(arg0);
		// this.getHeight();
		
		
		this.targetKey = targetKey;
		// this.sourceProperties = sourceProperties;
		// this.sinnoriConfigInfo = sinnoriConfigInfo;
		this.mainFrame = mainFrame;
		
		
		String itemID = sinnoriConfigInfo.getItemIDFromKey(targetKey);
		ConfigItem configItem = sinnoriConfigInfo.getConfigItem(itemID);
		ConfigItem.ConfigItemViewType configItemViewType = configItem.getConfigItemViewType();
		String targetItemValue = sourceProperties.getProperty(targetKey);
		
		if (configItemViewType == ConfigItem.ConfigItemViewType.FILE) {
			// Dimension textDimension = new Dimension(150, 30);
			valueTextField = new JTextField();
			valueTextField.setText(targetItemValue);
			valueTextField.setSize(150, 30);
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
			valueTextField.setSize(150, 30);
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
			String defaultValue = configItem.getDefaultValue();
			int selectedIndex = -1;			
			SingleSetValueGetterIF singleSetValueGetter = (SingleSetValueGetterIF)itemValueGetter;
			Set<String> singleSet = singleSetValueGetter.getStringTypeValueSet();
			int inx=0;
			valueComboBox = new JComboBox<String>();
			for (String value : singleSet) {
				valueComboBox.addItem(value);
				if (defaultValue.equals(value)) {
					selectedIndex=inx;
				}
				inx++;
			}
			if (-1 == selectedIndex) selectedIndex=0;
			valueComboBox.setSelectedIndex(selectedIndex);
			add(valueComboBox);
		} else {
			valueTextField = new JTextField();
			valueTextField.setSize(150, 30);
			valueTextField.setText(targetItemValue);
			add(valueTextField);
		}
	}

	public String getTargetKey() {
		return targetKey;
	}
	

	public String getValueOfComponent() {
		if (null == valueTextField) {
			return valueComboBox.getItemAt(valueComboBox.getSelectedIndex());
		} else {
			return valueTextField.getText();
		}
		
	}
}
