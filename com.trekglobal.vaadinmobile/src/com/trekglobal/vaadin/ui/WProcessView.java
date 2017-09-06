/**********************************************************************
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Diego Ruiz - Bx Service GmbH                                      *
 * Sponsored by:                                                       *
 * - TrekGlobal                                                        *
 **********************************************************************/
package com.trekglobal.vaadin.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.compiere.model.GridTab;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.util.CLogger;
import org.compiere.util.Msg;

import com.trekglobal.vaadin.mobile.MobileProcess;
import com.trekglobal.vaadin.ui.OnDemandFileDownloader.OnDemandStreamResource;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class WProcessView extends AbstractWebFieldView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -595318565371137073L;
	public static final String NAME = "WProcess";

	/**	Logger			*/
	protected static CLogger	log = CLogger.getCLogger(WProcessView.class);
	private ArrayList<WebField> processWebFields = new ArrayList<WebField>();
	
	private MProcess process;
	private MobileProcess mProcess;
	
	private Button submitButton;
	
	public WProcessView(WNavigatorUI loginPage, int AD_Menu_ID) {
		super(loginPage);
    	syncCtx();
		process = MProcess.getFromMenu(ctx, AD_Menu_ID);
		mProcess = new MobileProcess(process);
		
		if (process == null) {
			Notification.show("Process not found",
					Type.ERROR_MESSAGE);
			return;
		}
	}
	
	protected void initComponents() {

		windowTitle = process.get_Translation("Name");
		header = new WHeader(this, true, true);
		content = new CssLayout();
		content.addStyleName("bxprocess-content");
		
		if (process.isWorkflow()) {
			//Modified by Rob Klein 7/01/07
			/*if (mTab == null)
			{
				doc = MobileDoc.createWindow("No Tab found");
				return doc;
			}	
			
			//	Pop up Document Action (Workflow)
			if (columnName.toString().equals("DocAction"))			{
				
				readReference();			
								
				option[] Options = dynInit( windowID, tableID, recordID,
						 columnName,  mTab);				
				
				fieldset center = new fieldset();
				
				WebField wField = new WebField (wsc,
						columnName, columnName, columnName,
						//	no display length
						17, 22, 22, false,
						// 	not r/o, ., not error, not dependent
						false, false, false, false, false, processId,
						0, 0, 0, 0, null, null, null, null, null);
				
				if (process.get_Translation("Description") != null)
					center.addElement(new p(new i(process.get_Translation("Description"))));
				if (process.get_Translation("Help") != null)
					center.addElement(new p(process.get_Translation("Help"), AlignType.LEFT));
				form myForm = new form ("WProcess")
					.setName("process" + process.getAD_Process_ID());

				myForm.setTarget("WProcess");
				myForm.setMethod("GET");
				//myForm.setOnSubmit("this.Submit.disabled=true;return true;");
				myForm.addElement(new input(input.TYPE_HIDDEN, "AD_Process_ID", process.getAD_Process_ID()));
				myForm.addElement(new input(input.TYPE_HIDDEN, "AD_Window_ID", windowID));
				myForm.addElement(new input(input.TYPE_HIDDEN, "AD_Table_ID", tableID));
				myForm.addElement(new input(input.TYPE_HIDDEN, "AD_Record_ID", recordID));
				table myTable = new table("0", "0", "5", "100%", null);
				myTable.setID("WProcessParameter");
				
				myTable.addElement(new tr()
				.addElement(wField.getLabel(true))
				.addElement(createSelectField(columnName,Options)));
				

				// Reset
				String text = "Reset";
				if (ctx != null)
					text = Msg.getMsg (ctx, "Reset");		
				input restbtn = new input(input.TYPE_RESET, text, "  "+text);		
				restbtn.setID(text);
				restbtn.setClass("resetbtn");	
				
				//	Submit
				 text = "Submit";
				if (ctx != null)
					text = Msg.getMsg (ctx, "submit");		
				input submitbtn = new input(input.TYPE_SUBMIT, text, "  "+text);		
				submitbtn.setID(text);
				submitbtn.setClass("submitbtn");
				
				myTable.addElement(new tr()
					.addElement(new td(null, AlignType.RIGHT, AlignType.MIDDLE, false, 
							restbtn))
					.addElement(new td(null, AlignType.LEFT, AlignType.MIDDLE, false, 
							submitbtn ))
					.addElement(new td(null, AlignType.RIGHT, AlignType.MIDDLE, false, 
							null)));
				myForm.addElement(myTable);
				center.addElement(myForm);	
				
			}	//	DocAction
			*/
		} else {		
			
		
			if (process.get_Translation("Description") != null) {
				Label descriptionText = new Label(process.get_Translation("Description"));
				descriptionText.addStyleName("bxprocess-desc");
				content.addComponent(descriptionText);
			}
			if (process.get_Translation("Help") != null) {
				Label helpText = new Label(process.get_Translation("Help"),
					    ContentMode.HTML);
				helpText.addStyleName("bxprocess-help");
				content.addComponent(helpText);
			}

			//If the Process is not a Report, show the Menu Button
			if (process.getJasperReport() != null || process.isReport());
				//myForm.setTarget("_self");

			CssLayout singleRowSection = new CssLayout();
			singleRowSection.addStyleName("singlerow-content");
			
			for (MProcessPara para : process.getParameters()) {
				WebField wField = new WebField(this, ctx, para.getColumnName(), 
						para.get_Translation("Name"), para.get_Translation("Description"),
						para.getAD_Reference_ID(), para.getFieldLength(), para.getFieldLength(),
						para.isMandatory(), para.getAD_Process_ID(),0,0,0, null);

				//Get the default Value of the process
				Object defaultValue = wField.getDefault(para.getDefaultValue());
				
				HorizontalLayout row = new HorizontalLayout();

				Label fieldLabel = wField.getLabel(true);
				fieldLabel.addStyleName("bxwindow-label");

				Component fieldValue = wField.getField(para.getLookup(), defaultValue);
				fieldValue.addStyleName("bxwindow-field");

				row.addComponents(fieldLabel, fieldValue);
				row.addStyleName("singlerow");
				row.addStyleName("process-para-row");
				
				singleRowSection.addComponent(row);
				processWebFields.add(wField);

				if (para.isRange()) {
					WebField wFieldforRange = new WebField(this, ctx, para.getColumnName(), 
							para.getName(), para.getDescription(), para.getAD_Reference_ID(), 
							para.getFieldLength(),para.getFieldLength(), para.isMandatory(), 
							para.getAD_Process_ID(),0,0,0, para.getColumnName()+"_2");

					Object defaultValueTo = wFieldforRange.getDefault(para.getDefaultValue2());
					HorizontalLayout rangeRow = new HorizontalLayout();

					Label toFieldLabel = wFieldforRange.getLabel(true);
					toFieldLabel.addStyleName("bxwindow-label");

					Component toFieldValue = wFieldforRange.getField(para.getLookup(), defaultValueTo);
					toFieldValue.addStyleName("bxwindow-field");

					rangeRow.addComponents(toFieldLabel, toFieldValue);
					rangeRow.addStyleName("singlerow");
					
					singleRowSection.addComponent(rangeRow);
					processWebFields.add(wFieldforRange);
				}
			} //for parameters

			//	Submit
			String text = "Submit";
			if (ctx != null)
				text = Msg.getMsg(ctx, "submit");

			submitButton = new Button(text);
			submitButton.addStyleName("bx-loginbutton");
			
			if (process.isReport()) {
				OnDemandStreamResource myResource = createOnDemandResource();
				OnDemandFileDownloader fileDownloader = new OnDemandFileDownloader(myResource);
				fileDownloader.extend(submitButton);
			} else
				submitButton.addClickListener(e -> runProcess());

			content.addComponents(singleRowSection, submitButton);
		}
	}
	
	private void runProcess() {
		HashMap<String, String> parameters = new HashMap<String, String>();

		//  loop through parameters
		for (WebField webField : processWebFields) {
			parameters.put(webField.getProcessParaKey(), webField.getNewValue());
		}   //  for all parameters

		if (process.getJasperReport() != null && processWebFields.size() == 0) {
			//parameters.put("AD_Record_ID", String.valueOf(curTab.getRecord_ID()));
		}

		try {
			String message = mProcess.runProcess(0, 0, 0, parameters);
			
			Type messageType = Type.HUMANIZED_MESSAGE;
			if (!mProcess.getProcessOK())
				messageType = Type.ERROR_MESSAGE;

			Notification.show(message, messageType);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	protected void init() {
		header.setBackButton();
		header.setHomeButton();

		createUI();
	}

	private void createUI() {
		setWidth("100%");

		// Enable Responsive CSS selectors for the component
		Responsive.makeResponsive(this);
		Responsive.makeResponsive(content);
		content.setSizeFull();

		addComponents(header, content);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		initView();
	}

	@Override
	public void onLeftButtonPressed() {
		backButton();
	}

	@Override
	public void onRightButtonPressed() {
		openMenu();
	}

	@Override
	public void onLocation(WebField webField) {
	}

	@Override
	public GridTab getCurTab() {
		return null;
	}

	@Override
	public void onChange(WebField webField) {
	}

	private OnDemandStreamResource createOnDemandResource() {
		return new OnDemandStreamResource() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -9220282386521371833L;

			public InputStream getStream() {
				runProcess();
				try {
					return new FileInputStream(mProcess.getPdfFile());
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
					return null;
				}
			}

			@Override
			public String getFilename() {
				return mProcess.getFileName() + ".pdf";
			}
		};
	}

}
