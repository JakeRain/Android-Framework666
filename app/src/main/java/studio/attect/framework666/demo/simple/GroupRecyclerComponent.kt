package studio.attect.framework666.demo.simple

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder
import kotlinx.android.synthetic.main.component_recycler_view.*
import net.steamcrafted.materialiconlib.IconValue
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import studio.attect.framework666.FragmentX
import studio.attect.framework666.compomentX.ComponentX
import studio.attect.framework666.compomentX.ComponentXProvider
import studio.attect.framework666.demo.R
import studio.attect.framework666.demo.fragment.RecyclerViewComponent
import studio.attect.framework666.simple.SimpleRecyclerViewAdapter
import java.util.*

class GroupRecyclerComponent : FragmentX() {



    private val recyclerViewAdapter = SimpleRecyclerViewAdapter(this)
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
            }, 2000) //模拟两秒后停止
        }

        recyclerView.layoutManager = layoutManager

        //设置分割线
        val divider = DividerItemDecoration(requireContext(), layoutManager.orientation)
        recyclerView.addItemDecoration(divider)

        recyclerViewAdapter.registerViewHolder(R.layout.list_item_text, RecyclerViewComponent.TextViewHolder::class.java)
        recyclerViewAdapter.registerViewHolder(R.layout.list_item_text_with_color, RecyclerViewComponent.TextColorViewHolder::class.java)

        // Setup expandable feature and RecyclerView
        val expMgr = RecyclerViewExpandableItemManager(null)


        recyclerView.adapter = expMgr.createWrappedAdapter(MyAdapter())


        // NOTE: need to disable change animations to ripple effect work properly
        (recyclerView.itemAnimator as SimpleItemAnimator).setSupportsChangeAnimations(false)

        expMgr.attachRecyclerView(recyclerView)

        reset.setOnClickListener {
        }
        addOne.setOnClickListener {
        }
        addMore.setOnClickListener {
        }
        updateOne.setOnClickListener {
        }
        updateMore.setOnClickListener {
        }
        removeOne.setOnClickListener {
        }
        removeMore.setOnClickListener {
        }
        removeAll.setOnClickListener {
        }

    }


    internal abstract class MyBaseItem(val id: Long, val text: String)

    internal class MyGroupItem(id: Long, text: String) : MyBaseItem(id, text) {
        val children: MutableList<MyChildItem>

        init {
            children = ArrayList()
        }
    }

    internal class MyChildItem(id: Long, text: String) : MyBaseItem(id, text)

    internal abstract class MyBaseViewHolder(itemView: View) : AbstractExpandableItemViewHolder(itemView) {
        var textView: TextView

        init {
            textView = itemView.findViewById(R.id.text)
        }
    }

    internal class MyGroupViewHolder(itemView: View) : MyBaseViewHolder(itemView)

    internal class MyChildViewHolder(itemView: View) : MyBaseViewHolder(itemView)

    internal class MyAdapter : AbstractExpandableItemAdapter<MyGroupViewHolder, MyChildViewHolder>() {
        var mItems: MutableList<MyGroupItem>

        init {
            setHasStableIds(true) // this is required for expandable feature.

            mItems = ArrayList()
            for (i in 0..19) {
                val group = MyGroupItem(i.toLong(), "GROUP $i")
                for (j in 0..4) {
                    group.children.add(MyChildItem(j.toLong(), "child $j"))
                }
                mItems.add(group)
            }
        }

        override fun getGroupCount(): Int {
            return mItems.size
        }

        override fun getChildCount(groupPosition: Int): Int {
            return mItems[groupPosition].children.size
        }

        override fun getGroupId(groupPosition: Int): Long {
            // This method need to return unique value within all group items.
            return mItems[groupPosition].id
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            // This method need to return unique value within the group.
            return mItems[groupPosition].children[childPosition].id
        }

        override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): MyGroupViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_text, parent, false)
            return MyGroupViewHolder(v)
        }

        override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): MyChildViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_text_with_color, parent, false)
            return MyChildViewHolder(v)
        }

        override fun onBindGroupViewHolder(holder: MyGroupViewHolder, groupPosition: Int, viewType: Int) {
            val group = mItems[groupPosition]
            holder.textView.text = group.text
        }

        override fun onBindChildViewHolder(holder: MyChildViewHolder, groupPosition: Int, childPosition: Int, viewType: Int) {
            val child = mItems[groupPosition].children[childPosition]
            holder.textView.text = child.text

        }

        override fun onCheckCanExpandOrCollapseGroup(holder: MyGroupViewHolder, groupPosition: Int, x: Int, y: Int, expand: Boolean): Boolean {
            return true
        }
    }

    companion object : ComponentXProvider {

        override fun getTag(): String = "group_recycler_view_demo"

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

        override fun getName(context: Context?): String = "group视图"

        override fun getColor(context: Context?): Int? = null

        override fun newInstance(): ComponentX = GroupRecyclerComponent()

    }


}