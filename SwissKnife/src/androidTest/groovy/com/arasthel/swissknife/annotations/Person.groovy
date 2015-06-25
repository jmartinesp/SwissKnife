package com.arasthel.swissknife.annotations

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

@Parcelable
@CompileStatic
@EqualsAndHashCode
public class Person {

    String name;
    int age;

    public Person(String name, int age){
        this.name = name;
        this.age = age;
    }


}
