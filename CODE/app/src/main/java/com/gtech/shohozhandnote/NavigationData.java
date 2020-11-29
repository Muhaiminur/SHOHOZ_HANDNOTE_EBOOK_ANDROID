package com.gtech.shohozhandnote;

public class NavigationData {
    private boolean isSelected;
    private String name;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NavigationData(boolean isSelected, String name) {
        this.isSelected = isSelected;
        this.name = name;
    }

    public NavigationData() {
    }

    @Override
    public String toString() {
        return "NavigationData{" +
                "isSelected=" + isSelected +
                ", name='" + name + '\'' +
                '}';
    }
}
