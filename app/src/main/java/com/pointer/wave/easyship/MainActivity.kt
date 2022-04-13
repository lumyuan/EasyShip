package com.pointer.wave.easyship

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.lxj.xpopup.XPopup
import com.pointer.wave.easyship.common.activity.BaseActivity
import com.pointer.wave.easyship.core.ZoomInTransformer
import com.pointer.wave.easyship.fragments.AboutFragment
import com.pointer.wave.easyship.fragments.HomeFragment
import com.pointer.wave.easyship.net.repo.UpdateRepo
import com.pointer.wave.easyship.pojo.VersionBen
import com.pointer.wave.easyship.utils.AndroidInfo
import com.pointer.wave.easyship.widget.NavigationBar
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : BaseActivity() {

    private var cancel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragments = listOf(HomeFragment(), AboutFragment())

        val adapter = ViewPagerAdapter(fragments, this)
        val pager = findViewById<ViewPager2>(R.id.pager).apply {
            setAdapter(adapter)
            //isUserInputEnabled = false
        }

        findViewById<NavigationBar>(R.id.main_navigation).apply {
            bindData(pager, arrayOf("开始", "关于"), intArrayOf(R.mipmap.ic_home, R.mipmap.ic_settings))
            setPositionListener { _, position ->
                pager.currentItem = position
            }
        }
        // FIXME: 2022/4/4 本来我是想 ViewPager 滑动的时候切换下面的 Tab，但是 Tab 上面那个有动画的 View 似乎没效果，我也不清楚应该怎么修复，就暂时禁用了
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !cancel) {
            cancel = true
            lifecycleScope.launchWhenResumed {
                UpdateRepo().update()
                    .catch {
                        it.printStackTrace()
                    }
                    .onEach {
                        val gson = Gson()
                        val versionBen = gson.fromJson(it.content, VersionBen::class.java)
                        val androidInfo = AndroidInfo(this@MainActivity)
                        if (versionBen.versionCode.toInt() > androidInfo.versionCode) {
                            XPopup.Builder(this@MainActivity)
                                .isDestroyOnDismiss(true)
                                .asConfirm("有更新啦~", versionBen.updateContent, "知道了", "去更新", {
                                    val intent = Intent().apply {
                                        action = "android.intent.action.VIEW"
                                        data = Uri.parse(versionBen.downloadUrl)
                                    }
                                    startActivity(intent)
                                }, null, false).show()
                        }
                    }
                    .catch {
                        it.printStackTrace()
                    }.launchIn(lifecycleScope)
            }
        }
    }

    inner class ViewPagerAdapter(
        private val fragments: List<Fragment>,
        activity: FragmentActivity
    ) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}