package com.trekglobal.vaadin.ui;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.compiere.model.DataStatusEvent;
import org.compiere.model.DataStatusListener;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.GridWindow;
import org.compiere.model.GridWindowVO;
import org.compiere.model.I_AD_Process;
import org.compiere.model.MLocation;
import org.compiere.model.MMenu;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.MQuery;
import org.compiere.model.MRole;
import org.compiere.model.MToolBarButton;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.compiere.util.ValueNamePair;

import com.trekglobal.vaadin.mobile.MobileProcess;
import com.trekglobal.vaadin.mobile.MobileWindow;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;

public class WWindowView extends AbstractWebFieldView implements LayoutClickListener, IFooterListener, 
IFindListener, Button.ClickListener, DataStatusListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7775755366215129119L;

	/**	Logger			*/
	protected static CLogger	log = CLogger.getCLogger(WWindowView.class);

	/** Error Indicator                     */
	public static final String NAME = "WWindow";
	private static final int    MAX_LINES   = 999999999;

	private Map<VerticalLayout, Integer> mapRowLine = new HashMap<VerticalLayout, Integer>();
	private Map<Button, GridTab> mapButtonNode = new HashMap<Button, GridTab>();
	private ArrayList<WebField> webFields = new ArrayList<WebField>();
	private ArrayList<MProcess> processes = new ArrayList<MProcess>();
	private ArrayList<WebField> processWebFields = new ArrayList<WebField>();

	/** Window Number Counter*/
	private static int s_WindowNo  = 1;
	private String sectionNameOld = "";

	private GridWindow mWindow;
	private GridTab curTab;
	private boolean singleRowSelected = false;

	//UI
	private CssLayout singleRowSection;
	private WFooter   footer;
	private Panel tabsPanel;
	private VerticalLayout tabs;
	private PopupView processPopup;
	private PopupView searchPopup;

	public WWindowView(WNavigatorUI loginPage, int AD_Menu_ID) {
		super(loginPage);
    	syncCtx();
		MMenu menu = new MMenu(ctx, AD_Menu_ID, null);

		GridWindowVO mWindowVO = GridWindowVO.create(ctx, s_WindowNo++, menu.getAD_Window_ID(), AD_Menu_ID);
		if (mWindowVO == null) {
			String msg = Msg.translate(ctx, "AD_Window_ID") + " "
					+ Msg.getMsg(ctx, "NotFound") + ", ID=" + menu.getAD_Window_ID() + "/" + AD_Menu_ID;
			Notification.show(msg,
					Type.ERROR_MESSAGE);
			return;
		}

		mWindow = new GridWindow(mWindowVO, true);
		Env.setContext(ctx, s_WindowNo, "IsSOTrx", mWindow.isSOTrx());
		setCurTab(mWindow.getTab(0));
	}

	protected void initComponents() {

		windowTitle = curTab.getName();
		header = new WHeader(this, true, true);
		content = createContent();
		footer = new WFooter(this);

		initTabs();

		generateMultirowView(singleRowSelected);
	}

	private void setCurTab(GridTab tab) {
		curTab = tab;
		mWindow.initTab(curTab.getTabNo());
		curTab.query(mWindow.isTransaction());
		curTab.addDataStatusListener(this);
	}

	private void generateMultirowView(boolean repaint) {

		if (repaint)
			replaceContent();

    	syncCtx();
		singleRowSelected = false;
		mapRowLine.clear();
		curTab.navigate(0);
		curTab.setSingleRow(false);
		updateHeader();
		footer.setMultirowButtons();

		int initRowNo = curTab.getCurrentRow();

		String idSQL = "SELECT ColumnName, AD_Column_ID from AD_Column" +
				" WHERE AD_Table_ID = " +curTab.getAD_Table_ID() +
				" AND (IsIdentifier ='Y' OR IsSelectionColumn ='Y') ORDER BY SeqNo,SeqNoSelection";

		ValueNamePair[] idColumns = DB.getValueNamePairs(idSQL, false,null);
		
		if (idColumns.length == 0) {
            log.warning("No columns to print in multirow");
            idSQL = "SELECT ColumnName, AD_Column_ID from AD_Column" +
    				" WHERE AD_Table_ID = " + curTab.getAD_Table_ID() +
    				" AND ismandatory = 'Y' AND isupdateable = 'Y' ORDER BY SeqNo,SeqNoSelection";
            idColumns = DB.getValueNamePairs(idSQL, false, null);
		}

		String primary = null;
		String secondary = null;

		String[] selectioncolumn = new String[3]; 

		int y=0;
		for (ValueNamePair pair : idColumns) {
			if (primary == null) {
				primary = pair.getValue();
			}
			else if (secondary == null) {
				secondary = pair.getValue();
			}
			else {
				selectioncolumn[y++]=pair.getValue();
				if (y==3) break;
			}
		}

		/**
		 * Lines
		 */
		int count=y;

		int lastRow = initRowNo + MAX_LINES;
		lastRow = Math.min(lastRow, curTab.getRowCount());
		for (int lineNo = initRowNo; lineNo >= 0 && lineNo < lastRow; lineNo++) {
			curTab.navigate(lineNo);
			y=0;

			VerticalLayout row = new VerticalLayout();
			row.addStyleName("multirow");

			for (int i = 1; i < (3+count); i++) {
				GridField field = null;

				if (i == 1 && primary != null && primary.length() >  0)
					field = curTab.getField(primary);
				else if (i == 2 && secondary != null && secondary.length() > 0)
					field = curTab.getField(secondary);
				else  if (i > 2 && selectioncolumn != null && selectioncolumn[y].length() > 0) {
					field = curTab.getField(selectioncolumn[y++]);
				}
				if (field == null)
					continue;

				//  Get Data - turn to string
				Object data = curTab.getValue(field.getColumnName());
				String info = null;
				//
				if (data == null)
					info = "";
				else {
					int dt = field.getDisplayType();
					switch (dt) {
					case DisplayType.Date:
						info = DisplayType.getDateFormat(DisplayType.Date, Env.getLanguage(ctx)).format(data);
						break;
					case DisplayType.DateTime:
						info = DisplayType.getDateFormat(DisplayType.DateTime, Env.getLanguage(ctx)).format(data);							
					case DisplayType.Amount:
						info = DisplayType.getNumberFormat(DisplayType.Amount, Env.getLanguage(ctx)).format(data);
						break;
					case DisplayType.Number:
					case DisplayType.CostPrice:
						info = DisplayType.getNumberFormat(DisplayType.Number, Env.getLanguage(ctx)).format(data);
						break;
					case DisplayType.Quantity:
						info = DisplayType.getNumberFormat(DisplayType.Quantity, Env.getLanguage(ctx)).format(data);
						break;
					case DisplayType.Integer:
						info = DisplayType.getNumberFormat(DisplayType.Integer, Env.getLanguage(ctx)).format(data);
						break;
					case DisplayType.YesNo:
						info = Msg.getMsg(ctx, data.toString());
						break;
						/** @todo output formatting 2 */
					default:
						if (DisplayType.isLookup(dt))
							info = field.getLookup().getDisplay(data);
						else
							info = data.toString();
					}
				}

				if (i == 1) {
					//  Empty info
					if (info == null || info.length() == 0)
						info = "No Identifier";

					Label label = new Label(info);
					label.addStyleName("primary");
					row.addComponent(label);
				}
				else if (info != null && info.length() > 0) {
					if (i==2) {
						Label label = new Label(info);
						label.addStyleName("secondary");
						row.addComponent(label);						
					} else {
						Label label = new Label(info);
						label.addStyleName("selectioncolumn");
						row.addComponent(label);
					}

				}
			}   

			content.addComponent(row);
			row.addLayoutClickListener(this);
			mapRowLine.put(row, lineNo);
		}
	}

	private CssLayout createContent() {
		CssLayout newContent = new CssLayout();
		Responsive.makeResponsive(newContent);
		newContent.setSizeFull();
		newContent.addStyleName("bxwindow-content");

		return newContent;
	}

	private void replaceContent() {
		CssLayout oldContent = content;
		content = createContent();

		Responsive.makeResponsive(content);
		content.setSizeFull();
		content.addStyleName("bxwindow-content");

		replaceComponent(oldContent, content);

	}

	private void initTabs() {
		tabs = new VerticalLayout();
		tabsPanel = new Panel(tabs);
		tabsPanel.addStyleName("bxwindow-tabs");

		for (int i = curTab.getTabNo(); i < mWindow.getTabCount(); i++) {
			GridTab tab = mWindow.getTab(i);

			if (tab.isSortTab())
				continue;

			Button button = new Button(tab.getName());
			button.addStyleName("bxtab-button");

			// not direct child
			if (tab.getTabLevel() != curTab.getTabLevel()+1) {
				button.setEnabled(false);

				// If it's not the tab that is currently active
				if (!tab.equals(curTab))
					button.addStyleName("bxtab-nochild-button");
			}

			button.addClickListener(this);
			mapButtonNode.put(button, tab);
			tabs.addComponent(button);
		}
	}

	protected void init() {
		updateHeader();
		header.setBackButton();

		createUI();
	}

	private void createUI() {
		setWidth("100%");

		// Enable Responsive CSS selectors for the component
		Responsive.makeResponsive(this);
		addComponents(header, content, footer, tabsPanel);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		initView();
	}

	@Override
	public void layoutClick(LayoutClickEvent event) {

		int lineNo = mapRowLine.get(event.getSource());

		if (lineNo != -1) {
			curTab.navigate(lineNo);
			generateSingleRowView(true);
		}
	}

	public void generateSingleRowView(boolean isReadOnly) {
		singleRowSelected = true;
		curTab.setSingleRow(true);
		updateHeader();
		webFields.clear();
		processes.clear();

		// Show single record
		if (isReadOnly) {
			header.setMenuButton();
		} else {
			footer.setEditRowButtons();
		}

		replaceContent();

		//m_searchField=null;
		if (curTab.isDisplayed()) {
			printCurrentTab(isReadOnly);
		}
	}

	private void printCurrentTab(boolean isReadOnly) {
    	syncCtx();
		int noFields = curTab.getFieldCount();
		MRole role = MRole.getDefault(ctx, false);

		singleRowSection = new CssLayout();
		singleRowSection.addStyleName("singlerow-content");
		content.addComponent(singleRowSection);

		isReadOnly = isReadOnly || curTab.isReadOnly();

		for (int i = 0; i < noFields; i++) {
			GridField field = curTab.getField(i);
			String columnName = field.getColumnName();

			/**
			 *  Get Data and convert to String (singleRow)
			 */
			Object oData = curTab.getValue(field);

			/**
			 *  Get Record ID and Table ID for Processes
			 */
			int recordID = curTab.getRecord_ID();
			int tableID = curTab.getAD_Table_ID();

			/**
			 *  Display field
			 */
			if (field.isDisplayed(true) && field.getDisplayType() != DisplayType.Button ) {

				boolean hasDependents = curTab.hasDependants(columnName);

				addField(field, oData, hasDependents, recordID, tableID, isReadOnly /*first time open -> curTab.isReadOnly()*/, role);

			} 
			// Processes
			else if (field.isDisplayed(true) && field.getDisplayType() == DisplayType.Button) {
				MProcess process = new MProcess(ctx, field.getAD_Process_ID(), null);
				processes.add(process);
			}
		}	//	for all fields

		MToolBarButton[] toolBarButtons = MToolBarButton.getProcessButtonOfTab(curTab.getAD_Tab_ID(), null);
		if (toolBarButtons != null && toolBarButtons.length > 0) { 
			for (MToolBarButton button : toolBarButtons) {
				MProcess process = new MProcess(ctx, button.getAD_Process_ID(), null);
				processes.add(process);
			}
		}

		if (isReadOnly)
			footer.setSinglerowButtons(!processes.isEmpty());
	}

	/**************************************************************************
	 *	Add Field to Line
	 *  @param row format element
	 *  @param field field
	 *  @param oData original data
	 *  @param hasDependents has Callout function(s)
	 */
	public void addField(GridField field, Object oData, boolean hasDependents, int recordID, 
			int tableID, boolean tabRO, MRole role) {

		HorizontalLayout line = new HorizontalLayout();
		String columnName = field.getColumnName();

		//  Any Error?
		boolean error = field.isErrorValue();
		if (error)
			oData = field.getErrorValue();

		int displayType = field.getDisplayType();
		boolean hasCallout = field.getCallout().length() > 0;

		String fieldgroup = field.getFieldGroup();
		if (fieldgroup != null && !fieldgroup.equals(sectionNameOld) && !fieldgroup.equals("")) {
			singleRowSection = new CssLayout();
			singleRowSection.addStyleName("singlerow-content");
			Label sectionHeader = new Label(fieldgroup);
			sectionHeader.addStyleName("section-header");
			content.addComponents(sectionHeader, singleRowSection);
			sectionNameOld = field.getFieldGroup();
		}

		/** 
		 * Set read only value
		 */
		boolean fieldRO = true;
		if (tabRO)
			fieldRO = true;
		else
			fieldRO = !field.isEditable(true);

		/**
		 *  HTML Label Element
		 *      ID = ID_columnName
		 *
		 *  HTML Input Elements
		 *      NAME = columnName
		 *      ID = ID_columnName
		 */
		WebField wField = new WebField (this, ctx, columnName, field.getHeader(), field.getDescription(),
				displayType, field.getFieldLength(), field.getDisplayLength(), field.isLongField(),
				fieldRO, field.isMandatory(false), error, hasDependents, hasCallout, 
				field.getAD_Process_ID(), field.getAD_Window_ID(), recordID, tableID,
				field.getDefault(), field.getCallout(), curTab, field, role);

		Label fieldLabel = wField.getLabel(!tabRO);
		fieldLabel.addStyleName("bxwindow-label");

		Component fieldValue = wField.getField(field.getLookup(), oData);
		fieldValue.addStyleName("bxwindow-field");

		webFields.add(wField);

		//Adding components to the row
		line.addComponent(fieldLabel);
		line.addComponent(fieldValue);
		line.addStyleName("singlerow");

		singleRowSection.addComponents(line);
	}	//	addField

	@Override
	public void onLeftButtonPressed() {
    	syncCtx();
		if (!footer.isVisible())
			updateTabMenu();

		if (singleRowSelected) {
			// just in case
			curTab.dataIgnore();

			generateMultirowView(singleRowSelected);
		} else {

			if (curTab.getTabNo() > 0) {
				int lineNo = curTab.getParentTab().getCurrentRow();
				setCurTab(curTab.getParentTab());
				curTab.navigate(lineNo);
				generateSingleRowView(true);
			} else {
				Env.clearWinContext(s_WindowNo);
				backButton();
			}
		}
	}

	@Override
	public void onRightButtonPressed() {
		if (curTab.getTabNo() == 0 && !singleRowSelected) {
			openMenu();
		} else
			updateTabMenu();
	}

	@Override
	public void onNewButtonPressed() {
    	syncCtx();
    	if (!curTab.isInsertRecord()) {
            log.warning("Insert Record disabled for Tab");
            return;
    	}

		if (!curTab.dataNew(false))
			curTab.dataIgnore();
		generateSingleRowView(false);
	}

	@Override
	public void onEditButtonPressed() {
    	syncCtx();
		generateSingleRowView(false);
	}

	@Override
	public void onSearchButtonPressed() {
		if (searchPopup == null) {
	    	syncCtx();
			WFindView searchContent = new WFindView(this, curTab);
			searchPopup = new PopupView(null, searchContent);
			searchPopup.addStyleName("searchdialog");
			addComponent(searchPopup);

			searchPopup.addPopupVisibilityListener(event -> {
				if (event.isPopupVisible())
					content.addStyleName("bxwindow-content-busy");
				else {
					content.removeStyleName("bxwindow-content-busy");
					searchContent.reset();
				}
			});
		}

		searchPopup.setPopupVisible(!searchPopup.isPopupVisible());

	}

	@Override
	public void onSearch(String value, String name, String description, String docNo) {
		MQuery query=new MQuery();
    	syncCtx();

		if (value !=null && value.length()!= 0) 
			query.addRestriction("UPPER(Value)", MQuery.LIKE, "%"+value.toUpperCase()+"%");
		if (docNo != null && docNo.length()!= 0) 
			query.addRestriction("UPPER(DocumentNo)", MQuery.LIKE, "%"+docNo.toUpperCase()+"%");
		if (name != null && name.length()!= 0) 
			query.addRestriction("UPPER(Name)", MQuery.LIKE, "%"+name.toUpperCase()+"%");
		if (description != null && description.length() != 0)
			query.addRestriction("(UPPER(Description", MQuery.LIKE, "%"+description.toUpperCase()+"%");

		curTab.setQuery(query);
		curTab.query(false);

		searchPopup.setPopupVisible(false);
		generateMultirowView(true);
	}

	@Override
	public void onCancelSearch() {
		searchPopup.setPopupVisible(false);
	}

	@Override
	public void onProcessButtonPressed() {

		if (processPopup == null) {

			VerticalLayout popupContent = new VerticalLayout();

			for (MProcess process : processes) {
				int processId = process.getAD_Process_ID();
				if (processId > 0) {
					Button processButton = new Button(process.get_Translation(I_AD_Process.COLUMNNAME_Name));
					processButton.addStyleName("bxprocess-list-button");
					processButton.addClickListener(e -> createProcessParameterPanel(process));
					popupContent.addComponent(processButton);
				}
			}

			processPopup = new PopupView(null, popupContent);
			processPopup.addStyleName("bxprocess-list");
			addComponent(processPopup);
		}

		processPopup.setPopupVisible(!processPopup.isPopupVisible());
	}

	public void createProcessParameterPanel(MProcess process) {

    	syncCtx();

		//	need to check if Role can access
		if (process == null)
			return;

		processWebFields.clear();
		processPopup.setPopupVisible(false);
		VerticalLayout popupContent = new VerticalLayout();
		PopupView parametersPopup = new PopupView(null, popupContent);
		parametersPopup.addStyleName("bxprocess-list");

		Label header = new Label(process.getName());
		header.addStyleName("bxprocesspara-title");
		popupContent.addComponent(header);

		for (MProcessPara para : process.getParameters()) {

			WebField wField = new WebField(this, ctx, para.getColumnName(), 
					para.getName(), para.getDescription(), para.getAD_Reference_ID(), para.getFieldLength(), 
					para.getFieldLength(), para.isMandatory(), para.getAD_Process_ID(),
					mWindow.getWindowNo(),curTab.getRecord_ID(),curTab.getAD_Table_ID(), null);

			Object defaultValue = wField.getDefault(para.getDefaultValue());

			HorizontalLayout row = new HorizontalLayout();

			Label fieldLabel = wField.getLabel(true);
			fieldLabel.addStyleName("bxwindow-label");

			Component fieldValue = wField.getField(para.getLookup(), defaultValue);
			fieldValue.addStyleName("bxwindow-field");
			processWebFields.add(wField);

			row.addComponents(fieldLabel, fieldValue);
			row.addStyleName("singlerow");
			row.addStyleName("process-para-row");

			if (para.isRange()) {
				WebField wFieldforRange =  new WebField(this, ctx, para.getColumnName(), 
						para.getName(), para.getDescription(), para.getAD_Reference_ID(), para.getFieldLength(), 
						para.getFieldLength(), para.isMandatory(), para.getAD_Process_ID(),
						mWindow.getWindowNo(),curTab.getRecord_ID(),curTab.getAD_Table_ID(), 
						para.getColumnName()+"_2");

				Component toField = wFieldforRange.getField(para.getLookup(), para.getDefaultValue2());

				row.addComponents(toField);
				processWebFields.add(wFieldforRange);
			}

			popupContent.addComponent(row);
		}

		HorizontalLayout row = new HorizontalLayout();
		row.addStyleName("confirmation-button-row");

		Button cancelButton = new Button();
		cancelButton.setIcon(VaadinIcons.CLOSE_SMALL);
		cancelButton.addStyleName("cancel-button");
		cancelButton.addClickListener(e -> { 
			parametersPopup.setPopupVisible(false);
		});

		Button okButton = new Button();
		okButton.setIcon(VaadinIcons.CHECK);
		okButton.addStyleName("ok-button");
		okButton.addClickListener(e ->  {
			parametersPopup.setPopupVisible(false);
			MobileProcess mProcess = new MobileProcess(process);

			HashMap<String, String> parameters = new HashMap<String, String>();

			//  loop through parameters
			for (WebField webField : processWebFields) {
				parameters.put(webField.getProcessParaKey(), webField.getNewValue());
			}   //  for all parameters

			if (process.getJasperReport() != null && processWebFields.size() == 0) {
				parameters.put("AD_Record_ID", String.valueOf(curTab.getRecord_ID()));
			}

			try {
				String message = mProcess.runProcess(mWindow.getAD_Window_ID(), curTab.getAD_Table_ID(), 
						curTab.getRecord_ID(), parameters);

				Type messageType = Type.HUMANIZED_MESSAGE;
				if (!mProcess.getProcessOK())
					messageType = Type.ERROR_MESSAGE;

				Notification.show(message, messageType);

			} catch (IOException e1) {
				e1.printStackTrace();
			}

		});

		row.addComponents(cancelButton, okButton);
		popupContent.addComponents(row);

		addComponent(parametersPopup);
		parametersPopup.setPopupVisible(true);
	}

	@Override
	public void onDeleteButtonPressed() {
		log.fine("Deleted!");
    	syncCtx();
		WConfirmationDialogView confirmationDialog = new WConfirmationDialogView(true, true, Msg.getMsg(Env.getLanguage(ctx), "DeleteRecord?"));
		PopupView confirmationPopup = new PopupView(null, confirmationDialog);
		confirmationPopup.addStyleName("bxconfirmation-dialog");


		confirmationDialog.getCancelButton().addClickListener(e -> {
			confirmationPopup.setPopupVisible(false);
		});
		confirmationDialog.getOkButton().addClickListener(e -> 
		{
			confirmationPopup.setPopupVisible(false);
			curTab.dataDelete();
			generateMultirowView(singleRowSelected);
		});

		confirmationPopup.addPopupVisibilityListener(event -> {
			if (event.isPopupVisible())
				content.addStyleName("bxwindow-content-busy");
			else 
				content.removeStyleName("bxwindow-content-busy");
		});

		addComponent(confirmationPopup);
		confirmationPopup.setPopupVisible(true);
	}

	@Override
	public void onSaveButtonPressed() {
    	syncCtx();
    	MobileWindow mobileWindow = new MobileWindow(curTab);
    	mobileWindow.updateFields(webFields);

		//  save it - of errors ignore changes
		if (!curTab.dataSave(true)) {
			//highlight the missing fields
			mobileWindow.saveRecord(webFields, null);
			generateSingleRowView(false);
			return;
		}
		log.fine("done");
		
		curTab.dataRefreshAll();
		generateSingleRowView(true);
	}

	/**
	 * Listens to the menu buttons
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		updateTabMenu();

		int lineNo = 0;

		if (mapButtonNode.get(event.getButton()).getTabNo() == 0)
			lineNo = mWindow.getTab(0).getCurrentRow();
		else if (curTab.getParentTab() != null)
			lineNo = curTab.getParentTab().getCurrentRow();

		setCurTab(mapButtonNode.get(event.getButton()));

		if (curTab.getTabLevel() == 0) {
			curTab.navigate(lineNo);
			generateSingleRowView(true);
		}
		else
			generateMultirowView(true);
	}

	private void updateTabMenu() {
		footer.setVisible(!footer.isVisible());
		if (!footer.isVisible())
			tabsPanel.addStyleName("bxwindow-tabs-active");
		else
			tabsPanel.removeStyleName("bxwindow-tabs-active");
	}

	private void updateHeader() {
		header.updateTitle(curTab.getName());
		if (curTab.getTabNo() == 0 && !singleRowSelected)
			header.setHomeButton();
		else {
			updateTabs();
			header.setMenuButton();
		}
	}

	private void updateTabs() {

		for (Map.Entry<Button, GridTab> entry : mapButtonNode.entrySet()) {

			Button  menuButton = entry.getKey();
			GridTab menuTab = entry.getValue();

			if (menuTab.getTabLevel() <= curTab.getTabLevel()  // master or same level tab
					&& !menuTab.equals(curTab)                // No current tab
					|| (menuTab.getTabLevel() == curTab.getTabLevel()+1 && menuTab.getParentTab().equals(curTab) && singleRowSelected) /*Direct child*/) {
				menuButton.setEnabled(true);
			} else
				menuButton.setEnabled(false);
		}
	}

	@Override
	public GridTab getCurTab() {
		return curTab;
	}

	@Override
	public void onChange(WebField webField) {
    	syncCtx();
		GridField field = webField.getGridField();
		MobileWindow mobileWindow = new MobileWindow(curTab);
		mobileWindow.saveRecord(webFields, field);
		generateSingleRowView(false);
	}
	
	@Override
	public void onLocation(WebField webField) {
		MLocation location = null;
		if (curTab.getValue(webField.getGridField()) != null) {
			location = new MLocation(ctx, (Integer) curTab.getValue(webField.getGridField()), null);
		}
		onLocation(webField, location, s_WindowNo);
	}

	@Override
	public void dataStatusChanged(DataStatusEvent e) {
        //  Set Message / Info
		if (e.getAD_Message() != null || e.getInfo() != null) {
			StringBuilder sb = new StringBuilder();
			String msg = e.getMessage();
			StringBuilder adMessage = new StringBuilder();
			String origmsg = null;
			if (msg != null && msg.length() > 0) {
				origmsg = Msg.getMsg(ctx, e.getAD_Message());
				adMessage.append(origmsg);
			}
			String info = e.getInfo();
			if (info != null && info.length() > 0) {
				Object[] arguments = info.split("[;]");
				int index = 0;
				while(index < arguments.length) {
					String expr = "{"+index+"}";
					if (adMessage.indexOf(expr) >= 0) {
						index++;
					} else {
						break;
					}
				} if (index < arguments.length) {
					if (adMessage.length() > 0 && !adMessage.toString().trim().endsWith(":"))
						adMessage.append(": ");
					StringBuilder tail = new StringBuilder();
					while(index < arguments.length) {
						if (tail.length() > 0) tail.append(", ");
						tail.append("{").append(index).append("}");
						index++;
					}
					adMessage.append(tail);
				}
				if (arguments.length == 1 
						&& origmsg != null 
						&& origmsg.equals(arguments[0])) { // check dup message
					sb.append(origmsg);
				} else {
					String adMessageQuot = Util.replace(adMessage.toString(), "'", "''");
					sb.append(MessageFormat.format(adMessageQuot, arguments));
				}
			} else {
				sb.append(adMessage);
			}

			if (sb.length() > 0) {
				int pos = sb.indexOf("\n");
				if (pos != -1 && pos+1 < sb.length())  // replace CR/NL
				{
					sb.replace(pos, pos+1, " - ");
				}
				
				if (e.isError()) {
					Notification.show(sb.toString(), Type.ERROR_MESSAGE);
				}				
			}
		}
		
		//  Confirm Error
        if (e.isError() && !e.isConfirmed()) {
        	//focus to error field
        	/*GridField[] fields = curTab.getFields();
        	for (GridField field : fields) {
        		if (field.isError()) {
        			setFocus(field);
        			break;
        		}
        	}*/
            e.setConfirmed(true);   //  show just once - if MTable.setCurrentRow is involved the status event is re-issued
        }
	}

}