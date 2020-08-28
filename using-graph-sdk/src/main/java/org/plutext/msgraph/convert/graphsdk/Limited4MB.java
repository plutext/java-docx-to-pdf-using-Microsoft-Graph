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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.DocxToPdfConverter;
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
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
public class Limited4MB extends DocxToPdfConverter {

	public Limited4MB(AuthConfig authConfig) {
		super(authConfig);
	}

	private static final Logger log = LoggerFactory.getLogger(Limited4MB.class);
		
	
	@Override
	public byte[] convert(byte[] docx) throws ConversionException {
		
		
    	List<String> scopes = new ArrayList<String>();
    	scopes.add("https://graph.microsoft.com/.default");
		ClientCredentialProvider authProvider = 
				new ClientCredentialProvider(authConfig.apiKey(), scopes, authConfig.apiSecret(), 
						authConfig.tenant(), NationalCloud.Global);	
		
//		Using msgraph-sdk-java
		IGraphServiceClient graphClient = GraphServiceClient
						.builder()
						.authenticationProvider(authProvider)
						.buildClient();

		// path = "https://graph.microsoft.com/v1.0/sites/" + siteId + "/drive/items/";
		
		
        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
        
//        String requestUrl = path +"root:/" + tmpFileName + ":/content";		
		String convertPathPrefix = "/sites/" + authConfig.site() + "/drive/items/";
		String item =  "root:/" + tmpFileName +":";	
		// or better, use buildRequest( requestOptions )

        // Note the obscure code
		MyCallback myCallback = new MyCallback(graphClient, convertPathPrefix, authConfig.site(), item);
		graphClient.sites(authConfig.site()).drive().items(item).content().buildRequest()
		.put(docx, myCallback );
		
		// wait
		try {
			myCallback.ft.get();
		} catch (Exception e) {
			throw new ConversionException(e.getMessage(), e);
		}
		return myCallback.pdf;
		
	}

	@Override
	public byte[] convert(File docx) throws ConversionException, IOException {
		
		return convert( FileUtils.readFileToByteArray(docx));
	}

	@Override
	public byte[] convert(InputStream docx) throws ConversionException, IOException {
		
		return convert( IOUtils.toByteArray(docx) );
	}	
	
	static class MyCallback implements ICallback<DriveItem> {

		MyCallback(IGraphServiceClient graphClient, String convertPathPrefix, String site, String item) {
			this.graphClient = graphClient;
			this.convertPathPrefix = convertPathPrefix;
			this.site = site;
			this.item = item;
		}
		
		String convertPathPrefix;
		String site;		
		String item;
		IGraphServiceClient graphClient;
		
		byte[] pdf;

		final FutureTask<Object> ft = new FutureTask<Object>(() -> {}, new Object());			
		
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
				pdf = IOUtils.toByteArray(inputStream);

	        } catch (ClientException e) {
	        	log.error(e.getMessage(), e);
	        	throw new RuntimeException(e.getMessage(), e);
			} catch (IOException e) {
	        	log.error(e.getMessage(), e);
	        	throw new RuntimeException(e.getMessage(), e);
			}			
						
			// Move to recycle bin
			graphClient.sites(site).drive().items(item)
					.buildRequest().delete();
			
			ft.run(); // so we can know this callback has been finished
			
		}

		@Override
		public void failure(ClientException ex) {
			log.error("Conversion failed", ex);
			
		}

		
	}


	

}
