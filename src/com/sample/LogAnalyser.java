package com.sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LogAnalyser {

	public class Node {
		public Node(Node owner, String value) {
			this.owner = owner;
			this.value = value;
		}
		public Map<String, Node> children = new TreeMap<String, Node>();
		public Node owner;
		public String value;
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			help();
			return;
		}
		LogAnalyser analyzer = new LogAnalyser();
		analyzer.run(args);
	}

	private FileWriter out = null;

	private void run(String[] args) throws Exception {
		Node root = new Node(null, "");
		out = new FileWriter(args[1], false);
		FileReader inFile = new FileReader(args[0]);
		BufferedReader in = new BufferedReader(inFile);
		while (in.ready()) {
			String line = in.readLine();
			List<String> split = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
			if (split.size() < 2)
				continue;	// At least date and time should be
			String date = split.remove(0);
			String time = split.remove(0);
			split.add(date);
			split.add(time);
			if (!root.children.isEmpty())
				findAllPairs(root, split, 0, 1, "", "");
			Node node = root;
			for (String value : split) {
				if (!node.children.containsKey(value))
					node.children.put(value, new Node(node, value));
				node = node.children.get(value);
			}
		}
		in.close();
		out.flush();
		out.close();
	}

	private void findAllPairs(Node root, List<String> path, int index, int allowedDiff, String changedWord1, String changedWord2) throws Exception {
		if (index >= path.size() - 2) {
			out.append(path.get(path.size() - 2) + " ");	// Date
			out.append(path.get(path.size() - 1));	// Time
			for (int i = 0; i < path.size() - 2; i++)
				out.append(" " + path.get(i));
			out.append("\n");
			List<String> otherPath = new ArrayList<String>();
			for (Node node = root; node != null; node = node.owner)
				otherPath.add(0, node.value);
			otherPath.remove(0);	// remove root
			for (String date : root.children.keySet()) {
				for (String time : root.children.get(date).children.keySet()) {
					out.append(date + " ");
					out.append(time);
					for (String value : otherPath)
						out.append(" " + value);
					out.append("\n");
				}
			}
			if (allowedDiff == 0)
				out.append("The changing word was: " + changedWord1 + ", " + changedWord2 + "\n\n");
			else
				out.append("The log lines where identical\n\n");
		} else if (allowedDiff == 0) {
			String value = path.get(index);
			if (root.children.containsKey(value))
				findAllPairs(root.children.get(value), path, index + 1, allowedDiff, changedWord1, changedWord2);
		} else {
			String value = path.get(index);
			for (String otherValue : root.children.keySet()) {
				if (otherValue.equals(value)) {
					findAllPairs(root.children.get(otherValue), path, index + 1, allowedDiff, changedWord1, changedWord2);
				} else {
					findAllPairs(root.children.get(otherValue), path, index + 1, allowedDiff - 1, value, otherValue);
				}
			}
		}
	}

	private static void help() {
		System.out.print(
				"Use this program like this:\n" +
				"	java com.sample.LogAnalyser sample.log out.log\n"
				);
	}
}