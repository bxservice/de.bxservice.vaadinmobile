/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package com.trekglobal.vaadin.ui;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.Lookup;
import org.compiere.model.MLocator;
import org.compiere.model.MRole;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.NamePair;
import org.compiere.util.Util;
import org.compiere.util.ValueNamePair;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Focusable;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 *	Web Field.
 *  Based on WebField of iuiMobile by Jorg Janke
 *
 *  @author Diego Ruiz - Bx Service GmbH
 */
public class WebField {
	protected static CLogger	log = CLogger.getCLogger(WWindowView.class);


	/**	CSS Field Mandatory Class				*/
	public static final String C_MANDATORY = "Cmandatory";
	/**	CSS Field Error Class					*/
	public static final String C_ERROR     = "Cerror";

	private IWebFieldListener fieldListener;
	private Properties ctx;
	private String 	m_columnName;
	private String 	m_processParaKey;
	private String	m_name;
	private String	m_description;
	private GridField m_Field;
	private String m_SuffixTo = "";
	//
	private Object	m_defaultObject;
	private int		m_displayType;
	private int		m_processID;
	private int		m_fieldLength;
	private int		m_displayLength;
	//
	private boolean	m_readOnly;
	private boolean	m_mandatory;
	private boolean	m_error;
	private boolean	m_hasDependents;
	private boolean	m_hasCallout;
	
	private Component componentField;
	private String oldValue;
	private String newValue;

	private Lookup m_lookup;

	/**
	 * 	Web Field
	 *	@param wsc session context
	 *	@param columnName column
	 *	@param name label
	 *	@param description description
	 *	@param displayType display type
	 *	@param fieldLength field length
	 *	@param displayLength optional display length
	 *	@param longField if true spans 3 columns
	 *	@param readOnly read only
	 *	@param mandatory mandatory
	 *	@param error error status
	 *	@param hasDependents has dependent fields
	 *	@param hasCallout has callout functions
	 */
	public WebField (IWebFieldListener fieldListener, Properties ctx, String columnName, String name, String description,
			int displayType, int fieldLength, int displayLength, boolean longField, 
			boolean readOnly, boolean mandatory, boolean error, 
			boolean hasDependents, boolean hasCallout, int AD_Process_ID,
			int AD_Window_ID, int AD_Record_ID, int AD_Table_ID, Object defaultvalue, 
			String callOut, GridTab mTab, GridField mField, MRole mRole, boolean rangeTo) {
		super();
		this.ctx = ctx;
		this.fieldListener = fieldListener;
		m_columnName = columnName;		
		if (rangeTo)
			m_columnName += "_2";
		if (name == null || name.length() == 0)
			m_name = columnName;
		else
			m_name = name;
		if (rangeTo)
			m_name += " - " + Msg.getMsg(ctx, "To");
		if (description != null && description.length() > 0)
			m_description = description;
		//
		m_defaultObject = defaultvalue; 
		m_displayType = displayType;
		m_processID = AD_Process_ID;
		m_fieldLength = fieldLength;
		m_displayLength = displayLength;
		if (m_displayLength <= 22)
			m_displayLength = 22;	//	default length
		else
			m_displayLength = 44;	//	default length
		//
		m_readOnly = readOnly;
		m_mandatory = mandatory;
		m_error = error;
		m_hasDependents = hasDependents;
		m_hasCallout = hasCallout;
		m_Field = mField;
		if (rangeTo)
			m_SuffixTo = "T";
		//

	}	//	WebField

	public WebField(IWebFieldListener fieldListener, Properties ctx, String columnName, String name, String description,
			int displayType, int fieldLength, int displayLength, boolean longField, 
			boolean readOnly, boolean mandatory, boolean error, 
			boolean hasDependents, boolean hasCallout, int AD_Process_ID,
			int AD_Window_ID, int AD_Record_ID, int AD_Table_ID, Object defaultvalue, 
			String callOut, GridTab mTab, GridField mField, MRole mRole) {
		this(fieldListener, ctx, columnName, name, description,
				displayType, fieldLength, displayLength, longField, 
				readOnly, mandatory, error, 
				hasDependents, hasCallout, AD_Process_ID,
				AD_Window_ID, AD_Record_ID, AD_Table_ID, defaultvalue, 
				callOut, mTab, mField, mRole, false);
	}
	
	/**
	 * Constructor to be called for process parameters 
	 */
	public WebField(IWebFieldListener fieldListener, Properties ctx, String columnName, String name, String description,
			int displayType, int fieldLength, int displayLength, boolean mandatory, 
			int AD_Process_ID, int AD_Window_ID, int AD_Record_ID, int AD_Table_ID, String key) {
		this(fieldListener, ctx, columnName, name, description,
				displayType, fieldLength, displayLength, false, 
				false, mandatory, false, 
				false, false, AD_Process_ID,
				AD_Window_ID, AD_Record_ID, AD_Table_ID, null, 
				null, null, null, null, false);
		
		if (key == null)
			m_processParaKey = columnName;
		else
			m_processParaKey = key;
	}

	/**
	 * 	Get the field Label
	 *	@return label
	 */
	public Label getLabel(boolean edit) {
		if (m_displayType == DisplayType.Button)
			return new Label();
		//
		Label myLabel = new Label(m_name);
		myLabel.setId(m_columnName + "L" + m_SuffixTo);
		if (m_description != null)
			myLabel.setDescription(m_description);
		if (edit && m_readOnly)
			myLabel.addStyleName("readonly");
		else if (edit && m_mandatory)
			myLabel.addStyleName("mandatory");

		return myLabel;
	}	//	getLabel

	/**
	 * 	Get Field
	 *	@param lookup lookup
	 *	@param data data
	 *	@return field
	 */
	public Component getField(Lookup lookup, Object data) {
		m_lookup=lookup;
		String dataValue = (data == null) ? "" : data.toString();
		oldValue = dataValue;

		if (m_displayType == DisplayType.Search
				|| m_displayType == DisplayType.Location
				|| m_displayType == DisplayType.Account
				|| m_displayType == DisplayType.PAttribute) {

			if (m_readOnly) {
				componentField = getDiv(lookup.getDisplay(data));
			} else {
				String dataDisplay = "";
				if (lookup != null && data != null)
					dataDisplay = lookup.getDisplay(data);
				
				componentField = getPopupField(dataDisplay, dataValue);
			}
		}
		else  if (DisplayType.isLookup(m_displayType) 
				|| m_displayType == DisplayType.Locator 
				|| m_displayType == DisplayType.Payment	) {	

			if (m_readOnly) {
				componentField = getDiv(lookup.getDisplay(data));
			} else
				componentField = getSelectField(lookup, dataValue);

		}
		else if (m_displayType == DisplayType.YesNo) {			
			componentField = getCheckField (dataValue);
		}
		else if (m_displayType == DisplayType.Button) {
			componentField = getButtonField ();
		}
		else if (DisplayType.isDate(m_displayType)) {
			componentField = getPopupDateField(data);
		}
		else if (DisplayType.isNumeric(m_displayType)) {
			componentField = getNumberField(data);
		}
		else if (m_displayType == DisplayType.Text || m_displayType == DisplayType.TextLong) {
			if (m_readOnly) 
				componentField = getDiv(dataValue);
			else
				componentField = getTextField(dataValue, 10);
		}
		else if (m_displayType == DisplayType.Memo) {

			if (m_readOnly) 
				componentField = getDiv(dataValue);
			else
				componentField = getTextField(dataValue, 10);
		}
		else if (m_displayType == DisplayType.Assignment)
			return getAssignmentField(data);
		
		if (componentField == null)
			componentField = getStringField(dataValue);
		
		return componentField;
	}	//	getField

	private Component getDiv(String data) {

		if (m_columnName.toLowerCase().contains("phone") && data != null) {
			Link link = new Link();
			link.setResource(new ExternalResource("tel:"+data));
			link.setCaption(data);
			link.setTargetName("_self");
			return link;
		}
		else if (m_columnName.toLowerCase().contains("email")&& data != null) {
			Link link = new Link();
			link.setResource(new ExternalResource("mailto:"+data));
			link.setCaption(data);
			link.setTargetName("_self");
			return link;
		}
		else if (m_displayType == DisplayType.URL && data != null) {
			Link link = new Link();
			link.setResource(new ExternalResource(data));
			link.setCaption(data);
			link.setTargetName("_self");
			return link;
		}
		else if (m_displayType == DisplayType.Location && data != null) {
			try {
				String map = "geo:0,0?q="
						+ URLEncoder.encode(data,"UTF-8");

				Link link = new Link();
				link.setResource(new ExternalResource(map));
				link.setCaption(data);
				link.setTargetName("_self");
				return link;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Label d = new Label(data);
				d.setStyleName("fieldValue");
				return d;
			}
		}
		else {
			Label d = new Label(data);
			d.setStyleName("fieldValue");
			return d;
		}
	}

	/**
	 * 	Create String Field
	 * 	@param data initial value
	 *	@return td
	 */
	private Component getStringField(String data) {

		Boolean isEncrypted = false;

		if (m_Field != null)
			isEncrypted = m_Field.isEncryptedField();

		if (m_readOnly && !isEncrypted)
			return getDiv(data);

		TextField string = null;

		if (isEncrypted)
			string = new PasswordField(null, data);
		else
			string = new TextField(null, data);


		string.setId(m_columnName + "F" + m_SuffixTo);

		if (m_fieldLength > 0)
			string.setMaxLength(m_fieldLength);
		//
		string.setEnabled(!m_readOnly);

		if (m_error)
			string.setStyleName(C_ERROR);
		else if (m_mandatory) {
			string.setStyleName(C_MANDATORY);
			//string.addAttribute("required", "");
		}
		//
		/*if (m_hasDependents || m_hasCallout)
			string.addValueChangeListener(null);*/

		return string;
	}	//	getStringField

	/**
	 * 	Create Text Field
	 * 	@param data initial value
	 * 	@param rows no of rows
	 *	@return td
	 */
	private Component getTextField(String data, int rows) {

		if (m_readOnly)
			return getDiv(data);

		AbstractTextField text;
		if (rows == 1) {
			text = new TextField();
			((TextField) text).setValue(data);
		} else {
			text = new TextArea();
			((TextArea) text).setWordWrap(true);
			((TextArea) text).setValue(data);
		}

		text.setId(m_columnName + "F" + m_SuffixTo);
		text.setEnabled(!m_readOnly);
		if (m_error)
			text.setStyleName(C_ERROR);
		else if (m_mandatory)
			text.setStyleName(C_MANDATORY);
		if (m_hasDependents || m_hasCallout)
			text.addValueChangeListener(event -> fieldListener.onChange(this));

		return text;
	}	//	getTextField

	/**
	 * 	Create Assignment Field
	 * 	@param data initial value
	 *	@return td
	 */
	private Component getAssignmentField(Object data) {

		TextField string = new TextField(null,"Not Yet Supported");

		if (m_fieldLength > 0)
			string.setMaxLength(m_fieldLength);
		//
		string.setEnabled(false);

		if (m_error)
			string.setStyleName(C_ERROR);
		else if (m_mandatory)
			string.setStyleName(C_MANDATORY);
		if (m_hasDependents || m_hasCallout)
			string.addValueChangeListener(event -> fieldListener.onChange(this));
		
		return string;
	}

	/**
	 * 	Create Number Field
	 * 	@param data initial value
	 *	@return td
	 */
	private Component getNumberField(Object data) {

		String formattedData = "";		
		//Modified by Rob Klein 4/29/07
		if (data == null)
			if (m_displayType == DisplayType.Amount	)
				formattedData = DisplayType.getNumberFormat(DisplayType.Amount, Env.getLanguage(ctx)).format(0.00);
			else if (m_displayType == DisplayType.Number
					|| m_displayType == DisplayType.CostPrice )
				formattedData = DisplayType.getNumberFormat(DisplayType.Number, Env.getLanguage(ctx)).format(0.00);
			else if (m_displayType == DisplayType.Integer)
				formattedData = DisplayType.getNumberFormat(DisplayType.Integer, Env.getLanguage(ctx)).format(0);
			else
				formattedData = "0";

		else if (m_displayType == DisplayType.Quantity)
			formattedData = DisplayType.getNumberFormat(DisplayType.Quantity, Env.getLanguage(ctx)).format(data);
		else if (m_displayType == DisplayType.Integer)
			formattedData = DisplayType.getNumberFormat(DisplayType.Integer, Env.getLanguage(ctx)).format(data);
		else
			formattedData = data.toString();

		if (m_readOnly)
			return getDiv(formattedData);


		TextField string = new TextField(null, formattedData);

		string.setId(m_columnName + "F" + m_SuffixTo);

		if (m_fieldLength > 0)
			string.setMaxLength(m_fieldLength);

		string.setEnabled(!m_readOnly);

		if (m_error)
			string.setStyleName(C_ERROR);
		else if (m_mandatory)
			string.setStyleName(C_MANDATORY);

		if (m_hasDependents || m_hasCallout)
			string.addValueChangeListener(event -> fieldListener.onChange(this));
	
		return string;
	}	//	getNumberField


	/**
	 * 	Create Checkbox Field
	 * 	@param data initial value
	 *	@return td
	 */
	private Component getCheckField(String data) {

		boolean check = data != null 
				&& (data.equals("true") || data.equals("Y"));
		//
		CheckBox cb = new CheckBox();
		cb.setValue(check);
		cb.setId(m_columnName + "F" + m_SuffixTo);
		cb.setEnabled(!m_readOnly);
		if (m_error)
			cb.setStyleName(C_ERROR);

		if (m_hasDependents || m_hasCallout)
			cb.addValueChangeListener(event -> fieldListener.onChange(this));
		//
		return cb;
	}	//	getCheckField

	/**
	 * 	Get Popup Field (lookup, location, account, ..)
	 *	@param dataDisplay data to be displayed
	 *	@param dataValue data of value field
	 *	@return td
	 */
	private Component getPopupField(String dataDisplay, String dataValue) {
		
		if ( m_readOnly )
			return getDiv(dataDisplay);
		
		if ( Util.isEmpty(dataDisplay))
			dataDisplay = Msg.getCleanMsg(ctx, "select")+ "...";
		
		Button display = new Button(dataDisplay);
		display.addStyleName("bxpopup-button");
		
		if (m_displayType == DisplayType.Location ){
			display.addClickListener(e -> fieldListener.onLocationLookUp(this));
		}else 
			display.addClickListener(e -> fieldListener.onLookUp(this));

		display.setId(m_columnName + "D" + m_SuffixTo);
		
		if (m_error)
			display.addStyleName(C_ERROR);
		else if (m_mandatory)
			display.addStyleName(C_MANDATORY);
		
		return display;
	}	//	getPopupField


	/**
	 * 	Get Popup Field (lookup, location, account, ..)
	 *	@param dataDisplay data to be displayed
	 *	@param dataValue data of value field
	 *	@return td
	 */
	private Component getPopupDateField(Object data) {

		String dataValue = (data == null) ? "" : data.toString();
		String formattedData = "";
		SimpleDateFormat dateFormat = DisplayType.getDateFormat(m_displayType, Env.getLanguage(ctx));
		
		try {
			if (data == null)
				;
			else if (dataValue.equals("@#Date@"))
				formattedData = dateFormat.format(new java.util.Date());
			else
				formattedData = dateFormat.format(data);
		} catch (IllegalArgumentException e) {
			// invalid date format
			formattedData = "Invalid date format: " + data;
		}

		if (m_readOnly)
			return getDiv(formattedData);

		DateField display = new DateField();
		if (data != null)
			display.setValue(((Timestamp) data).toLocalDateTime().toLocalDate());
		display.setDateFormat(dateFormat.toPattern());
		display.setId(m_columnName + "F" + m_SuffixTo);
		display.setReadOnly(m_readOnly);

		//
		if (m_error)
			display.setStyleName(C_ERROR);
		else if (m_mandatory)
			display.setStyleName(C_MANDATORY);
		//
		if (m_hasDependents || m_hasCallout)
			display.addValueChangeListener(event -> fieldListener.onChange(this));
		//
		return display;
	}	//	getPopupDateField

	/**
	 * 	Get Select Field
	 *	@param lookup lookup
	 *	@param dataValue default value
	 *	@return selction td
	 */
	private Component getSelectField(Lookup lookup, String dataValue) {		

		if (dataValue.length()<1 && m_defaultObject != null)	{
			dataValue = m_defaultObject.toString();
		}

		String dataDisplay = "";
		if (lookup != null && dataValue != null && dataValue.length() > 0)
			dataDisplay = lookup.getDisplay(dataValue);

		if (m_readOnly)
			return getDiv(dataDisplay);

		NativeSelect<?> ops = getOptions(lookup, dataValue);
		ops.setId(m_columnName + m_SuffixTo);
		ops.setEnabled(!m_readOnly);

		if (m_error)
			ops.setStyleName(C_ERROR);
		else if (m_mandatory)
			ops.setStyleName(C_MANDATORY);

		if (m_hasDependents || m_hasCallout)
			ops.addValueChangeListener(event -> fieldListener.onChange(this));

		return ops; 	
	}	//	getSelectField

	/**
	 * 	Get Array of options
	 *	@param lookup lookup
	 *	@param dataValue default value
	 *	@return selction td
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private NativeSelect getOptions(Lookup lookup, String dataValue) {
		if (lookup == null)
			return new NativeSelect<KeyNamePair>(null);

		NamePair value = null;
		Object[] list = lookup.getData(m_mandatory, true, !m_readOnly, false, false)
				.toArray();    //  if r/o also inactive
		int size = list.length;
		List data = new ArrayList<>();
		Object selectedObject = null;

		if (size == 0 && dataValue.length() > 0) {
			value = lookup.getDirect(dataValue, false, false);		
			if (value != null) {
				data.add(new KeyNamePair(-1, value.getName()));
				data.add(new KeyNamePair(Integer.parseInt(value.getID()), value.getName()));
			}
		} else {
			for (int i = 0; i < size; i++) {
				boolean isNumber = list[0] instanceof KeyNamePair;
				int key = -1;
				if (m_displayType == DisplayType.Locator) {
					MLocator loc = (MLocator)list[i];
					key = loc.getM_Locator_ID();
					String name = loc.getValue();
					data.add(new KeyNamePair(key, name));
					if (dataValue.equals(String.valueOf(key))) {
						selectedObject = data.get(data.size()-1);
					}				
				} else if (isNumber) {
					KeyNamePair p = (KeyNamePair)list[i];
					key = p.getKey();
					String name = p.getName();
					data.add(new KeyNamePair(key, name));
					if (dataValue.equals(String.valueOf(key))) {
						selectedObject = data.get(data.size()-1);
					}
				} else {
					ValueNamePair p = (ValueNamePair)list[i];

					String key2 = p.getValue();
					if (key2 == null || key2.length() == 0)
						key2 = "";
					String name = p.getName();
					if (name == null || name.length() == 0)
						name = "";
					data.add(new ValueNamePair(key2, name));
					if (dataValue.equals(String.valueOf(key2))) {
						selectedObject = data.get(data.size()-1);
					}
				}
			}			
		}

		NativeSelect options = new NativeSelect(null, data);
		options.setEmptySelectionAllowed(false);
		
		if (selectedObject != null)
			options.setSelectedItem(selectedObject);
		else if (!data.isEmpty())
			options.setSelectedItem(data.get(0));

		return options;
	}	//	getOptions


	/**
	 * 	Get Button Field
	 *	@return Button 
	 */
	private Component getButtonField() {
		/*
		//Modified by Rob Klein 4/29/07
		a button = new a("#", StringEscapeUtils.escapeHtml(m_name));
		button.setClass("whiteButton");		
		return button;*/
		return null;
	}	//	getButtonField


	/**
	 * 	Get Popup Menu
	 *	@return Menu String 
	 */
	/*	private String getpopUpMenu() {
		//Start Popup Menu
		//Add by Rob Klein 6/6/2007
		a buttonZoom = null;
		a buttonValuePref = null;		
		String menu = null;
		Boolean tableAccess =false;	

		if(m_dataDisplay!=null){
			//	Set ValuePreference
			//	Add by Rob Klein 6/6/2007
			buttonValuePref = new a("#", (new img(MobileEnv.getImageDirectory("vPreference10.gif")).setBorder(0))+"  Preference");
			buttonValuePref.setID(m_columnName + "PV" + m_SuffixTo);			
			buttonValuePref.setOnClick("startValuePref(" + m_displayType + ", '"+StringEscapeUtils.escapeHtml(m_dataDisplay.toString())+ "', '"
					+ m_Field.getValue()+ "', '"+m_Field.getHeader()+ "', '"+m_Field.getColumnName()+ "', "
					+ Env.getAD_User_ID(m_wsc.ctx)+ ", " + Env.getAD_Org_ID(m_wsc.ctx) + ", "+Env.getAD_Client_ID(m_wsc.ctx)
					+ ", "+m_Field.getAD_Window_ID()+");return false;");
			menu = ""+buttonValuePref+" \n";	
		}

		//Set Zoom			
		StringBuffer sql = null;
		int refID = m_Field.getAD_Reference_Value_ID();
		Object recordID =0;
		if (m_displayType == DisplayType.List ){
			sql = new StringBuffer ("SELECT AD_Table_ID " 
					+ "FROM AD_Table WHERE TableName = 'AD_Reference'");				

			recordID = refID;
		}
		else if (refID > 0){
			sql = new StringBuffer ("SELECT AD_Table_ID " 
					+ "FROM AD_Ref_Table WHERE AD_Reference_ID = "+refID);
			recordID =m_Field.getValue();
		}
		else{	
			sql = new StringBuffer ("SELECT AD_Table_ID " 
					+ "FROM AD_Table WHERE TableName = '"+m_columnName.replace("_ID", "")+"'");
			recordID =m_Field.getValue();
		}
		int tableID = DB.getSQLValue(null, sql.toString());		

		tableAccess = m_Role.isTableAccess(tableID, false);
		if(tableAccess==true){			
			buttonZoom = new a("#", (new img(MobileEnv.getImageDirectory("Zoom10.gif")).setBorder(0))+"  Zoom");
			buttonZoom.setID(m_columnName + "Z" + m_SuffixTo);			
			buttonZoom.setOnClick("startZoom(" + tableID + ", "+recordID+");return false;");
			if(m_dataDisplay!=null) 
				menu = menu + ""+buttonZoom+"\n";
			else
				menu = ""+buttonZoom+"\n";

		}
		return menu;
	}	//	getpopUpMenu */

	/**************************************************************************
	 *	Create default value.
	 *  <pre>
	 *		(a) Key/Parent/IsActive/SystemAccess
	 *      (b) SQL Default
	 *		(c) Column Default		//	system integrity
	 *      (d) User Preference
	 *		(e) System Preference
	 *		(f) DataType Defaults
	 *
	 *  Don't default from Context => use explicit defaultValue
	 *  (would otherwise copy previous record)
	 *  </pre>
	 *  @return default value or null
	 */
	public Object getDefault(String defaultValue) {
		/**
		 *  (a) Key/Parent/IsActive/SystemAccess
		 */

		//	No defaults for these fields m_vo.IsKey ||
		if ( m_displayType == DisplayType.RowID 
				|| DisplayType.isLOB(m_displayType)
				|| "Created".equals(m_columnName) // for Created/Updated default is managed on PO, and direct inserts on DB
				|| "Updated".equals(m_columnName))
			return null;
		//	Always Active
		if (m_columnName.equals("IsActive"))
		{
			if (log.isLoggable(Level.FINE)) log.fine("[IsActive] " + m_columnName + "=Y");
			return "Y";
		}

		/**
		 *  (b) SQL Statement (for data integity & consistency)
		 */
		String	defStr = "";
		if (defaultValue != null && defaultValue.startsWith("@SQL="))
		{
			String sql = defaultValue.substring(5);	
			if (sql.equals(""))
			{
				log.log(Level.WARNING, "(" + m_columnName + ") - Default SQL variable parse failed: "
						+ defaultValue);
			}
			else
			{
				PreparedStatement stmt = null;
				ResultSet rs = null;
				try
				{
					stmt = DB.prepareStatement(sql, null);
					rs = stmt.executeQuery();
					if (rs.next())
						defStr = rs.getString(1);
					else
					{
						if (log.isLoggable(Level.INFO))
							log.log(Level.INFO, "(" + m_columnName + ") - no Result: " + sql);
					}
				}
				catch (SQLException e)
				{
					log.log(Level.WARNING, "(" + m_columnName + ") " + sql, e);
				}
				finally
				{
					DB.close(rs, stmt);
					rs = null;
					stmt = null;
				}
			}
			if (defStr != null && defStr.length() > 0)
			{
				if (log.isLoggable(Level.FINE)) log.fine("[SQL] " + m_columnName + "=" + defStr);
				return createDefault(defStr);
			}
		}	//	SQL Statement

		/**
		 * 	(c) Field DefaultValue		=== similar code in AStartRPDialog.getDefault ===
		 */
		if (defaultValue != null && !defaultValue.equals("") && !defaultValue.startsWith("@SQL="))
		{
			defStr = "";		//	problem is with texts like 'sss;sss'
			//	It is one or more variables/constants
			StringTokenizer st = new StringTokenizer(defaultValue, ",;", false);
			while (st.hasMoreTokens())
			{
				defStr = st.nextToken().trim();
				if (defStr.equals("@SysDate@"))				//	System Time
					return new Timestamp (System.currentTimeMillis());
				else if (defStr.indexOf("'") != -1)			//	it is a 'String'
					defStr = defStr.replace('\'', ' ').trim();

				if (!defStr.equals(""))
				{
					if (log.isLoggable(Level.FINE)) log.fine("[DefaultValue] " + m_columnName + "=" + defStr);
					return createDefault(defStr);
				}
			}	//	while more Tokens
		}	//	Default value
		/**
		 *	(f) DataType defaults
		 */

		//	Button to N
		if (m_displayType == DisplayType.Button && !m_columnName.endsWith("_ID"))
		{
			if (log.isLoggable(Level.FINE)) log.fine("[Button=N] " + m_columnName);
			return "N";
		}
		//	CheckBoxes default to No
		if (m_displayType == DisplayType.YesNo)
		{
			if (log.isLoggable(Level.FINE)) log.fine("[YesNo=N] " + m_columnName);
			return "N";
		}
		//  lookups with one value
		//	if (DisplayType.isLookup(m_displayType) && m_lookup.getSize() == 1)
		//	{
		//		/** @todo default if only one lookup value */
		//	}
		//  IDs remain null
		if (m_columnName.endsWith("_ID"))
		{
			if (log.isLoggable(Level.FINE)) log.fine("[ID=null] "  + m_columnName);
			return null;
		}
		//  actual Numbers default to zero
		if (DisplayType.isNumeric(m_displayType))
		{
			if (log.isLoggable(Level.FINE)) log.fine("[Number=0] " + m_columnName);
			return createDefault("0");
		}

		/**
		 *  No resolution
		 */
		if (log.isLoggable(Level.FINE)) log.fine("[NONE] " + m_columnName);
		return null;
	}	//	getDefault

	/**
	 *	Create Default Object type.
	 *  <pre>
	 *		Integer 	(IDs, Integer)
	 *		BigDecimal 	(Numbers)
	 *		Timestamp	(Dates)
	 *		Boolean		(YesNo)
	 *		default: String
	 *  </pre>
	 *  @param value string
	 *  @return type dependent converted object
	 */
	private Object createDefault(String value) {
		//	true NULL
		if (value == null || value.toString().length() == 0)
			return null;
		//	see also MTable.readData
		try
		{
			//	IDs & Integer & CreatedBy/UpdatedBy
			if (m_columnName.endsWith("atedBy")
					|| (m_columnName.endsWith("_ID") && DisplayType.isID(m_displayType))) // teo_sarca [ 1672725 ] Process parameter that ends with _ID but is boolean
			{
				try	//	defaults -1 => null
				{
					int ii = Integer.parseInt(value);
					if (ii < 0)
						return null;
					return new Integer(ii);
				}
				catch (Exception e)
				{
					log.warning("Cannot parse: " + value + " - " + e.getMessage());
				}
				return new Integer(0);
			}
			//	Integer
			if (m_displayType == DisplayType.Integer)
				return new Integer(value);

			//	Number
			if (DisplayType.isNumeric(m_displayType))
				return new BigDecimal(value);

			//	Timestamps
			if (DisplayType.isDate(m_displayType))
			{
				// try timestamp format - then date format -- [ 1950305 ]
				java.util.Date date = null;
				SimpleDateFormat dateTimeFormat = DisplayType.getTimestampFormat_Default();
				SimpleDateFormat dateFormat = DisplayType.getDateFormat_JDBC();
				SimpleDateFormat timeFormat = DisplayType.getTimeFormat_Default();
				try {
					if (m_displayType == DisplayType.Date) {
						date = dateFormat.parse (value);
					} else if (m_displayType == DisplayType.Time) {
						date = timeFormat.parse (value);
					} else {
						date = dateTimeFormat.parse (value);
					}
				} catch (java.text.ParseException e) {
					date = DisplayType.getDateFormat_JDBC().parse (value);
				}
				return new Timestamp (date.getTime());
			}

			//	Boolean
			if (m_displayType == DisplayType.YesNo)
				return Boolean.valueOf ("Y".equals(value));

			//	Default
			return value;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, m_columnName + " - " + e.getMessage());
		}
		return null;
	}	//	createDefault

	public String getColumnName() {
		return m_columnName;
	}
	
	public int getProcessID() {
		return m_processID;
	}

	public String getFieldName() {
		return m_name;
	}

	public boolean isHasDependents() {
		return m_hasDependents;
	}

	public boolean isHasCallout() {
		return m_hasCallout;
	}

	public boolean isMandatory() {
		return m_mandatory;
	}

	public Lookup getLookup() {
		return m_lookup;
	}
	
	public GridField getGridField() {
		return m_Field;
	}
	
	public String getOldValue() {
		return oldValue;
	}
	
	public String getProcessParaKey() {
		return m_processParaKey;
	}
		
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	
	public void setNewValue(String newValue, String dataDisplay) {
		this.newValue = newValue;
		if (componentField != null && !m_readOnly) {
			if (componentField instanceof Button)
				((Button) componentField).setCaption(dataDisplay);
		}
	}
	
	public void focus() {
		if (componentField instanceof Focusable)
			((Focusable) componentField).focus();
	}

	public String getNewValue() {
		if (componentField != null && !m_readOnly) {
			if (componentField instanceof Link)
				return ((Link) componentField).getDescription();
			if (componentField instanceof Label)
				return ((Label) componentField).getValue();
			if (componentField instanceof NativeSelect) {
				@SuppressWarnings("rawtypes")
				Object data = ((NativeSelect) componentField).getValue();
				if (data instanceof KeyNamePair)
					return ((KeyNamePair) data).getID();
				else if (data != null)
					return ((ValueNamePair) data).getID();
			}
			if (componentField instanceof CheckBox)
				return String.valueOf(((CheckBox) componentField).getValue());
			if (componentField instanceof AbstractTextField)
				return ((AbstractTextField) componentField).getValue();
			if (componentField instanceof DateField) {
				LocalDate data = ((DateField) componentField).getValue();
				if (data != null) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DisplayType.getDateFormat(m_displayType, Env.getLanguage(ctx)).toPattern());
					String formattedString = data.format(formatter);
					return formattedString;
				}
			}
		}
		return newValue;
	}
}	//	WebField