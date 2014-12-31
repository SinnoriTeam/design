/*
 * Created by JFormDesigner on Sat Nov 29 13:48:34 KST 2014
 */

package kr.pe.sinnori.gui.screen;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import kr.pe.sinnori.common.config.ConfigItem;
import kr.pe.sinnori.common.config.SinnoriConfigInfo;
import kr.pe.sinnori.common.util.SequencedProperties;
import kr.pe.sinnori.gui.lib.MainProject;
import kr.pe.sinnori.gui.lib.WindowManger;
import kr.pe.sinnori.gui.table.ConfigItemCellEditor;
import kr.pe.sinnori.gui.table.ConfigItemCellRenderer;
import kr.pe.sinnori.gui.table.ConfigItemCellValue;
import kr.pe.sinnori.gui.table.ConfigItemTableModel;
import kr.pe.sinnori.gui.util.PathSwingAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Jonghoon Won
 */
@SuppressWarnings("serial")
public class ProjectEditScreen extends JPanel {
	private Logger log = LoggerFactory.getLogger(ProjectEditScreen.class);
	
	private JFrame mainFrame = null;
	private JFileChooser chooser = null;
	private MainProject mainProject = null;
	
	private String titlesOfConfigItemTable[] = {
			"key", "value"
		};
	
	private Class<?>[] columnTypesOfConfigItemTable = new Class[] {
		String.class, ConfigItemCellValue.class
	};
	
	private ConfigItemTableModel commonConfigItemTableModel = null;
	private HashMap<String, ConfigItemTableModel> dbcpConnPoolName2ConfigItemTableModelHash = null;
	private HashMap<String, ConfigItemTableModel> projectName2ConfigItemTableModelHash = null;
	
	//private Object valuesOfCommonConfigItemTable[][] = null;
	// private SequencedProperties saveSequencedProperties = new SequencedProperties();
	
	public ProjectEditScreen(JFrame mainFrame) {
		this.mainFrame = mainFrame;
		initComponents();
	}
	
	public void setProject(MainProject mainProject) {
		this.mainProject = mainProject;
		
		sinnoriInstalledPathValueLabel.setText(mainProject.getProjectPathString());
		mainProjectNameValueLabel.setText(mainProject.getMainProjectName());
		// serverCheckBox.setSelected(selectedProject.get);
		appClientCheckBox.setSelected(mainProject.isAppClient());
		webClientCheckBox.setSelected(mainProject.isWebClient());
		servletEnginLibinaryPathTextField.setEditable(mainProject.isWebClient());
		servletEnginLibinaryPathButton.setEnabled(mainProject.isWebClient());
		servletEnginLibinaryPathTextField.setText(mainProject.getServletEnginLibPathString());
				
		List<String> subProjectList = mainProject.getSubProjectNameList();
		int subProjectListSize = subProjectList.size();
		
		String[] subProjectArray = new String[subProjectListSize+1];
		subProjectArray[0] = "- Sub Project Name -";
		for (int i=0; i < subProjectListSize; i++) {
			subProjectArray[i+1] = subProjectList.get(i);
		}
		
		ComboBoxModel<String> subProjectNameComboBoxModel = new DefaultComboBoxModel<String>(subProjectArray);
		
		subProjectNameListComboBox.setModel(subProjectNameComboBoxModel);
		
		final List<String> dbcpConnPoolNameList = this.mainProject.getDBCPConnPoolNameList();
		int dbcpConnPoolNameListSize = dbcpConnPoolNameList.size();
		String[] dbcpConnPoolNameArray = new String[dbcpConnPoolNameListSize+1];
		dbcpConnPoolNameArray[0] = "- DBCP Conn Pool Name -";
		for (int i=0; i < dbcpConnPoolNameListSize; i++) {
			dbcpConnPoolNameArray[i+1] = dbcpConnPoolNameList.get(i);
		}
		
		ComboBoxModel<String> dbcpConnPoolNameComboBoxModel = new DefaultComboBoxModel<String>(dbcpConnPoolNameArray);
		
		dbcpConnPoolNameListComboBox.setModel(dbcpConnPoolNameComboBoxModel);
				
		SequencedProperties sourceSequencedProperties = 
				mainProject.getSourceSequencedProperties();
		
		SinnoriConfigInfo sinnoriConfigInfo = mainProject.getSinnoriConfigInfo();
		
		{
			List<ConfigItem> commonPartConfigItemList = 
					sinnoriConfigInfo.getCommonPartConfigItemList();		
			
			
			// commonConfigTable
			Object[][] valuesOfCommonConfigItemTable = new Object[commonPartConfigItemList.size()][titlesOfConfigItemTable.length];
			
			for (int i=0; i < valuesOfCommonConfigItemTable.length; i++) {
				ConfigItem configItem = commonPartConfigItemList.get(i);
				String itemID = configItem.getItemID();
				String targetKey = itemID;
				ConfigItemCellValue configItemCellValue = new ConfigItemCellValue(
						targetKey, 
						sourceSequencedProperties.getProperty(targetKey),
						sinnoriConfigInfo, mainFrame);
						
				valuesOfCommonConfigItemTable[i][0] = targetKey;
				valuesOfCommonConfigItemTable[i][1] = configItemCellValue;
				
				//saveSequencedProperties.put(targetKey, configItemCellValue.getValueOfComponent());
			}
			
			commonConfigItemTableModel = new ConfigItemTableModel(valuesOfCommonConfigItemTable, titlesOfConfigItemTable, columnTypesOfConfigItemTable);
			commonConfigTable.setModel(commonConfigItemTableModel);
			
			// commonConfigTable.getColumnModel().getColumn(0).setPreferredWidth(150);
					
			commonConfigTable.getColumnModel().getColumn(1).setResizable(false);
			commonConfigTable.getColumnModel().getColumn(1).setPreferredWidth(180);
					
			commonConfigTable.getColumnModel().getColumn(1).setCellRenderer(new ConfigItemCellRenderer());
			commonConfigTable.getColumnModel().getColumn(1).setCellEditor(new ConfigItemCellEditor(new JCheckBox()));
			commonConfigTable.setRowHeight(38);
			commonConfigScrollPane.repaint();
		}
		
		{
			List<ConfigItem> dbcpPartConfigItemList = sinnoriConfigInfo.getDBCPPartConfigItemList();
			int dbcpPartConfigItemListSize = dbcpPartConfigItemList.size();
			
			dbcpConnPoolName2ConfigItemTableModelHash = new HashMap<String, ConfigItemTableModel>();
			for (String dbcpConnPoolName : dbcpConnPoolNameList) {				
				Object[][] values = new Object[dbcpPartConfigItemListSize][titlesOfConfigItemTable.length];
				for (int i=0; i < values.length; i++) {
					ConfigItem configItem = dbcpPartConfigItemList.get(i);
					String itemID = configItem.getItemID();
					
					String targetKey = new StringBuilder("dbcp.")
					.append(dbcpConnPoolName)
					.append(".")
					.append(itemID).toString();
					
					log.info("dbcpConnPoolName={}, targetKey={}", dbcpConnPoolName, targetKey);
					
					ConfigItemCellValue configItemCellValue = new ConfigItemCellValue(
							targetKey, 
							sourceSequencedProperties.getProperty(targetKey),
							sinnoriConfigInfo, mainFrame);
							
					values[i][0] = targetKey;
					values[i][1] = configItemCellValue;
				}
				
				ConfigItemTableModel dbcpConfigItemTableModel = new ConfigItemTableModel(values, titlesOfConfigItemTable, columnTypesOfConfigItemTable);
				
				dbcpConnPoolName2ConfigItemTableModelHash.put(dbcpConnPoolName, dbcpConfigItemTableModel);				
			}
		}
		
		{
			// projectName2ConfigItemTableModelHash
			List<String> subProjectNameList = mainProject.getSubProjectNameList();
			
			List<ConfigItem> projectPartConfigItemList = 
					sinnoriConfigInfo.getProjectPartConfigItemList();
			int projectPartConfigItemListSize = projectPartConfigItemList.size();
				
			
			projectName2ConfigItemTableModelHash = new HashMap<String, ConfigItemTableModel>();
			
			{
				String mainProjectName = mainProject.getMainProjectName();
				Object[][] values = new Object[projectPartConfigItemListSize][titlesOfConfigItemTable.length];
				for (int i=0; i < values.length; i++) {
					ConfigItem configItem = projectPartConfigItemList.get(i);
					String itemID = configItem.getItemID();
					
					String targetKey = new StringBuilder("project.")
					.append(mainProjectName)
					.append(".")
					.append(itemID).toString();
					
					ConfigItemCellValue configItemCellValue = new ConfigItemCellValue(
							targetKey, 
							sourceSequencedProperties.getProperty(targetKey),
							sinnoriConfigInfo, mainFrame);
							
					values[i][0] = targetKey;
					values[i][1] = configItemCellValue;
				}
				
				ConfigItemTableModel mainProjectPartConfigItemTableModel = new ConfigItemTableModel(values, titlesOfConfigItemTable, columnTypesOfConfigItemTable);
				
				projectName2ConfigItemTableModelHash.put(mainProjectName, mainProjectPartConfigItemTableModel);	
			}
			
			for (String subProjectName : subProjectNameList) {				
				Object[][] values = new Object[projectPartConfigItemListSize][titlesOfConfigItemTable.length];
				for (int i=0; i < values.length; i++) {
					ConfigItem configItem = projectPartConfigItemList.get(i);
					String itemID = configItem.getItemID();
					
					String targetKey = new StringBuilder("project.")
					.append(subProjectName)
					.append(".")
					.append(itemID).toString();
					
					ConfigItemCellValue configItemCellValue = new ConfigItemCellValue(
							targetKey, 
							sourceSequencedProperties.getProperty(targetKey),
							sinnoriConfigInfo, mainFrame);
							
					values[i][0] = targetKey;
					values[i][1] = configItemCellValue;
				}
				
				ConfigItemTableModel subProjectPartConfigItemTableModel = new ConfigItemTableModel(values, titlesOfConfigItemTable, columnTypesOfConfigItemTable);
				
				projectName2ConfigItemTableModelHash.put(subProjectName, subProjectPartConfigItemTableModel);				
			}
		}
		
		{
			String mainProjectName = mainProject.getMainProjectName();
			mainProjectConfigTable.setModel(projectName2ConfigItemTableModelHash.get(mainProjectName));
			
			// commonConfigTable.getColumnModel().getColumn(0).setPreferredWidth(150);
					
			mainProjectConfigTable.getColumnModel().getColumn(1).setResizable(false);
			mainProjectConfigTable.getColumnModel().getColumn(1).setPreferredWidth(180);
					
			mainProjectConfigTable.getColumnModel().getColumn(1).setCellRenderer(new ConfigItemCellRenderer());
			mainProjectConfigTable.getColumnModel().getColumn(1).setCellEditor(new ConfigItemCellEditor(new JCheckBox()));
			mainProjectConfigTable.setRowHeight(38);
			mainProjectConfigScrollPane.repaint();
		}
	}
	
	private void projectWorkSaveButtonActionPerformed(ActionEvent e) {
		// TODO add your code here
	}

	private void prevButtonActionPerformed(ActionEvent e) {
		WindowManger.getInstance().changeProjectEditScreenToFirstScreen();
	}

	private void subProjectNameAddButtonActionPerformed(ActionEvent e) {
		String mainProjectName = mainProject.getMainProjectName();
		
		String newSubProjectName = subProjectNameTextField.getText();
		if (newSubProjectName.equals(mainProjectName)) {
			String errorMessage = 
					String.format("입력한 서브 프포르젝트 이름[%s]은 메인 프로젝트 이름과 동일합니다. 다른 이름을 넣어주세요.", newSubProjectName);
			subProjectNameTextField.requestFocusInWindow();
			JOptionPane.showMessageDialog(mainFrame, errorMessage);			
			return;
		}
		List<String> subProjectNameList = mainProject.getSubProjectNameList();
		if (subProjectNameList.contains(newSubProjectName)) {
			String errorMessage = 
					String.format("입력한 서브 프포르젝트 이름[%s]은 기존에 입력한 서브 프로젝트 이름입니다. 다른 이름을 넣어주세요.", newSubProjectName);
			subProjectNameTextField.requestFocusInWindow();
			JOptionPane.showMessageDialog(mainFrame, errorMessage);			
			return;
		}

		SinnoriConfigInfo sinnoriConfigInfo = mainProject.getSinnoriConfigInfo();
		List<ConfigItem> projectPartConfigItemList = sinnoriConfigInfo.getProjectPartConfigItemList();
		int projectPartConfigItemListSize = projectPartConfigItemList.size();
		Object[][] values = new Object[projectPartConfigItemListSize][titlesOfConfigItemTable.length];
		for (int i=0; i < values.length; i++) {
			ConfigItem configItem = projectPartConfigItemList.get(i);
			String itemID = configItem.getItemID();
			String defaultValue = configItem.getDefaultValue();
			
			String targetKey = new StringBuilder("project.")
			.append(newSubProjectName)
			.append(".")
			.append(itemID).toString();
			
			ConfigItemCellValue configItemCellValue = new ConfigItemCellValue(
					targetKey, 
					defaultValue,
					sinnoriConfigInfo, mainFrame);
					
			values[i][0] = targetKey;
			values[i][1] = configItemCellValue;
		}
		
		ConfigItemTableModel subProjectPartConfigItemTableModel = new ConfigItemTableModel(values, titlesOfConfigItemTable, columnTypesOfConfigItemTable);
		
		mainProject.addSubProjectName(newSubProjectName);
		projectName2ConfigItemTableModelHash.put(newSubProjectName, subProjectPartConfigItemTableModel);
		subProjectNameListComboBox.addItem(newSubProjectName);
		
		JOptionPane.showMessageDialog(mainFrame, String.format("Adding a new sub project name[%s] is success", newSubProjectName));	
	}
	
	private void subProjectEditButtonActionPerformed(ActionEvent e) {
		int selectedInx = subProjectNameListComboBox.getSelectedIndex();
		if (selectedInx <= 0) {
			String errorMessage = "Please, choose Sub Project Name";
			subProjectNameListComboBox.requestFocusInWindow();
			JOptionPane.showMessageDialog(mainFrame, errorMessage);
		} else {
			String mainProjectName = mainProject.getMainProjectName();
			String selectedSubProjectName = subProjectNameListComboBox.getItemAt(selectedInx);
			ConfigItemTableModel subProjectPartConfigTableModel = 
					projectName2ConfigItemTableModelHash.get(selectedSubProjectName);
			
			SubProjectPartConfigPopup popup = new SubProjectPartConfigPopup(mainFrame, 
					mainProjectName, selectedSubProjectName, subProjectPartConfigTableModel);
			popup.setTitle("Sub Project Part Conifg");
			popup.setSize(740, 380);
			popup.setVisible(true);
		}
	}
	
	private void subProjectNameDeleteButtonActionPerformed(ActionEvent e) {
		int selectedInx = subProjectNameListComboBox.getSelectedIndex();
		if (selectedInx <= 0) {
			String errorMessage = "Please, choose Sub Project Name";
			subProjectNameListComboBox.requestFocusInWindow();
			JOptionPane.showMessageDialog(mainFrame, errorMessage);
		} else {
			String selectedSubProjectName = subProjectNameListComboBox.getItemAt(selectedInx);			
			mainProject.removeSubProjectName(selectedSubProjectName);
			projectName2ConfigItemTableModelHash.remove(selectedSubProjectName);
			subProjectNameListComboBox.removeItemAt(selectedInx);
		}
	}		
	
	private void dbcpConnPoolNameEditButtonActionPerformed(ActionEvent e) {
		int selectedInx = dbcpConnPoolNameListComboBox.getSelectedIndex();
		if (selectedInx <= 0) {
			String errorMessage = "Please, choose DBCP Connection Pool Name";
			dbcpConnPoolNameListComboBox.requestFocusInWindow();
			JOptionPane.showMessageDialog(mainFrame, errorMessage);
		} else {
			String mainProjectName = mainProject.getMainProjectName();
			String selectedDBCPConnPoolName = dbcpConnPoolNameListComboBox.getItemAt(selectedInx);
			ConfigItemTableModel dbcpConfigItemTableModel = 
					dbcpConnPoolName2ConfigItemTableModelHash.get(selectedDBCPConnPoolName);
			
			DBCPConnPoolNamePopup popup = new DBCPConnPoolNamePopup(mainFrame, 
					mainProjectName, selectedDBCPConnPoolName, dbcpConfigItemTableModel);
			popup.setTitle("DBCP Connection Pool Name Conifg");
			popup.setSize(740, 220);
			popup.setVisible(true);
		}		
	}

	private void dbcpConnPoolNameAddButtonActionPerformed(ActionEvent e) {
		String newDBCPConnPoolName = dbcpConnPoolNameTextField.getText();
		
		List<String> dbcpConnPoolNameList = this.mainProject.getDBCPConnPoolNameList();
		
		if (dbcpConnPoolNameList.contains(newDBCPConnPoolName)) {
			String errorMessage = String.format("중복된 이름[%s]을 가진 DBCP 연결 폴 이름이 있습니다.", newDBCPConnPoolName);
			dbcpConnPoolNameTextField.requestFocusInWindow();
			JOptionPane.showMessageDialog(mainFrame, errorMessage);			
			return;
		}
		
		SinnoriConfigInfo sinnoriConfigInfo = mainProject.getSinnoriConfigInfo();
		
				
		
		List<ConfigItem> dbcpPartConfigItemList = sinnoriConfigInfo.getDBCPPartConfigItemList();
		int dbcpPartConfigItemListSize = dbcpPartConfigItemList.size();
		
		Object[][] values = new Object[dbcpPartConfigItemListSize][titlesOfConfigItemTable.length];
		for (int i=0; i < values.length; i++) {
			ConfigItem configItem = dbcpPartConfigItemList.get(i);
			String itemID = configItem.getItemID();
			String defaultValue = configItem.getDefaultValue();
			
			String targetKey = new StringBuilder("dbcp.")
			.append(newDBCPConnPoolName)
			.append(".")
			.append(itemID).toString();
			
			if (itemID.equals("confige_file.value")) {
				defaultValue = sinnoriConfigInfo.getDefaultValueOfDBCPConnPoolConfigFile(newDBCPConnPoolName);
			}
			
			log.info("dbcpConnPoolName={}, targetKey={}", newDBCPConnPoolName, targetKey);
						
			ConfigItemCellValue configItemCellValue = new ConfigItemCellValue(
					targetKey, 
					defaultValue,
					sinnoriConfigInfo, mainFrame);
					
			values[i][0] = targetKey;
			values[i][1] = configItemCellValue;
		}
		
		ConfigItemTableModel dbcpConfigItemTableModel = new ConfigItemTableModel(values, titlesOfConfigItemTable, columnTypesOfConfigItemTable);
		
		mainProject.addDBCPConnectionPoolName(newDBCPConnPoolName);
		dbcpConnPoolName2ConfigItemTableModelHash.put(newDBCPConnPoolName, dbcpConfigItemTableModel);		
		dbcpConnPoolNameListComboBox.addItem(newDBCPConnPoolName);		
		
		JOptionPane.showMessageDialog(mainFrame, String.format("Adding a new DBCP Connection Pool Name[%s] is success", newDBCPConnPoolName));	
	}

	private void dbcpConnPoolNameDeleteButtonActionPerformed(ActionEvent e) {
		int selectedInx = dbcpConnPoolNameListComboBox.getSelectedIndex();
		if (selectedInx <= 0) {
			String errorMessage = "Please, choose DBCP Connection Pool Name";
			dbcpConnPoolNameListComboBox.requestFocusInWindow();
			JOptionPane.showMessageDialog(mainFrame, errorMessage);
		} else {
			String selectedDBCPConnPoolName = dbcpConnPoolNameListComboBox.getItemAt(selectedInx);
			
			mainProject.removeDBCPConnectionPoolName(selectedDBCPConnPoolName);			
			dbcpConnPoolName2ConfigItemTableModelHash.remove(selectedDBCPConnPoolName);
			dbcpConnPoolNameListComboBox.removeItemAt(selectedInx);
		}
	}

		
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		sinnoriInstalledPathLinePanel = new JPanel();
		sinnoriInstalledPathTitleLabel = new JLabel();
		sinnoriInstalledPathValueLabel = new JLabel();
		mainProjectNameLinePanel = new JPanel();
		mainProjectNameTitleLabel = new JLabel();
		mainProjectNameValueLabel = new JLabel();
		projectStructLinePanel = new JPanel();
		projectStructLabel = new JLabel();
		projectStructFuncPanel = new JPanel();
		serverCheckBox = new JCheckBox();
		appClientCheckBox = new JCheckBox();
		webClientCheckBox = new JCheckBox();
		servletEnginLibinaryPathLinePanel = new JPanel();
		servletEnginLibinaryPathLabel = new JLabel();
		servletEnginLibinaryPathTextField = new JTextField();
		servletEnginLibinaryPathButton = new JButton();
		hSpacer1 = new JPanel(null);
		projectWorkSaveLinePanel = new JPanel();
		projectWorkSaveButton = new JButton();
		prevButton = new JButton();
		subProjectNameInputLinePanel = new JPanel();
		subProjectNameInputLabel = new JLabel();
		subProjectNameTextField = new JTextField();
		subProjectNameAddButton = new JButton();
		subProjectListLinePanel = new JPanel();
		subProjectNameListLabel = new JLabel();
		subProjectNameListComboBox = new JComboBox<>();
		subProjectNameListFuncPanel = new JPanel();
		subProjectNameEditButton = new JButton();
		subProjectNameDeleteButton = new JButton();
		dbcpConnPoolNameInputLinePanel = new JPanel();
		dbcpConnPoolNameInputLabel = new JLabel();
		dbcpConnPoolNameTextField = new JTextField();
		dbcpConnPoolNameAddButton = new JButton();
		dbcpConnPoolNameListLinePanel = new JPanel();
		dbcpConnPoolNameListLabel = new JLabel();
		dbcpConnPoolNameListComboBox = new JComboBox<>();
		dbcpConnNameListFuncPanel = new JPanel();
		dbcpConnPoolNameEditButton = new JButton();
		dbcpConnPoolNameDeleteButton = new JButton();
		commonConfigLabel = new JLabel();
		commonConfigScrollPane = new JScrollPane();
		commonConfigTable = new JTable();
		mainProjectConfigLabel = new JLabel();
		mainProjectConfigScrollPane = new JScrollPane();
		mainProjectConfigTable = new JTable();

		//======== this ========
		setLayout(new FormLayout(
			"[443dlu,pref]:grow",
			"11*(default, $lgap), 104dlu, $lgap, default, $lgap, 104dlu, $lgap, default"));
		/** Post-initialization Code start */
		UIManager.put("FileChooser.readOnly", Boolean.TRUE); 
		chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		PathSwingAction pathAction = new PathSwingAction(mainFrame, chooser, servletEnginLibinaryPathTextField);
		servletEnginLibinaryPathButton.setAction(pathAction);
		/** Post-initialization Code end */

		//======== sinnoriInstalledPathLinePanel ========
		{
			sinnoriInstalledPathLinePanel.setLayout(new FormLayout(
				"default, $lcgap, default:grow",
				"default"));

			//---- sinnoriInstalledPathTitleLabel ----
			sinnoriInstalledPathTitleLabel.setText("\uc2e0\ub180\uc774 \uc124\uce58 \uacbd\ub85c :");
			sinnoriInstalledPathLinePanel.add(sinnoriInstalledPathTitleLabel, CC.xy(1, 1));

			//---- sinnoriInstalledPathValueLabel ----
			sinnoriInstalledPathValueLabel.setText("d:\\gitsinnori\\sinnori");
			sinnoriInstalledPathLinePanel.add(sinnoriInstalledPathValueLabel, CC.xy(3, 1));
		}
		add(sinnoriInstalledPathLinePanel, CC.xy(1, 1));

		//======== mainProjectNameLinePanel ========
		{
			mainProjectNameLinePanel.setLayout(new FormLayout(
				"default, $lcgap, default:grow",
				"default"));

			//---- mainProjectNameTitleLabel ----
			mainProjectNameTitleLabel.setText("\uc8fc \ud504\ub85c\uc81d\ud2b8 \uc774\ub984 :");
			mainProjectNameLinePanel.add(mainProjectNameTitleLabel, CC.xy(1, 1));

			//---- mainProjectNameValueLabel ----
			mainProjectNameValueLabel.setText("sample_test");
			mainProjectNameLinePanel.add(mainProjectNameValueLabel, CC.xy(3, 1));
		}
		add(mainProjectNameLinePanel, CC.xy(1, 3));

		//======== projectStructLinePanel ========
		{
			projectStructLinePanel.setLayout(new FormLayout(
				"default, $lcgap, [364dlu,pref]:grow",
				"default"));

			//---- projectStructLabel ----
			projectStructLabel.setText("\ud504\ub85c\uc81d\ud2b8 \uad6c\uc131 :");
			projectStructLinePanel.add(projectStructLabel, CC.xy(1, 1));

			//======== projectStructFuncPanel ========
			{
				projectStructFuncPanel.setLayout(new BoxLayout(projectStructFuncPanel, BoxLayout.X_AXIS));

				//---- serverCheckBox ----
				serverCheckBox.setText("\uc11c\ubc84");
				serverCheckBox.setSelected(true);
				serverCheckBox.setEnabled(false);
				projectStructFuncPanel.add(serverCheckBox);

				//---- appClientCheckBox ----
				appClientCheckBox.setText("\uc751\uc6a9 \ud074\ub77c\uc774\uc5b8\ud2b8");
				appClientCheckBox.setSelected(true);
				projectStructFuncPanel.add(appClientCheckBox);

				//---- webClientCheckBox ----
				webClientCheckBox.setText("\uc6f9 \ud074\ub77c\uc774\uc5b8\ud2b8");
				webClientCheckBox.setSelected(true);
				projectStructFuncPanel.add(webClientCheckBox);
			}
			projectStructLinePanel.add(projectStructFuncPanel, CC.xy(3, 1));
		}
		add(projectStructLinePanel, CC.xy(1, 5));

		//======== servletEnginLibinaryPathLinePanel ========
		{
			servletEnginLibinaryPathLinePanel.setLayout(new FormLayout(
				"default, $lcgap, ${growing-button}, $lcgap, default",
				"default"));

			//---- servletEnginLibinaryPathLabel ----
			servletEnginLibinaryPathLabel.setText("\uc11c\ube14\ub9bf \uc5d4\uc9c4 \ub77c\uc774\ube0c\ub7ec\ub9ac \uacbd\ub85c :");
			servletEnginLibinaryPathLinePanel.add(servletEnginLibinaryPathLabel, CC.xy(1, 1));
			servletEnginLibinaryPathLinePanel.add(servletEnginLibinaryPathTextField, CC.xy(3, 1));

			//---- servletEnginLibinaryPathButton ----
			servletEnginLibinaryPathButton.setText("\uacbd\ub85c \uc120\ud0dd");
			servletEnginLibinaryPathLinePanel.add(servletEnginLibinaryPathButton, CC.xy(5, 1));
		}
		add(servletEnginLibinaryPathLinePanel, CC.xy(1, 7));

		//---- hSpacer1 ----
		hSpacer1.setBorder(LineBorder.createBlackLineBorder());
		add(hSpacer1, CC.xy(1, 9));

		//======== projectWorkSaveLinePanel ========
		{
			projectWorkSaveLinePanel.setAlignmentX(1.0F);
			projectWorkSaveLinePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));

			//---- projectWorkSaveButton ----
			projectWorkSaveButton.setText("\ubcc0\uacbd \ub0b4\uc5ed \uc800\uc7a5");
			projectWorkSaveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					projectWorkSaveButtonActionPerformed(e);
				}
			});
			projectWorkSaveLinePanel.add(projectWorkSaveButton);

			//---- prevButton ----
			prevButton.setText("\uba54\uc778 \ud654\uba74\uc73c\ub85c \ub3cc\uc544\uac00\uae30");
			prevButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					prevButtonActionPerformed(e);
				}
			});
			projectWorkSaveLinePanel.add(prevButton);
		}
		add(projectWorkSaveLinePanel, CC.xy(1, 11));

		//======== subProjectNameInputLinePanel ========
		{
			subProjectNameInputLinePanel.setLayout(new FormLayout(
				"default, $lcgap, ${growing-button}, $lcgap, 37dlu",
				"default"));

			//---- subProjectNameInputLabel ----
			subProjectNameInputLabel.setText("Sub Project Name :");
			subProjectNameInputLinePanel.add(subProjectNameInputLabel, CC.xy(1, 1));
			subProjectNameInputLinePanel.add(subProjectNameTextField, CC.xy(3, 1));

			//---- subProjectNameAddButton ----
			subProjectNameAddButton.setText("add");
			subProjectNameAddButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					subProjectNameAddButtonActionPerformed(e);
				}
			});
			subProjectNameInputLinePanel.add(subProjectNameAddButton, CC.xy(5, 1));
		}
		add(subProjectNameInputLinePanel, CC.xy(1, 13));

		//======== subProjectListLinePanel ========
		{
			subProjectListLinePanel.setLayout(new FormLayout(
				"2*(default, $lcgap), default",
				"default"));

			//---- subProjectNameListLabel ----
			subProjectNameListLabel.setText("Sub Project Name Choose");
			subProjectListLinePanel.add(subProjectNameListLabel, CC.xy(1, 1));

			//---- subProjectNameListComboBox ----
			subProjectNameListComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
				"- Sub Project Name -",
				"sample_test_sub1",
				"sample_test_sub2"
			}));
			subProjectListLinePanel.add(subProjectNameListComboBox, CC.xy(3, 1));

			//======== subProjectNameListFuncPanel ========
			{
				subProjectNameListFuncPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));

				//---- subProjectNameEditButton ----
				subProjectNameEditButton.setText("edit");
				subProjectNameEditButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						subProjectEditButtonActionPerformed(e);
					}
				});
				subProjectNameListFuncPanel.add(subProjectNameEditButton);

				//---- subProjectNameDeleteButton ----
				subProjectNameDeleteButton.setText("remove");
				subProjectNameDeleteButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						subProjectNameDeleteButtonActionPerformed(e);
					}
				});
				subProjectNameListFuncPanel.add(subProjectNameDeleteButton);
			}
			subProjectListLinePanel.add(subProjectNameListFuncPanel, CC.xy(5, 1));
		}
		add(subProjectListLinePanel, CC.xy(1, 15));

		//======== dbcpConnPoolNameInputLinePanel ========
		{
			dbcpConnPoolNameInputLinePanel.setLayout(new FormLayout(
				"default, $lcgap, ${growing-button}, $lcgap, 37dlu",
				"default"));

			//---- dbcpConnPoolNameInputLabel ----
			dbcpConnPoolNameInputLabel.setText("DBCP Connection Pool Name :");
			dbcpConnPoolNameInputLinePanel.add(dbcpConnPoolNameInputLabel, CC.xy(1, 1));
			dbcpConnPoolNameInputLinePanel.add(dbcpConnPoolNameTextField, CC.xy(3, 1));

			//---- dbcpConnPoolNameAddButton ----
			dbcpConnPoolNameAddButton.setText("add");
			dbcpConnPoolNameAddButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dbcpConnPoolNameAddButtonActionPerformed(e);
				}
			});
			dbcpConnPoolNameInputLinePanel.add(dbcpConnPoolNameAddButton, CC.xy(5, 1));
		}
		add(dbcpConnPoolNameInputLinePanel, CC.xy(1, 17));

		//======== dbcpConnPoolNameListLinePanel ========
		{
			dbcpConnPoolNameListLinePanel.setLayout(new FormLayout(
				"2*(default, $lcgap), default",
				"default"));

			//---- dbcpConnPoolNameListLabel ----
			dbcpConnPoolNameListLabel.setText("DBCP Conn Pool Name Choose");
			dbcpConnPoolNameListLinePanel.add(dbcpConnPoolNameListLabel, CC.xy(1, 1));

			//---- dbcpConnPoolNameListComboBox ----
			dbcpConnPoolNameListComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
				"- DBCP Conn Pool Name -",
				"tw_sinnoridb"
			}));
			dbcpConnPoolNameListLinePanel.add(dbcpConnPoolNameListComboBox, CC.xy(3, 1));

			//======== dbcpConnNameListFuncPanel ========
			{
				dbcpConnNameListFuncPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));

				//---- dbcpConnPoolNameEditButton ----
				dbcpConnPoolNameEditButton.setText("edit");
				dbcpConnPoolNameEditButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dbcpConnPoolNameEditButtonActionPerformed(e);
					}
				});
				dbcpConnNameListFuncPanel.add(dbcpConnPoolNameEditButton);

				//---- dbcpConnPoolNameDeleteButton ----
				dbcpConnPoolNameDeleteButton.setText("remote");
				dbcpConnPoolNameDeleteButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dbcpConnPoolNameDeleteButtonActionPerformed(e);
					}
				});
				dbcpConnNameListFuncPanel.add(dbcpConnPoolNameDeleteButton);
			}
			dbcpConnPoolNameListLinePanel.add(dbcpConnNameListFuncPanel, CC.xy(5, 1));
		}
		add(dbcpConnPoolNameListLinePanel, CC.xy(1, 19));

		//---- commonConfigLabel ----
		commonConfigLabel.setText("Common Part Config");
		add(commonConfigLabel, CC.xy(1, 21));

		//======== commonConfigScrollPane ========
		{

			//---- commonConfigTable ----
			commonConfigTable.setModel(new DefaultTableModel(
				new Object[][] {
					{null, null},
					{null, null},
					{null, null},
				},
				new String[] {
					"key", "value"
				}
			) {
				Class<?>[] columnTypes = new Class<?>[] {
					String.class, Object.class
				};
				boolean[] columnEditable = new boolean[] {
					false, false
				};
				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return columnEditable[columnIndex];
				}
			});
			{
				TableColumnModel cm = commonConfigTable.getColumnModel();
				cm.getColumn(1).setMinWidth(150);
			}
			commonConfigTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			commonConfigTable.setAutoCreateColumnsFromModel(false);
			commonConfigScrollPane.setViewportView(commonConfigTable);
		}
		add(commonConfigScrollPane, CC.xy(1, 23));

		//---- mainProjectConfigLabel ----
		mainProjectConfigLabel.setText("Main Project Part Config");
		add(mainProjectConfigLabel, CC.xy(1, 25));

		//======== mainProjectConfigScrollPane ========
		{

			//---- mainProjectConfigTable ----
			mainProjectConfigTable.setModel(new DefaultTableModel(
				new Object[][] {
					{null, null},
					{null, null},
				},
				new String[] {
					"key", "value"
				}
			) {
				Class<?>[] columnTypes = new Class<?>[] {
					String.class, Object.class
				};
				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
			});
			mainProjectConfigTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mainProjectConfigScrollPane.setViewportView(mainProjectConfigTable);
		}
		add(mainProjectConfigScrollPane, CC.xy(1, 27));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel sinnoriInstalledPathLinePanel;
	private JLabel sinnoriInstalledPathTitleLabel;
	private JLabel sinnoriInstalledPathValueLabel;
	private JPanel mainProjectNameLinePanel;
	private JLabel mainProjectNameTitleLabel;
	private JLabel mainProjectNameValueLabel;
	private JPanel projectStructLinePanel;
	private JLabel projectStructLabel;
	private JPanel projectStructFuncPanel;
	private JCheckBox serverCheckBox;
	private JCheckBox appClientCheckBox;
	private JCheckBox webClientCheckBox;
	private JPanel servletEnginLibinaryPathLinePanel;
	private JLabel servletEnginLibinaryPathLabel;
	private JTextField servletEnginLibinaryPathTextField;
	private JButton servletEnginLibinaryPathButton;
	private JPanel hSpacer1;
	private JPanel projectWorkSaveLinePanel;
	private JButton projectWorkSaveButton;
	private JButton prevButton;
	private JPanel subProjectNameInputLinePanel;
	private JLabel subProjectNameInputLabel;
	private JTextField subProjectNameTextField;
	private JButton subProjectNameAddButton;
	private JPanel subProjectListLinePanel;
	private JLabel subProjectNameListLabel;
	private JComboBox<String> subProjectNameListComboBox;
	private JPanel subProjectNameListFuncPanel;
	private JButton subProjectNameEditButton;
	private JButton subProjectNameDeleteButton;
	private JPanel dbcpConnPoolNameInputLinePanel;
	private JLabel dbcpConnPoolNameInputLabel;
	private JTextField dbcpConnPoolNameTextField;
	private JButton dbcpConnPoolNameAddButton;
	private JPanel dbcpConnPoolNameListLinePanel;
	private JLabel dbcpConnPoolNameListLabel;
	private JComboBox<String> dbcpConnPoolNameListComboBox;
	private JPanel dbcpConnNameListFuncPanel;
	private JButton dbcpConnPoolNameEditButton;
	private JButton dbcpConnPoolNameDeleteButton;
	private JLabel commonConfigLabel;
	private JScrollPane commonConfigScrollPane;
	private JTable commonConfigTable;
	private JLabel mainProjectConfigLabel;
	private JScrollPane mainProjectConfigScrollPane;
	private JTable mainProjectConfigTable;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
