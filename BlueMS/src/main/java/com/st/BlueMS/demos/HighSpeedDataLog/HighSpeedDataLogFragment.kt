package com.st.BlueMS.demos.HighSpeedDataLog

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.st.BlueMS.R
import com.st.BlueMS.demos.HighSpeedDataLog.tagging.HSDTaggingFragment
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Features.PnPL.FeaturePnPL
import com.st.BlueSTSDK.Features.highSpeedDataLog.FeatureHSDataLogConfig
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import com.st.STWINBoard_Gui.HSDConfigFragment
import kotlin.math.roundToInt


@DemoDescriptionAnnotation(name = "HighSpeed Data Log",
        iconRes = R.drawable.ic_hsdatalog,
        demoCategory = ["AI","Data Log"],
        requireAll = [FeatureHSDataLogConfig::class],
        notRequireOneOf = [FeaturePnPL::class])
class HighSpeedDataLogFragment : BaseDemoFragment(){

    private lateinit var viewModel : HighSpeedDataLogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //need to hide the start log button
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_high_speed_data_log,container,false)
        val bottomNavigation = rootView.findViewById<BottomNavigationView>(R.id.hsdl_bottom_navigation)

        bottomNavigation.setOnNavigationItemSelectedListener (this::onBottomNavigationItem)
        bottomNavigation.selectedItemId = R.id.hsdl_config_menu

        viewModel = ViewModelProvider(requireActivity()).get(HighSpeedDataLogViewModel::class.java)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        attachBoardName(view.findViewById(R.id.hsdl_boardName))
        attachBoardId(view.findViewById(R.id.hsdl_boardId))
        attachBoardBatteryLevel(view.findViewById(R.id.hsdl_batteryLevel), view.findViewById(R.id.hsdl_batteryLevelImage))
        attachBoardCPUUsage(view.findViewById(R.id.hsdl_cpuUsage), view.findViewById(R.id.hsdl_cpuUsageImage))
        viewModel.skipConfig.observe(viewLifecycleOwner, Observer {skipConfig ->
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

//    override fun onDestroyView() {
//        super.onDestroyView()
//        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.startLog).isVisible = false
    }

    private fun updateFragmentDisplayed(bottomNav: BottomNavigationView, itemId: Int) {
        bottomNav.selectedItemId = itemId;
    }

    private fun attachBoardName(nameLabel: TextView) {
        viewModel.boardName.observe(viewLifecycleOwner, Observer { newName ->
            nameLabel.text=newName
            nameLabel.visibility = if(newName.isNullOrBlank()){
                View.GONE
            }else{
                View.VISIBLE
            }
        })
    }

    private fun attachBoardId(idLabel: TextView) {
        viewModel.boardId.observe(viewLifecycleOwner, Observer { newId ->
            idLabel.text=newId
            idLabel.visibility = if(newId.isNullOrBlank()){
                View.GONE
            }else{
                View.VISIBLE
            }
        })
    }

    private fun attachBoardBatteryLevel(batteryLabel: TextView, batteryImage: ImageView) {
        viewModel.boardBatteryValue.observe(viewLifecycleOwner, Observer { newValue ->
            batteryLabel.visibility = if(newValue == null){
                View.GONE
            }else{
                View.VISIBLE
            }

            if(newValue == null)
                return@Observer

            setBatteryText(batteryLabel,newValue)
            setBatteryImage(batteryImage,newValue)

        })
    }

    private fun setBatteryImage(batteryImage: ImageView,charge:Double){
        when(charge){
            0.0 -> { batteryImage.setImageResource(R.drawable.battery_100c) }
            in 0.0..20.0 -> batteryImage.setImageResource(R.drawable.battery_00)
            in 20.0..40.0 -> batteryImage.setImageResource(R.drawable.battery_20)
            in 40.0..60.0 -> batteryImage.setImageResource(R.drawable.battery_40)
            in 60.0..80.0 -> batteryImage.setImageResource(R.drawable.battery_60)
            in 80.0..100.0 -> batteryImage.setImageResource(R.drawable.battery_80)
            else -> batteryImage.setImageResource(R.drawable.battery_100)
        }
    }

    private fun setBatteryText(batteryLabel: TextView,charge: Double){
        batteryLabel.text = if(charge == 0.0){
            getString(R.string.hsdl_battery_charged)
        }else {
            getString(R.string.hsdl_battery_format, charge)
        }
    }

    private fun attachBoardCPUUsage(cpuUsageLabel: TextView, cpuImage: ImageView) {
        viewModel.boardCPUusageValue.observe(viewLifecycleOwner, Observer { newValue ->
            cpuUsageLabel.visibility = if (newValue == null) {
                View.GONE
            } else {
                View.VISIBLE
            }
            if(newValue == null)
                return@Observer

            if (newValue == 100.0) {
                cpuUsageLabel.text = getString(R.string.hsdl_cpu_format,0.0)
                cpuImage.setColorFilter(ColorUtils.getColor(0.0f))
            }else{
                cpuUsageLabel.text = getString(R.string.hsdl_cpu_format,newValue)
                cpuImage.setColorFilter(ColorUtils.getColor(newValue.toFloat()/100f))
            }
        })
    }

    private object ColorUtils {
        private val FIRST_COLOR: Int = Color.parseColor("#BBCC00")
        private val SECOND_COLOR: Int = Color.parseColor("#FFD200")
        private val THIRD_COLOR: Int = Color.parseColor("#E6007E")
        fun getColor(percentage: Float): Int {
            var p = percentage
            val c0: Int
            val c1: Int
            if (p <= 0.5f) {
                p *= 2f
                c0 = FIRST_COLOR
                c1 = SECOND_COLOR
            } else {
                p = (p - 0.5f) * 2
                c0 = SECOND_COLOR
                c1 = THIRD_COLOR
            }
            val a = ave(Color.alpha(c0), Color.alpha(c1), p)
            val r = ave(Color.red(c0), Color.red(c1), p)
            val g = ave(Color.green(c0), Color.green(c1), p)
            val b = ave(Color.blue(c0), Color.blue(c1), p)
            return Color.argb(a, r, g, b)
        }

        private fun ave(src: Int, dst: Int, p: Float): Int {
            return src + (p * (dst - src)).roundToInt()
        }
    }

    override fun enableNeededNotification(node: Node) {

        showIntroductionMessage(
            "Please check Firmware version.\nIf you have v1.1.0 something may not work\nPlease update to latest version\n(https://www.st.com/en/embedded-software/fp-sns-datalog1.html)",
            context
        );

        if(childFragmentManager.findFragmentByTag(CONFIG_FRAGMENT_TAG)==null){
            showConfigFragment()
        }
        viewModel.enableNotification(node)
    }

    override fun disableNeedNotification(node: Node) {
        viewModel.disableNotification(node)
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
                        HSDTaggingFragment.newInstance(node), TAG_FRAGMENT_TAG)
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
            if(fragmentTag!=null)
                hide(fragmentTag)
            if(fragmentConfig==null){
                add(R.id.hsdl_fragmentContent,
                        HSDConfigFragment.newInstance(node), CONFIG_FRAGMENT_TAG)
            }else{
                show(fragmentConfig)
            }
        }
    }

    companion object{
        private val TAG_FRAGMENT_TAG = HighSpeedDataLogFragment::class.qualifiedName + ".TagFragment"
        private val CONFIG_FRAGMENT_TAG = HighSpeedDataLogFragment::class.qualifiedName + ".ConfigFragment"
    }

}