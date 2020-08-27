package org.plutext.msgraph.convert;


import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.plutext.msgraph.convert.scribe.OurMicrosoftAzureActiveDirectoryEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;


/**
 * To use scribejava, we need 3 tweaks; see https://github.com/scribejava/scribejava/pull/979
 * Until that PR is implemented, we workaround locally.
 * 
 * @author jharrop
 *
 */
public class PdfConverter {
	
	private static final Logger LOG = LoggerFactory.getLogger(PdfConverter.class);
	
	
	public static void main(String[] args) {
		
		File f = new File(System.getProperty("user.dir")
				+ "/../sample-docx.docx");
		PdfConverter converter = new PdfConverter(f);
	}

	
	PdfConverter(File inFile) {

    	
    	// Seee https://docs.microsoft.com/en-us/azure/active-directory/azuread-dev/v1-oauth2-client-creds-grant-flow
		
		MicrosoftAzureActiveDirectory20Api api = OurMicrosoftAzureActiveDirectoryEndpoint.custom(AuthConfig.tenant());
		OAuth20Service azureAuthService = getAuthService(api);
		//System.out.println(azureAuthService.getAuthorizationUrl());

		try {

			FileService fs = new FileService(azureAuthService, api); 
			
			// Upload the file
			// Let's work with a known filename.  This way we can ignore the returned itemid (which we need JSON parsing to read)
	        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
			String item =  "root:/" + tmpFileName +":";	
			String path = "https://graph.microsoft.com/v1.0/sites/" + AuthConfig.siteId + "/drive/items/" + item + "/content";
			
			Boolean result = fs.uploadStreamAsync(path, FileUtils.readFileToByteArray(inFile), 
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document").get();
//			System.out.println(fileId);
			if (result==null || result.booleanValue()==false) {
				System.out.println("Upload failed, terminating.");
				return;
			}
			
			// Convert
			byte[] pdfBytes = fs.downloadConvertedFileAsync(path + "?format=pdf").get();
			//System.out.println(new String(pdfBytes));
			String sniffed = new String(pdfBytes, 0, 8);  // PDF?
			if (sniffed.startsWith("%PDF")) {
				System.out.println("PDF containing " + pdfBytes.length + " bytes");				
			} else {
				System.out.println("Not a PDF? " + sniffed );								
			}
			
			// Move temp file to recycle bin
			path = "https://graph.microsoft.com/v1.0/sites/" + AuthConfig.siteId + "/drive/items/" + item;  // filename is easier than item id here
			boolean deleted = fs.deleteFileAsync(path).get();
			System.out.println(deleted);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
	}
	
	
	private OAuth20Service getAuthService(MicrosoftAzureActiveDirectory20Api api /* O365Authentication authentication */) {
		try {
			LOG.debug("create connection with apiKey: {} apiSecret: {}", AuthConfig.apiKey(), AuthConfig.apiSecret());
			
			
			return new ServiceBuilder(AuthConfig.apiKey())
						.defaultScope("openid Files.ReadWrite.All")
					       .apiSecret(AuthConfig.apiSecret())
					.build(api);
		} catch (Exception e) {
			LOG.error("Office 365 authentication is misconfigured, original error was : {}", e.getMessage());
			LOG.debug("Office 365 authentication detail misconfiguration", e);
			throw new RuntimeException("Office 365 authentication is misconfigured", e);
		}
	}	
	
}
