//package frangsierra.kotlinfirechat.common.flux
//
//import dagger.Component
//import frangsierra.kotlinfirechat.authentication.SessionStore
//import frangsierra.kotlinfirechat.common.WelcomeActivity
//import frangsierra.kotlinfirechat.common.dagger.AppComponent
//import frangsierra.kotlinfirechat.common.dagger.AppScope
//import frangsierra.kotlinfirechat.common.dagger.ComponentFactory
//import frangsierra.kotlinfirechat.data.FeedDatabaseStore
//import frangsierra.kotlinfirechat.data.UserDatabaseStore
//import frangsierra.kotlinfirechat.data.WorkoutDatabaseStore
//import frangsierra.kotlinfirechat.feed.SearchUserActivity
//import frangsierra.kotlinfirechat.feed.UserDetailActivity
//import frangsierra.kotlinfirechat.notifications.NotificationActivity
//import frangsierra.kotlinfirechat.notifications.NotificationStore
//
//
//@Component(modules = arrayOf(
//        UserDatabaseStore::class
//))
//@AppScope
//interface ConnectedUserComponent : StoreHolderComponent {
//    fun inject(target: WelcomeActivity)
//
//    fun feedComponent(): FeedComponent
//
//    fun workoutComponent(): WorkoutComponent
//
//    fun notificationComponent(): NotificationComponent
//}
//
//object UserComponentFactory : ComponentFactory<ConnectedUserComponent> {
//    override fun createComponent() =
//            app.findComponent(AppComponent::class)
//                    .connectedUserComponent()
//                    .also { initStores(it.stores().values) }
//
//    override fun destroyComponent(component: ConnectedUserComponent) {
//        disposeStores(component.stores().values)
//    }
//
//    override val componentType = ConnectedUserComponent::class
//}
//
//@Component(modules = arrayOf(
//        NotificationStore::class
//))
//@AppScope
//interface NotificationComponent : StoreHolderComponent {
//    fun inject(target: NotificationActivity)
//}
//
//object NotificationComponentFactory : ComponentFactory<NotificationComponent> {
//    override fun createComponent() =
//            app.findComponent(AppComponent::class)
//                    .connectedUserComponent()
//                    .notificationComponent()
//                    .also { initStores(it.stores().values) }
//
//    override fun destroyComponent(component: NotificationComponent) {
//        disposeStores(component.stores().values)
//    }
//
//    override val componentType = NotificationComponent::class
//}
//
//
//@Component(modules = arrayOf(
//        FeedDatabaseStore::class
//))
//@AppScope
//interface FeedComponent : StoreHolderComponent {
//    fun inject(target: SearchUserActivity)
//
//    fun workoutComponent(): WorkoutComponent
//}
//
//object FeedComponentFactory : ComponentFactory<FeedComponent> {
//    override fun createComponent() =
//            app.findComponent(AppComponent::class)
//                    .connectedUserComponent()
//                    .feedComponent()
//                    .also { initStores(it.stores().values) }
//
//    override fun destroyComponent(component: FeedComponent) {
//        disposeStores(component.stores().values)
//    }
//
//    override val componentType = FeedComponent::class
//}
//
//@Component(modules = arrayOf(
//        WorkoutDatabaseStore::class
//))
//@AppScope
//interface WorkoutComponent : StoreHolderComponent {
//    fun inject(target: UserDetailActivity)
//}
//
//object WorkoutComponentFactory : ComponentFactory<WorkoutComponent> {
//    override fun createComponent() =
//            app.findComponent(AppComponent::class)
//                    .connectedUserComponent()
//                    .feedComponent()
//                    .workoutComponent()
//                    .also { initStores(it.stores().values) }
//
//    override fun destroyComponent(component: WorkoutComponent) {
//        disposeStores(component.stores().values)
//    }
//
//    override val componentType = WorkoutComponent::class
//}
//
//
//@Component(modules = arrayOf(
//        SessionStore::class
//))
//@AppScope
//interface SessionComponent : StoreHolderComponent {
//    fun inject(target: UserDetailActivity)
//}
//
//object SessionComponentFactory : ComponentFactory<SessionComponent> {
//    override fun createComponent() =
//            app.findComponent(AppComponent::class)
//                    .connectedUserComponent()
//                    .feedComponent()
//                    .workoutComponent()
//                    .also { initStores(it.stores().values) }
//
//    override fun destroyComponent(component: SessionComponent) {
//        disposeStores(component.stores().values)
//    }
//
//    override val componentType = SessionComponent::class
//}
//
