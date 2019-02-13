package com.pigdroid.openapi2proto.model.yaml;

import static com.pigdroid.utils.Nodes.find;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.pigdroid.utils.Nodes;
import com.pigdroid.utils.Strings;

public class YamlBean {

	private final JsonNode mainTree;
	private final List<JsonNode> includes;
	private final String name;

	public YamlBean(String fileName) throws JsonProcessingException, IOException {
		final File file = new File(fileName);
		this.name = Strings.capitalize(Strings.cleanFileName(file.getName()));
		this.mainTree = Nodes.readFile(fileName);
        this.includes = Nodes.readDeepIncludes(mainTree, file.getParent());
	}

	@Override
	public String toString() {
		return "YamlBean [mainTree=" + mainTree + ", includes=" + includes + "]";
	}

	public List<JsonNode> getDefinitions() {
		return getDefinitions(mainTree);
	}

	private List<JsonNode> getDefinitions(JsonNode root) {
		final JsonNode parent = find(root, (node) -> node.has("schemas")).get(0).get("schemas");
		return find(root, (node) -> {
			return Nodes.isChild(parent, node);
		});
	}

	public List<JsonNode> getOperations() {
		final List<JsonNode> parents = find(mainTree, (node) -> node.has("paths"));
		if (!parents.isEmpty()) {
			final JsonNode parent = parents.get(0).get("paths");
			return find(mainTree, (node) -> {
				return Nodes.isChild(parent, node);
			});
		}
		return Collections.emptyList();
	}

	public String getNodeName(JsonNode definition) {
		return Nodes.getNameByParent(this.mainTree, definition);
	}

	public List<String> getIncludes() {
		return Nodes.findExternalReferences(mainTree);
	}

	public JsonNode getDefinitionOrImport(String typeName) {
		JsonNode ret = getDefinitions().stream().filter(each -> typeName.equals(Nodes.getNameByParent(mainTree, each))).findFirst().orElse(null);
		if (ret == null) {
			for (final JsonNode root : includes) {
				ret = getDefinitions(root).stream().filter(each -> typeName.equals(Nodes.getNameByParent(root, each))).findFirst().orElse(null);
				if (ret != null) {
					break;
				}
			}
		}
		return ret;
	}

	public String getName() {
		return name;
	}

}
