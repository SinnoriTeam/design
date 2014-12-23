/*
 * Created by JFormDesigner on Sat Nov 29 13:48:34 KST 2014
 */

package kr.pe.sinnori.gui.screen;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import javax.swing.table.DefaultTableModel;

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

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Jonghoon Won
 */
@SuppressWarnings("serial")
public class ProjectEditScreen extends JPanel {
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
	private ConfigItemTableModel mainProjectConfigItemTableModel = null;
	
	private Object valuesOfCommonConfigItemTable[][] = null;
	private SequencedProperties saveSequencedProperties = new SequencedProperties();
	
	public ProjectEditScreen(JFrame mainFrame) {
		this.mainFrame = mainFrame;
		initComponents();
	}
	
	public void setProject(MainProject selectedProject) {
		this.mainProject = selectedProject;
		
		sinnoriInstalledPathValueLabel.setText(selectedProject.getProjectPathString());
		mainProjectNameValueLabel.setText(selectedProject.getMainProjectName());
		// serverCheckBox.setSelected(selectedProject.get);
		appClientCheckBox.setSelected(selectedProject.isAppClient());
		webClientCheckBox.setSelected(selectedProject.isWebClient());
		servletEnginLibinaryPathTextField.setEditable(selectedProject.isWebClient());
		servletEnginLibinaryPathButton.setEnabled(selectedProject.isWebClient());
		servletEnginLibinaryPathTextField.setText(selectedProject.getServletEnginLibPathString());
				
		List<String> subProjectList = mainProject.getSubProjectNameList();
		String[] subProjectArray = new String[subProjectList.size()];
		for (int i=0; i < subProjectArray.length; i++) {
			subProjectArray[i] = subProjectList.get(i);
		}
		
		ComboBoxModel<String> subProjectNameComboBoxModel = new DefaultComboBoxModel<String>(subProjectArray);
		
		subProjectNameListComboBox.setModel(subProjectNameComboBoxModel);
		
		List<String> dbcpConnPoolNameList = mainProject.getDBCPConnPoolNameList();
		String[] dbcpConnPoolNameArray = new String[dbcpConnPoolNameList.size()];
		for (int i=0; i < dbcpConnPoolNameArray.length; i++) {
			dbcpConnPoolNameArray[i] = dbcpConnPoolNameList.get(i);
		}
		
		ComboBoxModel<String> dbcpConnPoolNameComboBoxModel = new DefaultComboBoxModel<String>(dbcpConnPoolNameArray);
		
		dbcpConnNameListComboBox.setModel(dbcpConnPoolNameComboBoxModel);
		
		
		// FIXME!
		SequencedProperties sourceSequencedProperties = 
				mainProject.getSourceSequencedProperties();
		
		SinnoriConfigInfo sinnoriConfigInfo = mainProject.getSinnoriConfigInfo();
		
		//sinnoriConfigInfo.getDbcpPartConfigItemList();
		List<ConfigItem> commonConfigItemList = 
				sinnoriConfigInfo.getCommonPartConfigItemList();		
		/*List<ConfigItem> projectConfigItemList = 
				sinnoriConfigInfo.getProjectPartConfigItemList();*/
		
		// commonConfigTable
		this.valuesOfCommonConfigItemTable = new Object[commonConfigItemList.size()][titlesOfConfigItemTable.length];
		
		for (int i=0; i < valuesOfCommonConfigItemTable.length; i++) {
			ConfigItem configItem = commonConfigItemList.get(i);
			String itemID = configItem.getItemID();
			String targetKey = itemID;
			ConfigItemCellValue configItemCellValue = new ConfigItemCellValue(
					targetKey, 
					sourceSequencedProperties,
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

	private void prevButtonActionPerformed(ActionEvent e) {
		WindowManger.getInstance().changeProjectEditScreenToFirstScreen();
	}

	private void subProjectEditButtonActionPerformed(ActionEvent e) {
		SubProjectConfigPopup popup = new SubProjectConfigPopup(WindowManger.getInstance().getMainWindow());
		popup.setVisible(true);
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
		subProjectNameInputTextField = new JTextField();
		subProjectNameInputButton = new JButton();
		subProjectListLinePanel = new JPanel();
		subProjectNameListLabel = new JLabel();
		subProjectNameListComboBox = new JComboBox<>();
		subProjectNameListFuncPanel = new JPanel();
		subProjectNameEditButton = new JButton();
		subProjectNameDeleteButton = new JButton();
		dbcpConnPoolNameInputLinePanel = new JPanel();
		dbcpConnPoolNameInputLabel = new JLabel();
		dbcpConnPoolNameInputTextField = new JTextField();
		dbcpConnPoolNameInputButton = new JButton();
		dbcpConnPoolNameListLinePanel = new JPanel();
		dbcpConnPoolNameListLabel = new JLabel();
		dbcpConnNameListComboBox = new JComboBox<>();
		dbcpConnNameListFuncPanel = new JPanel();
		dbcpConnNameEditButton = new JButton();
		dbcpConnNameDeleteButton = new JButton();
		commonConfigLabel = new JLabel();
		commonConfigScrollPane = new JScrollPane();
		commonConfigTable = new JTable();
		projectConfigLabel = new JLabel();
		projectConfigScrollPane = new JScrollPane();
		projectConfigTable = new JTable();

		//======== this ========
		setLayout(new FormLayout(
			"[443dlu,pref]",
			"11*(default, $lgap), 104dlu, $lgap, default, $lgap, 116dlu, $lgap, default"));
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
			subProjectNameInputLabel.setText("\uc11c\ube0c \ud504\ub85c\uc81d\ud2b8 \uc774\ub984 :");
			subProjectNameInputLinePanel.add(subProjectNameInputLabel, CC.xy(1, 1));
			subProjectNameInputLinePanel.add(subProjectNameInputTextField, CC.xy(3, 1));

			//---- subProjectNameInputButton ----
			subProjectNameInputButton.setText("\ucd94\uac00");
			subProjectNameInputLinePanel.add(subProjectNameInputButton, CC.xy(5, 1));
		}
		add(subProjectNameInputLinePanel, CC.xy(1, 13));

		//======== subProjectListLinePanel ========
		{
			subProjectListLinePanel.setLayout(new FormLayout(
				"2*(default, $lcgap), default",
				"default"));

			//---- subProjectNameListLabel ----
			subProjectNameListLabel.setText("\uc0dd\uc131\ub41c \uc11c\ube0c \ud504\ub85c\uc81d\ud2b8 \ubaa9\ub85d");
			subProjectListLinePanel.add(subProjectNameListLabel, CC.xy(1, 1));

			//---- subProjectNameListComboBox ----
			subProjectNameListComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
				"- \uc11c\ube0c \ud504\ub85c\uc81d\ud2b8 \uc120\ud0dd -",
				"sample_test_sub1",
				"sample_test_sub2"
			}));
			subProjectListLinePanel.add(subProjectNameListComboBox, CC.xy(3, 1));

			//======== subProjectNameListFuncPanel ========
			{
				subProjectNameListFuncPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));

				//---- subProjectNameEditButton ----
				subProjectNameEditButton.setText("\ud3b8\uc9d1");
				subProjectNameEditButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						subProjectEditButtonActionPerformed(e);
					}
				});
				subProjectNameListFuncPanel.add(subProjectNameEditButton);

				//---- subProjectNameDeleteButton ----
				subProjectNameDeleteButton.setText("\uc0ad\uc81c");
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
			dbcpConnPoolNameInputLabel.setText("DBCP Connection Name :");
			dbcpConnPoolNameInputLinePanel.add(dbcpConnPoolNameInputLabel, CC.xy(1, 1));
			dbcpConnPoolNameInputLinePanel.add(dbcpConnPoolNameInputTextField, CC.xy(3, 1));

			//---- dbcpConnPoolNameInputButton ----
			dbcpConnPoolNameInputButton.setText("\ucd94\uac00");
			dbcpConnPoolNameInputLinePanel.add(dbcpConnPoolNameInputButton, CC.xy(5, 1));
		}
		add(dbcpConnPoolNameInputLinePanel, CC.xy(1, 17));

		//======== dbcpConnPoolNameListLinePanel ========
		{
			dbcpConnPoolNameListLinePanel.setLayout(new FormLayout(
				"2*(default, $lcgap), default",
				"default"));

			//---- dbcpConnPoolNameListLabel ----
			dbcpConnPoolNameListLabel.setText("\uc0dd\uc131\ub41c \uc11c\ube0c \ud504\ub85c\uc81d\ud2b8 \ubaa9\ub85d");
			dbcpConnPoolNameListLinePanel.add(dbcpConnPoolNameListLabel, CC.xy(1, 1));

			//---- dbcpConnNameListComboBox ----
			dbcpConnNameListComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
				"- DB Connection pool name -",
				"tw_sinnoridb"
			}));
			dbcpConnPoolNameListLinePanel.add(dbcpConnNameListComboBox, CC.xy(3, 1));

			//======== dbcpConnNameListFuncPanel ========
			{
				dbcpConnNameListFuncPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));

				//---- dbcpConnNameEditButton ----
				dbcpConnNameEditButton.setText("\ud3b8\uc9d1");
				dbcpConnNameEditButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						subProjectEditButtonActionPerformed(e);
					}
				});
				dbcpConnNameListFuncPanel.add(dbcpConnNameEditButton);

				//---- dbcpConnNameDeleteButton ----
				dbcpConnNameDeleteButton.setText("\uc0ad\uc81c");
				dbcpConnNameListFuncPanel.add(dbcpConnNameDeleteButton);
			}
			dbcpConnPoolNameListLinePanel.add(dbcpConnNameListFuncPanel, CC.xy(5, 1));
		}
		add(dbcpConnPoolNameListLinePanel, CC.xy(1, 19));

		//---- commonConfigLabel ----
		commonConfigLabel.setText("\uacf5\ud1b5 \uc124\uc815");
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
					"\ud0a4", "\uac12"
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

		//---- projectConfigLabel ----
		projectConfigLabel.setText("\uc8fc \ud504\ub85c\uc81d\ud2b8 \uc124\uc815");
		add(projectConfigLabel, CC.xy(1, 25));

		//======== projectConfigScrollPane ========
		{

			//---- projectConfigTable ----
			projectConfigTable.setModel(new DefaultTableModel(
				new Object[][] {
					{null, null},
					{null, null},
				},
				new String[] {
					"\ud0a4", "\uac12"
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
			projectConfigTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			projectConfigScrollPane.setViewportView(projectConfigTable);
		}
		add(projectConfigScrollPane, CC.xy(1, 27));
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
	private JTextField subProjectNameInputTextField;
	private JButton subProjectNameInputButton;
	private JPanel subProjectListLinePanel;
	private JLabel subProjectNameListLabel;
	private JComboBox<String> subProjectNameListComboBox;
	private JPanel subProjectNameListFuncPanel;
	private JButton subProjectNameEditButton;
	private JButton subProjectNameDeleteButton;
	private JPanel dbcpConnPoolNameInputLinePanel;
	private JLabel dbcpConnPoolNameInputLabel;
	private JTextField dbcpConnPoolNameInputTextField;
	private JButton dbcpConnPoolNameInputButton;
	private JPanel dbcpConnPoolNameListLinePanel;
	private JLabel dbcpConnPoolNameListLabel;
	private JComboBox<String> dbcpConnNameListComboBox;
	private JPanel dbcpConnNameListFuncPanel;
	private JButton dbcpConnNameEditButton;
	private JButton dbcpConnNameDeleteButton;
	private JLabel commonConfigLabel;
	private JScrollPane commonConfigScrollPane;
	private JTable commonConfigTable;
	private JLabel projectConfigLabel;
	private JScrollPane projectConfigScrollPane;
	private JTable projectConfigTable;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
