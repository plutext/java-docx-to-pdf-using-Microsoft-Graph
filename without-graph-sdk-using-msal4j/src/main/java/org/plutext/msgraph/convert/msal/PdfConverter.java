package org.plutext.msgraph.convert.msal;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.AuthConfigImpl;
import org.plutext.msgraph.convert.DocxToPDF;
import org.plutext.msgraph.convert.PRIVATE_AuthConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;


public class PdfConverter  extends DocxToPDF  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PdfConverter.class);
		
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		
		File f = new File(System.getProperty("user.dir")
				+ "/../sample-docx.docx");
		
		PdfConverter converter = new PdfConverter();
		byte[] pdfBytes = converter.convert(f);
		
		//System.out.println(new String(pdfBytes));
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		if (sniffed.startsWith("%PDF")) {
			System.out.println("PDF containing " + pdfBytes.length + " bytes");				
		} else {
			System.out.println("Not a PDF? " + sniffed );								
		}
        
        File file = new File(System.getProperty("user.dir")
				+ "/out.pdf");

        FileUtils.writeByteArrayToFile(file, pdfBytes); ;//.copyInputStreamToFile(inputStream, file);
        System.out.println("saved " + file.getName());
		
	}

	PdfConverter() {

    	// Seee https://docs.microsoft.com/en-us/azure/active-directory/azuread-dev/v1-oauth2-client-creds-grant-flow
		
    	ConfidentialClientApplication confidentialClientApp =null;
		authConfig = new PRIVATE_AuthConfigImpl();//AuthConfigImpl();
		try {
			confidentialClientApp = ConfidentialClientApplication
			          .builder(authConfig.apiKey(), 
			        		  ClientCredentialFactory.createFromSecret(authConfig.apiSecret()))
			          //.authority("https://login.microsoftonline.com/common/oauth2/token") 
			          .authority("https://login.microsoftonline.com/" + authConfig.tenant() + "/oauth2/token")
			          .build();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}    	

		fs = new FileService(confidentialClientApp); 
		
	}
	FileService fs = null ;
	AuthConfig authConfig = null;
	
	@Override
	public byte[] convert(byte[] docx) {
		try {
			
			// Upload the file
			// Let's work with a known filename.  This way we can ignore the returned itemid (which we need JSON parsing to read)
	        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
			String item =  "root:/" + tmpFileName +":";	
			String path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item + "/content";
			
			
			// Upload the file
			Boolean result = fs.uploadStreamAsync(path, docx, 
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document").get();
			if (result==null || result.booleanValue()==false) {
				System.out.println("Upload failed, terminating.");
				throw new RuntimeException("upload failed");
			}
			
			// Convert
			byte[] pdfBytes = fs.downloadConvertedFileAsync(path + "?format=pdf").get();
			
			// Move temp file to recycle bin
			path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item;  // filename is easier than item id here			
			boolean deleted = fs.deleteFileAsync(path).get();
			System.out.println(deleted);
			
			return pdfBytes;
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public byte[] convert(InputStream docx) throws IOException {
		return convert( IOUtils.toByteArray(docx) );
	}	
	
	
	@Override
	public byte[] convert(File inFile) throws IOException {

		try {
			
			// Upload the file
			// Let's work with a known filename.  This way we can ignore the returned itemid (which we need JSON parsing to read)
	        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
			String item =  "root:/" + tmpFileName +":";	
			String path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item + "/content";
			
			
			// Upload the file
			Boolean result = fs.uploadStreamAsync(path, inFile, 
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document").get();
			if (result==null || result.booleanValue()==false) {
				System.out.println("Upload failed, terminating.");
				throw new RuntimeException("upload failed");
			}
			
			// Convert
			byte[] pdfBytes = fs.downloadConvertedFileAsync(path + "?format=pdf").get();
			
			// Move temp file to recycle bin
			path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item;  // filename is easier than item id here			
			boolean deleted = fs.deleteFileAsync(path).get();
			System.out.println(deleted);
			
			return pdfBytes;
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
