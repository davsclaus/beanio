/*
 * Copyright 2011-2012 Kevin Seim
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.beanio.internal.util;

import java.util.*;

/**
 * A basic tree node implementation.  Each node holds references to its
 * children, and not its parent, thereby allowing a node to have multiple parents.
 * 
 * <p>Subclasses can override {@link #isSupportedChild(TreeNode)} to restrict
 * a node's children.
 * 
 * <p>A tree node is not thread safe.  Instead, <tt>TreeNode</tt> implements 
 * {@link Replicateable} so that an entire tree structure can be safely copied
 * to support multiple independent clients if needed.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
@SuppressWarnings("rawtypes")
public class TreeNode<T extends TreeNode> implements Replicateable, Iterable<T> {

    private String name;
    private List<T> children = null;
    
    /**
     * Constructs a new <tt>TreeNode</tt>.
     */
    public TreeNode() { 
        this(-1);
    }
    
    /**
     * Constructs a new <tt>TreeNode</tt>.
     * @param size the initial size of the node for accommodating children
     */
    public TreeNode(int size) {
        if (size == 0) {
            children = null;
        }
        else if (size < 0 || size > 10) {
            children = new ArrayList<T>();
        }
        else {
            children = new ArrayList<T>(size);
        }
    }
    
    /**
     * Returns the name of this node.
     * @return the node name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this node.
     * @param name the node name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the immediate children of this node.
     * @return the List of immediate children of this node
     */
    public List<T> getChildren() {
        if (children == null) {
            return Collections.emptyList();
        }
        else {
            return children;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<T> iterator() {
        assert children != null;
        return children.iterator();
    }
    
    /**
     * Returns the first child of this node.
     * @return the first child of this node
     * @throws NullPointerException if this node does not have any children
     */
    public T getFirst() {
        return children.get(0);
    }
    
    /**
     * Adds a child to this node.
     * @param child the child to add
     * @throws IllegalArgumentException if the child is not supported by this node 
     */
    public void add(T child) throws IllegalArgumentException {
        if (children == null) {
            children = new ArrayList<T>();
        }
        
        if (isSupportedChild(child)) {
            children.add(child);
        }
        else {
            throw new IllegalArgumentException("Child type not supported: " + child.getClass());
        }
    }
    
    /**
     * Returns whether a node is a supported child of this node.  Called
     * by {@link #add(TreeNode)}.
     * @param child the node to test
     * @return true if the child is allowed
     */
    protected boolean isSupportedChild(T child) {
        return true;
    }
    
    /**
     * Sorts all descendants of this node.
     * @param comparator the Comparator to use for comparing nodes
     */
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super T> comparator) {
        if (children == null) {
            return;
        }
        for (T child : children) {
            child.sort(comparator);
        }        
        Collections.sort(children, comparator);
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @SuppressWarnings("unchecked")
    public T clone() {
        try {
            return (T) super.clone();
        }
        catch (CloneNotSupportedException e) { 
            throw new IllegalStateException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.util.Replicateable#updateReferences(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public void updateReferences(Map<Object, Object> map) {
        if (children == null) {
            return;
        }
        if (children.isEmpty()) {
            children = null;
            return;
        }
        
        List<T> list = new ArrayList<T>(children.size());
        for (T node : children) {
            list.add((T) map.get(node));
        }
        this.children = list;
    }
    
    /**
     * Prints this node and its descendants to standard out.
     */
    public void print() {
        print("");
    }
    private void print(String indent) {
        System.out.println(indent + this);
        indent += "  ";
        for (T node : getChildren()) {
            node.print(indent);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getClass().getSimpleName()).append("[");
        toParamString(s);
        s.append("]");
        return s.toString();
    }
    
    /**
     * Called by {@link #toString()} to append node parameters to the output.
     * @param s the output to append
     */
    protected void toParamString(StringBuilder s) {
        s.append("name=").append(name);
    }
}
