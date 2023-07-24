# joshua-common

工作中总结的各种通用的类，均在生产环境中稳定运行

## 引用项目

```xml
<dependency>
    <groupId>com.mogudiandian</groupId>
    <artifactId>joshua-common</artifactId>
    <version>LATEST</version>
</dependency>
```

## 工具说明

### tree
#### AbstractTree & AbstractTreeNode 通用的树结构
```java
public class TreeTest {

    public static void main(String[] args) {
        List<Department> list = new ArrayList<>();

        list.add(new Department("tech", "产品研发中心", null));
        list.add(new Department("opr", "运营中心", null));
        list.add(new Department("admin", "行政中心", null));
        list.add(new Department("boss", "总裁办", null));
        list.add(new Department("product", "产品部", "tech"));
        list.add(new Department("develop", "研发部", "tech"));
        list.add(new Department("product-mw", "中台产品部", "product"));
        list.add(new Department("tech-be", "后台研发部", "develop"));
        list.add(new Department("tech-mw", "中台研发部", "develop"));
        list.add(new Department("tech-fe", "前台研发部", "develop"));
        list.add(new Department("sec", "秘书部", "boss"));
        list.add(new Department("admin2", "行政部", "admin"));
        list.add(new Department("hr", "人力资源部", "admin"));
        list.add(new Department("job", "招聘组", "hr"));
        list.add(new Department("salary", "薪酬组", "hr"));
        list.add(new Department("opr-sku", "商品运营部", "opr"));
        list.add(new Department("opr-act", "活动运营部", "opr"));
        list.add(new Department("opr-mw", "中台运营部", "opr"));

        Collections.shuffle(list);

        List<DepartmentTreeNode> nodes = list.stream()
                                             .map(DepartmentTreeNode::new)
                                             .collect(Collectors.toList());

        DepartmentTree departmentTree = new DepartmentTree(nodes);

        departmentTree.print(System.out, x -> x.getValue().name, "+-", "--");
    }

    public static class Department {
        protected final String id;
        protected final String name;
        protected final String parentId;

        public Department(String id, String name, String parentId) {
            this.id = id;
            this.name = name;
            this.parentId = parentId;
        }
    }

    private static class DepartmentTreeNode extends AbstractTreeNode<Department, String, DepartmentTreeNode> {

        public DepartmentTreeNode(Department value) {
            super(value);
        }

        @Override
        protected boolean isTopNode() {
            return value.parentId == null;
        }

        @Override
        protected String extractIdentifier() {
            return value.id;
        }

        @Override
        protected String extractParentIdentifier() {
            return value.parentId;
        }
    }

    private static class DepartmentTree extends AbstractTree<Department, String, DepartmentTreeNode> {

        public <C extends Collection<DepartmentTreeNode>> DepartmentTree(C nodes) {
            super(nodes, true, OrphanPolicy.TOP);
        }
    }

}
```
#### AbstractSortedTree & AbstractSortedTreeNode 通用的可排序树结构
```java
public class SortedTreeTest {

    public static void main(String[] args) {
        List<SortedDepartment> list = new ArrayList<>();

        list.add(new SortedDepartment("tech", "产品研发中心", null, 3));
        list.add(new SortedDepartment("opr", "运营中心", null, 4));
        list.add(new SortedDepartment("admin", "行政中心", null, 2));
        list.add(new SortedDepartment("boss", "总裁办", null, 1));
        list.add(new SortedDepartment("product", "产品部", "tech", 1));
        list.add(new SortedDepartment("develop", "研发部", "tech", 2));
        list.add(new SortedDepartment("product-mw", "中台产品部", "product", 1));
        list.add(new SortedDepartment("tech-be", "后台研发部", "develop", 1));
        list.add(new SortedDepartment("tech-mw", "中台研发部", "develop", 2));
        list.add(new SortedDepartment("tech-fe", "前台研发部", "develop", 3));
        list.add(new SortedDepartment("sec", "秘书部", "boss", 1));
        list.add(new SortedDepartment("admin2", "行政部", "admin", 1));
        list.add(new SortedDepartment("hr", "人力资源部", "admin", 2));
        list.add(new SortedDepartment("job", "招聘组", "hr", 1));
        list.add(new SortedDepartment("salary", "薪酬组", "hr", 2));
        list.add(new SortedDepartment("opr-sku", "商品运营部", "opr", 2));
        list.add(new SortedDepartment("opr-act", "活动运营部", "opr", 3));
        list.add(new SortedDepartment("opr-mw", "中台运营部", "opr", 1));

        Collections.shuffle(list);

        List<SortedDepartmentTreeNode> nodes = list.stream()
                                                   .map(SortedDepartmentTreeNode::new)
                                                   .collect(Collectors.toList());

        SortedDepartmentTree sortedDepartmentTree = new SortedDepartmentTree(nodes);

        sortedDepartmentTree.print(System.out, x -> x.getValue().order + "." + x.getValue().name, "+-", "--");
    }

    private static class SortedDepartment extends TreeTest.Department {
        private final int order;

        public SortedDepartment(String id, String name, String parentId, int order) {
            super(id, name, parentId);
            this.order = order;
        }
    }

    private static class SortedDepartmentTreeNode extends AbstractSortedTreeNode<SortedDepartment, String, SortedDepartmentTreeNode> {

        public SortedDepartmentTreeNode(SortedDepartment value) {
            super(value);
        }

        @Override
        protected boolean isTopNode() {
            return value.parentId == null;
        }

        @Override
        protected String extractIdentifier() {
            return value.id;
        }

        @Override
        protected String extractParentIdentifier() {
            return value.parentId;
        }

        @Override
        public int compareTo(SortedDepartmentTreeNode o) {
            return this.value.order - o.value.order;
        }
    }

    private static class SortedDepartmentTree extends AbstractSortedTree<SortedDepartment, String, SortedDepartmentTreeNode> {
        public <C extends Collection<SortedDepartmentTreeNode>> SortedDepartmentTree(C nodes) {
            super(nodes, true);
        }
    }

}
```

## 依赖三方库

| 依赖                   | 版本号            | 说明                    |
|----------------------|----------------|-----------------------|
| lombok               | 1.18.16        |                       |

