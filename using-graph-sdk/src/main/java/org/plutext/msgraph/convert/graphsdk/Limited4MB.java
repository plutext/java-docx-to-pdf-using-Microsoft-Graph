/*
 *  Copyright 2020, Plutext Pty Ltd.
 *   
    This module is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.plutext.msgraph.convert.graphsdk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.AbstractOpenXmlToPDF;
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrate using the Graph SDK high level API for PDF Conversion.
 * 
 * This doesn't support converting files bigger than 4MB, so you should use PdfConverterLarge instead.  
 * 
 *
 * @author jharrop
 *
 */
public class Limited4MB extends AbstractOpenXmlToPDF {

	public Limited4MB(AuthConfig authConfig) {
		super(authConfig);
	}

	private static final Logger log = LoggerFactory.getLogger(Limited4MB.class);
			
	@Override
	public byte[] convert(byte[] bytes, String ext) throws ConversionException {
		// The client credentials flow requires that you request the
		// /.default scope, and pre-configure your permissions on the
		// app registration in Azure. An administrator must grant consent
		// to those permissions beforehand.
		final String[] scopes = new String[] { "https://graph.microsoft.com/.default" };

		// Authentication provider - Client credentials provider - Using a client secret
		// https://learn.microsoft.com/en-us/graph/sdks/choose-authentication-providers?tabs=java
		final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
				.clientId(authConfig.apiKey())
				.tenantId(authConfig.tenant())
				.clientSecret(authConfig.apiSecret())
				.build();

		final GraphServiceClient graphClient = new GraphServiceClient(credential, scopes);
		
        String tmpFileName = UUID.randomUUID().toString() + ".docx"; // an extension is required
		String itemPath =  "root:/" + tmpFileName +":";

		// Determined the driveId
		String driveId = graphClient.sites().bySiteId(authConfig.site()).drive().get().getId();

		// Upload
		DriveItem driveItem = graphClient.drives()
				.byDriveId(driveId)
				.items()
				.byDriveItemId(itemPath)
				.content()
				.put(new ByteArrayInputStream(bytes));

		// Download as pdf
		InputStream inputStream = graphClient.drives().byDriveId(driveId).items().byDriveItemId(itemPath).content().get(requestConfiguration -> {
			requestConfiguration.queryParameters.format = "pdf";
		});


		try {
			// Move to trash
			graphClient.drives().byDriveId(driveId).items().byDriveItemId(itemPath).delete();
			return IOUtils.toByteArray(inputStream);
		} catch (Exception e) {
			throw new ConversionException(e.getMessage(), e);
		}
		
	}

	public byte[] convert(File docx) throws ConversionException, IOException {
		
		String filename = docx.getName();
		String ext = filename.substring(filename.lastIndexOf("."));
		
		return convert( FileUtils.readFileToByteArray(docx), ext);
	}

	@Override
	public byte[] convert(InputStream docx, String ext) throws ConversionException, IOException {
		
		return convert( IOUtils.toByteArray(docx), ext );
	}	
	
	

}
