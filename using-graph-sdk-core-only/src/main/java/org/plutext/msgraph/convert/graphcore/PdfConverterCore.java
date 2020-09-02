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

package org.plutext.msgraph.convert.graphcore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.DocxToPdfConverter;
import org.plutext.msgraph.convert.AbstractOpenXmlToPDF;
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.httpcore.HttpClients;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Demonstrate using the Graph SDK high level API for PDF Conversion.
 * 
 * Currently limited to 4MB.
 * 
 * @author jharrop
 *
 */
public abstract class PdfConverterCore  extends AbstractOpenXmlToPDF {

	public PdfConverterCore(AuthConfig authConfig) {
		super(authConfig);
	}

	private static final Logger log = LoggerFactory.getLogger(PdfConverterCore.class);
	

	@Override
	public byte[] convert(InputStream docx, String ext) throws ConversionException, IOException {
		
		// RequestBody can't handle an input stream directly
		return convert( IOUtils.toByteArray(docx), ext );
	}	
	
	
	/**
	 * We can create RequestBody from byte[] or File
	 * @param body
	 * @return
	 * @throws IOException 
	 */
	public byte[] convert(RequestBody body, String ext) throws ConversionException, IOException {
		
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

        String tmpFileName = UUID.randomUUID()+ ext; 
		String item =  "root:/" + tmpFileName +":";	
		String path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item + "/content";
		
		// Upload
		OkHttpClient client = HttpClients.createDefault(authProvider);
		Request request = new Request.Builder().url(path).put(body).build();
		Response response = client.newCall(request).execute();
//		System.out.println(response.body().string());


		// Convert/download
		request = new Request.Builder().url(path + "?format=pdf").build();
		response = client.newCall(request).execute();
		//System.out.println(response.body().string());
		byte[] pdf = null;
		try (
				InputStream inputStream = response.body().byteStream()
        ) {
			
			pdf = IOUtils.toByteArray(inputStream);;
        } catch (ClientException e) {
        	throw new ConversionException(e.getMessage(), e);
        } 		
		
		// Move temp file to recycle
		path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item;  // filename is easier than item id here
		request = new Request.Builder().url(path).delete().build();
		response = client.newCall(request).execute();
		log.debug("Delete? " + response.code());
		log.debug(response.body().string());
		
		return pdf;

	}
	

}
