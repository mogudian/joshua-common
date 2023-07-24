package com.mogudiandian.common.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试树
 *
 * @author Joshua Sun
 * @since 2023/7/24
 */
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
