package org.plutext.msgraph.convert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.httpcore.HttpClients;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

import okhttp3.MediaType;
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
public class PdfConverterCore {

	private static final Logger LOG = LoggerFactory.getLogger(PdfConverterCore.class);
	
	public static void main(String[] args) throws IOException, InterruptedException {

		File inFile = new File(System.getProperty("user.dir")
//				+ "/../sample-docx.docx");
//				+ "/../79_half.docx"); // 413 : Request Entity Too Large
				+ "/toc.docx");
		
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

        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
		String item =  "root:/" + tmpFileName +":";	
		String path = "https://graph.microsoft.com/v1.0/sites/" + AuthConfig.siteId + "/drive/items/" + item + "/content";
		
		// Upload
		OkHttpClient client = HttpClients.createDefault(authProvider);
		MediaType mt = MediaType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document; charset=utf-8");
		RequestBody body = RequestBody.create(mt, inFile);
		Request request = new Request.Builder().url(path).put(body).build();
		Response response = client.newCall(request).execute();
//		System.out.println(response.body().string());


		// Convert/download
		request = new Request.Builder().url(path + "?format=pdf").build();
		response = client.newCall(request).execute();
		//System.out.println(response.body().string());
		try (
				InputStream inputStream = response.body().byteStream()
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
		
		// Move temp file to recycle
		path = "https://graph.microsoft.com/v1.0/sites/" + AuthConfig.siteId + "/drive/items/" + item;  // filename is easier than item id here
		request = new Request.Builder().url(path).delete().build();
		response = client.newCall(request).execute();
		System.out.println("Delete? " + response.code());
		System.out.println(response.body().string());
		

	}
	

}
