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

import javax.servlet.ServletException;

import org.compiere.model.MSysConfig;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;

public class MobileVaadinServlet extends VaadinServlet implements SessionInitListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -791829446766865105L;

	@Override
	protected void servletInitialized() throws ServletException {
		super.servletInitialized();
		getService().addSessionInitListener(this);
	}

	@Override
	public void sessionInit(SessionInitEvent event) throws ServiceException {
		event.getSession().addBootstrapListener(new MyBootstrapListener());
	}

	private static class MyBootstrapListener implements BootstrapListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4418018072590536473L;

		@Override
		public void modifyBootstrapPage(BootstrapPageResponse response) {
			String zk_theme_value=MSysConfig.getValue("ZK_THEME");
			String urldef = "/webui/theme/"+zk_theme_value+"/images/icon.png";
			String imgdef = MSysConfig.getValue(MSysConfig.ZK_BROWSER_ICON, urldef);
			response.getDocument().head()
			.getElementsByAttributeValue("rel", "shortcut icon")
			.attr("href", imgdef);
			response.getDocument().head()
			.getElementsByAttributeValue("rel", "icon")
			.attr("href", imgdef);
		}

		@Override
		public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
		}
	}

}