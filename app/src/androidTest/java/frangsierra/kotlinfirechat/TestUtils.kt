package frangsierra.kotlinfirechat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import com.durmin.mini_android_testing.CleanStateRule
import com.durmin.mini_android_testing.TestDispatcherRule
import frangsierra.kotlinfirechat.core.flux.appComponent
import mini.Store
import kotlin.reflect.KClass

fun cleanStateRule() = CleanStateRule(appComponent.stores().values.toList())
fun testDispatcherRule() = TestDispatcherRule(appComponent.dispatcher())

inline fun <reified T : Store<*>> store(): T {
    return appComponent.stores()[T::class.java] as T
}

fun <T : Activity> testActivity(clazz: KClass<T>,
                                createIntent: ((context: Context) -> Intent)? = null): ActivityTestRule<T> {
    //Overriding the rule throws an exception
    if (createIntent == null) return ActivityTestRule<T>(clazz.java)
    return object : ActivityTestRule<T>(clazz.java) {
        override fun getActivityIntent(): Intent = createIntent(InstrumentationRegistry.getTargetContext())
    }
}

inline fun <reified T : Activity> testActivity(intent: Intent): ActivityTestRule<T> =
    ActivityTestRule(T::class.java)