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

import org.compiere.model.MSession;
import org.compiere.util.Env;

/**
 *  Vaadin Application Environment and utilities
 *
 *  @author Diego Ruiz
 */
public class VEnv {
	
	public static final String LOCALE = "#Locale";
	
	/** Timeout - 15 Minutes                    */
	public static final int         TIMEOUT     = 15*60;

	/**
	 * logout AD_Session
	 */
	public static void logout()
	{
		//String sessionID = Env.getContext(Env.getCtx(), "#AD_Session_ID");
		//windowCache.remove(sessionID);
		//	End Session
		MSession session = MSession.get(Env.getCtx(), false);	//	finish
		if (session != null)
			session.logout();
		//
	}
}
