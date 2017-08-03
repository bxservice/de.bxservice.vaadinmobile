package com.trekglobal.vaadin.mobile;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;

import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;

import com.trekglobal.vaadin.ui.WNavigatorUI;
import com.trekglobal.vaadin.ui.WebField;

/**
 * @author druiz
 */
public class MobileWindow {


	/**	Logger			*/
	protected CLogger log = CLogger.getCLogger(getClass());
	private static final String ERROR = " ERROR! ";

	private Properties ctx;
	private GridTab curTab;

	public MobileWindow(GridTab curTab) {
		ctx = WNavigatorUI.getContext();
		this.curTab = curTab;
	}

	public boolean saveRecord(ArrayList<WebField> webFields, GridField calloutField) {

		boolean error = updateFields(webFields);

		//  Check Mandatory
		log.fine("Mandatory check");
		int size = curTab.getFieldCount();
		for (int i = 0; i < size; i++) {
			GridField field = curTab.getField(i);
			
			//Process callouts
			if (calloutField != null && calloutField.getColumnName().equals(field.getColumnName())
					&& !field.getCallout().isEmpty()) {
				curTab.processCallout(field);
			}
			//  context check
			if (field.isMandatory(true)) {
				Object value = field.getValue();
				if (value == null || value.toString().length() == 0) {
					field.setInserting (true);  //  set editable otherwise deadlock
					field.setError(true);
					field.setErrorValue(value == null ? null : value.toString());
					if (!error)
						error = true;
					log.info("Mandatory Error: " + field.getColumnName());
				}
				else
					field.setError(false);
			}
		}

		return error;
	}

	/**
	 * 	Update Field Values from Parameter
	 *	@return true if error
	 */
	private boolean updateFields(ArrayList<WebField> webFields) { 
		boolean error = false;

		//  loop through parameters
		for (WebField webField : webFields) {

			GridField mField = webField.getGridField();

			//  we found a writable field
			if (mField != null && mField.isEditable(true)) {
				String oldValue = webField.getNewValue();
				//String newValue = null;
				String value = null;

				/*if (newValue != null) {
					Object val= lookupValue(newValue, mField.getLookup());
					if (val != null) 
						value = val.toString();
				}*/
				if (value == null)
					value = oldValue;

				Object dbValue = mField.getValue();
				boolean fieldError = false;

				log.finest(mField.getColumnName() 
						+ ": " + (dbValue==null ? "null" : dbValue.toString()) 
						+ " -> " + (value==null ? "null" : value.toString()));

				if (dbValue == null && value == null)
					continue;
				//   new value null
				else if (dbValue != null && value == null)
					curTab.setValue(mField, null);
				//  from null to new value
				else if (dbValue == null && value != null)
					fieldError = !setFieldValue(mField, value);
				//  same
				else if (String.valueOf(dbValue).equals(value))
					continue;
				else
					fieldError = !setFieldValue(mField, value);
				//
				if (!error && fieldError) {
					log.info("Error: " + mField.getColumnName());
					error = true;
				}
			}
		}   //  for all parameters

		return error;
	}	//	updateFields

	/**************************************************************************
	 *  Set Field Value
	 *  @param mField field
	 *  @param value as String
	 *  @return true if correct
	 */
	private boolean setFieldValue(GridField mField, String value) {
		Object newValue = getFieldValue(mField, value);
		if (ERROR.equals(newValue)) {
			mField.setErrorValue(value);
			return false;
		}
		Object dbValue = mField.getValue();
		if ((newValue == null && dbValue != null)
				|| (newValue != null && !newValue.equals(dbValue)))
			curTab.setValue(mField, newValue);
		return true;
	}   //  setFieldValue

	/**
	 *  Get Field value (convert value to datatype of MField)
	 *  @param mField field
	 *  @param value String Value
	 *  @return converted Field Value
	 */
	private Object getFieldValue(GridField mField, String value) {

		Object defaultObject = null;

		int dt = mField.getDisplayType();

		if (value == null || value.length() == 0) {
			defaultObject = mField.getDefault();			
			mField.setValue (defaultObject, true);			
			if (value == null || value.length() == 0 || mField.getValue() == null) {
				return null;
			}
			else
				value = mField.getValue().toString();
		}		
		//  BigDecimal
		if (DisplayType.isNumeric(dt)) {
			BigDecimal bd = null;
			try {
				Number nn = null;
				if (dt == DisplayType.Amount)
					nn = DisplayType.getNumberFormat(DisplayType.Amount, Env.getLanguage(ctx)).parse(value);
				else if (dt == DisplayType.Quantity)
					nn = DisplayType.getNumberFormat(DisplayType.Quantity, Env.getLanguage(ctx)).parse(value);
				else	//	 DisplayType.CostPrice
					nn = DisplayType.getNumberFormat(DisplayType.Number, Env.getLanguage(ctx)).parse(value);
				if (nn instanceof BigDecimal)
					bd = (BigDecimal)nn;
				else
					bd = new BigDecimal(nn.toString());		
			}
			catch (Exception e) {
				log.warning("BigDecimal: " + mField.getColumnName() + "=" + value + ERROR);
				return ERROR;
			}
			log.fine("BigDecimal: " + mField.getColumnName() + "=" + value + " -> " + bd);
			return bd;
		}

		//  ID
		else if (DisplayType.isID(dt)) {			
			Integer ii = null;
			try {
				ii = new Integer (value);
			}
			catch (Exception e) {
				ii = null;
			}
			//  -1 indicates NULL
			if (ii != null && ii.intValue() == -1)
				ii = null;
			return ii;
		}

		//  Date/Time
		else if (DisplayType.isDate(dt)) {
			Timestamp ts = null;
			try
			{
				java.util.Date d = null;
				if (dt == DisplayType.Date)
					d = DisplayType.getDateFormat(DisplayType.Date, Env.getLanguage(ctx)).parse(value);
				else
					d = DisplayType.getDateFormat(DisplayType.DateTime, Env.getLanguage(ctx)).parse(value);
				ts = new Timestamp(d.getTime());
			}
			catch (Exception e) {
				return ERROR;
			}
			return ts;
		}

		//  Checkbox
		else if (dt == DisplayType.YesNo) {
			Boolean retValue = Boolean.FALSE;
			if (value.equals("true"))
				retValue = Boolean.TRUE;
			return retValue;			
		}

		//  treat as string
		return value;
	}   //  getFieldValue

}
