package org.plutext.msgraph.convert;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.httpclient.apache.ApacheHttpClient;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;

public class FileService {
	
	private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final ConfidentialClientApplication authenticationService;
    private HttpClient httpClient;
    private IAuthenticationResult authResult = null;

    public FileService(ConfidentialClientApplication authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    private CompletableFuture<HttpClient> getHttpClient() {
    	
    	return CompletableFuture.supplyAsync(new Supplier<HttpClient>() {
    		
    		//@Override
    		public HttpClient get() {

    	        if (httpClient != null) return httpClient;
    	        
    	    	// Use Scribe's approach to getting an HttpClient        
//    	        httpClient = new JDKHttpClient(JDKHttpClientConfig.defaultConfig()); // uses HttpURLConnection, but not async
    	        httpClient = new ApacheHttpClient();
    	        log.info("Using HTTP client implementation: " + httpClient.getClass().getName() );
    	        return httpClient;
    		}
    	});
    	
    }
    
    private Future<IAuthenticationResult> getBearerToken() {
    	    	        
	    	Set<String> scopes = new HashSet<String>();
	    	scopes.add("https://graph.microsoft.com/.default"); // see https://stackoverflow.com/questions/51781898/aadsts70011-the-provided-value-for-the-input-parameter-scope-is-not-valid 
//    	    	not scopes.add("Files.ReadWrite.All"); 
//    	    	com.microsoft.aad.msal4j.MsalServiceException: AADSTS70011: The provided request must include a 'scope' input parameter. 
//    	    	The provided value for the input parameter 'scope' is not valid. 
//    	    	The scope openid profile offline_access Files.ReadWrite.All openid is not valid.
	    	ClientCredentialParameters ccParameters = ClientCredentialParameters.builder(scopes).build();
	    	
	    	return authenticationService.acquireToken(ccParameters);
    	
    }
        
    public Future<Boolean> uploadStreamAsync(String requestUrl, byte[] bodyContents, String contentType) throws InterruptedException, ExecutionException {
    	
    	HttpClient client = getHttpClient().get();

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ContentType",  contentType);
        
        if (authResult == null) {
        	authResult = getBearerToken().get();
        }
        headers.put("Authorization",  "Bearer " + authResult.accessToken() );
        System.out.println(authResult.accessToken());
        // 'Accept':'application/json;odata.metadata=minimal'}
        headers.put("Accept",  "application/json;odata.metadata=minimal");
        
      System.out.println(requestUrl);
      OAuthRequest.ResponseConverter uploadResponseConverter = new UploadResponseConverter(); 
      OAuthAsyncRequestCallback callback = new UploadOAuthAsyncRequestCallback(); 
        return client.executeAsync("ScribeJava", headers, Verb.PUT, requestUrl, bodyContents, 
        		callback,  uploadResponseConverter);
        		
    }

    class UploadOAuthAsyncRequestCallback implements OAuthAsyncRequestCallback<Boolean> /* must match ResponseConverter */ {

		public void onCompleted(Boolean response) {
			System.out.println("UploadOAuthAsyncRequestCallback: " + response);  // fileid; its the response converter output
			
		}

		public void onThrowable(Throwable t) {
			t.printStackTrace();
			
		}
    	
    }
    
	class UploadResponseConverter implements OAuthRequest.ResponseConverter<Boolean> {
	
		public Boolean convert(Response response) throws IOException {
	        log.info("received response for upload");
	        String body=null; 
	        if (log.isInfoEnabled()) {
	            log.info("response status code: " + response.getCode());
	            body = response.getBody();
	            log.info("response body: " + body);
	        }
	        if (!response.isSuccessful() ) {
	        	log.warn(response.getBody());
		        response.close();
	        	return null;
	        }
	        response.close();
	        return true; 
	    }
	
	}
	
    public Future<byte[]> downloadConvertedFileAsync(String requestUrl) throws InterruptedException, ExecutionException {
    	
    	HttpClient client = getHttpClient().get();
                        
        Map<String, String> headers = new HashMap<String, String>();
//        headers.put("ContentType",  contentType);
        headers.put("Authorization",  "Bearer " + authResult.accessToken() );
        // 'Accept':'application/json;odata.metadata=minimal'}
//        headers.put("Accept",  "application/json;odata.metadata=minimal");
        
      System.out.println(requestUrl);
      byte[] nullBytes=null;
        return client.executeAsync("ScribeJava", headers, Verb.GET, requestUrl, nullBytes, 
        		new DownloadOAuthAsyncRequestCallback(), new DownloadResponseConverter() );
        		
    }
    
    class DownloadOAuthAsyncRequestCallback implements OAuthAsyncRequestCallback<byte[]> {

		public void onCompleted(byte[] response) {
			System.out.println("callback oncompleted: downloaded " + response.length + " bytes");
			
			
		}

		public void onThrowable(Throwable t) {
			t.printStackTrace();
			
		}
    	
    }
    

	class DownloadResponseConverter implements OAuthRequest.ResponseConverter<byte[]> {
		
		public byte[] convert(Response response) throws IOException {
	        log.info("received response for upload");
	        byte[] bytes = IOUtils.toByteArray(response.getStream());
	        response.close();
	        return bytes;
	    }
	
	}

    public Future<Boolean> deleteFileAsync(String requestUrl) throws InterruptedException, ExecutionException {
    	
    	HttpClient client = getHttpClient().get();
        
        Map<String, String> headers = new HashMap<String, String>();
//        headers.put("ContentType",  contentType);
        headers.put("Authorization",  "Bearer " + authResult.accessToken() );
        // 'Accept':'application/json;odata.metadata=minimal'}
//        headers.put("Accept",  "application/json;odata.metadata=minimal");
        
      System.out.println(requestUrl);
      byte[] nullBytes=null;
        return client.executeAsync("ScribeJava", headers, Verb.DELETE, requestUrl, nullBytes, null, new DeleteResponseConverter() );
        		
    }

	class DeleteResponseConverter implements OAuthRequest.ResponseConverter<Boolean> {
		
		public Boolean convert(Response response) throws IOException {
	        log.info("received response for delete");
	        String body=null; 
	        if (log.isInfoEnabled()) {
	            log.info("response status code: " + response.getCode());
	            body = response.getBody();
	            log.info("response body: " + body);
	        }
	        
	        return (response.isSuccessful());
	    }
	
	}
    
}
