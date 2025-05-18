package com.staticconstants.flowpad.frontend.folders;

import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderTreeBuilder {

    public static List<TreeItem<String>> buildTreeItems(List<List<String>> paths) {
        Map<String, TreeItem<String>> roots = new HashMap<>();

        for (List<String> path : paths) {
            if (path.isEmpty()) continue;
            String rootVal = path.get(0);
            TreeItem<String> rootItem = roots.computeIfAbsent(rootVal, TreeItem::new);

            TreeItem<String> current = rootItem;
            for (int i = 1; i < path.size(); i++) {
                String value = path.get(i);
                TreeItem<String> child = findChild(current, value);
                if (child == null) {
                    child = new TreeItem<>(value);
                    current.getChildren().add(child);
                }
                current = child;
            }
        }

        return new ArrayList<>(roots.values());
    }

    private static TreeItem<String> findChild(TreeItem<String> parent, String value) {
        for (TreeItem<String> child : parent.getChildren()) {
            if (child.getValue().equals(value)) {
                return child;
            }
        }
        return null;
    }
}
