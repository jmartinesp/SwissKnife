package com.coupledays.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.android.ast.InjectViews
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.transform.TypeCheckingMode

@InheritConstructors
@InjectViews(R.layout.activity_user_form)
@CompileStatic
class UserFormActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        def user = new User(name: 'Name')
        form(R.id.user_form, user) { form ->
            editText(R.id.user_name).attach('name')
            editText(R.id.user_phone).attach('phone')
            editText(R.id.user_balance).attach('balance')
            form.submit(R.id.submit_button) {
                if (form.object.validate()) {
                    this.showToast('Validated with success!')
                } else {
                    form.object.errors.each {
                        this.showToast(it.toString())
                    }
                }
            }
        }
    }
}



