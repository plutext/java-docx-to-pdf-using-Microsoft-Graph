package org.plutext.msgraph.convert.scribe.adaption;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * In order to send resource param using scribejava 7.0.0; 
 * see https://github.com/scribejava/scribejava/pull/979
 * @author jharrop
 *
 */
public class OurOAuth20ServiceBridge {

	public OurOAuth20ServiceBridge(OAuth20Service authenticationService, MicrosoftAzureActiveDirectory20Api api) {
		this.authenticationService = authenticationService;
		this.api = api;
	}
	
	private final OAuth20Service authenticationService;
    private final MicrosoftAzureActiveDirectory20Api api; 
	

    public OAuth2AccessToken getAccessTokenClientCredentialsGrant()
            throws IOException, InterruptedException, ExecutionException {
    	
        final OAuthRequest request = createAccessTokenClientCredentialsGrantRequest();
        return sendAccessTokenRequestSync(request);
    }
    
    protected OAuthRequest createAccessTokenClientCredentialsGrantRequest() {

    	final OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());        
        
        api.getClientAuthentication().addClientAuthentication(request, authenticationService.getApiKey(), authenticationService.getApiSecret());

        request.addParameter(OAuthConstants.SCOPE, "openid Files.ReadWrite.All");
        request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.CLIENT_CREDENTIALS);

        request.addParameter("resource", "https://graph.microsoft.com"); // necessary for correct audience claim
        
        return request;
    }

    protected OAuth2AccessToken sendAccessTokenRequestSync(OAuthRequest request)
            throws IOException, InterruptedException, ExecutionException {
        try (Response response = execute(request)) {
            return api.getAccessTokenExtractor().extract(response);
        }
    }
    

    public Response execute(OAuthRequest request) throws InterruptedException, ExecutionException, IOException {
    	return authenticationService.execute(request);
    }    
}
