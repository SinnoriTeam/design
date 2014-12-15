/*
 * Created by JFormDesigner on Sat Nov 29 11:23:08 KST 2014
 */

package kr.pe.sinnori.gui.screen;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import kr.pe.sinnori.common.exception.ConfigErrorException;
import kr.pe.sinnori.gui.lib.Project;
import kr.pe.sinnori.gui.lib.ProjectManger;
import kr.pe.sinnori.gui.lib.WindowManger;
import kr.pe.sinnori.gui.util.PathSwingAction;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Jonghoon Won
 */
@SuppressWarnings("serial")
public class FirstScreen extends JPanel {
	// private Logger log = LoggerFactory.getLogger(FirstScreen.class);
	private JFrame mainFrame = null;
	private JFileChooser chooser = null;
	private ProjectManger projectManger = null;
	// private List<String> projectNameList = new ArrayList<String>();
	// private HashMap<String, Project> projectHash = new HashMap<String, Project>();
	
	public FirstScreen(JFrame mainFrame) {
		this.mainFrame =mainFrame;
		initComponents();
	}

	private void projectEditButtonActionPerformed(ActionEvent e) {
		// FIXME!
		if (projectListComboBox.getSelectedIndex() > 0) {
			String projectName = (String)projectListComboBox.getSelectedItem();
			Project selectedProject = projectManger.getProject(projectName);
			if (null == selectedProject) {
				JOptionPane.showMessageDialog(mainFrame, "프로젝트를 얻는데 실패하였습니다.");
				sinnoriInstalledPathInputTextField.requestFocusInWindow();
				return;
			}
			
			projectNameValueLabel.setText(selectedProject.getProjectName());
			// serverCheckBox.setSelected(selectedProject.get);
			appClientCheckBox.setSelected(selectedProject.isAppClient());
			webClientCheckBox.setSelected(selectedProject.isWebClient());
			
			WindowManger.getInstance().changeFirstScreenToProjectEditScreen(selectedProject);
		}
	}

	private void sinnoriInstalledPathAnalysisButtonActionPerformed(ActionEvent e) {
		// TODO add your code here
		String sinnoriInstalledPathString = sinnoriInstalledPathInputTextField.getText();
		if ( null == sinnoriInstalledPathString) {
			JOptionPane.showMessageDialog(mainFrame, "신놀이 설치 경로를 입력해 주세요.");
			sinnoriInstalledPathInputTextField.requestFocusInWindow();
			return;
		}
		sinnoriInstalledPathString = sinnoriInstalledPathString.trim();
		sinnoriInstalledPathInputTextField.setText(sinnoriInstalledPathString);
		
		if (sinnoriInstalledPathString.equals("")) {
			JOptionPane.showMessageDialog(mainFrame, "신놀이 설치 경로를 다시 입력해 주세요.");
			sinnoriInstalledPathInputTextField.requestFocusInWindow();
			return;
		}
		
		File sinnoriInstalledPath = new File(sinnoriInstalledPathString);
		if (!sinnoriInstalledPath.exists()) {
			String errorMessage = String.format("신놀이 설치 경로[%s] 가 존재하지 않습니다.", sinnoriInstalledPathString);
			JOptionPane.showMessageDialog(mainFrame, errorMessage);
			sinnoriInstalledPathInputTextField.requestFocusInWindow();
			return;
		}
		
		if (!sinnoriInstalledPath.isDirectory()) {
			String errorMessage = String.format("신놀이 설치 경로[%s] 가 디렉토리가 아닙니다.", sinnoriInstalledPathString);
			JOptionPane.showMessageDialog(mainFrame, errorMessage);
			sinnoriInstalledPathInputTextField.requestFocusInWindow();
			return;
		}
		
		if (!sinnoriInstalledPath.canRead()) {
			String errorMessage = String.format("신놀이 설치 경로[%s] 에 대한 읽기 권한이 없습니다.", sinnoriInstalledPathString);
			JOptionPane.showMessageDialog(mainFrame, errorMessage);
			sinnoriInstalledPathInputTextField.requestFocusInWindow();
			return;
		}
		
		if (!sinnoriInstalledPath.canWrite()) {
			String errorMessage = String.format("신놀이 설치 경로[%s] 에 대한 쓰기 권한이 없습니다.", sinnoriInstalledPathString);
			JOptionPane.showMessageDialog(mainFrame, errorMessage);
			sinnoriInstalledPathInputTextField.requestFocusInWindow();
			return;
		}
		
		try {
			sinnoriInstalledPathString = sinnoriInstalledPath.getCanonicalFile().getAbsolutePath();			
			sinnoriInstalledPathInputTextField.setText(sinnoriInstalledPathString);
		} catch (IOException e1) {
			String errorMessage = String.format("신놀이 설치 경로[%s]를 시스템 절대 경로로 변경하는데 실패하였습니다.", sinnoriInstalledPathString);
			JOptionPane.showMessageDialog(mainFrame, errorMessage);
			sinnoriInstalledPathInputTextField.requestFocusInWindow();
			return;
		}
		
		String projectBasePathString = new StringBuilder(sinnoriInstalledPathString)
		.append(File.separator).append("project").toString();
		
		try {
			projectManger = new ProjectManger(projectBasePathString);
		} catch (ConfigErrorException e2) {
			JOptionPane.showMessageDialog(mainFrame, e2.getMessage());
			sinnoriInstalledPathInputTextField.requestFocusInWindow();
			return;
		}		
		
		projectListComboBox.removeAllItems();
		projectListComboBox.addItem("- project -");
		
		List<Project> projectList = projectManger.getProjectList();
		for (Project project : projectList) {
			projectListComboBox.addItem(project.getProjectName());
		}
		
		
		sinnoriInstalledPathInfoValueLabel.setText(sinnoriInstalledPathString);
		allProjectWorkSaveButton.setEnabled(true);
		projectNameInputTextField.setEnabled(true);
		projectNameInputButton.setEnabled(true);
		
		projectListComboBox.setEnabled(true);
		projectEditButton.setEnabled(true);
		projectDeleteButton.setEnabled(true);
	}

	private void projectListComboBoxItemStateChanged(ItemEvent e) {
		// TODO add your code here
		
		if (ItemEvent.SELECTED == e.getStateChange()) {
			
			if (projectListComboBox.getSelectedIndex() > 0) {
				String projectName = (String)e.getItem();
				Project selectedProject = projectManger.getProject(projectName);
				if (null == selectedProject) {
					JOptionPane.showMessageDialog(mainFrame, "selectedProject is null");
					sinnoriInstalledPathInputTextField.requestFocusInWindow();
					return;
				}
				
				projectNameValueLabel.setText(selectedProject.getProjectName());
				appClientCheckBox.setSelected(selectedProject.isAppClient());
				webClientCheckBox.setSelected(selectedProject.isWebClient());
				servletEnginLibinaryPathTextField.setText(selectedProject.getServletEnginLibPathString());
			} else {
				projectNameValueLabel.setText("");
				servletEnginLibinaryPathTextField.setText("");
			}
		}
	}
	

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Jonghoon Won
		sinnoriInstalledPathInputLinePanel = new JPanel();
		sinnoriInstalledPathInputLabel = new JLabel();
		sinnoriInstalledPathInputTextField = new JTextField();
		sinnoriInstalledPathInputButton = new JButton();
		sinnoriInstalledPathAnalysisLinePanel = new JPanel();
		sinnoriInstalledPathAnalysisButton = new JButton();
		hSpacer1 = new JPanel(null);
		sinnoriInstalledPathInfoLinePanel = new JPanel();
		sinnoriInstalledPathInfoTitleLabel = new JLabel();
		sinnoriInstalledPathInfoValueLabel = new JLabel();
		allProjectWorkSaveLinePanel = new JPanel();
		allProjectWorkSaveButton = new JButton();
		projectNameInputLinePanel = new JPanel();
		projectNameInputLabel = new JLabel();
		projectNameInputTextField = new JTextField();
		projectNameInputButton = new JButton();
		projectListLinePanel = new JPanel();
		projectListLabel = new JLabel();
		projectListFuncPanel = new JPanel();
		projectListComboBox = new JComboBox<>();
		projectEditButton = new JButton();
		projectDeleteButton = new JButton();
		hSpacer2 = new JPanel(null);
		projectNameLinePanel = new JPanel();
		projectNameTitleLabel = new JLabel();
		projectNameValueLabel = new JLabel();
		projectStructLinePanel = new JPanel();
		projectStructLabel = new JLabel();
		projectStructFuncPanel = new JPanel();
		serverCheckBox = new JCheckBox();
		appClientCheckBox = new JCheckBox();
		webClientCheckBox = new JCheckBox();
		servletEnginLibinaryPathLinePanel = new JPanel();
		servletEnginLibinaryPathLabel = new JLabel();
		servletEnginLibinaryPathTextField = new JTextField();
		projectConfigVeiwLinePanel = new JPanel();
		projectConfigVeiwButton = new JButton();

		//======== this ========

		// JFormDesigner evaluation mark
		setBorder(new javax.swing.border.CompoundBorder(
			new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
				"JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
				javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
				java.awt.Color.red), getBorder())); addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

		setLayout(new FormLayout(
			"${growing-button}",
			"18dlu, 2*($lgap, default), $lgap, 13dlu, 8*($lgap, default)"));
		/** Post-initialization Code start */
		UIManager.put("FileChooser.readOnly", Boolean.TRUE); 
		chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		PathSwingAction pathAction = new PathSwingAction(mainFrame, chooser, sinnoriInstalledPathInputTextField);
		sinnoriInstalledPathInputButton.setAction(pathAction);
		/** Post-initialization Code end */

		//======== sinnoriInstalledPathInputLinePanel ========
		{
			sinnoriInstalledPathInputLinePanel.setLayout(new FormLayout(
				"55dlu, $lcgap, ${growing-button}, $lcgap, 52dlu",
				"default"));

			//---- sinnoriInstalledPathInputLabel ----
			sinnoriInstalledPathInputLabel.setText("\uc2e0\ub180\uc774 \uc124\uce58 \uacbd\ub85c");
			sinnoriInstalledPathInputLinePanel.add(sinnoriInstalledPathInputLabel, CC.xy(1, 1));
			sinnoriInstalledPathInputLinePanel.add(sinnoriInstalledPathInputTextField, CC.xy(3, 1));

			//---- sinnoriInstalledPathInputButton ----
			sinnoriInstalledPathInputButton.setText("\uacbd\ub85c \uc120\ud0dd");
			sinnoriInstalledPathInputLinePanel.add(sinnoriInstalledPathInputButton, CC.xy(5, 1));
		}
		add(sinnoriInstalledPathInputLinePanel, CC.xy(1, 1));

		//======== sinnoriInstalledPathAnalysisLinePanel ========
		{
			sinnoriInstalledPathAnalysisLinePanel.setLayout(new BoxLayout(sinnoriInstalledPathAnalysisLinePanel, BoxLayout.X_AXIS));

			//---- sinnoriInstalledPathAnalysisButton ----
			sinnoriInstalledPathAnalysisButton.setText("\ud504\ub85c\uc81d\ud2b8 \uc815\ubcf4 \ucd94\ucd9c\ud558\uae30");
			sinnoriInstalledPathAnalysisButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sinnoriInstalledPathAnalysisButtonActionPerformed(e);
				}
			});
			sinnoriInstalledPathAnalysisLinePanel.add(sinnoriInstalledPathAnalysisButton);
		}
		add(sinnoriInstalledPathAnalysisLinePanel, CC.xy(1, 3));

		//---- hSpacer1 ----
		hSpacer1.setBorder(LineBorder.createBlackLineBorder());
		add(hSpacer1, CC.xy(1, 5));

		//======== sinnoriInstalledPathInfoLinePanel ========
		{
			sinnoriInstalledPathInfoLinePanel.setLayout(new FormLayout(
				"default, $lcgap, 317dlu",
				"default"));

			//---- sinnoriInstalledPathInfoTitleLabel ----
			sinnoriInstalledPathInfoTitleLabel.setText("\uc2e0\ub180\uc774 \uc124\uce58 \uacbd\ub85c :");
			sinnoriInstalledPathInfoLinePanel.add(sinnoriInstalledPathInfoTitleLabel, CC.xy(1, 1));
			sinnoriInstalledPathInfoLinePanel.add(sinnoriInstalledPathInfoValueLabel, CC.xy(3, 1));
		}
		add(sinnoriInstalledPathInfoLinePanel, CC.xy(1, 7));

		//======== allProjectWorkSaveLinePanel ========
		{
			allProjectWorkSaveLinePanel.setLayout(new BoxLayout(allProjectWorkSaveLinePanel, BoxLayout.X_AXIS));

			//---- allProjectWorkSaveButton ----
			allProjectWorkSaveButton.setText("\uc804\uccb4 \ud504\ub85c\uc81d\ud2b8 \ubcc0\uacbd \ub0b4\uc5ed \uc800\uc7a5");
			allProjectWorkSaveButton.setEnabled(false);
			allProjectWorkSaveLinePanel.add(allProjectWorkSaveButton);
		}
		add(allProjectWorkSaveLinePanel, CC.xy(1, 9));

		//======== projectNameInputLinePanel ========
		{
			projectNameInputLinePanel.setLayout(new FormLayout(
				"default, $lcgap, ${growing-button}, $lcgap, 37dlu",
				"default"));

			//---- projectNameInputLabel ----
			projectNameInputLabel.setText("\ud504\ub85c\uc81d\ud2b8 \uc774\ub984 :");
			projectNameInputLinePanel.add(projectNameInputLabel, CC.xy(1, 1));

			//---- projectNameInputTextField ----
			projectNameInputTextField.setEnabled(false);
			projectNameInputLinePanel.add(projectNameInputTextField, CC.xy(3, 1));

			//---- projectNameInputButton ----
			projectNameInputButton.setText("\ucd94\uac00");
			projectNameInputButton.setEnabled(false);
			projectNameInputLinePanel.add(projectNameInputButton, CC.xy(5, 1));
		}
		add(projectNameInputLinePanel, CC.xy(1, 11));

		//======== projectListLinePanel ========
		{
			projectListLinePanel.setLayout(new FormLayout(
				"default, $lcgap, default",
				"default"));

			//---- projectListLabel ----
			projectListLabel.setText("\uc0dd\uc131\ub41c \ud504\ub85c\uc81d\ud2b8 \ubaa9\ub85d");
			projectListLinePanel.add(projectListLabel, CC.xy(1, 1));

			//======== projectListFuncPanel ========
			{
				projectListFuncPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));

				//---- projectListComboBox ----
				projectListComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
					"- project -"
				}));
				projectListComboBox.setEnabled(false);
				projectListComboBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						projectListComboBoxItemStateChanged(e);
					}
				});
				projectListFuncPanel.add(projectListComboBox);

				//---- projectEditButton ----
				projectEditButton.setText("\ud3b8\uc9d1");
				projectEditButton.setEnabled(false);
				projectEditButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						projectEditButtonActionPerformed(e);
					}
				});
				projectListFuncPanel.add(projectEditButton);

				//---- projectDeleteButton ----
				projectDeleteButton.setText("\uc0ad\uc81c");
				projectDeleteButton.setEnabled(false);
				projectListFuncPanel.add(projectDeleteButton);
			}
			projectListLinePanel.add(projectListFuncPanel, CC.xy(3, 1));
		}
		add(projectListLinePanel, CC.xy(1, 13));

		//---- hSpacer2 ----
		hSpacer2.setBorder(LineBorder.createBlackLineBorder());
		add(hSpacer2, CC.xy(1, 15));

		//======== projectNameLinePanel ========
		{
			projectNameLinePanel.setLayout(new FormLayout(
				"default, $lcgap, 330dlu",
				"default"));

			//---- projectNameTitleLabel ----
			projectNameTitleLabel.setText("\ud504\ub85c\uc81d\ud2b8 \uc774\ub984 :");
			projectNameLinePanel.add(projectNameTitleLabel, CC.xy(1, 1));
			projectNameLinePanel.add(projectNameValueLabel, CC.xy(3, 1));
		}
		add(projectNameLinePanel, CC.xy(1, 17));

		//======== projectStructLinePanel ========
		{
			projectStructLinePanel.setLayout(new FormLayout(
				"default, $lcgap, 330dlu",
				"default"));

			//---- projectStructLabel ----
			projectStructLabel.setText("\ud504\ub85c\uc81d\ud2b8 \uad6c\uc131 :");
			projectStructLinePanel.add(projectStructLabel, CC.xy(1, 1));

			//======== projectStructFuncPanel ========
			{
				projectStructFuncPanel.setLayout(new BoxLayout(projectStructFuncPanel, BoxLayout.X_AXIS));

				//---- serverCheckBox ----
				serverCheckBox.setText("\uc11c\ubc84");
				serverCheckBox.setEnabled(false);
				serverCheckBox.setSelected(true);
				projectStructFuncPanel.add(serverCheckBox);

				//---- appClientCheckBox ----
				appClientCheckBox.setText("\uc751\uc6a9 \ud074\ub77c\uc774\uc5b8\ud2b8");
				appClientCheckBox.setEnabled(false);
				appClientCheckBox.setSelected(true);
				projectStructFuncPanel.add(appClientCheckBox);

				//---- webClientCheckBox ----
				webClientCheckBox.setText("\uc6f9 \ud074\ub77c\uc774\uc5b8\ud2b8");
				webClientCheckBox.setEnabled(false);
				webClientCheckBox.setSelected(true);
				projectStructFuncPanel.add(webClientCheckBox);
			}
			projectStructLinePanel.add(projectStructFuncPanel, CC.xy(3, 1));
		}
		add(projectStructLinePanel, CC.xy(1, 19));

		//======== servletEnginLibinaryPathLinePanel ========
		{
			servletEnginLibinaryPathLinePanel.setLayout(new FormLayout(
				"default, $lcgap, ${growing-button}",
				"default"));

			//---- servletEnginLibinaryPathLabel ----
			servletEnginLibinaryPathLabel.setText("\uc11c\ube14\ub9bf \uc5d4\uc9c4 \ub77c\uc774\ube0c\ub7ec\ub9ac \uacbd\ub85c :");
			servletEnginLibinaryPathLinePanel.add(servletEnginLibinaryPathLabel, CC.xy(1, 1));

			//---- servletEnginLibinaryPathTextField ----
			servletEnginLibinaryPathTextField.setEditable(false);
			servletEnginLibinaryPathLinePanel.add(servletEnginLibinaryPathTextField, CC.xy(3, 1));
		}
		add(servletEnginLibinaryPathLinePanel, CC.xy(1, 21));

		//======== projectConfigVeiwLinePanel ========
		{
			projectConfigVeiwLinePanel.setLayout(new BoxLayout(projectConfigVeiwLinePanel, BoxLayout.X_AXIS));

			//---- projectConfigVeiwButton ----
			projectConfigVeiwButton.setText("\uc124\uc815 \ud30c\uc77c \ub0b4\uc6a9 \ubcf4\uae30");
			projectConfigVeiwButton.setEnabled(false);
			projectConfigVeiwLinePanel.add(projectConfigVeiwButton);
		}
		add(projectConfigVeiwLinePanel, CC.xy(1, 23));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// Logger.getGlobal().info("call");
		
		
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Jonghoon Won
	private JPanel sinnoriInstalledPathInputLinePanel;
	private JLabel sinnoriInstalledPathInputLabel;
	private JTextField sinnoriInstalledPathInputTextField;
	private JButton sinnoriInstalledPathInputButton;
	private JPanel sinnoriInstalledPathAnalysisLinePanel;
	private JButton sinnoriInstalledPathAnalysisButton;
	private JPanel hSpacer1;
	private JPanel sinnoriInstalledPathInfoLinePanel;
	private JLabel sinnoriInstalledPathInfoTitleLabel;
	private JLabel sinnoriInstalledPathInfoValueLabel;
	private JPanel allProjectWorkSaveLinePanel;
	private JButton allProjectWorkSaveButton;
	private JPanel projectNameInputLinePanel;
	private JLabel projectNameInputLabel;
	private JTextField projectNameInputTextField;
	private JButton projectNameInputButton;
	private JPanel projectListLinePanel;
	private JLabel projectListLabel;
	private JPanel projectListFuncPanel;
	private JComboBox<String> projectListComboBox;
	private JButton projectEditButton;
	private JButton projectDeleteButton;
	private JPanel hSpacer2;
	private JPanel projectNameLinePanel;
	private JLabel projectNameTitleLabel;
	private JLabel projectNameValueLabel;
	private JPanel projectStructLinePanel;
	private JLabel projectStructLabel;
	private JPanel projectStructFuncPanel;
	private JCheckBox serverCheckBox;
	private JCheckBox appClientCheckBox;
	private JCheckBox webClientCheckBox;
	private JPanel servletEnginLibinaryPathLinePanel;
	private JLabel servletEnginLibinaryPathLabel;
	private JTextField servletEnginLibinaryPathTextField;
	private JPanel projectConfigVeiwLinePanel;
	private JButton projectConfigVeiwButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
