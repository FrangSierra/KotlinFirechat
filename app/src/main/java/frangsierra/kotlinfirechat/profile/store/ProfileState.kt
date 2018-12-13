package frangsierra.kotlinfirechat.profile.store

import frangsierra.kotlinfirechat.profile.model.PrivateData
import frangsierra.kotlinfirechat.profile.model.PublicProfile
import mini.Task
import mini.taskIdle

data class ProfileState(val privateData: PrivateData? = null,
                        val publicProfile: PublicProfile? = null,
                        val loadProfileTask: Task = taskIdle())