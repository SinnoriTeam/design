/*
 * Created by JFormDesigner on Sun Nov 30 01:28:48 KST 2014
 */

package kr.pe.sinnori.gui.screen;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author Jonghoon Won
 */
@SuppressWarnings("serial")
public class SubProjectConfigPopup extends JDialog {
	public SubProjectConfigPopup(Frame owner) {
		super(owner, true);
		initComponents();
	}

	public SubProjectConfigPopup(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void okButtonActionPerformed(ActionEvent e) {
		this.dispose();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Jonghoon Won
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		sinnoriInstalledPathLinePanel = new JPanel();
		sinnoriInstalledPathTitleLabel = new JLabel();
		sinnoriInstalledPathValueLabel = new JLabel();
		mainProjectNameLinePanel = new JPanel();
		mainProjectNameTitleLabel = new JLabel();
		mainProjectNameValueLabel = new JLabel();
		subProjectNameLinePanel = new JPanel();
		subProjectNameTitleLabel = new JLabel();
		subProjectNameValueLabel = new JLabel();
		subProjectConfigScrollPane = new JScrollPane();
		subProjectConfigTable = new JTable();
		buttonBar = new JPanel();
		okButton = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.createEmptyBorder("7dlu, 7dlu, 7dlu, 7dlu"));

			// JFormDesigner evaluation mark
			dialogPane.setBorder(new javax.swing.border.CompoundBorder(
				new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
					"JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
					javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
					java.awt.Color.red), dialogPane.getBorder())); dialogPane.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"${growing-button}",
					"3*(default, $lgap), 90dlu:grow"));

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
				contentPanel.add(sinnoriInstalledPathLinePanel, CC.xy(1, 1));

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
				contentPanel.add(mainProjectNameLinePanel, CC.xy(1, 3));

				//======== subProjectNameLinePanel ========
				{
					subProjectNameLinePanel.setLayout(new FormLayout(
						"default, $lcgap, default:grow",
						"default"));

					//---- subProjectNameTitleLabel ----
					subProjectNameTitleLabel.setText("\uc11c\ube0c \ud504\ub85c\uc81d\ud2b8 \uc774\ub984 :");
					subProjectNameLinePanel.add(subProjectNameTitleLabel, CC.xy(1, 1));

					//---- subProjectNameValueLabel ----
					subProjectNameValueLabel.setText("sample_test_sub1");
					subProjectNameLinePanel.add(subProjectNameValueLabel, CC.xy(3, 1));
				}
				contentPanel.add(subProjectNameLinePanel, CC.xy(1, 5));

				//======== subProjectConfigScrollPane ========
				{

					//---- subProjectConfigTable ----
					subProjectConfigTable.setModel(new DefaultTableModel(
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
					subProjectConfigScrollPane.setViewportView(subProjectConfigTable);
				}
				contentPanel.add(subProjectConfigScrollPane, CC.xy(1, 7));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.createEmptyBorder("5dlu, 0dlu, 0dlu, 0dlu"));
				buttonBar.setLayout(new FormLayout(
					"$glue, $button",
					"pref"));

				//---- okButton ----
				okButton.setText("OK");
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
	// Generated using JFormDesigner Evaluation license - Jonghoon Won
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JPanel sinnoriInstalledPathLinePanel;
	private JLabel sinnoriInstalledPathTitleLabel;
	private JLabel sinnoriInstalledPathValueLabel;
	private JPanel mainProjectNameLinePanel;
	private JLabel mainProjectNameTitleLabel;
	private JLabel mainProjectNameValueLabel;
	private JPanel subProjectNameLinePanel;
	private JLabel subProjectNameTitleLabel;
	private JLabel subProjectNameValueLabel;
	private JScrollPane subProjectConfigScrollPane;
	private JTable subProjectConfigTable;
	private JPanel buttonBar;
	private JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
