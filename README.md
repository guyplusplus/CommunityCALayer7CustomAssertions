# Community-CALayer7-CustomAssertions

This repository is for the community to create and develop new Custom Assertions that will be deployed on the CA Layer7 API Gateway.

Layer7 API Gateway is a fine tool, though a number of features are not available, and custom script assertion is still not there as of v9.3. While it is often possible to implement these feature via Regular Expression or XSLT, it is often inelegant and slow.

Custom Assertion is the answer to this. It is a great and simple extension mechanism that Layer7 has built.

This product is widely deployed, and probably many developers are willing to share their own work.

To compile the code, simply drop all these directories inside the SDK at the same level of the TrafficLoggerSample folder. Then run the build command.

## String Simple Transform

This assertion allows simple transformation such as:
* trim spaces
* to upper case and lower case
* encore and decode hexadecimal strings in UTF-8 and UTF-16 character sets
* encode and decode JSON and XML 1.0 and 1.1 strings

![Dialog Screenshot](/StringSimpleTransform/DialogScreenShot.png)

It has been designed to support easy addition of a new type transformation without the need of the Custom Assertion SDK. The 'transforms' package is independent and can be compiled without the SDK.

## No Duplicate JSON Name

Duplicate name in a JSON object is actually allowed but not recommended. For example `{"a":1, "a":2}`. For reference [JSON Object RFC 7159](https://tools.ietf.org/html/rfc7159#section-4) where SHOULD is defined in [Key words RFC 2119](https://tools.ietf.org/html/rfc2119).

To avoid any doubt, this assertion parses a JSON message inside the request object ${request.mainpart} and detects any possible duplicated name in any object contained in it. In the case the JSON request can not be parsed, this assertion does not fail, the service logic continues.

The first duplicated name found is stored in the context variable 'duplicatedName', in a JSON path format, for example `$.a`. In case no duplication found, this variable is set to empty.

**Warning:** use this assertion after the request JSON object size is bounded, typically via a 'Protect Against JSON Document Structure Assertion' and set 'container depth' and 'object entry count' with reasonable values.

## XML-JSON Transform

This assertion transforms XML / JSON based on JSON schema included in the swagger document. This document contains XML hints that adds more capability. For more information:
* introduction: https://swagger.io/docs/specification/data-models/representing-xml/
* specifications: https://swagger.io/specification/#xmlObject

The assertion supports:
* schema caching
* $ref, which shall point to the definitions section
* for XML to JSON: if an empty non wrapped array (hence no XML element) creates an empty JSON array


![Dialog Screenshot](/XMLJSONTransform/DialogScreenShot.png)

_Schema caching_ is controlled with the 3 following cluster properties. Every 5 minutes these properties are refreshed:
* jsonxml.schemaCache.maxAge (in ms): entries older than this age are flushed. Set to -1 to avoid cache flush. Default is -1
* jsonxml.schemaCache.maxDownloadSize (in characters). Default is 128KB
* jsonxml.schemaCache.maxEntries. Set to 0 to avoid cache. Default is 128

Release History

|Version|Date|Details|
|---|---|---|
|0.8.0|28-Oct-18|Support for XML to JSON|
|0.8.1|4-Nov-18|Syntax highlighting<br>optimize code for xpath (used when raising exception)|
|0.9.0|8-Nov-18|JSON to XML implemented|

TODO (by order of priority):
* JSON to XML
* additionalProperties support:
  * true or false
* ability to control for XML to JSON if an empty XML non wrapped array (hence no XML element) creates an empty JSON array
* ability to control for XML to JSON space trim for string content (text node) and XML attributes (quote delimited)
* Support for const without type
* additionalProperties support:
  * with schema
* Support schema oneOf, anyOf, allOf, not
* support for $ref
  * a definitions in schema is just a proxy to another schema in definitions
  * support relative URI
  * external $ref
