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
package com.trekglobal.vaadin.mobile;

import java.io.File;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.compiere.Adempiere;
import org.compiere.util.CLogger;
import org.compiere.util.Ini;

/**
 * @author druiz
 */
public class MobileServlet
{
	/**	Logging									*/
	private static CLogger logger = CLogger.getCLogger(MobileServlet.class);

	public static void init(ServletConfig servletConfig) throws ServletException {

		/** Initialise context for the current thread*/
		String propertyFile = Ini.getFileName(false);
		File file = new File(propertyFile);
		if (!file.exists()) {
			throw new IllegalStateException("idempiere.properties is not setup. PropertyFile="+propertyFile);
		}
		if (!Adempiere.isStarted()) {
			boolean started = Adempiere.startup(false);
			if(!started) {
				throw new ServletException("Could not start iDempiere");
			}
		}

		logger.log(Level.OFF, "Vaadin Web Client started successfully");
		/**
		 * End iDempiere Start
		 */
	}
}
