package com.st.BlueMS.demos.HighSpeedDatalog2

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.st.BlueMS.R
import com.st.BlueMS.demos.HighSpeedDatalog2.config.HSD2ConfigFragment
import com.st.BlueMS.demos.HighSpeedDatalog2.tagging.HSD2TaggingFragment
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Features.PnPL.FeaturePnPL
import com.st.BlueSTSDK.Features.highSpeedDataLog.FeatureHSDataLogConfig
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation


@DemoDescriptionAnnotation(name = "HighSpeed Data Log 2",
        iconRes = R.drawable.ic_hsdatalog,
        demoCategory = ["AI","Data Log"],
        requireAll = [FeatureHSDataLogConfig::class, FeaturePnPL::class],
        requireDTDLLoaded = true)
class HSD2MainFragment : BaseDemoFragment(){

    private lateinit var mainViewModel : HSD2MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //need to hide the start log button
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_high_speed_data_log,container,false)
        val bottomNavigation = rootView.findViewById<BottomNavigationView>(R.id.hsdl_bottom_navigation)

        val batteryLayout = rootView.findViewById<LinearLayout>(R.id.hsdl_batteryValueLayout)
        batteryLayout.visibility = View.GONE
        val cpuLayout = rootView.findViewById<LinearLayout>(R.id.hsdl_cpuValueLayout)
        cpuLayout.visibility = View.GONE

        bottomNavigation.setOnNavigationItemSelectedListener (this::onBottomNavigationItem)
        bottomNavigation.selectedItemId = R.id.hsdl_config_menu

        mainViewModel = ViewModelProvider(requireActivity()).get(HSD2MainViewModel::class.java)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachBoardName(view.findViewById(R.id.hsdl_boardName))
        attachBoardId(view.findViewById(R.id.hsdl_boardId))
        mainViewModel.skipConfig.observe(viewLifecycleOwner, Observer { skipConfig ->
            if(skipConfig){
                val fm = childFragmentManager
                val fragmentTag = fm.findFragmentByTag(TAG_FRAGMENT_TAG)
                if (fragmentTag == null) {
                    updateFragmentDisplayed(view.findViewById(R.id.hsdl_bottom_navigation), R.id.hsdl_tags_menu)
                }
            }else{
                updateFragmentDisplayed(view.findViewById(R.id.hsdl_bottom_navigation),R.id.hsdl_config_menu)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.startLog).isVisible = false
    }

    private fun updateFragmentDisplayed(bottomNav: BottomNavigationView, itemId: Int) {
        bottomNav.selectedItemId = itemId;
    }

    private fun attachBoardName(nameLabel: TextView) {
        mainViewModel.boardName.observe(viewLifecycleOwner, Observer { newName ->
            nameLabel.text=newName
            nameLabel.visibility = if(newName.isNullOrBlank()){
                View.GONE
            }else{
                View.VISIBLE
            }
        })
    }

    private fun attachBoardId(idLabel: TextView) {
        mainViewModel.boardId.observe(viewLifecycleOwner, Observer { newId ->
            idLabel.text=newId
            idLabel.visibility = if(newId.isNullOrBlank()){
                View.GONE
            }else{
                View.VISIBLE
            }
        })
    }

    override fun enableNeededNotification(node: Node) {

        if(childFragmentManager.findFragmentByTag(CONFIG_FRAGMENT_TAG)==null){
            showConfigFragment()
        }
        mainViewModel.enableNotificationfromNode(node)
    }

    override fun disableNeedNotification(node: Node) {
        mainViewModel.disableNotificationFromNode(node)
    }

    private fun onBottomNavigationItem(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.hsdl_config_menu -> {
                showConfigFragment()
                true
            }
            R.id.hsdl_tags_menu -> {
                showTagFragment()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun showTagFragment(){
        val node = node ?: return
        val fm = childFragmentManager
        val fragmentConfig = fm.findFragmentByTag(CONFIG_FRAGMENT_TAG)
        val fragmentTag = fm.findFragmentByTag(TAG_FRAGMENT_TAG)
        fm.commit {
            if(fragmentConfig!=null)
                hide(fragmentConfig)
            if(fragmentTag==null){
                add(R.id.hsdl_fragmentContent,
                        HSD2TaggingFragment.newInstance(node), TAG_FRAGMENT_TAG)
            }else{
                show(fragmentTag)
            }
        }
    }

    private fun showConfigFragment() {
        val node = node ?: return
        val fm = childFragmentManager
        val fragmentConfig = fm.findFragmentByTag(CONFIG_FRAGMENT_TAG)
        val fragmentTag = fm.findFragmentByTag(TAG_FRAGMENT_TAG)
        fm.commit {
            if(fragmentTag!=null) {
                hide(fragmentTag)
            }
            if(fragmentConfig==null){
                add(R.id.hsdl_fragmentContent,
                    HSD2ConfigFragment.newInstance(node), CONFIG_FRAGMENT_TAG)
            }else{
                show(fragmentConfig)
                if(fragmentTag!=null) {
                    if ((fragmentTag as HSD2TaggingFragment).isLogging()){
                        (fragmentConfig as HSD2ConfigFragment).showIsLoggingView(true)
                    }
                }
            }
        }
    }

    companion object{
        private val TAG_FRAGMENT_TAG = HSD2MainFragment::class.qualifiedName + ".TagFragment"
        private val CONFIG_FRAGMENT_TAG = HSD2MainFragment::class.qualifiedName + ".ConfigFragment"
    }

}