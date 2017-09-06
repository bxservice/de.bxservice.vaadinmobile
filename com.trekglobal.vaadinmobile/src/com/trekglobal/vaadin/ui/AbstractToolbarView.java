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

import java.util.Properties;

import org.adempiere.util.ServerContext;

import com.vaadin.ui.CssLayout;

public abstract class AbstractToolbarView extends CssLayout implements IToolbarView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3558812737278075168L;
	protected boolean initialized = false;

	protected WNavigatorUI loginPage;
	protected String windowTitle ="";
    protected Properties ctx;
	
	//Main UI components
	protected CssLayout content;
	protected WHeader   header;
	
	public AbstractToolbarView(WNavigatorUI loginPage) {
		this.loginPage = loginPage;
		ctx = WNavigatorUI.getContext();
	}
	
	protected abstract void initComponents();
	protected abstract void init();
	
	public void initView() {
		//Avoid problem of double initialization
	    if (initialized)
	        return;
	    
        initComponents();
        init();
        initialized = true;
	}
	
	protected void syncCtx() {
		ServerContext.setCurrentInstance(ctx);
	}
	
	protected void openMenu() {
		loginPage.openMainMenu();
	}
	
	protected void backButton() {
		loginPage.onBackPressed();
	}
	
	public void logout() {
		loginPage.logout();
	}
	
	@Override
	public String getWindowTitle() {
		return windowTitle;
	}
	
	@Override
	public void onLeftButtonPressed() {		
	}

	@Override
	public void onRightButtonPressed() {
	}

}
