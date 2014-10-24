package me.champeau.speakertime
import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.android.ast.InjectViews
import com.android.components.GFragment
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.transform.TypeCheckingMode

@InheritConstructors
@CompileStatic
@InjectViews(R.layout.activity_user_form)
class UserFormActivity extends Activity {
    boolean showImageFragment
    GFragment currentFragment

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
        button(R.id.button).onClick {
            if (!this.showImageFragment) {
                this.currentFragment = createDynamicForm(this, new User(name: 'Second User'))
                this.showImageFragment = true
            } else {
                this.currentFragment = createImageFragment(this)
                this.showImageFragment = false
            }
        }

    }

    @CompileStatic(TypeCheckingMode.SKIP)
    private GFragment createDynamicForm(Context context, User user) {
        def fragment = context.newFragment(R.id.fragment_layout, R.layout.simple_fragment) { fragment ->
            form(R.id.user_form2, user) { form ->
                editText(R.id.user_name2).attach(user) { it.name }
                editText(R.id.user_phone2).attach(user) { it.phone }
                editText(R.id.user_balance2).attach(user) { it.balance }
                form.submit(R.id.submit_button2) {
                    if (form.object.validate()) {
                        this.showToast('Validated with success!')
                    } else {
                        form.object.errors.each {
                            this.showToast(it.toString())
                        }
                    }
                }
            }
            fragment.resume {
                this.showToast('Fragment just resumed')
            }
        }
        if (this.currentFragment) {
            this.currentFragment >> fragment
        }
        fragment
    }

    private GFragment createImageFragment(Context context) {
        def fragment = context.newFragment(R.id.fragment_layout, R.layout.image_fragment) {
            image(R.id.image_on_fragment).onClick {
                this.showToast('Image clicked!')
            }
        }
        if (this.currentFragment) {
            this.currentFragment >> fragment
        }
        fragment
    }
}



