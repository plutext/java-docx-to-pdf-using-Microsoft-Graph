package org.plutext.msgraph.convert;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;


public class PdfConverter {
	
	private static final Logger LOG = LoggerFactory.getLogger(PdfConverter.class);
		
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		File f = new File(System.getProperty("user.dir")
				+ "/../sample-docx.docx");
		PdfConverter converter = new PdfConverter(f);

	}
	
	PdfConverter(File inFile) throws InterruptedException, ExecutionException {
    	
    	// Seee https://docs.microsoft.com/en-us/azure/active-directory/azuread-dev/v1-oauth2-client-creds-grant-flow
    	
    	ConfidentialClientApplication confidentialClientApp =null;
		try {
			confidentialClientApp = ConfidentialClientApplication
			          .builder(AuthConfig.apiKey(), 
			        		  ClientCredentialFactory.createFromSecret(AuthConfig.apiSecret()))
			          //.authority("https://login.microsoftonline.com/common/oauth2/token") 
			          .authority("https://login.microsoftonline.com/" + AuthConfig.tenant() + "/oauth2/token")
			          .build();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}    	

		try {

			FileService fs = new FileService(confidentialClientApp); 
			
			// Upload the file
			// Let's work with a known filename.  This way we can ignore the returned itemid (which we need JSON parsing to read)
	        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
			String item =  "root:/" + tmpFileName +":";	
			String path = "https://graph.microsoft.com/v1.0/sites/" + AuthConfig.siteId + "/drive/items/" + item + "/content";
			
			
			// Upload the file
			Boolean result = fs.uploadStreamAsync(path, FileUtils.readFileToByteArray(inFile), 
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document").get();
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
	
	
	
}
