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

