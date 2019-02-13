package com.pigdroid.openapi2proto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.pigdroid.openapi2proto.model.proto.AbstractPropertyTypeBean;
import com.pigdroid.openapi2proto.model.proto.EnumDefinitionBean;
import com.pigdroid.openapi2proto.model.proto.EnumFieldBean;
import com.pigdroid.openapi2proto.model.proto.MessageDefinitionBean;
import com.pigdroid.openapi2proto.model.proto.MessageFieldBean;
import com.pigdroid.openapi2proto.model.proto.NamePropertyTypeBean;
import com.pigdroid.openapi2proto.model.proto.OneOfPropertyTypeBean;
import com.pigdroid.openapi2proto.model.proto.ProtoBean;
import com.pigdroid.openapi2proto.model.proto.RPCBean;
import com.pigdroid.openapi2proto.model.yaml.YamlBean;
import com.pigdroid.utils.Strings;

public class Main {

    private static final MessageDefinitionBean EMPTY = new MessageDefinitionBean("google.protobuf.Empty");

	public static void main(String[] args) throws IOException {
//        final YamlBean source = new YamlBean(new File("src/main/resources/main/commons.yaml").getAbsolutePath());
        final YamlBean source = new YamlBean(new File("src/main/resources/main/commonSorAPI.yaml").getAbsolutePath());
        final ProtoBean proto = process(source);
        try (OutputStream out = new FileOutputStream("out.txt")) {
            proto.write(out);
        }
        proto.print(System.out);
    }

    private static ProtoBean process(YamlBean source) {
        final ProtoBean proto = new ProtoBean(source.getName() + "Service");

        proto.addOption("java_multiple_files", "true");

        for (final String include : source.getIncludes()) {
            proto.addImport(toProtoFileName(include));
        }
        for (final JsonNode definition : source.getDefinitions()) {
            processDefinition(definition, source, proto);
        }
        for(final JsonNode definition : source.getDefinitions()) {
            processAllOfDefinition(definition, source, proto);
        }
        for (final JsonNode operation : source.getOperations()) {
        	processOperation(operation, source, proto);
        }
        return proto;
    }

    private static void processOperation(JsonNode operation, YamlBean source, ProtoBean proto) {
        final MessageDefinitionBean parameter = getParametersType(operation, source, proto);
        if (parameter != null && parameter != EMPTY) {
        	proto.addDefinition(parameter);
        }
        final MessageDefinitionBean response = getResponseType(operation, source, proto);
        if (response != null && response != EMPTY) {
        	proto.addDefinition(response);
        }
        proto.addOperation(new RPCBean(Strings.capitalize(operation.findValue("operationId").asText()), parameter, response));
	}

	private static MessageDefinitionBean getResponseType(JsonNode operation, YamlBean source, ProtoBean proto) {
		final JsonNode responses = operation.findValue("responses");
		if (responses != null && !responses.isNull()) {
			final JsonNode content = responses.findValue("content");
			if (content != null && !content.isNull()) {
				return getWrappedResponseType(operation, proto);
			} else {
				return getEmpty(proto);
			}
		}
		return getEmpty(proto);
	}

	private static String toProtoFileName(String include) {
        final String ret = include.substring(0, include.indexOf('#'));
        return ret.replace(".yaml", ".proto").replace(".yml", ".proto");
    }

    private static void processAllOfDefinition(JsonNode definition, YamlBean source, ProtoBean proto) {
        if (isAllOf(definition)) {
            createAllOfMessageDefinition(source, definition, proto);
        }
    }

    private static void processDefinition(JsonNode definition, YamlBean source, ProtoBean proto) {
        if (!isAllOf(definition)) {
            if (isEnum(definition)) {
                createEnumDefinition(source, definition, proto);
            } else {
                createMessageDefinition(source, definition, proto);
            }
        }
    }

    private static void createMessageDefinition(YamlBean source, JsonNode definition, ProtoBean proto) {
        final MessageDefinitionBean protoDef = new MessageDefinitionBean(source.getNodeName(definition));
        final JsonNode properties = definition.get("properties");
        final List<MessageFieldBean> protoProperties = getProperties(properties, proto);
        protoProperties.forEach(protoDef::addField);
        proto.addDefinition(protoDef);
    }

    private static List<MessageFieldBean> getProperties(JsonNode properties, ProtoBean proto) {
        final Iterator<String> propertyNames = properties.fieldNames();
        final List<MessageFieldBean> ret = new ArrayList<>();
        while (propertyNames.hasNext()) {
            final String propertyName = propertyNames.next();
            final JsonNode property = properties.get(propertyName);
            ret.add(getProperty(propertyName, property, proto));
        }
        return ret;
    }

    private static MessageFieldBean getProperty(String propertyName, JsonNode property, ProtoBean proto) {
        if (isArray(property)) {
            return getRepeatedProperty(propertyName, property, proto);
        } else {
            return getSingleProperty(propertyName, property, proto);
        }
    }

    private static MessageFieldBean getSingleProperty(String propertyName, JsonNode property, ProtoBean proto) {
        return new MessageFieldBean(propertyName, getPropertyType(property, proto), false);
    }

    private static MessageFieldBean getRepeatedProperty(String propertyName, JsonNode property, ProtoBean proto) {
        return new MessageFieldBean(propertyName, getPropertyType(property.get("items"), proto), false);
    }

    private static AbstractPropertyTypeBean getPropertyType(JsonNode property, ProtoBean proto) {
        if (isRef(property)) {
            return getTypeFromRef(property);
        } else if (isScalar(property)) {
            return getScalarType(property, proto);
        } else if (isOneOf(property)){
            return getOneOfType(property);
        }
        return null;
    }

    private static AbstractPropertyTypeBean getOneOfType(JsonNode property) {
        final OneOfPropertyTypeBean ret = new OneOfPropertyTypeBean();
        final JsonNode oneofs = property.has("oneOf") ? property.get("oneOf") : property.get("oneof");
        final int max = oneofs.size();
        for (int i = 0; i < max; i++) {
            final JsonNode oneof = oneofs.get(i);
            if (isRef(oneof)) {
                final AbstractPropertyTypeBean type = getTypeFromRef(oneof);
                ret.addField(new MessageFieldBean(getNameFromRef(oneof), type, false));
            }
        }
        return ret;
    }

    private static String getNameFromRef(JsonNode oneof) {
        return getNameFromRef(oneof.get("$ref").asText());
    }

    private static String getNameFromRef(String asText) {
        final String ret = asText.substring(asText.lastIndexOf('/') + 1);
        return ret.substring(0, 1).toLowerCase() + ret.substring(1);
    }

    private static boolean isOneOf(JsonNode property) {
        return property.has("oneOf") || property.has("oneof");
    }

    private static AbstractPropertyTypeBean getScalarType(JsonNode property, ProtoBean proto) {
        if (isOptional(property)) {
            return getScalarTypeWrapper(property, proto);
        } else {
            return getSimpleScalarType(property);
        }
    }

    private static AbstractPropertyTypeBean getScalarTypeWrapper(JsonNode property, ProtoBean proto) {
        if (isInteger(property)) {
            return getIntegerWraperScalarType(property, proto);
        } else if (isFloat(property)) {
            return getFloatWraperSalarType(property, proto);
        } else if (isString(property)) {
            return getStringWraperScalarType(property, proto);
        }
        return getSimpleStringScalarType(property);
    }

    private static AbstractPropertyTypeBean getStringWraperScalarType(JsonNode property, ProtoBean proto) {
    	proto.addImport("google/protobuf/wrappers.proto");
        return new NamePropertyTypeBean("StringValue");
    }

    private static AbstractPropertyTypeBean getFloatWraperSalarType(JsonNode property, ProtoBean proto) {
    	proto.addImport("google/protobuf/wrappers.proto");
        if (containsValue(property, "format", "double")) {
            return new NamePropertyTypeBean("DoubleValue");
        }
        return new NamePropertyTypeBean("FloatValue");
    }

    private static AbstractPropertyTypeBean getIntegerWraperScalarType(JsonNode property, ProtoBean proto) {
    	proto.addImport("google/protobuf/wrappers.proto");
        if (containsValue(property, "format", "int64")) {
            return new NamePropertyTypeBean("Int64Value");
        }
        return new NamePropertyTypeBean("Int32Value");
    }

    private static AbstractPropertyTypeBean getSimpleScalarType(JsonNode property) {
        if (isInteger(property)) {
            return getSimpleIntegerScalarType(property);
        } else if (isFloat(property)) {
            return getSimpleFloatSalarType(property);
        } else if (isString(property)) {
            return getSimpleStringScalarType(property);
        } else if (isBoolean(property)) {
        	return getSimpleBooleanType(property);
        }
        return getSimpleStringScalarType(property);
    }

    private static AbstractPropertyTypeBean getSimpleBooleanType(JsonNode property) {
        return new NamePropertyTypeBean("bool");
	}

	private static AbstractPropertyTypeBean getSimpleStringScalarType(JsonNode property) {
        return new NamePropertyTypeBean("string");
    }

    private static AbstractPropertyTypeBean getSimpleFloatSalarType(JsonNode property) {
        if (containsValue(property, "format", "double")) {
            return new NamePropertyTypeBean("double");
        }
        return new NamePropertyTypeBean("float");
    }

    private static AbstractPropertyTypeBean getSimpleIntegerScalarType(JsonNode property) {
        if (containsValue(property, "format", "int64")) {
            return new NamePropertyTypeBean("int64");
        }
        return new NamePropertyTypeBean("int32");
    }

    private static boolean isOptional(JsonNode property) {
        return containsValue(property, "nullable", "true") || containsValue(property, "required", "false");
    }

    private static boolean isScalar(JsonNode property) {
        return isFloat(property)
                || isInteger(property)
                || isBoolean(property)
                || isString(property);

    }

    private static boolean isString(JsonNode property) {
        return containsValue(property, "type", "string");
    }

    private static boolean isBoolean(JsonNode property) {
        return containsValue(property, "type", "boolean");
    }

    private static boolean isFloat(JsonNode property) {
        return containsValue(property, "type", "number");
    }

    private static boolean isInteger(JsonNode property) {
        return containsValue(property, "type", "integer");
    }

    private static AbstractPropertyTypeBean getTypeFromRef(JsonNode property) {
        return getTypeFromRef(property.get("$ref").asText());
    }

    private static AbstractPropertyTypeBean getTypeFromRef(String asText) {
        return new NamePropertyTypeBean(asText.substring(asText.lastIndexOf('/') + 1));
    }

    private static boolean isRef(JsonNode property) {
        return property.has("$ref");
    }

    private static boolean isArray(JsonNode property) {
        return containsValue(property, "type", "array")
        		&& property.has("items");
    }

    private static void createAllOfMessageDefinition(YamlBean source, JsonNode definition, ProtoBean proto) {
        final JsonNode allOfelements = definition.get("allOf");
        final int max = allOfelements.size();
        final List<MessageFieldBean> fields = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            final JsonNode allOfElement = allOfelements.get(i);
            fields.addAll(getAllOfFields(source, allOfElement, proto));
        }
        final MessageDefinitionBean protoDef = new MessageDefinitionBean(source.getNodeName(definition));
        fields.forEach(protoDef::addField);
        proto.addDefinition(protoDef);
    }

    private static List<MessageFieldBean> getAllOfFields(YamlBean source, JsonNode allOfElement, ProtoBean proto) {
        final List<MessageFieldBean> ret = new ArrayList<>();
        if (isRef(allOfElement)) {
        	final AbstractPropertyTypeBean refType = getTypeFromRef(allOfElement);
			final String typeName = refType.getName();
			final JsonNode node = source.getDefinitionOrImport(typeName);
			ret.addAll(getAllOfFields(source, node, proto));
        } else {
        	ret.addAll(getProperties(allOfElement.findValue("properties"), proto));
        }
        return ret;
    }

    private static boolean isAllOf(JsonNode definition) {
        return definition.has("allOf") && definition.get("allOf").isArray();
    }

    private static void createEnumDefinition(YamlBean source, JsonNode definition, ProtoBean proto) {
        final EnumDefinitionBean protoDef = new EnumDefinitionBean(source.getNodeName(definition));
        final JsonNode values = definition.get("enum");
        final int max = values.size();
        for (int i = 0; i < max; i++) {
            final JsonNode value = values.get(i);
            final EnumFieldBean field = new EnumFieldBean(value.asText());
            protoDef.addField(field);
        }
        proto.addDefinition(protoDef);
    }

    private static boolean isEnum(JsonNode definition) {
        return isStringType(definition) && definition.has("enum") && definition.get("enum").isArray();
    }

    private static boolean isStringType(JsonNode definition) {
        return isString(definition);
    }

    private static boolean containsValue(JsonNode node, String field, String value) {
        return node.has(field)
        		&& value.equalsIgnoreCase(node.get(field).asText());
    }

    private static MessageDefinitionBean getParametersType(JsonNode operation, YamlBean source, ProtoBean proto) {
    	if (operation.has("parameters")) {
    		if (operation.get("parameters").size() > 1) {
    			return getWrappedParameters(operation, proto);
//    		} else if (operation.get("parameters").size() == 1) {
//    			return getSingleParameter(operation);
//    		} else {
//    			return getEmptyType(operation);
    		}
    	}
    	return getEmpty(proto);

    }

	private static MessageDefinitionBean getEmpty(ProtoBean proto) {
		proto.addImport("google/protobuf/empty.proto");
		return EMPTY;
	}

	private static MessageDefinitionBean getWrappedParameters(JsonNode operation, ProtoBean proto) {
		final JsonNode parameters = operation.get("parameters");
		final int max = parameters.size();
		final MessageDefinitionBean ret = new MessageDefinitionBean(Strings.capitalize(operation.findValue("operationId").asText()) + "Request");
		for (int i = 0; i < max; i++) {
			final JsonNode parameter = parameters.get(i);
			final MessageFieldBean field = getParameterType(parameter, proto);
			ret.addField(field);
		}
		return ret;
	}

	private static MessageFieldBean getParameterType(JsonNode parameter, ProtoBean proto) {
		return getParameterType(parameter, proto, null);
	}

	private static MessageFieldBean getParameterType(JsonNode parameter, ProtoBean proto, String name) {
		return getProperty(name == null ? parameter.get("name").asText() : name, parameter, proto);
	}

	private static MessageDefinitionBean getWrappedResponseType(JsonNode operation, ProtoBean proto) {
		final JsonNode schema = operation.findValue("content").findValue("schema");
		//Ad hoc
		JsonNode properties = null;
		if (schema.findValue("properties") != null) {
			properties = schema.findValue(schema.findValue("properties").fieldNames().next());
		}
		final MessageDefinitionBean ret = new MessageDefinitionBean(Strings.capitalize(operation.findValue("operationId").asText()) + "Response");
		ret.addField(getParameterType(properties != null ? properties : schema, proto, "value"));
		return ret;
	}

}
