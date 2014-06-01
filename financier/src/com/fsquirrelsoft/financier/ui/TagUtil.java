package com.fsquirrelsoft.financier.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fsquirrelsoft.financier.data.Tag;

public class TagUtil {

    public static List<IndentNode> toIndentNode(List<Tag> tagl) {
        List<IndentNode> better = new ArrayList<IndentNode>();
        Map<String, IndentNode> tree = new LinkedHashMap<String, IndentNode>();
        for (Tag tag : tagl) {
            String name = tag.getName();
            StringBuilder path = new StringBuilder();
            IndentNode node = null;
            String pp = null;
            String np = null;
            int indent = 0;
            for (String t : name.split("\\.")) {
                if (t.length() == 0) {
                    continue;
                }
                pp = path.toString();
                if (path.length() != 0) {
                    path.append(".");
                }
                np = path.append(t).toString();
                if ((node = tree.get(np)) != null) {
                    indent++;
                    continue;
                }
                node = new IndentNode(pp, t, indent, null);
                indent++;
                tree.put(np, node);
            }
            if (node != null) {
                node.tag = tag;
            }
        }

        for (String key : tree.keySet()) {
            IndentNode tn = tree.get(key);
            better.add(tn);
        }

        return better;
    }

    public static class IndentNode {
        private String path;
        private String name;
        private Tag tag;
        private int indent;
        private String fullpath;

        public IndentNode(String path, String name, int indent, Tag tag) {
            this.path = path;
            this.name = name;
            this.indent = indent;
            this.tag = tag;
            fullpath = (path == null || path.equals("")) ? name : path + "." + name;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public Tag getTag() {
            return tag;
        }

        public int getIndent() {
            return indent;
        }

        public String getFullPath() {
            return fullpath;
        }

    }
}
