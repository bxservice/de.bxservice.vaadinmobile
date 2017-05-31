package com.trekglobal.vaadin.ui;

import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.util.Env;
import org.compiere.util.Msg;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class WFindView extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1382729531274399234L;
	
	private IFindView view;
	private TextField valueTextField;
	private TextField nameTextField;
	private TextField docNoTextField;
	private TextField descriptionTextField;
	private Button okButton; 
	private Button cancelButton;
	
	public WFindView(IFindView view, GridTab curTab) {
		
		this.view = view;
		
		boolean hasValue = false;
		boolean hasName = false;
		boolean hasDocNo = false;
		boolean hasDescription = false;
		
		//Get Info from target Tab
		int size = curTab.getFieldCount();
		for (int i = 0; i < size; i++) {
			GridField mField = curTab.getField(i);
			String columnName = mField.getColumnName();

			if (mField.isDisplayed()) {
				if (columnName.equals("Value"))
					hasValue = true;
				else if (columnName.equals("Name"))
					hasName = true;
				else if (columnName.equals("DocumentNo"))
					hasDocNo = true;
				else if (columnName.equals("Description"))
					hasDescription = true;
			}
		}
		

		Label title = new Label(Msg.getMsg(Env.getLanguage(Env.getCtx()), "search"));
		title.addStyleName("bxprocesspara-title");
		addComponent(title);
		
		if (hasValue) {
			valueTextField = new TextField();
			valueTextField.setPlaceholder(Msg.getCleanMsg(Env.getCtx(), "Value"));
			valueTextField.addStyleName("search-field");
			addComponent(valueTextField);
		}
		
		if (hasDocNo) {
			docNoTextField = new TextField();
			docNoTextField.setPlaceholder(Msg.getCleanMsg(Env.getCtx(), "DocumentNo"));
			docNoTextField.addStyleName("search-field");
			addComponent(docNoTextField);
		}
		
		if (hasName) {
			nameTextField = new TextField();
			nameTextField.setPlaceholder(Msg.getCleanMsg(Env.getCtx(), "Name"));
			nameTextField.addStyleName("search-field");
			addComponent(nameTextField);
		}
		
		if (hasDescription) {
			descriptionTextField = new TextField();
			descriptionTextField.setPlaceholder(Msg.getCleanMsg(Env.getCtx(), "Description"));
			descriptionTextField.addStyleName("search-field");
			addComponent(descriptionTextField);
		}
		
		if (!hasDescription && !hasDocNo && !hasName && !hasValue){
			Label warning = new Label("N/A!");
			addComponent(warning);
		}
		
		HorizontalLayout buttonRow = new HorizontalLayout();
		buttonRow.addStyleName("confirmation-button-row");

		cancelButton = new Button();
		cancelButton.setIcon(VaadinIcons.CLOSE_SMALL);
		cancelButton.addStyleName("cancel-button");
		cancelButton.addClickListener(e -> view.onCancelSearch());
		buttonRow.addComponent(cancelButton);
		
		okButton = new Button();
		okButton.setIcon(VaadinIcons.CHECK);
		okButton.addStyleName("ok-button");
		okButton.addClickListener(e -> onSearch());
		buttonRow.addComponent(okButton);
		
		addComponent(buttonRow);
	}
	
	private void onSearch() {
		
		String value = null;
		String name = null;
		String description = null;
		String docNo = null;
		
		if (valueTextField != null)
			value = valueTextField.getValue();
		if (nameTextField != null)
			name = nameTextField.getValue();
		if (docNoTextField != null)
			docNo = docNoTextField.getValue();
		if (descriptionTextField != null)
			description = descriptionTextField.getValue();
		
		view.onSearch(value, name, description, docNo);
	}
	
	public void reset() {
		
		if (valueTextField != null)
			valueTextField.clear();
		if (nameTextField != null)
			nameTextField.clear();
		if (docNoTextField != null)
			docNoTextField.clear();
		if (descriptionTextField != null)
			descriptionTextField.clear();
		
	}
	
}
