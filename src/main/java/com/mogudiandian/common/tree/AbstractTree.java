package com.mogudiandian.common.tree;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 抽象树
 * 非线程安全
 *
 * @param <V> 节点值类型
 * @param <I> 节点唯一标识类型
 * @param <N> 当前对象类型
 * @author sunbo
 * @since 2023/7/24
 */
public abstract class AbstractTree<V, I, N extends AbstractTreeNode<V, I, N>> {

    /**
     * 类型 树/森林
     */
    protected Type type;

    /**
     * 根节点 如果为树则不为空
     */
    protected N root;

    /**
     * 顶层节点 如果为森林则不为空 要保证不重复 所以用Set
     */
    protected Set<N> topNodes;

    /**
     * 节点数量 构造后则不变
     */
    private int size;

    /**
     * 构造树
     * @param nodes 未树化的节点集合
     * @param <C> 节点集合类型
     */
    public <C extends Collection<N>> AbstractTree(C nodes) {
        this(nodes, false);
    }

    /**
     * 构造树
     * @param nodes 未树化的节点集合
     * @param orphanPolicy 集合中的节点树化时的孤儿策略
     * @param <C> 节点集合类型
     */
    public <C extends Collection<N>> AbstractTree(C nodes, OrphanPolicy orphanPolicy) {
        this(nodes, false, orphanPolicy);
    }

    /**
     * 构造
     * @param nodes 未树化的节点集合
     * @param noRoot 构造树/森林 树为false 森林为true
     * @param <C> 节点集合类型
     */
    public <C extends Collection<N>> AbstractTree(C nodes, boolean noRoot) {
        this(nodes, noRoot, null);
    }

    /**
     * 构造
     * @param nodes 未树化的节点集合
     * @param noRoot 构造树/森林 树为false 森林为true
     * @param orphanPolicy 集合中的节点树化时的孤儿策略
     * @param <C> 节点集合类型
     */
    public <C extends Collection<N>> AbstractTree(C nodes, boolean noRoot, OrphanPolicy orphanPolicy) {
        if (nodes == null || nodes.isEmpty()) {
            throw new RuntimeException("Nodes can not be empty");
        }

        this.type = noRoot ? Type.FOREST : Type.TREE;

        treeize(nodes, noRoot, orphanPolicy);
    }

    /**
     * 树化
     * @param nodes 未树化的节点集合
     * @param noRoot 构造树/森林 树为false 森林为true
     * @param orphanPolicy 集合中的节点树化时的孤儿策略
     * @param <C> 节点集合类型
     */
    private <C extends Collection<N>> void treeize(C nodes, boolean noRoot, OrphanPolicy orphanPolicy) {
        // 按唯一标识分组
        Map<I, N> map = nodes.stream()
                             .collect(HashMap::new, (m, e) -> m.put(e.extractIdentifier(), e), Map::putAll);

        // 参数中孤儿的节点
        List<N> orphans = new LinkedList<>();

        // 遍历节点
        for (N node : nodes) {
            // 无效则跳过
            if (!node.isValidNode()) {
                continue;
            }

            // 顶层节点 加入到顶层
            if (node.isTopNode()) {
                if (noRoot) {
                    if (topNodes == null) {
                        topNodes = initTopNodes();
                    }
                    topNodes.add(node);
                } else if (root == null) {
                    root = node;
                } else {
                    throw new IllegalStateException("Found replicated root node " + node);
                }
                size++;
                continue;
            }

            // 父节点唯一标识
            I parentIdentifier = node.extractParentIdentifier();

            // 获取父节点
            N parentNode = map.get(parentIdentifier);

            // 父节点不为空 直接建立父子关系
            if (parentNode != null) {
                parentNode.addChild(node);
                size++;
            } else if (orphanPolicy == null || orphanPolicy == OrphanPolicy.DISCARD) {
                // 找不到父节点 孤儿策略是丢弃
            } else if (orphanPolicy == OrphanPolicy.REJECT) {
                // 找不到父节点 孤儿策略是拒绝
                throw new IllegalStateException("Cannot find parent node '" + parentIdentifier + "' for node '" + node.extractIdentifier() + "'");
            } else if (orphanPolicy == OrphanPolicy.TOP) {
                // 找不到父节点 孤儿策略是置顶
                orphans.add(node);
            }
        }

        // 如果需要置顶孤儿 这里将孤儿置顶
        if (!orphans.isEmpty()) {
            if (type == Type.FOREST) {
                if (topNodes == null) {
                    throw new IllegalStateException("Cannot process orphan nodes because this tree has no top nodes");
                }
                topNodes.addAll(orphans);
            } else {
                if (root == null) {
                    throw new IllegalStateException("Cannot process orphan nodes because this tree has no root");
                }
                nodes.forEach(root::addChild);
            }
            size += orphans.size();
        }
    }

    /**
     * 深度优先遍历
     * @param fromNodes 要从哪些节点开始遍历
     * @param visitor 访问到节点执行的函数 并返回是否遍历动作
     */
    public void dft(Collection<N> fromNodes, Function<N, TraversingAction> visitor) {
        for (LinkedList<N> linkedList = new LinkedList<>(fromNodes); !linkedList.isEmpty(); ) {
            N current = linkedList.removeFirst();
            TraversingAction action = visitor.apply(current);
            if (action == TraversingAction.STOP) {
                break;
            }
            if (action == TraversingAction.SKIP) {
                continue;
            }
            if (!current.isLeaf()) {
                // 这里要用头插法 将该元素的子节点按顺序加入到遍历列表中
                linkedList.addAll(0, current.getChildren());
            }
        }
    }

    /**
     * 深度优先遍历
     * @param fromNode 要从哪个节点开始遍历
     * @param visitor 访问到节点执行的函数 并返回是否遍历动作
     */
    public void dft(N fromNode, Function<N, TraversingAction> visitor) {
        dft(Collections.singletonList(fromNode), visitor);
    }

    /**
     * 深度优先遍历整个树
     * @param visitor 访问到节点执行的函数 并返回是否遍历动作
     */
    public void dft(Function<N, TraversingAction> visitor) {
        if (type == Type.TREE) {
            dft(root, visitor);
        } else {
            dft(topNodes, visitor);
        }
    }

    /**
     * 深度优先遍历
     * @param fromNodes 要从哪些节点开始遍历
     */
    public void dft(Collection<N> fromNodes, Consumer<N> consumer) {
        dft(fromNodes, continueTraversing(consumer));
    }

    /**
     * 深度优先遍历
     * @param fromNode 要从哪个节点开始遍历
     */
    public void dft(N fromNode, Consumer<N> consumer) {
        dft(fromNode, continueTraversing(consumer));
    }

    /**
     * 深度优先遍历整个树
     */
    public void dft(Consumer<N> consumer) {
        dft(continueTraversing(consumer));
    }

    /**
     * Consumer转为继续遍历的函数
     * @param consumer 消费函数
     * @return 永真的谓词
     */
    private Function<N, TraversingAction> continueTraversing(Consumer<N> consumer) {
        return x -> {
            consumer.accept(x);
            return TraversingAction.CONTINUE;
        };
    }

    /**
     * 扁平化
     * @param fromNodes 从哪些节点执行
     * @param predicate 判断是否需要当前节点
     * @return 扁平的节点集合
     */
    public List<N> flat(Collection<N> fromNodes, Predicate<N> predicate) {
        List<N> list = new ArrayList<>();
        dft(fromNodes, current -> {
            if (predicate.test(current)) {
                list.add(current);
            }
        });
        return list;
    }

    /**
     * 扁平化
     * @param fromNode 从哪个节点执行
     * @param predicate 判断是否继续处理子树的条件 返回true表示继续处理子树 返回false表示不处理子树
     * @return 扁平的节点集合
     */
    public List<N> flat(N fromNode, Predicate<N> predicate) {
        return flat(Collections.singletonList(fromNode), predicate);
    }

    /**
     * 扁平化
     * @param fromNodes 从哪些节点执行
     * @return 扁平的节点集合
     */
    public List<N> flat(Collection<N> fromNodes) {
        List<N> list = new ArrayList<>();
        dft(fromNodes, current -> {
            list.add(current);
        });
        return list;
    }

    /**
     * 扁平化
     * @param fromNode 从哪个节点执行
     * @return 扁平的节点集合
     */
    public List<N> flat(N fromNode) {
        return flat(Collections.singletonList(fromNode));
    }

    /**
     * 扁平化整个树
     * @param predicate 判断是否继续处理子树的条件 返回true表示继续处理子树 返回false表示不处理子树
     * @return 扁平的节点集合
     */
    public List<N> flat(Predicate<N> predicate) {
        if (type == Type.TREE) {
            return flat(root, predicate);
        } else {
            return flat(topNodes, predicate);
        }
    }

    /**
     * 扁平化整个树
     * @return 扁平的节点集合
     */
    public List<N> flat() {
        if (type == Type.TREE) {
            return flat(root);
        } else {
            return flat(topNodes);
        }
    }

    /**
     * 根据条件查找节点
     * @param fromNodes 从哪些节点开始查找
     * @param predicate 判断是否是待查找的节点
     * @return 查找到的节点 找不到则返回null
     */
    public N dfs(Collection<N> fromNodes, Predicate<N> predicate) {
        AtomicReference<N> nodeRef = new AtomicReference<>();
        dft(fromNodes, current -> {
            if (predicate.test(current)) {
                nodeRef.set(current);
            }
            return nodeRef.get() == null ? TraversingAction.CONTINUE : TraversingAction.STOP;
        });
        return nodeRef.get();
    }

    /**
     * 根据条件查找节点
     * @param fromNode 从哪个节点开始查找
     * @param predicate 判断是否是待查找的节点
     * @return 查找到的节点 找不到则返回null
     */
    public N dfs(N fromNode, Predicate<N> predicate) {
        return dfs(Collections.singletonList(fromNode), predicate);
    }

    /**
     * 根据条件从整个树查找节点
     * @param predicate 判断是否是待查找的节点
     * @return 查找到的节点 找不到则返回null
     */
    public N dfs(Predicate<N> predicate) {
        if (type == Type.TREE) {
            return dfs(root, predicate);
        }
        return dfs(topNodes, predicate);
    }

    /**
     * 获取树大小 也就是节点数量 使用构造后的节点数量 如果构造后不变 使用这个方法
     * @return 树大小
     */
    public int cachedSize() {
        return size;
    }

    /**
     * 获取树大小 也就是节点数量 如果构造后还有调整 使用这个方法获取实时数量
     * @return 树大小
     */
    public int size() {
        int[] temp = new int[]{0};
        dft(node -> {
            temp[0]++;
        });
        return temp[0];
    }

    /**
     * 输出树形结构
     * @param printer 输出函数
     * @param formatter 当前节点要输出的信息函数
     * @param firstPrefix 第一个前缀
     * @param otherPrefix 后续前缀
     */
    public void print(Consumer<String> printer, Function<N, String> formatter, String firstPrefix, String otherPrefix) {
        dft(x -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0, len = x.getLayer(); i < len; ) {
                stringBuilder.append(firstPrefix != null ? firstPrefix : otherPrefix);
                while (++i < len) {
                    stringBuilder.append(otherPrefix);
                }
            }
            stringBuilder.append(formatter.apply(x));
            printer.accept(stringBuilder.toString());
        });
    }

    /**
     * 输出树形结构
     * @param printer 输出函数
     * @param formatter 当前节点要输出的信息函数
     * @param prefix 前缀
     */
    public void print(Consumer<String> printer, Function<N, String> formatter, String prefix) {
        print(printer, formatter, null, prefix);
    }

    /**
     * 输出树形结构
     * @param printStream 输出流
     * @param formatter 当前节点要输出的信息函数
     * @param firstPrefix 第一个前缀
     * @param otherPrefix 后续前缀
     */
    public void print(PrintStream printStream, Function<N, String> formatter, String firstPrefix, String otherPrefix) {
        print(printStream::println, formatter, firstPrefix, otherPrefix);
    }

    /**
     * 输出树形结构
     * @param printStream 输出流
     * @param formatter 当前节点要输出的信息函数
     * @param prefix 前缀
     */
    public void print(PrintStream printStream, Function<N, String> formatter, String prefix) {
        print(printStream, formatter, null, prefix);
    }

    /**
     * 将树映射为Map
     * @param keyMapping 映射函数
     * @param predicate 判断是否需要当前节点
     * @return Map key为函数定义 value为节点
     * @param <T> key的类型
     */
    public <T> Map<T, N> toMap(Function<N, T> keyMapping, Predicate<N> predicate) {
        Map<T, N> map = new LinkedHashMap<>();
        dft(x -> {
            if (predicate.test(x)) {
                map.put(keyMapping.apply(x), x);
            }
        });
        return map;
    }

    /**
     * 将树映射为Map
     * @param keyMapping 映射函数
     * @return Map key为函数定义 value为节点
     * @param <T> key的类型
     */
    public <T> Map<T, N> toMap(Function<N, T> keyMapping) {
        Map<T, N> map = new LinkedHashMap<>();
        dft(x -> {
            map.put(keyMapping.apply(x), x);
        });
        return map;
    }

    /**
     * 子节点的初始化方法 默认使用LinkedList
     * @return 初始化方法
     */
    protected Set<N> initTopNodes() {
        return new LinkedHashSet<>();
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("type=" + getType())
                .add("size=" + size())
                .toString();
    }

    /**
     * 树的类型
     */
    public enum Type {
        /**
         * 树
         */
        TREE,

        /**
         * 森林
         */
        FOREST,
        ;
    }

    /**
     * 孤儿策略
     */
    public enum OrphanPolicy {

        /**
         * 丢弃
         */
        DISCARD,

        /**
         * 置顶
         */
        TOP,

        /**
         * 拒绝（抛出异常）
         */
        REJECT,
        ;
    }

    /**
     * 遍历动作
     */
    public enum TraversingAction {

        /**
         * 继续
         */
        CONTINUE,

        /**
         * 结束
         */
        STOP,

        /**
         * 跳过子节点
         */
        SKIP,
        ;
    }

}
