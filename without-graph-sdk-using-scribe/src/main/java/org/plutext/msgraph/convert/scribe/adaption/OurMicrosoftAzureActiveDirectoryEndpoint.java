package org.plutext.msgraph.convert.scribe.adaption;

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;

public class OurMicrosoftAzureActiveDirectoryEndpoint extends MicrosoftAzureActiveDirectory20Api {
	
    protected OurMicrosoftAzureActiveDirectoryEndpoint(String tenant) {
        super(tenant);
    }
	
    public static MicrosoftAzureActiveDirectory20Api custom(String tenant) {
        return new OurMicrosoftAzureActiveDirectoryEndpoint(tenant);
    }

    @Override
    protected String getEndpointVersionPath() {
        return "";
    }
	
}
