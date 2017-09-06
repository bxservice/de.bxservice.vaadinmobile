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

import java.io.IOException;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;

public class OnDemandFileDownloader extends FileDownloader {

	/**
	 * Provide both the {@link StreamSource} and the filename in an on-demand way.
	 */
	public interface OnDemandStreamResource extends StreamSource {
		String getFilename();
	}

	private static final long serialVersionUID = 1L;
	private final OnDemandStreamResource onDemandStreamResource;

	public OnDemandFileDownloader (OnDemandStreamResource onDemandStreamResource) {
		super(new StreamResource(onDemandStreamResource, ""));
		this.onDemandStreamResource = onDemandStreamResource;
	}

	@Override
	public boolean handleConnectorRequest (VaadinRequest request, VaadinResponse response, String path)
			throws IOException {
		getResource().setFilename(onDemandStreamResource.getFilename());
		return super.handleConnectorRequest(request, response, path);
	}

	private StreamResource getResource () {
		return (StreamResource) this.getResource("dl");
	}

}
