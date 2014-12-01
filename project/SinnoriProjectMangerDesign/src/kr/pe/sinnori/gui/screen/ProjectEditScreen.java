/*
 * Created by JFormDesigner on Sat Nov 29 13:48:34 KST 2014
 */

package kr.pe.sinnori.gui.screen;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import kr.pe.sinnori.gui.lib.WindowManger;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Jonghoon Won
 */
@SuppressWarnings("serial")
public class ProjectEditScreen extends JPanel {	
	public ProjectEditScreen() {
		initComponents();
	}

	private void prevButtonActionPerformed(ActionEvent e) {
		WindowManger.getInstance().changeProjectEditScreenToFirstScreen();
	}

	private void subProjectEditButtonActionPerformed(ActionEvent e) {
		// TODO add your code here
		SubProjectConfigPopup popup = new SubProjectConfigPopup(WindowManger.getInstance().getMainWindow());
		popup.setVisible(true);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Jonghoon Won
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
		subProjectListLabel = new JLabel();
		subProjectListComboBox = new JComboBox<>();
		subProjectListFuncPanel = new JPanel();
		subProjectEditButton = new JButton();
		subProjectDeleteButton = new JButton();
		commonConfigLabel = new JLabel();
		commonConfigScrollPane = new JScrollPane();
		commonConfigTable = new JTable();
		projectConfigLabel = new JLabel();
		projectConfigScrollPane = new JScrollPane();
		projectConfigTable = new JTable();

		//======== this ========

		// JFormDesigner evaluation mark
		setBorder(new javax.swing.border.CompoundBorder(
			new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
				"JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
				javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
				java.awt.Color.red), getBorder())); addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

		setLayout(new FormLayout(
			"[300dlu,pref]:grow",
			"9*(default, $lgap), 104dlu, $lgap, default, $lgap, 116dlu"));

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
				"default, $lcgap, ${growing-button}",
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

			//---- subProjectListLabel ----
			subProjectListLabel.setText("\uc0dd\uc131\ub41c \uc11c\ube0c \ud504\ub85c\uc81d\ud2b8 \ubaa9\ub85d");
			subProjectListLinePanel.add(subProjectListLabel, CC.xy(1, 1));

			//---- subProjectListComboBox ----
			subProjectListComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
				"- \uc11c\ube0c \ud504\ub85c\uc81d\ud2b8 \uc120\ud0dd -",
				"sample_test_sub1",
				"sample_test_sub2"
			}));
			subProjectListLinePanel.add(subProjectListComboBox, CC.xy(3, 1));

			//======== subProjectListFuncPanel ========
			{
				subProjectListFuncPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));

				//---- subProjectEditButton ----
				subProjectEditButton.setText("\ud3b8\uc9d1");
				subProjectEditButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						subProjectEditButtonActionPerformed(e);
					}
				});
				subProjectListFuncPanel.add(subProjectEditButton);

				//---- subProjectDeleteButton ----
				subProjectDeleteButton.setText("\uc0ad\uc81c");
				subProjectListFuncPanel.add(subProjectDeleteButton);
			}
			subProjectListLinePanel.add(subProjectListFuncPanel, CC.xy(5, 1));
		}
		add(subProjectListLinePanel, CC.xy(1, 15));

		//---- commonConfigLabel ----
		commonConfigLabel.setText("\uacf5\ud1b5 \uc124\uc815");
		add(commonConfigLabel, CC.xy(1, 17));

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
				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
			});
			commonConfigScrollPane.setViewportView(commonConfigTable);
		}
		add(commonConfigScrollPane, CC.xy(1, 19));

		//---- projectConfigLabel ----
		projectConfigLabel.setText("\uc8fc \ud504\ub85c\uc81d\ud2b8 \uc124\uc815");
		add(projectConfigLabel, CC.xy(1, 21));

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
			projectConfigScrollPane.setViewportView(projectConfigTable);
		}
		add(projectConfigScrollPane, CC.xy(1, 23));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Jonghoon Won
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
	private JLabel subProjectListLabel;
	private JComboBox<String> subProjectListComboBox;
	private JPanel subProjectListFuncPanel;
	private JButton subProjectEditButton;
	private JButton subProjectDeleteButton;
	private JLabel commonConfigLabel;
	private JScrollPane commonConfigScrollPane;
	private JTable commonConfigTable;
	private JLabel projectConfigLabel;
	private JScrollPane projectConfigScrollPane;
	private JTable projectConfigTable;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
