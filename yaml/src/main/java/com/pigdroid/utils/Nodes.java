package com.pigdroid.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Nodes {

	private Nodes() {

	}

	public static List<JsonNode> readDeepIncludes(JsonNode mainTree, String path) throws JsonProcessingException, IOException {
		final List<String> remaining = new ArrayList<>();
		final List<JsonNode> ret = new ArrayList<>();
		remaining.addAll(refToFileName(findExternalReferences(mainTree)));
		while (!remaining.isEmpty()) {
			final String currentName = remaining.remove(0);
			final JsonNode current = readFile(path + "/" + currentName);
			remaining.addAll(refToFileName(findExternalReferences(current)));
			ret.add(current);
		}
		return ret;
	}

	public static Collection<? extends String> refToFileName(List<String> refs) {
		return refs.stream().map(Nodes::refToFileName).collect(Collectors.toList());
	}

	public static String refToFileName(String ref) {
		return ref.substring(0, ref.indexOf('#'));
	}

	public static JsonNode readFile(String fileName) throws IOException, JsonProcessingException {
		try (InputStream in = new FileInputStream(fileName)) {
            return readFile(in);
        }
	}

	public static JsonNode readFile(InputStream in) throws IOException, JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readTree(in);
	}

	public static List<String> findExternalReferences(JsonNode tree) {
		return find(tree,
				(each) -> each.has("$ref")
				&& each.get("$ref") != null
				&& !each.get("$ref").isNull()
				&& each.get("$ref").asText().indexOf('#') > 0).stream()
				.map(each -> each.get("$ref").asText()).collect(Collectors.toList());
	}

	public static List<JsonNode> find(List<JsonNode> tree, Predicate<JsonNode> condition) {
		return tree.stream().map(each -> find(each, condition)).flatMap(each -> each.stream()).collect(Collectors.toList());
	}

	public static List<JsonNode> find(JsonNode tree, Predicate<JsonNode> condition) {
		final List<JsonNode> ret = new ArrayList<>();
		traverse(tree, condition, ret::add);
		return ret;
	}

	public static void traverse(List<JsonNode> tree, Predicate<JsonNode> condition, Consumer<JsonNode> visitor) {
		tree.stream().forEach(each -> traverse(each, condition, visitor));
	}

	public static void traverse(JsonNode tree, Predicate<JsonNode> condition, Consumer<JsonNode> visitor) {
		if (condition.test(tree)) {
			visitor.accept(tree);
		}
		if (tree.isArray()) {
			final int max = tree.size();
			for (int i = 0; i < max; i++) {
				final JsonNode each = tree.get(i);
				traverse(each, condition, visitor);
			}
		} else {
			final Iterator<JsonNode> iterator = tree.elements();
			while (iterator.hasNext()) {
				traverse(iterator.next(), condition, visitor);
			}
		}
	}

	public static boolean isChild(JsonNode parent, JsonNode node) {
		if (parent != null) {
			final Iterator<JsonNode> iterator = parent.elements();
			while (iterator.hasNext()) {
				if (iterator.next() == node) {
					return true;
				}
			}
		}
		return false;
	}

	public static String getNameByParent(JsonNode root, JsonNode definition) {
		final List<JsonNode> list = find(root, (each) -> getChildren(each).contains(definition));
		final JsonNode parent = list.isEmpty() ? null : list.get(0);
		if (parent != null) {
			final Iterator<String> iterator = parent.fieldNames();
			while (iterator.hasNext()) {
				final String name = iterator.next();
				if (parent.get(name) == definition) {
					return name;
				}
			}
		}
		return null;
	}

	private static List<JsonNode> getChildren(JsonNode each) {
		final Iterator<JsonNode> iterator = each.elements();
		final List<JsonNode> ret = new ArrayList<>();
		while (iterator.hasNext()) {
			ret.add(iterator.next());
		}
		return ret;
	}

}
