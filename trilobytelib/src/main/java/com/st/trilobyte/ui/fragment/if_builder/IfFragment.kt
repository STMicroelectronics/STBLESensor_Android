package com.st.trilobyte.ui.fragment.if_builder

import android.content.Context
import androidx.fragment.app.Fragment
import com.st.trilobyte.ui.ComposeIfActivity
import com.st.trilobyte.ui.fragment.StFragment

open class IfFragment : StFragment() {

    private var parentActivity: ComposeIfActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = context as ComposeIfActivity?
    }

    fun getParentActivity(): ComposeIfActivity? {
        return parentActivity
    }

    fun switchFragment(fragment: Fragment) {
        parentActivity?.switchFragment(fragment)
    }
}