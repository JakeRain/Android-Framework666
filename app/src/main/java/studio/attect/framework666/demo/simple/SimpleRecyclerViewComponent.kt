package studio.attect.framework666.demo.simple

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import kotlinx.android.synthetic.main.component_recycler_view.*
import net.steamcrafted.materialiconlib.IconValue
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import studio.attect.framework666.FragmentX
import studio.attect.framework666.compomentX.ComponentX
import studio.attect.framework666.compomentX.ComponentXProvider
import studio.attect.framework666.demo.R
import studio.attect.framework666.extensions.systemLongAnimTime
import studio.attect.framework666.interfaces.UniqueData
import studio.attect.framework666.simple.SimpleExpandableAdapter
import studio.attect.framework666.simple.SimpleRecyclerViewAdapter

/**
 * 回收视图Demo组件
 *
 * @author caoyu
 */
class SimpleRecyclerViewComponent : FragmentX() {
    private val recyclerViewAdapter = SimpleExpandableAdapter(this)
    private val layoutManager by lazy { LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.component_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setColorSchemeColors(ResourcesCompat.getColor(resources, R.color.colorPrimary, requireActivity().theme))
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.postDelayed({
                swipeRefreshLayout.isRefreshing = false
                resetAllData()
            }, 2000) //模拟两秒后停止
        }

        // Setup expandable feature and RecyclerView
        val expMgr = RecyclerViewExpandableItemManager(null)

        recyclerView.layoutManager = layoutManager


        //设置分割线
        val divider = DividerItemDecoration(requireContext(), layoutManager.orientation)
        recyclerView.addItemDecoration(divider)

        recyclerViewAdapter.registerViewHolder(R.layout.list_item_text, TextViewHolder::class.java)
        recyclerViewAdapter.registerViewHolder(R.layout.list_item_text_with_color, TextColorViewHolder::class.java)

        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = true
        recyclerView.adapter = expMgr.createWrappedAdapter(recyclerViewAdapter)
        expMgr.attachRecyclerView(recyclerView)


        reset.setOnClickListener {
            addOneChildData(0)
        }
        addOne.setOnClickListener {
            addOneGroupData()
        }
        addMore.setOnClickListener { addMoreData() }
        updateOne.setOnClickListener { updateOneData() }
        updateMore.setOnClickListener { updateMoreData() }
        removeOne.setOnClickListener { removeOneData() }
        removeMore.setOnClickListener { removeMoreData() }
        removeAll.setOnClickListener { recyclerViewAdapter.clearData() }

        resetAllData()
    }

    /**
     * 刷新整个列表
     */
    private fun resetAllData() {
        recyclerViewAdapter.clearData()

        for(i in 0 until 5){
            recyclerViewAdapter.addGroupData(ItemData() , randomLayoutRes())
            for(j in 0 until 5){
                recyclerViewAdapter.addChildData(ItemData(), randomLayoutRes() )
            }
        }
    }


    private fun addOneGroupData(){
        val position = (0..recyclerViewAdapter.groupCount).shuffled().last()
        val layoutRes = randomLayoutRes()
        recyclerView.smoothScrollToPosition(position)

        recyclerView.postDelayed(
            {
                val data = ItemData()
                recyclerViewAdapter.addGroupData(data, layoutRes , position)
                Toast.makeText(requireContext(), "添加一个列表项：位置[$position] 布局:$layoutRes 数据:${data.text}", Toast.LENGTH_LONG).show()
            },
            resources.systemLongAnimTime
        )
    }


    private fun addOneChildData(groupPosition : Int){
        val position = (0..recyclerViewAdapter.getChildCount(groupPosition)).shuffled().last()
        val layoutRes = randomLayoutRes()
        recyclerView.smoothScrollToPosition(position)
        recyclerView.postDelayed({
            val data = ItemData()
            recyclerViewAdapter.addChildData(data , layoutRes , groupPosition , position)
        },
            resources.systemLongAnimTime)
    }



    /**
     * 添加多个随机数据
     * 随机的布局
     */
    private fun addMoreData() {
        val moreData = arrayListOf<SimpleRecyclerViewAdapter.SimpleListData<ItemData>>()
        val position = (0..recyclerViewAdapter.itemCount).shuffled().last()
        val number = (2..5).shuffled().last()
        for (i in 0 until number) {
            moreData.add(SimpleRecyclerViewAdapter.SimpleListData(ItemData(), randomLayoutRes()))
        }
        recyclerView.smoothScrollToPosition(position)

        recyclerView.postDelayed(
            {
//                recyclerViewAdapter.addMoreData(moreData, position)
                Toast.makeText(requireContext(), "添加${number}个列表项", Toast.LENGTH_LONG).show()
            },
            resources.systemLongAnimTime
        )
    }

    /**
     * 更新一个随机数据
     * 可能没有数据被更新
     */
    private fun updateOneData() {
//        val data = ItemData()
//        val position = recyclerViewAdapter.updateData(data, 0, true)
//        val layoutRes = randomLayoutRes()
//        if (position != null) {
//            recyclerView.smoothScrollToPosition(position)
//            recyclerView.postDelayed(
//                {
//                    recyclerViewAdapter.updateData(data, layoutRes)
//                    Toast.makeText(requireContext(), "更新一个列表项：位置[$position] 布局:$layoutRes 数据:${data.text}", Toast.LENGTH_LONG).show()
//                },
//                resources.systemLongAnimTime
//            )
//        } else {
//            Toast.makeText(requireContext(), "未更新任何数据", Toast.LENGTH_LONG).show()
//        }
    }

    /**
     * 更新多个随机数据
     * 可能只有部分甚至没有数据被更新
     */
    private fun updateMoreData() {
        val moreData = arrayListOf<SimpleRecyclerViewAdapter.SimpleListData<ItemData>>()
        val number = (2..10).shuffled().last()
        val position = recyclerViewAdapter.updateMoreData(moreData, 0, true)
        for (i in 0 until number) {
            moreData.add(SimpleRecyclerViewAdapter.SimpleListData(ItemData(), randomLayoutRes()))
        }
        if (position.isNotEmpty()) {
            recyclerView.smoothScrollToPosition(position[0])
            recyclerView.postDelayed(
                {
                    recyclerViewAdapter.updateMoreData(moreData, 0) //layoutRes已经由SimpleListData确定
                },
                resources.systemLongAnimTime
            )
        } else {
            Toast.makeText(requireContext(), "未更新任何数据", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 删除一个随机数据
     * 可能没有数据被删除
     */
    private fun removeOneData() {
        val data = ItemData()
        val position = recyclerViewAdapter.removeData(data, true)
        if (position != null) {
            recyclerView.smoothScrollToPosition(position)
            recyclerView.postDelayed(
                {
                    recyclerViewAdapter.removeData(data)
                    Toast.makeText(requireContext(), "删除一个列表项：位置[$position]", Toast.LENGTH_LONG).show()
                },
                resources.systemLongAnimTime
            )
        } else {
            Toast.makeText(requireContext(), "未删除任何数据", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 删除多个随机数据
     * 可能只有部分甚至没有数据被删除
     */
    private fun removeMoreData() {
        val moreData = arrayListOf<SimpleRecyclerViewAdapter.SimpleListData<ItemData>>()
        val number = (2..10).shuffled().last()
        for (i in 0 until number) {
            moreData.add(SimpleRecyclerViewAdapter.SimpleListData(ItemData(), randomLayoutRes()))
        }
        val position = recyclerViewAdapter.removeMoreData(moreData, true)
        if (position.isNotEmpty()) {
            recyclerView.smoothScrollToPosition(position[0])
            recyclerView.postDelayed(
                {
                    recyclerViewAdapter.removeMoreData(moreData)
                },
                resources.systemLongAnimTime
            )
        } else {
            Toast.makeText(requireContext(), "未删除任何数据", Toast.LENGTH_LONG).show()
        }
    }


    /**
     * 产生一个随机数据类型
     * 三分之一的几率是未处理的数据类型
     */
    private fun randomLayoutRes() = arrayListOf(0, R.layout.list_item_text, R.layout.list_item_text_with_color).shuffled().last()

    /**
     * 列表中的数据的结构
     */
    class ItemData : UniqueData {
        var text = (1..50).shuffled().last().toString()
        var color = Color.argb(255, (0..255).shuffled().last(), (0..255).shuffled().last(), (0..255).shuffled().last())
        override fun uniqueTag(): Any = text
    }

    inner class TextViewHolder(itemView: View) : SimpleExpandableAdapter.ExpandableBasicViewHolder<ItemData>(itemView) {
        private val textView = itemView.findViewById<AppCompatTextView>(R.id.text)

        override fun applyData(data: ItemData, position: Int) {
            textView.text = data.text
        }
    }

    inner class TextColorViewHolder(itemView: View) : SimpleExpandableAdapter.ExpandableBasicViewHolder<ItemData>(itemView) {
        private val textView = itemView.findViewById<AppCompatTextView>(R.id.text)
        private val colorView = itemView.findViewById<View>(R.id.colorBlock)

        override fun applyData(data: ItemData, position: Int) {
            textView.text = data.text
            colorView.background = ColorDrawable(data.color)
        }
    }

    companion object : ComponentXProvider {

        override fun getTag(): String = "recycler_expandable_view_demo"

        override fun getIcon(context: Context?): Drawable? {
            context?.let {
                val builder = MaterialDrawableBuilder.with(it).apply {
                    setIcon(IconValue.FORMAT_LIST_BULLETED)
                    setColor(Color.WHITE)
                    setSizeDp(24)
                }

                return builder.build()
            }
            return null
        }

        override fun getName(context: Context?): String = "扩张的recyclerView"

        override fun getColor(context: Context?): Int? = null

        override fun newInstance(): ComponentX = SimpleRecyclerViewComponent()

    }
}