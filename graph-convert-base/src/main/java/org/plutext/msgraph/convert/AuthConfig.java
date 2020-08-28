package org.plutext.msgraph.convert;

/**
 * For what to do here, please see https://medium.com/medialesson/convert-files-to-pdf-using-microsoft-graph-azure-functions-20bc84d2adc4 
 * 
 * The following may also help:

- https://docs.microsoft.com/en-us/graph/tutorials/java
- https://docs.microsoft.com/en-us/graph/auth-register-app-v2

 * @author jharrop
 *
 */
public interface AuthConfig  {
	
	
	/**
	 * Application (client) ID
	 */
	public String apiKey();

	/**
	 * Client secret
	 */
	public String apiSecret();

	/**
	 * Directory (tenant) ID
	 */
	public String tenant();

	/**
	 * Site ID
	 */
	public String site();

}

