package org.plutext.msgraph.convert;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.plutext.msgraph.convert.ConversionException;
import org.plutext.msgraph.convert.XlsxToPdfConverter;
import org.plutext.msgraph.convert.graphsdk.XlsxToPdfConverterLarge;

//import com.github.scribejava.httpclient.armeria.ArmeriaHttpClient;
//import com.github.scribejava.httpclient.ahc.AhcHttpClient;
import com.github.scribejava.httpclient.apache.ApacheHttpClient;
//import com.github.scribejava.httpclient.ning.NingHttpClient;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient;

import junit.framework.Assert;

//import com.github.scribejava.httpclient.apache.ApacheHttpClient;

public class ConvertXlsxileToPdfTest {

	static File inFile = new File(System.getProperty("user.dir")
			+ "/src/test/resources/test.xlsx");
	
	@Test
	public void testConversionSdk() throws IOException, ConversionException {

		XlsxToPdfConverter converter = new XlsxToPdfConverterLarge(new PRIVATE_AuthConfigImpl());
		byte[] pdfBytes = converter.convert(inFile);
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}

	@Test
	public void testConversionSdkCore() throws IOException, ConversionException {

		
		// First, using-graph-sdk
		XlsxToPdfConverter converter = new org.plutext.msgraph.convert.graphcore.XlsxToPdfConverter(new PRIVATE_AuthConfigImpl());
		byte[] pdfBytes = converter.convert(inFile);
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}

	@Test
	public void testConversionScribeDefaultClient() throws IOException, ConversionException {

		XlsxToPdfConverter converter = new org.plutext.msgraph.convert.scribe.XlsxToPdfConverter(new PRIVATE_AuthConfigImpl());
		// If you choose without-graph-sdk-using-scribe, you can optionally specify your preferred httpclient
		// (uncommented in your pom)
//        httpClient = new ApacheHttpClient();
//		XlsxToPdfConverter converter = new PdfConverterLarge(new AuthConfigImpl(), httpClient);
		byte[] pdfBytes = converter.convert(inFile);
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}

	@Test
	public void testConversionScribeApache() throws IOException, ConversionException {

		XlsxToPdfConverter converter = new org.plutext.msgraph.convert.scribe.XlsxToPdfConverter(new PRIVATE_AuthConfigImpl(), new ApacheHttpClient());
		byte[] pdfBytes = converter.convert(inFile);
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}

	@Test
	public void testConversionScribeOkHttp() throws IOException, ConversionException {

		XlsxToPdfConverter converter = new org.plutext.msgraph.convert.scribe.XlsxToPdfConverter(new PRIVATE_AuthConfigImpl(), new OkHttpHttpClient());
		byte[] pdfBytes = converter.convert(inFile);
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}

//	// TODO: Ning and AHC both need to be configured to follow redirects 
//	@Test
//	public void testConversionScribeNing() throws IOException, ConversionException {
//
//		XlsxToPdfConverter converter = new org.plutext.msgraph.convert.scribe.XlsxToPdfConverter(new PRIVATE_AuthConfigImpl(), new NingHttpClient());
//		byte[] pdfBytes = converter.convert(inFile);
//		
//		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
//		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
//		
//	}

//	@Test
//	public void testConversionScribeAhc() throws IOException, ConversionException {
//
//		XlsxToPdfConverter converter = new org.plutext.msgraph.convert.scribe.XlsxToPdfConverter(new PRIVATE_AuthConfigImpl(), new AhcHttpClient());
//		byte[] pdfBytes = converter.convert(inFile);
//		
//		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
//		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
//		
//	}

//	@Test
//	public void testConversionScribeArmeria() throws IOException, ConversionException {
//
//		XlsxToPdfConverter converter = new org.plutext.msgraph.convert.scribe.XlsxToPdfConverter(new PRIVATE_AuthConfigImpl(), new ArmeriaHttpClient());
//		byte[] pdfBytes = converter.convert(inFile);
//		
//		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
//		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
//		
//	}
	
	@Test
	public void testConversionMSAL4J() throws IOException, ConversionException {

		XlsxToPdfConverter converter = new org.plutext.msgraph.convert.msal.XlsxToPdfConverter(new PRIVATE_AuthConfigImpl());
		byte[] pdfBytes = converter.convert(inFile);
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}
	
}
