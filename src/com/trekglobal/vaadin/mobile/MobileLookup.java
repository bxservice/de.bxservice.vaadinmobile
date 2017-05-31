package com.trekglobal.vaadin.mobile;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class MobileLookup {

	/**	Logger			*/
	protected CLogger log = CLogger.getCLogger(getClass());
	private String header = "";
	private GridTab curTab;
	private boolean isProcessLookUp = false;
	private boolean isProcessButtonLookUp = false;
	private String[] m_searchFields;
	private String[] m_searchLabels;

	public MobileLookup(boolean isProcessLookUp, boolean isProcessButtonLookUp, GridTab curTab) {
		this.isProcessLookUp = isProcessLookUp;
		this.isProcessButtonLookUp = isProcessButtonLookUp;
		this.curTab = curTab;
	}

	public void runLookup(String columnName, int AD_Process_ID) {
		int refValueId = 0;
		boolean startUpdate = false;
		String targetBase = "'" + columnName;

		if (isProcessLookUp) {

			MProcess process = MProcess.get(Env.getCtx(), AD_Process_ID);
			MProcessPara para = null;

			MProcessPara[] parameters = process.getParameters();

			for (int i = 0; i < parameters.length; i++) {						
				if (parameters[i].getColumnName().equals(columnName))
					para = parameters[i];
			}

			if ( para !=null )
				refValueId= para.getAD_Reference_Value_ID();

			header = para.getColumnName();
		}
		//Lookup called from a window
		else {

			//	Modified by Rob Klein 7/01/07
			GridField mField = curTab.getField(columnName);

			log.config("ColumnName=" + columnName 
					+ ", MField=" + mField);
			if (mField == null) {
				Notification.show("ParameterMissing",
						Type.ERROR_MESSAGE);
				return;
			}

			header = mField.getHeader();

			boolean hasDependents = curTab.hasDependants(columnName);
			boolean hasCallout = mField.getCallout().length() > 0;

			startUpdate = hasDependents || hasCallout;

			refValueId = mField.getAD_Reference_Value_ID();
		}


		if (m_searchFields == null || m_searchFields.length == 0) {
			getSearchFields(columnName, refValueId);
		}
	}

	private void getSearchFields(String columnName, int refValueId) {

		String sqlSelect = null;

		if (refValueId > 0)
			sqlSelect = "SELECT AD_Column.ColumnName, CASE WHEN AD_Column_Trl.Name is null THEN AD_Column.Name ELSE AD_Column_Trl.Name END as Name FROM AD_Column left join AD_Column_Trl " +
					" ON AD_Column.AD_Column_ID=AD_Column_Trl.AD_Column_ID  AND AD_Column_Trl.ad_language='" + Env.getAD_Language(Env.getCtx())  +"' "+
					" WHERE AD_Table_ID IN (SELECT AD_Table_ID FROM AD_Ref_Table WHERE AD_Reference_ID = " + refValueId + ")" +
					" AND (IsSelectionColumn = 'Y' OR ColumnName LIKE '%Name%') AND AD_Reference_ID IN " +
					" (" + DisplayType.String + "," + DisplayType.Text +")" +
					" ORDER BY SEQNO";			
		else	
			sqlSelect = "SELECT AD_Column.ColumnName, CASE WHEN AD_Column_Trl.Name is null THEN AD_Column.Name ELSE AD_Column_Trl.Name END as Name FROM AD_Column left join AD_Column_Trl " +
					" ON AD_Column.AD_Column_ID=AD_Column_Trl.AD_Column_ID  AND AD_Column_Trl.ad_language='" + Env.getAD_Language(Env.getCtx())  +"' "+
					" WHERE AD_Table_ID IN (SELECT AD_Table_ID FROM AD_Table WHERE TableName = '" + columnName.replace("_ID", "") + "') " +
					" AND (IsSelectionColumn = 'Y' OR ColumnName LIKE '%Name%') AND AD_Reference_ID IN " +
					" (" + DisplayType.String + "," + DisplayType.Text +")" +
					" ORDER BY SEQNO";

		ArrayList<String> columns = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();

		PreparedStatement pstmt = null;			
		ResultSet rs = null;
		try {			
			pstmt = DB.prepareStatement(sqlSelect.toString(), null);			
			rs = pstmt.executeQuery();
			while (rs.next()) {
				columns.add(rs.getString(1));
				names.add(rs.getString(2));
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, sqlSelect.toString(), e);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		m_searchFields = columns.toArray(new String[columns.size()]);
		m_searchLabels = names.toArray(new String[names.size()]);
	}

	public String getHeader() {
		return header;
	}

	public String[] getSearchFields() {
		return m_searchFields;
	}

	public String[] getSearchLabels() {
		return m_searchLabels;
	}
	
	
}
