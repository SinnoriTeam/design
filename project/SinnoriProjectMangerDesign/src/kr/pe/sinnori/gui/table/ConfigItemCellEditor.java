package kr.pe.sinnori.gui.table;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;



@SuppressWarnings("serial")
public class ConfigItemCellEditor extends DefaultCellEditor {
	public ConfigItemCellEditor(JCheckBox checkBox) {
		super(checkBox);
	}

	public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column) {		
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
	
	public Object getCellEditorValue() {
	    return null;
	}

}
