package frangsierra.kotlinfirechat

import com.agoda.kakao.KEditText
import com.agoda.kakao.Screen
import frangsierra.kotlinfirechat.session.LoginActivity
import frangsierra.kotlinfirechat.session.model.User
import frangsierra.kotlinfirechat.session.store.LoginWithCredentials
import frangsierra.kotlinfirechat.session.store.SessionState
import frangsierra.kotlinfirechat.session.store.SessionStore
import mini.onUiSync
import mini.taskSuccess
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

class LoginActivityTest {

    internal class LoginScreen : Screen<LoginScreen>() {
        val email = KEditText { withId(R.id.editTextEmail) }
        val password = KEditText { withId(R.id.editTextPassword) }
        val loginButton = KEditText { withId(R.id.loginPasswordButton) }
    }

    internal class HomeScreen : Screen<HomeScreen>() {
        val sendButton = KEditText { withId(R.id.sendButton) }
    }

    val testDispatcher = testDispatcherRule()
    val testActivity = testActivity(LoginActivity::class)

    @get:Rule
    val ruleChain = RuleChain
        .outerRule(cleanStateRule())
        .around(testDispatcher)
        .around(testActivity)

    @Test
    fun login_success_redirects_properly() {
        val loginScreen = LoginScreen()
        val homeScreen = HomeScreen()
        val sessionStore = store<SessionStore>()
        val email = "GarroshWasRight@gmail.com"
        val password = "illidanDidNothingWrong"

        loginScreen {
            email { replaceText(email) }
            password { replaceText(password) }
            loginButton { click() }

            //Set login state to error
            onUiSync {
                val state = SessionState(loginTask = taskSuccess(), verified = true, loggedUser = User("", "test", email = email))
                sessionStore.setTestState(state)
            }
        }
        homeScreen {
            sendButton.isVisible()
        }

    }

    @Test
    fun login_button_dispatch_login_action() {
        val loginScreen = LoginScreen()
        val email = "GarroshWasRight@gmail.com"
        val password = "illidanDidNothingWrong"

        loginScreen {
            email { replaceText(email) }
            password { replaceText(password) }
            loginButton { click() }
            //Check action
            view.check { _, _ ->
                val expectedAction = LoginWithCredentials(email, password)
                Assert.assertTrue(testDispatcher.testInterceptor.actions.contains(expectedAction))
            }
        }
    }
}