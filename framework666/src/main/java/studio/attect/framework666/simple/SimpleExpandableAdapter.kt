package studio.attect.framework666.simple;


import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.util.*
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder
import studio.attect.framework666.R
import studio.attect.framework666.interfaces.UniqueData
import java.lang.IllegalStateException

/**
 * 有三个集合数据
 * 1. viewHolderMap 存放注册进来的布局文件
 * 2. groupList     存放组数据
 * 3. childMap   存放二级数据
 *
 * 一级数据和二级数据通过一级数据来与二级的数据来互相绑定
 *
 * SimpleListData中有一个id 不但给adapter用,也是给一级数据和二级数据之间绑定关系用的
 *
 * group 的id值和child的id值是分别取自 自增长的groupIndex和childIndex的值
 *
 *
 * @author caoyu
 */
class SimpleExpandableAdapter<T>(val owner: T, val exManager: RecyclerViewExpandableItemManager) : AbstractExpandableItemAdapter<SimpleExpandableAdapter.ExpandableBasicViewHolder<out UniqueData>, SimpleExpandableAdapter.ExpandableBasicViewHolder<out UniqueData>>() {

    init {
        setHasStableIds(true)
    }

    /**
     * 为数据添加自增长的id
     */
    private var groupIndex = 0L
        get() {
            return field++
        }
    private var childIndex = 0L
        get() {
            return field++
        }

    /**
     * 根据不同的类型持有不同ViewHolder的class
     */
    private val viewHolderMap = SparseArray<Class<out ExpandableBasicViewHolder<out UniqueData>>>()

    /**
     * group的数据
     */
    val groupList = ArrayList<SimpleListData<out UniqueData>>()

//    /**
//     * 二级列表的数据
//     */
//    val childSparse = SparseArray<ArrayList<SimpleListData<out UniqueData>>>()


    val childMap = HashMap<SimpleListData<out UniqueData>, ArrayList<SimpleListData<out UniqueData>>>()

    /**
     * 注册一个类型的ViewHolder（Class）
     * 与之对应类型的数据将交给其使用
     *
     * @param layoutRes 对应的布局的id
     */
    fun registerViewHolder(@LayoutRes layoutRes: Int = 0, basicViewHolderClass: Class<out ExpandableBasicViewHolder<out UniqueData>>) {
        viewHolderMap[layoutRes] = basicViewHolderClass
    }

    /**
     * 添加一个空组数据
     *
     * @param data 要加入列表的数据
     * @param layoutRes 添加的数据对应的布局,fake为true时，可以给定任意值
     * @param position 要插入的位置，默认为列表末尾
     * @param fake 是否做假操作，可以获取到新数据的位置
     * @return 添加的新数据的位置
     */
    @JvmOverloads
    fun addGroupData(data: UniqueData, @LayoutRes layoutRes: Int, groupPosition: Int = groupList.size, fake: Boolean = false): Int {
        if (groupPosition !in 0..groupList.size) throw IllegalStateException("SimpleExpandableAdapter group position is out of range")
        if (fake) {
            return groupPosition
        }
        val simpData = SimpleListData(data, layoutRes)
        simpData.id = groupIndex
        groupList.add(groupPosition, simpData)
        exManager.notifyGroupItemInserted(groupPosition)
        return groupPosition
    }

    /**
     * 添加某一组中的一条child数据
     */
    @JvmOverloads
    fun addChildIntoGroupData(data: UniqueData, @LayoutRes childLayout: Int, groupPosition: Int = groupList.lastIndex,
                              childPosition: Int = childMap[groupList[groupPosition]]?.size ?: 0,
                              fake: Boolean = false): Int {
        if (groupPosition !in 0..groupList.lastIndex) throw IllegalArgumentException("SimpleExpandableAdapter child position is out of range")
        val childSize = childMap[groupList[groupPosition]]?.size ?: 0
        if (childPosition !in 0..childSize) throw IllegalStateException("SimpleExpandableAdapter group position is out of range")
        if (fake) {
            return childPosition
        }
        val groupSimpleData = groupList[groupPosition]
        if (childMap[groupSimpleData] == null) childMap[groupSimpleData] = ArrayList()
        val simpleData = SimpleListData(data, childLayout)
        simpleData.id = childIndex
        childMap[groupSimpleData]?.add(childPosition, simpleData)
        exManager.notifyChildItemInserted(groupPosition, childPosition)
        return childPosition
    }

    /**
     * 一次性添加同一组中的多条字数据
     */
    @JvmOverloads
    fun addChildrenIntoGroup(childDatas: ArrayList<UniqueData>, @LayoutRes childLayout: Int, groupPosition: Int = groupList.size, childPosition: Int = childMap[groupList[groupPosition]]?.size
            ?: 0, fake: Boolean = false): Int {
        //判断是否越界
        if (groupPosition !in 0..groupList.size) throw IllegalStateException("SimpleExpandableAdapter groupPosition is out of range")
        val childSize = childMap[groupList[groupPosition]]?.size ?: 0
        if (childPosition !in 0..childSize) throw IllegalStateException("SimpleExpandableAdapter childPosition is out of range")
        if (fake) return childPosition
        val groupDatas = childMap[groupList[groupPosition]]
        childDatas.forEachIndexed { index, child ->
            val simpleData = SimpleListData(child, childLayout)
            simpleData.id = childIndex
            groupDatas?.add(childPosition + index, simpleData)
        }
        exManager.notifyChildItemRangeInserted(groupPosition, childPosition, childDatas.size)
        return childPosition
    }


    /**
     * 一次性添加一组
     * 和这个组下面的多条数据
     * @param groupData 组数据
     * @param childDatas 子数据列表
     * @param groupLayout 组布局文件
     * @param childLayout 子布局文件
     * @param groupPosition 组位置
     * @return 返回需要插入的组的数据
     */
    @JvmOverloads
    fun addGroupChildDatas(childDatas: ArrayList<UniqueData>,
                           groupData: UniqueData,
                           @LayoutRes childLayout: Int, @LayoutRes groupLayout: Int,
                           groupPosition: Int = groupList.size, fake: Boolean = false): Int {
        if (fake) return groupPosition
        val groupSimpleData = SimpleListData(groupData, groupLayout)
        groupSimpleData.id = groupIndex
        groupList.add(groupSimpleData)
        if (childMap[groupSimpleData] == null) {
            childMap.put(groupSimpleData, ArrayList())
        }
        childMap[groupSimpleData]?.clear()
        childDatas.forEachIndexed { _, child ->
            val childSimpleData = SimpleListData(child, childLayout)
            childSimpleData.id = childIndex
            childMap[groupSimpleData]?.add(childSimpleData)
        }
        exManager.notifyGroupItemInserted(groupPosition)
        exManager.notifyChildItemRangeInserted(groupPosition , 0 , childDatas.size)
        return groupPosition
    }


    /**
     * 实现一次性添加多组空数据
     */
    @JvmOverloads
    fun addEmptyGroupDataList(groupDatas: ArrayList<UniqueData>, @LayoutRes groupLayout: Int, groupPosition: Int = groupList.size, fake: Boolean = false): Int {
        if (fake) return groupPosition
        groupDatas.forEachIndexed { index, data ->
            val simpData = SimpleListData(data, groupLayout)
            simpData.id = groupIndex
            groupList.add(groupPosition + index, simpData)
        }
        exManager.notifyGroupItemRangeInserted(groupPosition, groupDatas.size)
        return groupPosition
    }

    /**
     *
     */
    @JvmOverloads
    fun updateChildData(childData: UniqueData, @LayoutRes childLayout: Int, groupPosition: Int = -1, fake: Boolean = false): Int {
        var cPosition = -1
        var gPosition = groupPosition
        if (groupPosition == -1) {

            groupList.forEachIndexed stepOut@ { gIndex, groupData ->
                childMap[groupData]?.forEachIndexed { cIndex, childData ->
                    if (childData.uniqueTag() == childData.uniqueTag()) {
                        cPosition = cIndex
                        gPosition = gIndex
                        return@stepOut
                    }
                }
            }
        }
        if (gPosition !in 0..groupList.lastIndex) {
            throw IllegalStateException("SimpleExpandableRecyclerView group position is out of range")
        }
        if (cPosition == -1 || fake) return cPosition
        val childList = childMap[groupList[gPosition]]
        //复用旧数据的id
        val oldId = childList?.get(cPosition)?.id ?: childIndex
        val simpleListData = SimpleListData(childData, childLayout)
        simpleListData.id = oldId
        childList?.set(cPosition, simpleListData)
        exManager.notifyChildItemChanged(gPosition, cPosition)
        return cPosition
    }

    /**
     * 直接根据tag查处所有tag的数据修改调
     * 前提layout相同
     */
    @JvmOverloads
    fun updateChildrenDataInGroup(childList: ArrayList<UniqueData>, @LayoutRes childLayout: Int, groupPosition: Int = -1) {
        childList.forEach { child ->
            updateChildData(child, childLayout, groupPosition)
        }
    }


    /**
     * 删除一条数据
     * 根据唯一的tag进行删除
     * 此方法可用于无需判断数据是否存在进行安全删除操作调用，并能得知结果
     *
     * @param data 要删除的数据，不依赖内存地址判断而是根据唯一Tag
     * @param fake 是否做假操作，可以获得被删除的数据的位置或判断有没有数据被删除，fake为true时，可以给定任意值
     * @return 删除的数据的位置，如果没有数据被删除则为null
     */
    fun removeChildData(data: UniqueData, fake: Boolean = false): Int? {
        var gPosition = -1
        var cPosition = -1
        childMap.keys.forEach { group ->
            childMap[group]?.forEachIndexed { index, child ->
                if (data.uniqueTag() == child.uniqueTag()) {
                    cPosition = index
                    gPosition = groupList.indexOf(group)
                    return@forEach
                }
            }
        }

        if (cPosition != -1 && gPosition != -1) {
            childMap[groupList[gPosition]]?.removeAt(cPosition)
            exManager.notifyChildItemRemoved(gPosition, cPosition)
        }
        return cPosition
    }

    /**
     * 删除多个数据
     * 删除可能/可以是不连续的操作
     * 根据唯一的tag作为判断标准进行删除
     * 此方法可用于无需判断数据是否存在进行安全删除操作调用，并能得知结果
     *
     * @param moreData 要删除的多个数据，删除可能/可以不连续，不依赖内存地址判断而是根据唯一Tag
     * @return 被删除的数据的所有位置，若没有数据被删除则为空的数组
     */
    fun removeMoreData(moreData: List<UniqueData>) {
        val removeData = HashMap<SimpleListData<out UniqueData>, ArrayList<SimpleListData<out UniqueData>>>()
        moreData.forEach { removeTarget ->
            childMap.keys.forEach { group ->
                childMap[group]?.forEachIndexed { index, simpleListData ->
                    if (removeTarget.uniqueTag() == simpleListData.uniqueTag()) {
                        if (removeData[group] == null) {
                            removeData[group] = ArrayList()
                        }
                        removeData[group]?.add(simpleListData)
                    }
                }
            }
        }
        if (removeData.isNotEmpty()) {
           removeData.keys.forEach{group->
               val gPosition = groupList.indexOf(group)
               removeData[group]?.forEach {child->
                   val cPosition = childMap[group]?.indexOf(child)?:-1
                   if(cPosition == -1)return
                   childMap[group]?.remove(child)
                   exManager.notifyChildItemRemoved(gPosition , cPosition)
               }
           }
        }
    }


    /**
     * 删除一组数据
     */
    @JvmOverloads
    fun removeGroupData(groupData:UniqueData , fake: Boolean = false):Int{
        var position = -1
        var groupSimpleListData : SimpleListData<out UniqueData>? = null
        groupList.forEachIndexed{index , group->
            if(group.uniqueTag() == groupData.uniqueTag()){
                position = index
                groupSimpleListData = group
                return@forEachIndexed
            }
        }
        if(position == -1 || fake)return position
        groupList.removeAt(position)
        childMap.remove(groupSimpleListData)
        exManager.notifyGroupItemRemoved(position)
        return position
    }




    /**
     * 清空表内容
     * @return 移除了多少条数据
     */
    fun clearData(): Int {
        val size = groupList.size
        groupList.clear()
        childMap.clear()
        notifyDataSetChanged()
        return size
    }


    override fun getChildCount(groupPosition: Int): Int {
        return childMap[groupList[groupPosition]]?.size ?: 0
    }

    override fun onCheckCanExpandOrCollapseGroup(holder: ExpandableBasicViewHolder<out UniqueData>, groupPosition: Int, x: Int, y: Int, expand: Boolean): Boolean {
        return true
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): ExpandableBasicViewHolder<out UniqueData> {
        if (viewHolderMap.contains(viewType)) {
            return buildViewHolder(LayoutInflater.from(parent?.context).inflate(viewType, parent, false), viewHolderMap[viewType])
        }
        return DefaultViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.recycler_view_unprocessed_data, parent, false))
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): ExpandableBasicViewHolder<out UniqueData> {
        if (viewHolderMap.contains(viewType)) {
            return buildViewHolder(LayoutInflater.from(parent?.context).inflate(viewType, parent, false), viewHolderMap[viewType])
        }
        return DefaultViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.recycler_view_unprocessed_data, parent, false))
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupList[groupPosition].id
    }

    override fun onBindChildViewHolder(holder: ExpandableBasicViewHolder<out UniqueData>, groupPosition: Int, childPosition: Int, viewType: Int) {
        if (holder is DefaultViewHolder) {
            holder.applyData(groupList[groupPosition], groupPosition)
            return
        }
        @Suppress("UNCHECKED_CAST")
        childMap[groupList[groupPosition]]?.get(childPosition)?.listData?.let { (holder as ExpandableBasicViewHolder<UniqueData>).applyData(it, childPosition) }

    }

    override fun onBindGroupViewHolder(holder: ExpandableBasicViewHolder<out UniqueData>, groupPosition: Int, viewType: Int) {
        if (holder is DefaultViewHolder) {
            holder.applyData(groupList[groupPosition], groupPosition)
            return
        }
        @Suppress("UNCHECKED_CAST")
        (holder as ExpandableBasicViewHolder<UniqueData>).applyData(groupList[groupPosition].listData, groupPosition)
    }


    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childMap[groupList[groupPosition]]?.get(childPosition)?.id ?: 0L
    }

    override fun getGroupCount(): Int {
        return groupList.size
    }


    override fun getChildItemViewType(groupPosition: Int, childPosition: Int): Int {
        if (childMap[groupList[groupPosition]]?.size ?: 0 > childPosition) {
            return childMap[groupList[groupPosition]]?.get(childPosition)?.layoutRes
                    ?: R.layout.recycler_view_unprocessed_data
        }
        return R.layout.recycler_view_unprocessed_data
    }

    override fun getGroupItemViewType(groupPosition: Int): Int {
        if (groupPosition < groupList.size) {
            return groupList[groupPosition].layoutRes
        }
        return R.layout.recycler_view_unprocessed_data
    }


    /**
     * 根据BasicViewHolder相关Class实例化类并传递参数
     *
     * @param view #onCreateViewHolder 中对应布局的实例
     * @param cls BasicViewHolder.class
     */
    private fun buildViewHolder(view: View, cls: Class<out ExpandableBasicViewHolder<out UniqueData>>): ExpandableBasicViewHolder<out UniqueData> {
        //处理ViewHolder为内部类的情况
        cls.constructors[0]?.parameterTypes?.let { parameterTypes ->
            if (parameterTypes.size > 1) {
                val constructor = cls.getConstructor(parameterTypes[0], View::class.java)
                return constructor.newInstance(owner, view)
            }
        }
        val constructor = cls.getConstructor(View::class.java)
        return constructor.newInstance(view)
    }

    /**
     * 一个默认的ViewHolder
     * 用于防止疏忽导致崩溃，并在实际界面上显示出具体问题
     * 这同时也是一个ViewHolder的例子
     */
    private class DefaultViewHolder(itemView: View) : SimpleExpandableAdapter.ExpandableBasicViewHolder<SimpleListData<out UniqueData>>(itemView) {

        private val textView: AppCompatTextView = itemView.findViewById(R.id.text)

        /**
         * 注意[position]是创建ViewHolder时的数据位置，不会随着列表的变化而更新
         */
        override fun applyData(data: SimpleListData<out UniqueData>, position: Int) {
            textView.text = itemView.context.getString(R.string.simple_recycler_view_adapter_unprocessed_data).format(data.layoutRes, data.uniqueTag())
        }

    }


    abstract class ExpandableBasicViewHolder<T : UniqueData>(itemView: View) : AbstractExpandableItemViewHolder(itemView) {
        abstract fun applyData(data: T, position: Int)
    }


    /**
     * 简单列表数据类
     * 将UniqueData与布局资源进行绑定
     */
    class SimpleListData<T : UniqueData>(val listData: T, @LayoutRes val layoutRes: Int) : UniqueData {
        var id: Long = 0L
        override fun uniqueTag() = listData.uniqueTag()
    }
}