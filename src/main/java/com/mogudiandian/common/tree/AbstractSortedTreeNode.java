package com.mogudiandian.common.tree;

import java.util.Set;
import java.util.TreeSet;

/**
 * 有序的树节点 用于构造可排序树
 *
 * @param <V> 节点值类型
 * @param <I> 节点唯一标识类型
 * @param <N> 当前对象类型
 * @author Joshua Sun
 * @since 2023/7/24
 */
public abstract class AbstractSortedTreeNode<V, I, N extends AbstractSortedTreeNode<V, I, N>> extends AbstractTreeNode<V, I, N> implements Comparable<N> {

    public AbstractSortedTreeNode(V value) {
        super(value);
    }

    @Override
    protected Set<N> initChildren() {
        return new TreeSet<>();
    }

}
