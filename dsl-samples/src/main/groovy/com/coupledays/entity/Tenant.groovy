package com.coupledays.entity

import com.coupledays.ast.ToJson
import groovy.transform.CompileStatic

@CompileStatic
@ToJson(includes = ['id', 'name', 'phone'])
class Tenant {
    Long id
    String name
    String phone
}
