package org.plutext.msgraph.convert;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.scribe.OurOAuth20ServiceBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

public class FileService {
	
	private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final OAuth20Service authenticationService;
    private final MicrosoftAzureActiveDirectory20Api api; 
    private HttpClient httpClient;
    private String bearerToken = null;

    public FileService(OAuth20Service authenticationService, MicrosoftAzureActiveDirectory20Api api)
    {
        this.authenticationService = authenticationService;
        this.api = api;
    }

    private CompletableFuture<HttpClient> getHttpClient() {
    	
    	return CompletableFuture.supplyAsync(new Supplier<HttpClient>() {
    		
    		//@Override
    		public HttpClient get() {

    	        if (httpClient != null) return httpClient;
    	        
    	    	// Use Scribe's approach to getting an HttpClient
    	        // See the without-graph-sdk-using-msal4j for an example using scribejava-httpclient-apache
    	        httpClient = new JDKHttpClient(JDKHttpClientConfig.defaultConfig());
    	        return httpClient;
    		}
    	});
    	
    }
    
    private CompletableFuture<String> getBearerToken() {
    	
    	return CompletableFuture.supplyAsync(new Supplier<String>() {
    		
    		public String get() {

    	        if (bearerToken != null) return bearerToken;
    	        
    	        OAuth2AccessToken accessToken = null;
				try {
					OurOAuth20ServiceBridge bridge = new OurOAuth20ServiceBridge(authenticationService, api);
					accessToken = bridge.getAccessTokenClientCredentialsGrant();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
    	        bearerToken = accessToken.getAccessToken();
    	        System.out.println(bearerToken);
    	        return bearerToken;
    		}
    	});
    	
    }

        
    public Future<Boolean> uploadStreamAsync(String requestUrl, byte[] bodyContents, String contentType) throws InterruptedException, ExecutionException {
    	
    	HttpClient client = getHttpClient().get();
                
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ContentType",  contentType);
        headers.put("Authorization",  "Bearer " + getBearerToken().get() );
        // 'Accept':'application/json;odata.metadata=minimal'}
        headers.put("Accept",  "application/json;odata.metadata=minimal");
        
      System.out.println(requestUrl);
        return client.executeAsync("ScribeJava", headers, Verb.PUT, requestUrl, bodyContents, 
        		new UploadOAuthAsyncRequestCallback(), new UploadResponseConverter() );
        		
    }
    
    class UploadOAuthAsyncRequestCallback implements OAuthAsyncRequestCallback<Boolean> {

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
        
        //String requestUrl = path + fileId + "/content?format=" + targetFormat;
                
        Map<String, String> headers = new HashMap<String, String>();
//        headers.put("ContentType",  contentType);
        headers.put("Authorization",  "Bearer " + getBearerToken().get() );
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
        headers.put("Authorization",  "Bearer " + getBearerToken().get() );
        // 'Accept':'application/json;odata.metadata=minimal'}
//        headers.put("Accept",  "application/json;odata.metadata=minimal");
        
      System.out.println(requestUrl);
      byte[] nullBytes=new byte[0];
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
