package dsa;

import java.util.*;

/**
 * Trie (Prefix Tree) data structure.
 * Used for efficient string operations like autocomplete, command parsing.
 */
public class Trie {
    private TrieNode root;

    private static class TrieNode {
        Map<Character, TrieNode> children;
        boolean isEndOfWord;
        String value; // Store complete word at end nodes

        TrieNode() {
            children = new HashMap<>();
            isEndOfWord = false;
        }
    }

    public Trie() {
        root = new TrieNode();
    }

    /**
     * Insert a word into the trie.
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) return;

        TrieNode current = root;
        for (char c : word.toLowerCase().toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
        }
        current.isEndOfWord = true;
        current.value = word;
    }

    /**
     * Search for exact word match.
     */
    public boolean search(String word) {
        TrieNode node = findNode(word);
        return node != null && node.isEndOfWord;
    }

    /**
     * Check if any word starts with given prefix.
     */
    public boolean startsWith(String prefix) {
        return findNode(prefix) != null;
    }

    /**
     * Get all words with given prefix (autocomplete).
     */
    public List<String> getWordsWithPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode node = findNode(prefix);

        if (node != null) {
            collectAllWords(node, results);
        }

        return results;
    }

    /**
     * Delete a word from the trie.
     */
    public boolean delete(String word) {
        return delete(root, word.toLowerCase(), 0);
    }

    private boolean delete(TrieNode current, String word, int index) {
        if (index == word.length()) {
            if (!current.isEndOfWord) {
                return false;
            }
            current.isEndOfWord = false;
            return current.children.isEmpty();
        }

        char c = word.charAt(index);
        TrieNode node = current.children.get(c);
        if (node == null) {
            return false;
        }

        boolean shouldDeleteChild = delete(node, word, index + 1);

        if (shouldDeleteChild) {
            current.children.remove(c);
            return current.children.isEmpty() && !current.isEndOfWord;
        }

        return false;
    }

    private TrieNode findNode(String prefix) {
        if (prefix == null) return null;

        TrieNode current = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            if (!current.children.containsKey(c)) {
                return null;
            }
            current = current.children.get(c);
        }
        return current;
    }

    private void collectAllWords(TrieNode node, List<String> results) {
        if (node.isEndOfWord) {
            results.add(node.value);
        }

        for (TrieNode child : node.children.values()) {
            collectAllWords(child, results);
        }
    }

    /**
     * Get count of all words in the trie.
     */
    public int getWordCount() {
        return countWords(root);
    }

    private int countWords(TrieNode node) {
        int count = node.isEndOfWord ? 1 : 0;
        for (TrieNode child : node.children.values()) {
            count += countWords(child);
        }
        return count;
    }

    /**
     * Find longest common prefix among all words.
     */
    public String getLongestCommonPrefix() {
        StringBuilder prefix = new StringBuilder();
        TrieNode current = root;

        while (current.children.size() == 1 && !current.isEndOfWord) {
            Map.Entry<Character, TrieNode> entry = 
                current.children.entrySet().iterator().next();
            prefix.append(entry.getKey());
            current = entry.getValue();
        }

        return prefix.toString();
    }
}
