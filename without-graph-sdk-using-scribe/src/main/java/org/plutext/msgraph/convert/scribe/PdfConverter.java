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

package org.plutext.msgraph.convert.scribe;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.DocxToPdfConverter;
import org.plutext.msgraph.convert.AbstractOpenXmlToPDF;
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;
import org.plutext.msgraph.convert.scribe.adaption.OurMicrosoftAzureActiveDirectoryEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.oauth.OAuth20Service;


/**
 * To use scribejava, we need 3 tweaks; see https://github.com/scribejava/scribejava/pull/979
 * Until that PR is implemented, we workaround locally.
 * 
 * @author jharrop
 *
 */
public abstract class PdfConverter  extends AbstractOpenXmlToPDF  {
	
	private static final Logger log = LoggerFactory.getLogger(PdfConverter.class);
	

	/**
	 * PdfConverter using scribe's JDKHttpClient
	 * @param authConfig
	 * @throws ConversionException 
	 */
	public PdfConverter(AuthConfig authConfig) throws ConversionException {
		super(authConfig);
    	
		// See https://docs.microsoft.com/en-us/azure/active-directory/azuread-dev/v1-oauth2-client-creds-grant-flow
		
		MicrosoftAzureActiveDirectory20Api api = OurMicrosoftAzureActiveDirectoryEndpoint.custom(authConfig.tenant());
		OAuth20Service azureAuthService = getAuthService(api, authConfig);
		//System.out.println(azureAuthService.getAuthorizationUrl());
		

		fs = new FileService(azureAuthService, api); 
	}
	
	/**
	 * PdfConverter using specified HttpClient, configured in your pom.
	 * @param authConfig
	 * @throws ConversionException 
	 */
	public PdfConverter(AuthConfig authConfig, HttpClient httpClient) throws ConversionException {
		super(authConfig);
    	
		// See https://docs.microsoft.com/en-us/azure/active-directory/azuread-dev/v1-oauth2-client-creds-grant-flow
		
		MicrosoftAzureActiveDirectory20Api api = OurMicrosoftAzureActiveDirectoryEndpoint.custom(authConfig.tenant());
		OAuth20Service azureAuthService = getAuthService(api, authConfig);
		//System.out.println(azureAuthService.getAuthorizationUrl());
		

		fs = new FileService(azureAuthService, api, httpClient); 
	}
	
	FileService fs = null ;
		

	
	public byte[] convertMime(byte[] docx, String mimetype) throws ConversionException {
		try {
			
			// Upload the file
			// Let's work with a known filename.  This way we can ignore the returned itemid (which we need JSON parsing to read)
	        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
			String item =  "root:/" + tmpFileName +":";	
			String path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item + "/content";
			
			Boolean result = fs.uploadStreamAsync(path, docx, mimetype).get();
//			System.out.println(fileId);
			if (result==null || result.booleanValue()==false) {
				throw new ConversionException("upload failed");
			}
			
			// Convert
			byte[] pdfBytes = fs.downloadConvertedFileAsync(path + "?format=pdf").get();
			
			// Move temp file to recycle bin
			path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item;  // filename is easier than item id here
			boolean deleted = fs.deleteFileAsync(path).get();
			log.debug(""+deleted);
			
			return pdfBytes;
			
		} catch (Exception e) {
			throw new ConversionException(e.getMessage(), e);			
		}
		
	}
	
	
	/**
	 * Note that JDKHttpClient does not support File payload
	 */
	public byte[] convertMime(File inFile, String mimetype) throws ConversionException, IOException {

		try {
			
			// Upload the file
			// Let's work with a known filename.  This way we can ignore the returned itemid (which we need JSON parsing to read)
	        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
			String item =  "root:/" + tmpFileName +":";	
			String path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item + "/content";
			
			
			// Upload the file
			Boolean result = fs.uploadStreamAsync(path, inFile, mimetype).get();
			if (result==null || result.booleanValue()==false) {
				throw new ConversionException("upload failed");
			}
			
			// Convert
			byte[] pdfBytes = fs.downloadConvertedFileAsync(path + "?format=pdf").get();
			
			// Move temp file to recycle bin
			path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item;  // filename is easier than item id here			
			boolean deleted = fs.deleteFileAsync(path).get();
			log.debug(""+deleted);
			
			return pdfBytes;
			
		} catch (Exception e) {
			throw new ConversionException(e.getMessage(), e);			
		}
	}
	
	
	@Override
	public byte[] convert(InputStream docx, String ext) throws ConversionException, IOException {
		return convert( IOUtils.toByteArray(docx), ext );
	}
	
	
	private OAuth20Service getAuthService(MicrosoftAzureActiveDirectory20Api api,
			AuthConfig authConfig) throws ConversionException {
		try {
			log.debug("create connection with apiKey: {} apiSecret: {}", authConfig.apiKey(), authConfig.apiSecret());
			
			
			return new ServiceBuilder(authConfig.apiKey())
						.defaultScope("openid Files.ReadWrite.All")
					       .apiSecret(authConfig.apiSecret())
					.build(api);
		} catch (Exception e) {
			log.error("Office 365 authentication is misconfigured, original error was : {}", e.getMessage());
			log.debug("Office 365 authentication detail misconfiguration", e);
			throw new ConversionException("Office 365 authentication is misconfigured", e);
		}
	}	
	
}
