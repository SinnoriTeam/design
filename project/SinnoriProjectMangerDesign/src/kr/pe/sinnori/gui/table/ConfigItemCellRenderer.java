package kr.pe.sinnori.gui.table;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@SuppressWarnings("serial")
public class ConfigItemCellRenderer extends JPanel implements TableCellRenderer {
	private Logger log = LoggerFactory.getLogger(ConfigItemCellRenderer.class);
	
	public ConfigItemCellRenderer() {
		setOpaque(true);
		init();
	}

	private void init() {
		LayoutManager layoutManager = this.getLayout();
		log.info("current layout manger class name={}", layoutManager.getClass().getName());
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		/*LayoutManager layoutManager = this.getLayout();
		log.info("current layout manger class name={}", layoutManager.getClass().getName());*/
		
		
		ConfigItemCellValue configItemCellValue = (ConfigItemCellValue)value;
				
		if (isSelected) {
			configItemCellValue.setForeground(table.getSelectionForeground());
			configItemCellValue.setBackground(table.getSelectionBackground());
		} else {
			configItemCellValue.setForeground(table.getForeground());
			configItemCellValue.setBackground(table.getBackground());
		}
		
		return configItemCellValue;
	}
	

}

