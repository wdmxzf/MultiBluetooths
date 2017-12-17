package com.zhg.bluetoothL.entity;


import java.util.ArrayList;
import java.util.List;

public class ClassNameEntity {

    private List<String> classNameList;

    public List<String> getClassNameList(){
        if (null == classNameList)
            classNameList = new ArrayList<>();
        return classNameList;
    }

    public void setClassNameList(List<String> classNameList){
        this.classNameList = classNameList;
    }

    public void addClassName(String className){
        getClassNameList().add(className);
    }
}
