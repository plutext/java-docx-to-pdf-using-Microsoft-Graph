package org.plutext.msgraph.convert.graphcore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.AuthConfigImpl;
import org.plutext.msgraph.convert.DocxToPDF;
import org.plutext.msgraph.convert.PRIVATE_AuthConfigImpl;
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
public class PdfConverterCore  extends DocxToPDF {

	private static final Logger LOG = LoggerFactory.getLogger(PdfConverterCore.class);
	
	public static void main(String[] args) throws IOException, InterruptedException {

		File inFile = new File(System.getProperty("user.dir")
				+ "/../sample-docx.docx");
//				+ "/79_half.docx"); // 413 : Request Entity Too Large
		
		DocxToPDF converter = new PdfConverterCore();
		byte[] pdf = converter.convert(inFile);
		        
        File file = new File(System.getProperty("user.dir")
				+ "/out.pdf");

        FileUtils.writeByteArrayToFile(file, pdf); ;//.copyInputStreamToFile(inputStream, file);
        System.out.println("saved " + file.getName());
		
	}
	
	

	@Override
	public byte[] convert(File inFile) throws IOException {
		
		MediaType mt = MediaType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document; charset=utf-8");
		// can create RequestBody from byte[] or FIle
		RequestBody body = RequestBody.create(mt, inFile);
		return convert(body);
		
	}

	@Override
	public byte[] convert(InputStream docx) throws IOException {
		
		return convert( IOUtils.toByteArray(docx) );
	}	
	
	
	@Override
	public byte[] convert(byte[] docx) throws IOException { 

		MediaType mt = MediaType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document; charset=utf-8");
		
		RequestBody body = RequestBody.create(mt, docx);
		return convert(body);
		
}

	/**
	 * We can create RequestBody from byte[] or File
	 * @param body
	 * @return
	 * @throws IOException 
	 */
	public byte[] convert(RequestBody body) throws IOException {
		
		AuthConfig authConfig = new AuthConfigImpl();
		
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

        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
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
        } catch (ClientException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}			
		
		// Move temp file to recycle
		path = "https://graph.microsoft.com/v1.0/sites/" + authConfig.site() + "/drive/items/" + item;  // filename is easier than item id here
		request = new Request.Builder().url(path).delete().build();
		response = client.newCall(request).execute();
		System.out.println("Delete? " + response.code());
		System.out.println(response.body().string());
		
		return pdf;

	}
	

}
