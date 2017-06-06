package com.trekglobal.vaadin.ui;

import java.util.ArrayList;
import java.util.List;

import org.compiere.util.DB;
import org.compiere.util.Util;

import com.trekglobal.vaadin.mobile.MobileLookup;
import com.trekglobal.vaadin.mobile.MobileLookupGenericObject;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class WLookupView  extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6099382690755046614L;

	private IWebFieldView parentView;
	private MobileLookup lookup;
	private Button okButton; 
	private Button cancelButton;
	
	private ArrayList<TextField> searchFields = new ArrayList<>();

	public WLookupView(IWebFieldView parentView, MobileLookup lookup) {

		this.lookup = lookup;
		this.parentView = parentView;

		setTitle();
		initSearchUI();
		setConfirmationButtons();
	}

	private void setTitle() {
		Label title = new Label(lookup.getHeader());
		title.addStyleName("bxprocesspara-title");
		addComponent(title);
	}

	private void setConfirmationButtons() {
		HorizontalLayout buttonRow = new HorizontalLayout();
		buttonRow.addStyleName("confirmation-button-row");

		cancelButton = new Button();
		cancelButton.setIcon(VaadinIcons.CLOSE_SMALL);
		cancelButton.addStyleName("cancel-button");
		cancelButton.addClickListener(e -> parentView.onLookUpCancel());
		buttonRow.addComponent(cancelButton);

		okButton = new Button();
		okButton.setIcon(VaadinIcons.CHECK);
		okButton.addStyleName("ok-button");
		okButton.addClickListener(e -> createLookupResult());
		buttonRow.addComponent(okButton);

		addComponent(buttonRow);
	}

	private void initSearchUI() {
		if (lookup.getSearchFields() != null) {
			for (int i = 0; i < lookup.getSearchFields().length; i++ ) {

				TextField textField = new TextField();
				textField.setId(lookup.getSearchFields()[i]);
				textField.setPlaceholder(lookup.getSearchLabels()[i]);
				textField.addStyleName("search-field");
				addComponent(textField);
				searchFields.add(textField);
			}
		}
	}

	private void createLookupResult() {
		
		//Where clause for the lookup
		StringBuffer where = new StringBuffer();
		for (TextField textField : searchFields) {
			String value = textField.getValue();
			if (!Util.isEmpty(value))  {
				value = "%" + value + "%";
				where.append(" AND UPPER(")
				.append(textField.getId())   //Column name
				.append(") LIKE UPPER(")
				.append(DB.TO_STRING(value))
				.append(") ");
			}
		}
		
		removeAllComponents();

		fillRows(lookup.getLookupRows(where.toString()));		
	}
	
	private void fillRows(List<MobileLookupGenericObject> results) {
		Grid<MobileLookupGenericObject> resultList = new Grid<>();
		resultList.setItems(results);
		resultList.addColumn(MobileLookupGenericObject::getQueryValue).setCaption(lookup.getSearchLabels()[0]);
		
		resultList.addItemClickListener(event -> parentView.onLookUpOK(event.getItem()));
        
		resultList.setSizeFull();
        
        addComponent(resultList);
	}

}
