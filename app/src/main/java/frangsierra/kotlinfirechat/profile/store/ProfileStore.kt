package frangsierra.kotlinfirechat.profile.store

import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import frangsierra.kotlinfirechat.chat.store.SendMessageCompleteAction
import frangsierra.kotlinfirechat.core.dagger.AppScope
import frangsierra.kotlinfirechat.profile.controller.ProfileController
import frangsierra.kotlinfirechat.profile.controller.ProfileControllerImpl
import frangsierra.kotlinfirechat.session.store.CreateAccountCompleteAction
import frangsierra.kotlinfirechat.session.store.LoginCompleteAction
import frangsierra.kotlinfirechat.util.taskRunning
import mini.Reducer
import mini.Store
import javax.inject.Inject

@AppScope
class ProfileStore @Inject constructor(val controller: ProfileController) : Store<ProfileState>() {

    @Reducer
    fun loadAndCreateUserData(action: CreateAccountCompleteAction): ProfileState {
        if (!action.task.isSuccessful()) return state
        controller.loadUserProfile(action.user!!) //User can't be null if the request is successful
        return state.copy(loadProfileTask = taskRunning())
    }

    @Reducer
    fun loadUserOnLogin(action: LoginCompleteAction): ProfileState {
        if (!action.task.isSuccessful()) return state
        controller.loadUserProfile(action.user!!) //User can't be null if the request is successful
        return state.copy(loadProfileTask = taskRunning())
    }

    fun updateMessageCount(action: SendMessageCompleteAction): ProfileState {
        if (!action.task.isSuccessful()) return state
        return state.copy(publicProfile = state.publicProfile?.copy(totalMessages = state.publicProfile!!.totalMessages.plus(1)))
    }

    @Reducer
    fun userDataLoaded(action: LoadUserDataCompleteAction): ProfileState {
        if (!state.loadProfileTask.isRunning()) return state
        return state.copy(loadProfileTask = action.task, publicProfile = action.publicProfile, privateData = action.privateData)
    }
}

@Module
abstract class ProfileModule {
    @Binds
    @AppScope
    @IntoMap
    @ClassKey(ProfileStore::class)
    abstract fun provideProfileStore(store: ProfileStore): Store<*>

    @Binds
    @AppScope
    abstract fun bindProfileController(impl: ProfileControllerImpl): ProfileController
}
