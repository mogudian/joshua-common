package com.mogudiandian.common.tree;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * 抽象可排序树
 * 非线程安全
 *
 * @param <V> 节点值类型
 * @param <I> 节点唯一标识类型
 * @param <N> 当前对象类型
 * @author sunbo
 * @since 2023/7/24
 */
public abstract class AbstractSortedTree<V, I, N extends AbstractSortedTreeNode<V, I, N>> extends AbstractTree<V, I, N> {

    public <C extends Collection<N>> AbstractSortedTree(C nodes) {
        super(nodes);
    }

    public <C extends Collection<N>> AbstractSortedTree(C nodes, OrphanPolicy orphanPolicy) {
        super(nodes, orphanPolicy);
    }

    public <C extends Collection<N>> AbstractSortedTree(C nodes, boolean noRoot) {
        super(nodes, noRoot);
    }

    public <C extends Collection<N>> AbstractSortedTree(C nodes, boolean noRoot, OrphanPolicy orphanPolicy) {
        super(nodes, noRoot, orphanPolicy);
    }

    @Override
    protected Set<N> initTopNodes() {
        return new TreeSet<>();
    }
}
