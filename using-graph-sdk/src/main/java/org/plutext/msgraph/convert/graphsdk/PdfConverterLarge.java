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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.AbstractOpenXmlToPDF;
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;
import org.plutext.msgraph.convert.DocxToPdfConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.core.exceptions.ClientException;
import com.microsoft.graph.core.models.IProgressCallback;
import com.microsoft.graph.core.models.UploadResult;
import com.microsoft.graph.core.tasks.LargeFileUploadTask;
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.DriveItemUploadableProperties;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.serviceclient.GraphServiceClient;

/**
 * Demonstrate using the Graph SDK high level API for PDF Conversion.
 * 
 * Supports converting large files, but won't update your TOC before doing so!
 * 
 * Vote for this enhancement at 
 * https://microsoftgraph.uservoice.com/forums/920506-microsoft-graph-feature-requests/suggestions/41235295-docx-to-pdf-file-conversion-update-table-of-conte 
 * 
 * @author jharrop
 *
 */
public class PdfConverterLarge  extends AbstractOpenXmlToPDF {

	public PdfConverterLarge(AuthConfig authConfig) {
		super(authConfig);
	}


	private static final Logger log = LoggerFactory.getLogger(PdfConverterLarge.class);
			

	public byte[] convert(InputStream fileStream, long streamSize, String ext) throws ConversionException, IOException {
		// TODO: Pending fix, com.microsoft.kiota.ApiException: generalException
		// https://github.com/microsoftgraph/msgraph-sdk-java/issues/1806

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
		
		
		String tmpFileName = UUID.randomUUID().toString() + ext;
		String itemPath =  "root:/" + tmpFileName +":";

		// Set body of the upload session request
		CreateUploadSessionPostRequestBody uploadSessionRequest = new CreateUploadSessionPostRequestBody();
		DriveItemUploadableProperties properties = new DriveItemUploadableProperties();
		properties.getAdditionalData().put("@microsoft.graph.conflictBehavior", "replace");
		uploadSessionRequest.setItem(properties);

		// Support more than 4MB, using large file uploader; see https://docs.microsoft.com/en-us/graph/sdks/large-file-upload?tabs=java
		String driveId = graphClient.sites().bySiteId(authConfig.site()).drive().get().getId();
		UploadSession uploadSession = graphClient.drives()
				.byDriveId(driveId)
				.items()
				.byDriveItemId(itemPath)
				.createUploadSession()
				.post(uploadSessionRequest);

		// Create the upload task
		int maxSliceSize = 10 * 320 * 1024;
		LargeFileUploadTask<DriveItem> largeFileUploadTask = null;
		try {
			largeFileUploadTask = new LargeFileUploadTask<>(
					graphClient.getRequestAdapter(),
					uploadSession,
					fileStream,
					streamSize,
					maxSliceSize,
					DriveItem::createFromDiscriminatorValue);
		} catch (IllegalAccessException e) {
			throw new ConversionException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new ConversionException(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new ConversionException(e.getMessage(), e);
		}

		int maxAttempts = 5;
		// Create a callback used by the upload provider
		IProgressCallback callbackVerbose = (current, max) -> log.debug(
				String.format("Uploaded %d bytes of %d total bytes", current, max));

		// Do the upload
		try {
			UploadResult<DriveItem> uploadResult = largeFileUploadTask.upload(maxAttempts, callbackVerbose);
			if (uploadResult.isUploadSuccessful()) {
				log.debug(
						String.format("Uploaded file with ID: %s", uploadResult.itemResponse.getId())
				);

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

			} else {
				log.debug(
					String.format("Failed uploading file.")
				);
			}
		} catch (CancellationException e) {
			log.debug("Error uploading: " + e.getMessage());
			throw new ConversionException(e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new ConversionException(e.getMessage(), e);
		}

		return  null;
	}	
	

	@Override
	public byte[] convert(byte[] docx, String ext) throws ConversionException {
		
		InputStream fileStream = new ByteArrayInputStream(docx);

		try {
			return convert( fileStream,  docx.length, ext);
		} catch (IOException e) {
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
		// inefficient, but we need length
		return convert( IOUtils.toByteArray(docx), ext );
	}
}
