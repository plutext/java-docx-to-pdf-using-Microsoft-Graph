# java-docx-to-pdf-using-Microsoft-Graph

This project shows you how to use Microsoft's Graph for docx to PDF conversion from Java.

(Similar code would work to convert doc or rtf to docx, or for pptx to pdf etc)

Different combinations of libraries can be used to do this, and the purpose of this
project is to make it easier for you to assess your options.     

The dimensions include:
- whether to use the Graph SDK or not, and if so, whether to use the high- or low-level API
- if you choose not to use the Graph SDK:
  - choice of authentication library (MSAL4J, or scribe)
  - choice of http library (scribe offers this, and we can use with MSAL4J as well) 

Each permutation is in its own module (aka sub-project).

Once you have chosen which module to use, you can incorporate it in your project. Note: the code is intended for ease of understanding; it is not necessarily production ready.  We're happy to accept PRs which improve it.

Which module works best for you may depend on:
- whether you are already using and familiar with a particular http library: the without-graph-sdk modules allow you to choose between the http libraries supported by scribe.  Note that the default - JDKHttpClient - is not actually async.  As an example, without-graph-sdk-using-msal4j is setup to use scribejava-httpclient-apache
- whether you need to handle docx files larger than 4MB
- whether you want to minimise the size of the dependencies (see the dependency trees at dependency-trees.txt)
- your sensibilities (using-graph-sdk is the most obscure, but supports big (4MB) files 

Whichever you choose, look at the graph-convert-sample module; specify your chosen module in the pom there.  
(Copy the pom and Java source code from this module into your IDE as a new project) 

Before you start, you'll need to set some stuff up in Microsoft's cloud.  I followed https://medium.com/medialesson/convert-files-to-pdf-using-microsoft-graph-azure-functions-20bc84d2adc4 but the following may also help:

- https://docs.microsoft.com/en-us/graph/tutorials/java
- https://docs.microsoft.com/en-us/graph/auth-register-app-v2

This info you then setup in the graph-convert-sample module's AuthConfigImpl.

Once you've done that, you can run https://github.com/plutext/java-docx-to-pdf-using-Microsoft-Graph/blob/master/graph-convert-sample/src/main/java/samples/ConvertDocxToPdfExample.java.  You'll need to specify a docx file of course.

Notes/caveats:

1.  Refreshing a token which is nearing expiry is a TODO.
2.  There is a 4MB upload limit.  This is currently only circumvented in the using-graph-sdk sub-project
(though it ought to be possible to do in some of the other modules; PR welcome)
3.  There doesn't seem to be a way to update a TOC (either page numbers or entries).  Vote for this enhancement at https://microsoftgraph.uservoice.com/forums/920506-microsoft-graph-feature-requests/suggestions/41235295-docx-to-pdf-file-conversion-update-table-of-conte 

