package frangsierra.kotlinfirechat.profile.store

import frangsierra.kotlinfirechat.profile.model.PrivateData
import frangsierra.kotlinfirechat.profile.model.PublicProfile
import mini.Task
import mini.Action

data class LoadUserDataCompleteAction(val privateData: PrivateData?,
                                      val publicProfile: PublicProfile?,
                                      val task: Task) : Action