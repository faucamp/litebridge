package org.litebridge.orm.e2e.dto;

public class SingleTableNestedParent {

    private String parentValue1;
    private NestedChild nestedChild;

    public String getParentValue1() {
        return parentValue1;
    }

    public void setParentValue1(final String parentValue1) {
        this.parentValue1 = parentValue1;
    }

    public NestedChild getNestedChild() {
        return nestedChild;
    }

    public void setNestedChild(final NestedChild nestedChild) {
        this.nestedChild = nestedChild;
    }

    public static class NestedChild {
        private String childValue1;
        private NestedGrandChild grandChild;

        public String getChildValue1() {
            return childValue1;
        }

        public void setChildValue1(final String childValue1) {
            this.childValue1 = childValue1;
        }

        public NestedGrandChild getGrandChild() {
            return grandChild;
        }

        public void setGrandChild(final NestedGrandChild grandChild) {
            this.grandChild = grandChild;
        }

        public static class NestedGrandChild {
            private String grandChildValue1;

            public String getGrandChildValue1() {
                return grandChildValue1;
            }

            public void setGrandChildValue1(final String grandChildValue1) {
                this.grandChildValue1 = grandChildValue1;
            }
        }
    }

}
