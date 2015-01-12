package com.dexafree.sample

import com.arasthel.swissknife.annotations.Parcelable;
import groovy.transform.CompileStatic;

@Parcelable
@CompileStatic
public class Person {

    private String name;
    private int age;

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    int getAge() {
        return age;
    }

    void setAge(int age) {
        this.age = age;
    }

    public Person() {
    }

    public Person(String name, int age){
        this.name = name;
        this.age = age;
    }
}
