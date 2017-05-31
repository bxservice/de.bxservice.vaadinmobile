package com.trekglobal.vaadin.ui;

import com.trekglobal.vaadin.mobile.MobileLookup;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
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

	public WLookupView(IWebFieldView parentView, MobileLookup lookup, boolean isSearch) {

		this.lookup = lookup;
		this.parentView = parentView;

		setTitle();

		if (isSearch)
			initSearchUI();
		else 
			initUI();

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
		cancelButton.addClickListener(e -> parentView.onLookUpOK());
		buttonRow.addComponent(cancelButton);

		okButton = new Button();
		okButton.setIcon(VaadinIcons.CHECK);
		okButton.addStyleName("ok-button");
		cancelButton.addClickListener(e -> parentView.onLookUpCancel());
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
			}
		}
	}

	private void initUI() {

		/*StringBuffer where = new StringBuffer();
		for (String column : lookup.getSearchFields()) {
			String value = request.getParameter(column);
			if (!Util.isEmpty(value))  {
				value = "%" + value + "%";
				where.append(" AND UPPER(")
				.append(column)
				.append(") LIKE UPPER(")
				.append(DB.TO_STRING(value))
				.append(") ");
			}
		}

		div panel=new div();
		panel.setClass("dialog");
		panel.addAttribute("selected", "true");
		panel.setID("WLookup2");
		fieldset set = new fieldset();
		panel.addElement(set);
		set.addElement(fillTable(wsc, columnName, refValueId, request.getRequestURI(),targetBase, startUpdate, page, where.toString()));

		//  Reset
		String text = "Reset";
		//if (wsc.ctx != null)
		//	text = Msg.getMsg (wsc.ctx, "Reset");		
		input resetbtn = new input(input.TYPE_RESET, text, "  "+text);		
		resetbtn.setID(text);
		resetbtn.setClass("resetbtn");			

		String script = targetBase + "F.value='';" + targetBase + "D.value='';self.close();";
		if ( startUpdate )
			script += "startUpdate(" + targetBase + "F);";
		resetbtn.setOnClick(script);

		doc.getBody().addElement(panel);				

		doc.getBody()
		.addElement(resetbtn);

		MobileUtil.createResponseFragment (request, response, this, null, doc);*/

	}

}
