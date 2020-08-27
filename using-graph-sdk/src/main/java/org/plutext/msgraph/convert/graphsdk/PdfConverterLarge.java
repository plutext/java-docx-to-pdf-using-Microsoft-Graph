package org.plutext.msgraph.convert.graphsdk;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.AuthConfigImpl;
import org.plutext.msgraph.convert.DocxToPDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.DriveItemUploadableProperties;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.UploadSession;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

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
public class PdfConverterLarge  extends DocxToPDF {

	private static final Logger LOG = LoggerFactory.getLogger(PdfConverterLarge.class);
	
	public static void main(String[] args) throws IOException, InterruptedException {

		File inFile = new File(System.getProperty("user.dir")
				+ "/../sample-docx.docx");
//				+ "/79_half.docx"); 
		
		DocxToPDF converter = new PdfConverterLarge();
		byte[] pdf = converter.convert(inFile);
		        
        File file = new File(System.getProperty("user.dir")
				+ "/out.pdf");

        FileUtils.writeByteArrayToFile(file, pdf); ;//.copyInputStreamToFile(inputStream, file);
        System.out.println("saved " + file.getName());
		
	}
		
		

	public byte[] convert(InputStream fileStream, long streamSize) throws IOException {
		
		
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

		// path = "https://graph.microsoft.com/v1.0/sites/" + siteId + "/drive/items/";
		
		
        String tmpFileName = UUID.randomUUID()+ ".docx"; // TODO dotx/dotm etc
        
//        String requestUrl = path +"root:/" + tmpFileName + ":/content";		
		String convertPathPrefix = "/sites/" + authConfig.site() + "/drive/items/";
		String item =  "root:/" + tmpFileName +":";	

		
		// support more than 4MB, using large file uploader; see https://docs.microsoft.com/en-us/graph/sdks/large-file-upload?tabs=java

		// Create an upload session
		UploadSession uploadSession = graphClient
				.sites(authConfig.site()).drive().items(item)
		    .createUploadSession(new DriveItemUploadableProperties())
		    .buildRequest()
		    .post();

		ChunkedUploadProvider<DriveItem> chunkedUploadProvider =
		    new ChunkedUploadProvider<DriveItem>
		        (uploadSession, graphClient, fileStream, streamSize, DriveItem.class);

		// Config parameter is an array of integers
		// customConfig[0] indicates the max slice size
		// Max slice size must be a multiple of 320 KiB
		int[] customConfig = { 320 * 1024 };

		// Do the upload
		MyCallback myCallback = new  MyCallback(graphClient, convertPathPrefix, authConfig.site(), item);
		
		chunkedUploadProvider.upload(
				myCallback, 
				customConfig);
		
		// wait
		try {
			myCallback.ft.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return myCallback.pdf;
		
	}	
	

	@Override
	public byte[] convert(byte[] docx) throws IOException {
		
		InputStream fileStream = new ByteArrayInputStream(docx);

		return convert( fileStream,  docx.length);
		
	}

	@Override
	public byte[] convert(File docx) throws IOException {
		return convert( FileUtils.readFileToByteArray(docx));
	}
	
	@Override
	public byte[] convert(InputStream docx) throws IOException {
		// inefficient, but we need length
		return convert( IOUtils.toByteArray(docx) );
	}	
	
	
	static class MyCallback implements IProgressCallback<DriveItem> {

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
	        System.out.println(
		            String.format("Uploaded file with ID: %s", result.id)
		        );
			System.out.println(result.size);
			
			//         String requestUrl = path + fileId + "/content?format=" + targetFormat;
//			graphClient.sites(siteId).drive().items("root:/" +tmpFileName+":")..content()..buildRequest()

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
	        } catch (ClientException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}			
						
			// Move to recycle bin
			graphClient.sites(site).drive().items(item)
					.buildRequest().delete();
			
			ft.run(); // so we can know this callback has been finished
			
		}


	    public void failure(final ClientException ex) {
	        System.out.println(
	            String.format("Error uploading file: %s", ex.getMessage())
	        );
	    }

	    @Override
	    // Called after each slice of the file is uploaded
	    public void progress(final long current, final long max) {
	        System.out.println(
	            String.format("Uploaded %d bytes of %d total bytes", current, max)
	        );
	    }

		
	}


	
}
