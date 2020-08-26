package org.plutext.msgraph.convert;
//import java.awt.List;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

/**
 * Demonstrate using the Graph SDK high level API for PDF Conversion.
 * 
 * This doesn't support converting files bigger than 4MB, so you should use PdfConverterLarge instead.  
 * 
 * Note, neither update your TOC. Vote for this enhancement at 
 * https://microsoftgraph.uservoice.com/forums/920506-microsoft-graph-feature-requests/suggestions/41235295-docx-to-pdf-file-conversion-update-table-of-conte 
 * 
 * @author jharrop
 *
 */
public class Limited4MB {

	private static final Logger LOG = LoggerFactory.getLogger(Limited4MB.class);
		
	public static void main(String[] args) throws IOException, InterruptedException {

		File inFile = new File(System.getProperty("user.dir")
				+ "/sample-docx.docx");
//				+ "/79_half.docx"); // 413 : Request Entity Too Large
		
    	List<String> scopes = new ArrayList<String>();
    	scopes.add("https://graph.microsoft.com/.default");
		ClientCredentialProvider authProvider = 
				new ClientCredentialProvider(AuthConfig.apiKey(), scopes, AuthConfig.apiSecret(), 
						AuthConfig.tenant(), NationalCloud.Global);	
		
//		Using msgraph-sdk-java
		IGraphServiceClient graphClient = GraphServiceClient
						.builder()
						.authenticationProvider(authProvider)
						.buildClient();

		// path = "https://graph.microsoft.com/v1.0/sites/" + siteId + "/drive/items/";
		
		
        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
        
//        String requestUrl = path +"root:/" + tmpFileName + ":/content";		
		String convertPathPrefix = "/sites/" + AuthConfig.siteId + "/drive/items/";
		String item =  "root:/" + tmpFileName +":";	
		// or better, use buildRequest( requestOptions )

        // Note the obscure code
		graphClient.sites(AuthConfig.siteId).drive().items(item).content().buildRequest()
		.put(FileUtils.readFileToByteArray(inFile), new MyCallback(graphClient, convertPathPrefix, item) );
		        
		Thread.sleep(10000);
		
	}
	
	static class MyCallback implements ICallback<DriveItem> {

		MyCallback(IGraphServiceClient graphClient, String convertPathPrefix, String item) {
			this.graphClient = graphClient;
			this.convertPathPrefix = convertPathPrefix;
			this.item = item;
		}
		
		String convertPathPrefix;
		String item;
		IGraphServiceClient graphClient;
		
		@Override
		public void success(DriveItem result) {

			System.out.println("it worked!");
			System.out.println(result.size);
			
//			Option format = new PdfOption("format", "pdf");
//			List<Option> requestOptions = new ArrayList<Option>();
//			requestOptions.add(format);
			
			try (
					BufferedInputStream inputStream = (BufferedInputStream)graphClient.customRequest(convertPathPrefix + item+ "/content?format=pdf", Stream.class)
					.buildRequest()
					.get();
					
					// Both the following ignores format option!
//					BufferedInputStream inputStream = (BufferedInputStream)graphClient.customRequest(convertPathPrefix + item+ "/content", Stream.class)
//					.buildRequest(requestOptions)
//					.get();
					// customRequest request works, but the following is neater
//					BufferedInputStream inputStream = (BufferedInputStream)graphClient.sites(siteId).drive().items(item).content()
//						.buildRequest( requestOptions ).get();
			
	        ) {

	            File file = new File(System.getProperty("user.dir")
	    				+ "/out.pdf");

	            FileUtils.copyInputStreamToFile(inputStream, file);
	            System.out.println("saved " + file.getName());
	        } catch (ClientException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}			
						
			// Move to recycle bin
			graphClient.sites(AuthConfig.siteId).drive().items(item)
					.buildRequest().delete();
		}

		@Override
		public void failure(ClientException ex) {
			System.out.println("failed :-(");
			
		}

		
	}
	

}
