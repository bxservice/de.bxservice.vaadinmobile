package com.trekglobal.vaadin.mobile;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;

import com.trekglobal.vaadin.ui.WebField;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class MobileLookup {

	/**	Logger			*/
	protected CLogger log = CLogger.getCLogger(getClass());
	private MobileSessionCtx wsc;
	
	private String header = "";
	private GridTab curTab;
	private WebField webField;
	private String columnName;
	private int AD_Process_ID = 0;
	private int refValueId = 0;
	private boolean isProcessLookUp = false;
	private boolean isProcessButtonLookUp = false;
	private boolean mandatory = false;
	private String[] m_searchFields;
	private String[] m_searchLabels;

	public MobileLookup(MobileSessionCtx wsc, WebField webField, GridTab curTab) {

		this.curTab = curTab;
		this.wsc = wsc;
		this.webField = webField;

		//  Get Mandatory Parameters
		columnName = webField.getColumnName();
		AD_Process_ID = webField.getProcessID();

		if (AD_Process_ID > 0) {
			isProcessButtonLookUp = true;
			isProcessLookUp = true;
		}
	}

	public void runLookup() {

		if (isProcessLookUp) {

			MProcess process = MProcess.get(Env.getCtx(), AD_Process_ID);
			MProcessPara para = null;

			MProcessPara[] parameters = process.getParameters();

			for (int i = 0; i < parameters.length; i++) {						
				if (parameters[i].getColumnName().equals(columnName))
					para = parameters[i];
			}

			if ( para !=null )
				refValueId = para.getAD_Reference_Value_ID();

			header = para.getName();
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

			mandatory = hasDependents || hasCallout;

			refValueId = mField.getAD_Reference_Value_ID();
		}


		if (m_searchFields == null || m_searchFields.length == 0) {
			setSearchFields();
		}
	}

	private void setSearchFields() {

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

	public List<MobileLookupGenericObject> getLookupRows(String where) {
		List<MobileLookupGenericObject> rows = new ArrayList<MobileLookupGenericObject>();

		StringBuffer sqlSelect = null;
		StringBuffer sqlCount = null;
		String sql = null;
		String colKey = null;
		String colDisplay = null;

		if (refValueId > 0) {
			sql = "SELECT AD_Table_ID, AD_Key, AD_Display, WhereClause, OrderByClause FROM AD_Ref_Table WHERE AD_Reference_ID = " + refValueId;

			int tableID = 0;

			String whereClause = null;
			String orderBy = null;

			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				pstmt = DB.prepareStatement(sql.toString(), null);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					tableID = rs.getInt(1);
					whereClause = rs.getString(4);
					orderBy = rs.getString(5);
					sql="Select ColumnName FROM AD_Column Where AD_Column_ID = ? AND AD_Table_ID = " + tableID;
					colKey = DB.getSQLValueString(null, sql, rs.getInt(2));
					colDisplay = DB.getSQLValueString(null, sql, rs.getInt(3));
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, sql.toString(), e);
			} finally {
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}

			sql = "Select TableName FROM AD_Table Where AD_Table_ID = ?";
			String tableName = DB.getSQLValueString(null, sql , tableID);
			sqlSelect = new StringBuffer("SELECT " + colKey + ", " + colDisplay /*m_HeaderSelect */+ " FROM " + tableName + " WHERE AD_Client_ID=?");
			sqlCount = new StringBuffer("SELECT count(*) FROM " + tableName + " WHERE AD_Client_ID=?");

			if (whereClause != null && curTab != null) {
				sqlSelect.append(" AND " + Env.parseContext(wsc.ctx, curTab.getWindowNo(), whereClause, false)).append(where);
				sqlCount.append(" AND " + Env.parseContext(wsc.ctx, curTab.getWindowNo(), whereClause, false)).append(where);
			}
			if (orderBy != null)
				sqlSelect.append(" ORDER BY " + orderBy);
		} else {
			//Fill inititially with headers[0]
			colDisplay = m_searchFields[0];
			sqlSelect=new StringBuffer("SELECT " + columnName + ", " + colDisplay /*m_HeaderSelect */ + " FROM " + columnName.replace("_ID", "") + " WHERE AD_Client_ID=?");
			sqlCount=new StringBuffer("SELECT count(*) FROM " + columnName.replace("_ID", "") + " WHERE AD_Client_ID=?");

			sqlSelect.append(where);
			sqlCount.append(where);
			colKey = columnName;
			/*if (m_HeaderSelect.toString().contains("Name"))
				colDisplay="Name";
			else
				colDisplay="Description";*/
		}
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sqlSelect.toString(),
					ResultSet.TYPE_SCROLL_INSENSITIVE,	ResultSet.CONCUR_READ_ONLY, null);

			pstmt.setInt(1, Env.getAD_Client_ID(wsc.ctx));
			rs = pstmt.executeQuery();

			MobileLookupGenericObject object;
			while (rs.next()) {
				object = new MobileLookupGenericObject();
				object.setId(rs.getInt(colKey));
				object.setQueryValue(rs.getString(colDisplay));
				rows.add(object);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, sql.toString(), e);
		} finally {
			DB.close(rs, pstmt);
			rs = null; 
			pstmt = null;
		}
		return rows;
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
	
	public WebField getWebField() {
		return webField;
	}

	public boolean isDataSafe() {
		if (AD_Process_ID < 0 || columnName == null 
				|| columnName.equals("")) 
			return false;
		
		return true;
	}

}
