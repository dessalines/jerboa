package com.jerboa.datatypes.types

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModlogListParams(
    val community_id: CommunityId? = null,
    val mod_person_id: PersonId? = null,
    val other_person_id: PersonId? = null,
    val page: Int? = null,
    val limit: Int? = null,
    val hide_modlog_names: Boolean,
) : Parcelable
