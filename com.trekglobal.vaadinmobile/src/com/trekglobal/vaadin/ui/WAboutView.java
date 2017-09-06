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

import java.io.File;

import org.compiere.util.Env;
import org.compiere.util.Msg;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;

public class WAboutView extends AbstractToolbarView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3702086655872520698L;

	public static final String NAME = "WAbout";

	public WAboutView(WNavigatorUI loginPage) {
		super(loginPage);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		initView();		
	}

	@Override
	protected void initComponents() {
		syncCtx();
		String AD_Language = Env.getContext(ctx, Env.LANGUAGE);
		windowTitle = Msg.getMsg(AD_Language, "About");

		//Header
		header = new WHeader(this, true, true);
		header.setBackButton();

		//Content
		content = new CssLayout();
		content.addStyleName("login-content");

		Label title = new Label("Vaadin Client");
		title.addStyleName("about-logo");
		title.addStyleName("about-title");

		Label versionLabel = new Label("Version 1.0.0");
		versionLabel.addStyleName("about-logo");
		versionLabel.addStyleName("about-subs");

		Label sponsoredLabel = new Label("Sponsored by:");
		sponsoredLabel.addStyleName("about-logo");
		sponsoredLabel.addStyleName("about-subs");

		// Find the application directory
		String basepath = VaadinService.getCurrent()
				.getBaseDirectory().getAbsolutePath();

		// Image as a file resource
		FileResource resource = new FileResource(new File(basepath +
				"/WEB-INF/images/trek-logo.png"));

		// Show the image in the application
		Link iconic = new Link(null,
				new ExternalResource("http://www.trekglobal.com/"));
		iconic.setIcon(resource);
		iconic.setStyleName("about-logo");
		iconic.setTargetName("_blank");

		Link developedByLabel = new Link("Developed by: Diego Andres Ruiz Gomez",
				new ExternalResource("https://plus.google.com/+DiegoRuiz15"));
		developedByLabel.setTargetName("_blank");
		developedByLabel.addStyleName("about-logo");
		developedByLabel.addStyleName("about-dev");

		content.addComponent(title);
		content.addComponent(versionLabel);
		content.addComponent(sponsoredLabel);
		content.addComponent(iconic);
		content.addComponent(developedByLabel);
	}

	@Override
	protected void init() {
		setWidth("100%");

		// Enable Responsive CSS selectors for the component
		Responsive.makeResponsive(this);
		Responsive.makeResponsive(content);

		addComponents(header, content);
	}

	@Override
	public void onLeftButtonPressed() {
		backButton();
	}

}
