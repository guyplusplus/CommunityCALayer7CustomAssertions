package community.layer7.customassertion.xmljsonTransform.transforms2;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

public class XMLNodeSpecObject extends XMLNodeSpec {
	
	private HashMap<String, PropertyXMLNodeSpec> xmlElements = new HashMap<String, PropertyXMLNodeSpec>();
	private HashMap<String, PropertyXMLNodeSpec> xmlAttributes = new HashMap<String, PropertyXMLNodeSpec>();
	private ArrayList<String> nonWrappedJSONArrayNames = new ArrayList<String>();
	private HashMap<String, PropertyXMLNodeSpec> refs = new HashMap<String, PropertyXMLNodeSpec>();
	
	public XMLNodeSpecObject() {
		super(TYPE_OBJECT);
	}
	
	@Override
	public void loadJSONValue(JSONObject schema, String valueDesriptionForException) throws JSONSchemaLoadException {
		super.loadJSONValue(schema, valueDesriptionForException);
		//load all properties
		JSONObject properties = schema.optJSONObject("properties");
		if(properties == null)
			throw new JSONSchemaLoadException("Properties can not be found for object '" + valueDesriptionForException + "'");
		for(String propertyName:properties.keySet()) {
			if(propertyName.trim().length() == 0)
				throw new JSONSchemaLoadException("One property name (after trim) is empty for object '" + valueDesriptionForException + "'");				
			JSONObject propertyObject = properties.getJSONObject(propertyName);
			//check for $ref
			String ref = propertyObject.optString("$ref", null);
			if(ref != null) {
				refs.put(propertyName, new PropertyXMLNodeSpec(propertyName, ref, null));
				continue;
			}
			String propertyType = propertyObject.optString("type", null);
			if(propertyType == null)
				throw new JSONSchemaLoadException("Type is not defined for property '" + propertyName + "' for object '" + valueDesriptionForException + "'");
			if(propertyType.equals("object")) {
				XMLNodeSpecObject innerObject = new XMLNodeSpecObject();
				innerObject.loadJSONValue(propertyObject, propertyName);
				PropertyXMLNodeSpec propertyXMLNodeSpec = new PropertyXMLNodeSpec(propertyName, null, innerObject);
				//String hashKey = calculateTargetXMLNodeName(propertyName, innerObject.getFullXMLName());
				String innerObjectXMLElementName = innerObject.calculateTargetFullXMLName(propertyName);
				checkKeyNotExistInHashMap(innerObjectXMLElementName, xmlElements, valueDesriptionForException);
				xmlElements.put(innerObjectXMLElementName, propertyXMLNodeSpec);
				System.out.println("added object XML element " + innerObjectXMLElementName + " to " + valueDesriptionForException);
				continue;
			}
			if(propertyType.equals("array")) {
				XMLNodeSpecArray innerObject = new XMLNodeSpecArray();
				innerObject.loadJSONValue(propertyObject, propertyName);
				PropertyXMLNodeSpec propertyXMLNodeSpec = new PropertyXMLNodeSpec(propertyName, null, innerObject);
				//String hashKey = calculateTargetXMLNodeName(propertyName, innerObject.getFullXMLName());
				String innerObjectXMLElementName = innerObject.calculateTargetFullXMLName(propertyName);
				checkKeyNotExistInHashMap(innerObjectXMLElementName, xmlElements, valueDesriptionForException);
				xmlElements.put(innerObjectXMLElementName, propertyXMLNodeSpec);
				System.out.println("added array XML element " + innerObjectXMLElementName + " to " + valueDesriptionForException);
				if(!innerObject.isXMLWrapped())
					nonWrappedJSONArrayNames.add(propertyName);
				continue;
			}
			int type = typeStringToTYPE(propertyType);
			if(type == TYPE_UNDEFINED)
				throw new JSONSchemaLoadException("Type is undefined for property '" + propertyName + "' for object '" + valueDesriptionForException + "'");
			XMLNodeSpecSimpleValue innerObject = new XMLNodeSpecSimpleValue(type);
			innerObject.loadJSONValue(propertyObject, propertyName);
			PropertyXMLNodeSpec propertyXMLNodeSpec = new PropertyXMLNodeSpec(propertyName, null, innerObject);
			//String hashKey = calculateTargetXMLNodeName(propertyName, innerObject.getFullXMLName());
			String innerObjectXMLElementName = innerObject.calculateTargetFullXMLName(propertyName);
			if(innerObject.isXMLAttribute()) {
				checkKeyNotExistInHashMap(innerObjectXMLElementName, xmlAttributes, valueDesriptionForException);
				xmlAttributes.put(innerObjectXMLElementName, propertyXMLNodeSpec);
				System.out.println("added simple value XML attribute " + innerObjectXMLElementName + " to " + valueDesriptionForException);
			}
			else {
				checkKeyNotExistInHashMap(innerObjectXMLElementName, xmlElements, valueDesriptionForException);
				xmlElements.put(innerObjectXMLElementName, propertyXMLNodeSpec);
				System.out.println("added simple value XML element " + innerObjectXMLElementName + " to " + valueDesriptionForException);
			}
		}
	}
	
	public ArrayList<String> getNonWrappedJSONArrayNames() {
		return nonWrappedJSONArrayNames;
	}
	
	public PropertyXMLNodeSpec getChildElementPropertyXMLNodeSpecByName(String name) {
		return xmlElements.get(name);
	}
	
	public PropertyXMLNodeSpec getAttributePropertyXMLNodeSpecByName(String attributeName) {
		return xmlAttributes.get(attributeName);
	}
	
	private static void checkKeyNotExistInHashMap(String key, HashMap<String, PropertyXMLNodeSpec> hashMap, String valueDesriptionForException) throws JSONSchemaLoadException {
		if(hashMap == null || key == null)
			return;
		if(hashMap.containsKey(key))
			throw new JSONSchemaLoadException("Element or property '" + key + "' is duplicated for object '" + valueDesriptionForException + "'");
	}

}
