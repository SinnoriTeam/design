package main;

import javax.swing.JFrame;

import kr.pe.sinnori.gui.screen.MainWindow;

public class SinnoriProjectMangerMain {
	private static MainWindow mainWindow =  new MainWindow();
	public static void pack() {
		mainWindow.pack();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
			mainWindow.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
