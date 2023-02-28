package com.st.trilobyte.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.st.BlueSTSDK.Node
import com.st.trilobyte.R
import com.st.trilobyte.models.Flow
import com.st.trilobyte.ui.fragment.StFragment
import com.st.trilobyte.ui.fragment.if_builder.IfBuilderFragment

class ComposeIfActivity : AppCompatActivity() {

    var expression: Flow? = null

    var statements: List<Flow>? = null

    companion object {
        const val SEARCH_TIMEOUT = 10000
        val EXTRA_BOARD_TYPE = "extra-board_type"

        fun provideIntent(context: Context, board: Node.Type) : Intent {
            val intent = Intent(context,ComposeIfActivity::class.java)
            intent.putExtra(EXTRA_BOARD_TYPE,board)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose_if)

        val startIntent = intent
        val board = startIntent.getSerializableExtra(SelectFlowsActivity.EXTRA_BOARD_TYPE) as Node.Type

        switchFragment(IfBuilderFragment.getInstance(board))
    }

    fun switchFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is IfBuilderFragment) {
            val stFragment = currentFragment as StFragment
            if (stFragment.onBackPressed()) {

                if (supportFragmentManager.backStackEntryCount > 1) {
                    supportFragmentManager.popBackStack()
                    return
                }

                finish()
            }

            return
        }

        super.onBackPressed()
    }
}