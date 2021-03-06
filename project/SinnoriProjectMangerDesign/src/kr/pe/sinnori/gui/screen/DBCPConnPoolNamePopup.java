/*
 * Created by JFormDesigner on Thu Dec 25 10:39:38 KST 2014
 */

package kr.pe.sinnori.gui.screen;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import kr.pe.sinnori.common.exception.ConfigValueInvalidException;
import kr.pe.sinnori.common.util.SequencedProperties;
import kr.pe.sinnori.gui.config.SinnoriConfigInfo;
import kr.pe.sinnori.gui.table.ConfigItemTableModel;
import kr.pe.sinnori.gui.table.ConfigItemValue;
import kr.pe.sinnori.gui.table.ConfigItemValueEditor;
import kr.pe.sinnori.gui.table.ConfigItemValueRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Jonghoon Won
 */
@SuppressWarnings("serial")
public class DBCPConnPoolNamePopup extends JDialog {
	private Logger log = LoggerFactory.getLogger(DBCPConnPoolNamePopup.class);
	
	private String mainProjectName;
	private SinnoriConfigInfo sinnoriConfigInfo = null;
	private String selectedDBCPConnPoolName;
	private ConfigItemTableModel dbcpPartConfigTableModel;
	
	/*private String titlesOfConfigItemTable[] = {
			"key", "value"
		};
	
	private Class<?>[] columnTypesOfConfigItemTable = new Class[] {
		String.class, ConfigItemCellValue.class
	};*/
	
	public DBCPConnPoolNamePopup(Frame owner, 
			String mainProjectName, 
			SinnoriConfigInfo sinnoriConfigInfo,
			String selectedDBCPConnPoolName,
			ConfigItemTableModel dbcpPartConfigTableModel, int rowHavingBadValue, String targetKeyHavingBadValue) {
		super(owner);
		
		this.mainProjectName = mainProjectName;
		this.sinnoriConfigInfo = sinnoriConfigInfo;
		this.selectedDBCPConnPoolName = selectedDBCPConnPoolName;
		this.dbcpPartConfigTableModel = dbcpPartConfigTableModel;
		
		int maxRow = dbcpPartConfigTableModel.getRowCount();
		if (rowHavingBadValue >= maxRow) {			
			log.warn("parameter rowHavingBadValue[{}] is greater than maxRow[{}]", rowHavingBadValue, maxRow);
			JOptionPane.showMessageDialog(this, "parameter focusRow is greater than maxRow");	
			this.dispose();
		}
		
		initComponents();
		
		/** Post-Creation Code Start */
		mainProjectNameValueLabel.setText(this.mainProjectName);
		dbcpConnPoolNameValueLabel.setText(this.selectedDBCPConnPoolName);
		dbcpConnPoolNamePartTable.setModel(this.dbcpPartConfigTableModel);
				
		// commonConfigTable.setModel(commonConfigItemTableModel);
		dbcpConnPoolNamePartTable.getColumnModel().getColumn(1).setResizable(false);
		dbcpConnPoolNamePartTable.getColumnModel().getColumn(1).setPreferredWidth(250);
				
		dbcpConnPoolNamePartTable.getColumnModel().getColumn(1).setCellRenderer(new ConfigItemValueRenderer());
		dbcpConnPoolNamePartTable.getColumnModel().getColumn(1).setCellEditor(new ConfigItemValueEditor(new JCheckBox()));
		dbcpConnPoolNamePartTable.setRowHeight(38);
		dbcpConnPoolNamePartScrollPane.repaint();
		if (rowHavingBadValue >= 0) {			
			dbcpConnPoolNamePartTable.changeSelection(rowHavingBadValue, 1, false, false);
			dbcpConnPoolNamePartTable.editCellAt(rowHavingBadValue, 1);
			JOptionPane.showMessageDialog(this, new StringBuilder("Please check the value of key[")
			.append(targetKeyHavingBadValue).append("]").toString());
		}		
		/** Post-Creation Code End */
	}

	private void okButtonActionPerformed(ActionEvent e) {
		SequencedProperties newSinnoriConfig = new SequencedProperties();
		int maxRow = dbcpPartConfigTableModel.getRowCount();
		log.info("dbcpConnPoolName={}, maxRow={}", selectedDBCPConnPoolName, maxRow);
						
		for (int i=0; i < maxRow; i++) {
			Object tableModelValue = dbcpPartConfigTableModel.getValueAt(i, 1);
			
			if (!(tableModelValue instanceof ConfigItemValue)) {
				log.error("dbcpConnPoolName[{}] ConfigItemTableModel[{}][{}]'s value is not instanc of ConfigItemValue class",
						selectedDBCPConnPoolName, i, 1);
				System.exit(1);
			}
			 
			ConfigItemValue configItemCellValue = (ConfigItemValue)tableModelValue;
			String targetKey = configItemCellValue.getTargetKey();
			String targetValue = configItemCellValue.getValueOfComponent();
			
			log.info("dbcpConnPoolName={}, row index={}, targetKey={}, targetValue={}", 
					selectedDBCPConnPoolName, i, targetKey, targetValue);
			
			newSinnoriConfig.put(targetKey, targetValue);
			
			boolean isValidation = true;
			try {
				isValidation = sinnoriConfigInfo.isValidation(targetKey, newSinnoriConfig);
				
				if (isValidation) {
					sinnoriConfigInfo.getNativeValueAfterBreakChecker(targetKey, newSinnoriConfig);
				}
				
			} catch (ConfigValueInvalidException e1) {
				String errorMessage = e1.getMessage();
				log.warn("targetKey={}, errormessage={}", targetKey, errorMessage);
				
				dbcpConnPoolNamePartTable.changeSelection(i, 1, false, false);
				dbcpConnPoolNamePartTable.editCellAt(i, 1);
				
				JOptionPane.showMessageDialog(this, 
						new StringBuilder("Please check the value of key[")
				.append(targetKey).append("]").toString());
				return;
			}
		}
		
		this.dispose();
	}
/*
	public DBCPConnPoolNamePopup(Dialog owner) {
		super(owner);
		initComponents();
	}*/

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		mainProjectLinePanel = new JPanel();
		mainProjectNameTitleLabel = new JLabel();
		mainProjectNameValueLabel = new JLabel();
		dbcpConnPoolNameLinePanel = new JPanel();
		dbcpConnPoolNameTitleLabel = new JLabel();
		dbcpConnPoolNameValueLabel = new JLabel();
		/** Post-Creation Code Start */
		dbcpConnPoolNameValueLabel.setText(selectedDBCPConnPoolName);
		/** Post-Creation Code End */
		dbcpPartIntroductionLable = new JLabel();
		dbcpConnPoolNamePartScrollPane = new JScrollPane();
		dbcpConnPoolNamePartTable = new JTable();
		buttonBar = new JPanel();
		okButton = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.createEmptyBorder("7dlu, 7dlu, 7dlu, 7dlu"));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"394dlu:grow",
					"3*(default, $lgap), default"));

				//======== mainProjectLinePanel ========
				{
					mainProjectLinePanel.setLayout(new FormLayout(
						"110dlu, $lcgap, 168dlu",
						"default"));

					//---- mainProjectNameTitleLabel ----
					mainProjectNameTitleLabel.setText("Main Project Name :");
					mainProjectLinePanel.add(mainProjectNameTitleLabel, CC.xy(1, 1));
					mainProjectLinePanel.add(mainProjectNameValueLabel, CC.xy(3, 1));
				}
				contentPanel.add(mainProjectLinePanel, CC.xy(1, 1));

				//======== dbcpConnPoolNameLinePanel ========
				{
					dbcpConnPoolNameLinePanel.setLayout(new FormLayout(
						"110dlu, $lcgap, 200dlu",
						"default"));

					//---- dbcpConnPoolNameTitleLabel ----
					dbcpConnPoolNameTitleLabel.setText("DBCP Connection Pool Name :");
					dbcpConnPoolNameLinePanel.add(dbcpConnPoolNameTitleLabel, CC.xy(1, 1));
					dbcpConnPoolNameLinePanel.add(dbcpConnPoolNameValueLabel, CC.xy(3, 1));
				}
				contentPanel.add(dbcpConnPoolNameLinePanel, CC.xy(1, 3));

				//---- dbcpPartIntroductionLable ----
				dbcpPartIntroductionLable.setText("DBCP Part Config");
				contentPanel.add(dbcpPartIntroductionLable, CC.xy(1, 5));

				//======== dbcpConnPoolNamePartScrollPane ========
				{

					//---- dbcpConnPoolNamePartTable ----
					dbcpConnPoolNamePartTable.setModel(new DefaultTableModel(
						new Object[][] {
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
						TableColumnModel cm = dbcpConnPoolNamePartTable.getColumnModel();
						cm.getColumn(1).setMinWidth(150);
					}
					dbcpConnPoolNamePartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					dbcpConnPoolNamePartTable.setAutoCreateColumnsFromModel(false);
					dbcpConnPoolNamePartScrollPane.setViewportView(dbcpConnPoolNamePartTable);
				}
				contentPanel.add(dbcpConnPoolNamePartScrollPane, CC.xy(1, 7));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.createEmptyBorder("5dlu, 0dlu, 0dlu, 0dlu"));
				buttonBar.setLayout(new FormLayout(
					"$glue, $button",
					"pref"));

				//---- okButton ----
				okButton.setText("Close");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, CC.xy(2, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JPanel mainProjectLinePanel;
	private JLabel mainProjectNameTitleLabel;
	private JLabel mainProjectNameValueLabel;
	private JPanel dbcpConnPoolNameLinePanel;
	private JLabel dbcpConnPoolNameTitleLabel;
	private JLabel dbcpConnPoolNameValueLabel;
	private JLabel dbcpPartIntroductionLable;
	private JScrollPane dbcpConnPoolNamePartScrollPane;
	private JTable dbcpConnPoolNamePartTable;
	private JPanel buttonBar;
	private JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
