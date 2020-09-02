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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOpenXmlToPDF implements OpenXmlToPDF {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractOpenXmlToPDF.class);
	
	public AbstractOpenXmlToPDF(AuthConfig authConfig) {
		 this.authConfig = authConfig;
	}
	
	protected AuthConfig authConfig;

	protected static final String DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	protected static final String PPTX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation"; 
	protected static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	
	protected static final String DOCX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document; charset=utf-8"; 
	protected static final String PPTX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation; charset=utf-8"; 
	protected static final String XLSX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=utf-8"; 
	
	
	protected String mimeTypeToExt(String mimetype) {
		
		// TODO dotx/dotm etc
		if (mimetype.startsWith(DOCX_MIME_TYPE)) {
			return ".docx";
		} else if  (mimetype.startsWith(PPTX_MIME_TYPE)) {
			return ".pptx";
		} else if  (mimetype.startsWith(XLSX_MIME_TYPE)) {
			return ".xlsx";
		}
		log.error("Unknown mimetype " + mimetype);
		return ".docx";
	}
	
}
