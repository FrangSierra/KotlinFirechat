package frangsierra.kotlinfirechat.profile.store

import frangsierra.kotlinfirechat.profile.model.PrivateData
import frangsierra.kotlinfirechat.profile.model.PublicProfile
import frangsierra.kotlinfirechat.util.Task

data class ProfileState(val privateData: PrivateData? = null,
                        val publicProfile: PublicProfile? = null,
                        val loadProfileTask: Task = Task())