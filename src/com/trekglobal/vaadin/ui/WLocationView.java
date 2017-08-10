package com.trekglobal.vaadin.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.adempiere.util.ServerContext;
import org.compiere.model.GridField;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCountry;
import org.compiere.model.MLocation;
import org.compiere.model.MRegion;
import org.compiere.model.MSysConfig;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class WLocationView extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6099382690755046614L;

	private IWebFieldListener listener;
	private MLocation m_location;
	private int m_origCountry_ID;
	private int m_WindowNo = 0;

	private GridField m_gridField;
	private WebField webField;
	private ArrayList<String> countries = new ArrayList<>();
	private ArrayList<String> regions = new ArrayList<>();

	private HashMap<String, MCountry> hashMapCountries = new HashMap<>();
	private HashMap<String, MRegion> hashMapRegions = new HashMap<>();

	private boolean isCityMandatory = false;
	private boolean isRegionMandatory = false;
	private boolean isAddress1Mandatory = false;
	private boolean isAddress2Mandatory = false;
	private boolean isAddress3Mandatory = false;
	private boolean isAddress4Mandatory = false;
	private boolean isPostalMandatory = false;
	private boolean isPostalAddMandatory = false;

	private boolean inCountryAction;
	private boolean inOKAction;
	private boolean m_change = false;

	//UI
	private TextField txtAddress1;
	private TextField txtAddress2;
	private TextField txtAddress3;
	private TextField txtAddress4;
	private WComboBoxCity txtCity;
	private TextField txtPostal;
	private TextField txtPostalAdd;
	private NativeSelect<String> lstRegion;
	private NativeSelect<String> lstCountry;
	private Button okButton; 
	private Button cancelButton;

	public WLocationView(IWebFieldListener listener, MLocation location, WebField webField, int m_WindowNo) {
		ServerContext.setCurrentInstance(WNavigatorUI.getContext());

		this.listener = listener;
		this.webField = webField;
		this.m_gridField  = webField.getGridField();
		this.m_location = location;
		this.m_WindowNo = m_WindowNo;

		if (m_location == null)
			m_location = new MLocation (Env.getCtx(), 0, null);

		setTitle();

		// Reset TAB_INFO context
		Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Region_ID", null);
		Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Country_ID", null);
		initComponents();

		//      Current Country
		for (MCountry country : MCountry.getCountries(Env.getCtx())) {
			countries.add(country.toString());
			hashMapCountries.put(country.toString(), country);
		}
		lstCountry.setItems(countries);
		setCountry();

		lstCountry.addValueChangeListener(event -> countryChanged());
		lstRegion.addValueChangeListener(event -> regionChanged());

		m_origCountry_ID = m_location.getC_Country_ID();
		//  Current Region
		for (MRegion region : MRegion.getRegions(Env.getCtx(), m_origCountry_ID)) {
			hashMapRegions.put(region.getName(),region);
			regions.add(region.getName());
		}
		lstRegion.setItems(regions);
		if (m_location.getCountry().isHasRegion()) {
			setRegionCaption();
		}
		setRegion();
		initLocation();

	}

	private void setTitle() {
		Label title = new Label(Msg.getMsg(Env.getCtx(), "Location"));
		title.addStyleName("bxprocesspara-title");
		addComponent(title);
	}

	private void setConfirmationButtons() {
		HorizontalLayout buttonRow = new HorizontalLayout();
		buttonRow.addStyleName("confirmation-button-row");

		cancelButton = new Button();
		cancelButton.setIcon(VaadinIcons.CLOSE_SMALL);
		cancelButton.addStyleName("cancel-button");
		cancelButton.addClickListener(e -> listener.onLookUpCancel());
		buttonRow.addComponent(cancelButton);

		okButton = new Button();
		okButton.setIcon(VaadinIcons.CHECK);
		okButton.addStyleName("ok-button");
		okButton.addClickListener(e -> onOK());
		buttonRow.addComponent(okButton);

		addComponent(buttonRow);
	}

	private void initComponents() {
		txtAddress1 = new TextField();
		txtAddress1.setPlaceholder(Msg.getElement(Env.getCtx(), "Address1"));
		txtAddress1.setMaxLength(MLocation.getFieldLength(MLocation.COLUMNNAME_Address1));
		txtAddress1.addStyleName("search-field");

		txtAddress2 = new TextField();
		txtAddress2.setPlaceholder(Msg.getElement(Env.getCtx(), "Address2"));
		txtAddress2.setMaxLength(MLocation.getFieldLength(MLocation.COLUMNNAME_Address2));
		txtAddress2.addStyleName("search-field");

		txtAddress3 = new TextField();
		txtAddress3.setPlaceholder(Msg.getElement(Env.getCtx(), "Address3"));
		txtAddress3.setMaxLength(MLocation.getFieldLength(MLocation.COLUMNNAME_Address3));
		txtAddress3.addStyleName("search-field");

		txtAddress4 = new TextField();
		txtAddress4.setPlaceholder(Msg.getElement(Env.getCtx(), "Address4"));
		txtAddress4.setMaxLength(MLocation.getFieldLength(MLocation.COLUMNNAME_Address4));
		txtAddress4.addStyleName("search-field");

		txtCity = new WComboBoxCity(m_WindowNo);
		txtCity.setSizeFull();
		//txtCity.addStyleName("search-field");

		txtPostal = new TextField();
		txtPostal.setPlaceholder(Msg.getElement(Env.getCtx(), "Postal"));
		txtPostal.setMaxLength(MLocation.getFieldLength(MLocation.COLUMNNAME_Postal));
		txtPostal.addStyleName("search-field");

		txtPostalAdd = new TextField();
		txtPostalAdd.setPlaceholder(Msg.getElement(Env.getCtx(), "PostalAdd"));
		txtPostalAdd.setMaxLength(MLocation.getFieldLength(MLocation.COLUMNNAME_Postal_Add));
		txtPostalAdd.addStyleName("search-field");

		lstRegion = new NativeSelect<>(Msg.getMsg(Env.getCtx(), "Region"));
		lstRegion.setSizeFull();

		lstCountry = new NativeSelect<>(Msg.getMsg(Env.getCtx(), "Country"));
		lstCountry.setEmptySelectionAllowed(false);
		lstCountry.setSizeFull();
	}

	private void setCountry() {
		for (String country : countries) {
			if (country.equals(m_location.getCountry().getName())) {
				lstCountry.setSelectedItem(country);
				break;
			}
		}
	}

	private void setRegion() {
		if (m_location.getRegion() != null) {
			for (String region : regions) {
				if (region.equals(m_location.getRegion().getName())) {
					lstRegion.setSelectedItem(region);
					break;
				}
			}
		} else {
			lstRegion.setSelectedItem(null);
		}
	}

	private void initLocation() {
		removeAllComponents();

		MCountry country = m_location.getCountry();

		//  new Country
		if (m_location.getC_Country_ID() != m_origCountry_ID) {
			lstRegion.clear();
			regions.clear();
			hashMapRegions.clear();
			if (country.isHasRegion()) {
				regions.add("");
				for (MRegion region : MRegion.getRegions(Env.getCtx(), country.getC_Country_ID())) {
					hashMapRegions.put(region.getName(),region);
					regions.add(region.getName());
				}
				lstRegion.setItems(regions);
				setRegionCaption();
			}
			m_origCountry_ID = m_location.getC_Country_ID();
		}

		if (m_location.getC_Region_ID() > 0 && 
				m_location.getC_Region().getC_Country_ID() == country.getC_Country_ID()) {
			setRegion();
		} else {
			lstRegion.setSelectedItem(null);
			m_location.setC_Region_ID(0);
		}

		if (country.isHasRegion() && m_location.getC_Region_ID() > 0) {
			Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Region_ID", String.valueOf(m_location.getC_Region_ID()));
		} else {
			Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Region_ID", "0");
		}
		Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Country_ID", String.valueOf(country.get_ID()));

		txtCity.fillList();

		//      sequence of City Postal Region - @P@ @C@ - @C@, @R@ @P@
		String ds = country.getCaptureSequence();
		if (ds == null || ds.length() == 0) {
			ds = "";    //  @C@,  @P@
		}

		isCityMandatory = false;
		isRegionMandatory = false;
		isAddress1Mandatory = false;
		isAddress2Mandatory = false;
		isAddress3Mandatory = false;
		isAddress4Mandatory = false;
		isPostalMandatory = false;
		isPostalAddMandatory = false;
		StringTokenizer st = new StringTokenizer(ds, "@", false);
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s.startsWith("CO")) {
				addComponent(lstCountry);
			} else if (s.startsWith("A1")) {
				addComponent(txtAddress1);
				isAddress1Mandatory = s.endsWith("!");
			} else if (s.startsWith("A2")) {
				addComponent(txtAddress2);
				isAddress2Mandatory = s.endsWith("!");
			} else if (s.startsWith("A3")) {
				addComponent(txtAddress3);
				isAddress3Mandatory = s.endsWith("!");
			} else if (s.startsWith("A4")) {
				addComponent(txtAddress4);
				isAddress4Mandatory = s.endsWith("!");
			} else if (s.startsWith("C")) {
				addComponents(txtCity);
				isCityMandatory = s.endsWith("!");
			} else if (s.startsWith("P")) {
				addComponent(txtPostal);
				isPostalMandatory = s.endsWith("!");
			} else if (s.startsWith("A")) {
				addComponent(txtPostalAdd);
				isPostalAddMandatory = s.endsWith("!");
			} else if (s.startsWith("R") && m_location.getCountry().isHasRegion()) {
				addComponent(lstRegion);
				isRegionMandatory = s.endsWith("!");
			}
		}

		//      Fill it
		if (m_location.getC_Location_ID() != 0) {
			if (m_location.getAddress1() != null)
				txtAddress1.setValue(m_location.getAddress1());
			if (m_location.getAddress2() != null)
				txtAddress2.setValue(m_location.getAddress2());
			if (m_location.getAddress3() != null)
				txtAddress3.setValue(m_location.getAddress3());
			if (m_location.getAddress4() != null)
				txtAddress4.setValue(m_location.getAddress4());
			if (m_location.getCity() != null)
				txtCity.setText(m_location.getCity());
			if (m_location.getPostal() != null)
				txtPostal.setValue(m_location.getPostal());
			if (m_location.getPostal_Add() != null)
				txtPostalAdd.setValue(m_location.getPostal_Add());
		}

		setConfirmationButtons();

	}

	private void setRegionCaption() {
		if (m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName) != null
				&& m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName).trim().length() > 0)
			lstRegion.setCaption(m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName));
		else
			lstRegion.setCaption(Msg.getMsg(Env.getCtx(), "Region"));
	}

	private void countryChanged() {
		inCountryAction = true;
		MCountry c = hashMapCountries.get(lstCountry.getValue());
		m_location.setCountry(c);
		m_location.setC_City_ID(0);
		m_location.setCity(null);
		//  refresh
		initLocation();
		inCountryAction = false;
	}

	private void regionChanged() {
		if (inCountryAction || inOKAction)
			return;
		MRegion r = hashMapRegions.get(lstRegion.getValue());
		m_location.setRegion(r);
		m_location.setC_City_ID(0);
		m_location.setCity(null);
		//  refresh
		initLocation();
		lstRegion.focus();
	}

	private void onOK() {

		inOKAction = true;

		String msg = validateOK();
		if (msg != null) {
			Notification.show(Msg.getMsg(Env.getCtx(), "FillMandatory") + Msg.parseTranslation(Env.getCtx(), msg),
					Type.ERROR_MESSAGE);
			inOKAction = false;
			return;
		}

		if (actionOK()) {
			m_change = true;
			inOKAction = false;
			listener.onLocationOk(this);
		} else {
			Notification.show(Msg.getMsg(Env.getCtx(), "CityNotFound"),
					Type.ERROR_MESSAGE);
		}
		inOKAction = false;
	}

	// LCO - address 1, region and city required
	private String validateOK() {
		String fields = "";
		if (isAddress1Mandatory && txtAddress1.getValue().trim().length() == 0) {
			fields = fields + " " + "@Address1@, ";
		}
		if (isAddress2Mandatory && txtAddress2.getValue().trim().length() == 0) {
			fields = fields + " " + "@Address2@, ";
		}
		if (isAddress3Mandatory && txtAddress3.getValue().trim().length() == 0) {
			fields = fields + " " + "@Address3@, ";
		}
		if (isAddress4Mandatory && txtAddress4.getValue().trim().length() == 0) {
			fields = fields + " " + "@Address4@, ";
		}
		if (isCityMandatory && txtCity.getValue().trim().length() == 0) {
			fields = fields + " " + "@C_City_ID@, ";
		}
		if (isRegionMandatory && lstRegion.getValue() == null) {
			fields = fields + " " + "@C_Region_ID@, ";
		}
		if (isPostalMandatory && txtPostal.getValue().trim().length() == 0) {
			fields = fields + " " + "@Postal@, ";
		}
		if (isPostalAddMandatory && txtPostalAdd.getValue().trim().length() == 0) {
			fields = fields + " " + "@PostalAdd@, ";
		}

		if (fields.trim().length() > 0)
			return fields.substring(0, fields.length() -2);

		return null;
	}

	/**
	 *  OK - check for changes (save them) & Exit
	 */
	private boolean actionOK() {
		ServerContext.setCurrentInstance(WNavigatorUI.getContext());

		Trx trx = Trx.get(Trx.createTrxName("WLocationView"), true);
		trx.setDisplayName(getClass().getName()+"_action_Ok");
		m_location.set_TrxName(trx.getTrxName());
		m_location.setAddress1(txtAddress1.getValue());
		m_location.setAddress2(txtAddress2.getValue());
		m_location.setAddress3(txtAddress3.getValue());
		m_location.setAddress4(txtAddress4.getValue());
		m_location.setC_City_ID(txtCity.getC_City_ID()); 
		m_location.setCity(txtCity.getValue());
		m_location.setPostal(txtPostal.getValue());
		m_location.setPostal_Add(txtPostalAdd.getValue());
		//  Country/Region
		MCountry country = hashMapCountries.get(lstCountry.getValue());
		m_location.setCountry(country);
		if (country.isHasRegion() && lstRegion.getValue() != null) {
			MRegion r = hashMapRegions.get(lstRegion.getValue());
			m_location.setRegion(r);
		} else {
			m_location.setC_Region_ID(0);
		}

		boolean changedCity = m_location.is_ValueChanged(MLocation.COLUMNNAME_City) || m_location.is_ValueChanged(MLocation.COLUMNNAME_C_City_ID);
		boolean changedAddress1 = m_location.is_ValueChanged(MLocation.COLUMNNAME_Address1);
		boolean changedAddress2 = m_location.is_ValueChanged(MLocation.COLUMNNAME_Address2);
		boolean changedRegion = m_location.is_ValueChanged(MLocation.COLUMNNAME_RegionName) || m_location.is_ValueChanged(MLocation.COLUMNNAME_C_Region_ID);
		//  Save changes
		boolean success = m_location.save();
		if (success) {
			// IDEMPIERE-417 Force Update BPLocation.Name
			// just trigger BPLocation name change when the location change affects the name:
			// START_VALUE_BPLOCATION_NAME
			// 0 - City
			// 1 - City + Address1
			// 2 - City + Address1 + Address2
			// 3 - City + Address1 + Address2 + Region
			// 4 - City + Address1 + Address2 + Region + ID
			int bplocname = MSysConfig.getIntValue(MSysConfig.START_VALUE_BPLOCATION_NAME, 0, m_location.getAD_Client_ID(), m_location.getAD_Org_ID());
			if (bplocname < 0 || bplocname > 4)
				bplocname = 0;
			if (   changedCity
					|| (bplocname >= 1 && changedAddress1)
					|| (bplocname >= 2 && changedAddress2)
					|| (bplocname >= 3 && changedRegion)
					) {
				if (m_gridField != null && m_gridField.getGridTab() != null
						&& "C_BPartner_Location".equals(m_gridField.getGridTab().getTableName())) {
					m_gridField.getGridTab().setValue("Name", ".");
				} else {
					//Update BP_Location name IDEMPIERE 417
					int bplID = DB.getSQLValueEx(trx.getTrxName(), "SELECT C_BPartner_Location_ID FROM C_BPartner_Location WHERE C_Location_ID = " + m_location.getC_Location_ID());
					if (bplID>0) {
						MBPartnerLocation bpl = new MBPartnerLocation(Env.getCtx(), bplID, trx.getTrxName());
						bpl.setName(bpl.getBPLocName(m_location));
						success = bpl.save();
					}
				}
			}
		}
		if (success) {
			trx.commit();
		} else {
			trx.rollback();
		}
		trx.close();

		return success;
	}   //  actionOK

	public MLocation getLocation() {
		return m_location;
	}

	public WebField getWebField() {
		return webField;
	}

	public boolean isChanged() {
		return m_change;
	}

}
