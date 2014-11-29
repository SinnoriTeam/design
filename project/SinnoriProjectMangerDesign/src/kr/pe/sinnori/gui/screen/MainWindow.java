/*
 * Created by JFormDesigner on Sat Nov 29 11:20:24 KST 2014
 */

package kr.pe.sinnori.gui.screen;

import java.awt.Container;
import java.awt.EventQueue;
import java.util.logging.Logger;

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

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new FormLayout(
			"default",
			"default"));
		contentPane.add(firstScreenPanel, CC.xy(1, 1));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
				
		Logger.getLogger("test").info("hello");
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Jonghoon Won
	private FirstScreen firstScreenPanel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
