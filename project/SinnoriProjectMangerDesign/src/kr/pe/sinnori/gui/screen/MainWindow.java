/*
 * Created by JFormDesigner on Sat Nov 29 11:20:24 KST 2014
 */

package kr.pe.sinnori.gui.screen;

import java.awt.Container;

import javax.swing.JFrame;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Jonghoon Won
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame {	
	public MainWindow() {
		initComponents();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Jonghoon Won
		firstScreenPanel = new FirstScreen();
		projectEditScreenPanel = new ProjectEditScreen();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new FormLayout(
			"default",
			"default, $lgap, default"));
		contentPane.add(firstScreenPanel, CC.xy(1, 1));

		//---- projectEditScreenPanel ----
		projectEditScreenPanel.setVisible(false);
		contentPane.add(projectEditScreenPanel, CC.xy(1, 3));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Jonghoon Won
	private FirstScreen firstScreenPanel;
	private ProjectEditScreen projectEditScreenPanel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables	
	public FirstScreen getFirstScreenPanel() {
		return firstScreenPanel;
	}

	public ProjectEditScreen getProjectEditScreenPanel() {
		return projectEditScreenPanel;
	}
	
	
}
