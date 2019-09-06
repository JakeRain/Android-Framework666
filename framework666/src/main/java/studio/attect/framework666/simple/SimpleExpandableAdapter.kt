package studio.attect.framework666.simple;


import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.util.*
import com.h6ah4i.android.widget.advrecyclerview.adapter.ItemIdComposer
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder
import studio.attect.framework666.R
import studio.attect.framework666.interfaces.UniqueData


class SimpleExpandableAdapter<T>(val owner : T): AbstractExpandableItemAdapter<SimpleExpandableAdapter.ExpandableBasicViewHolder<out UniqueData>, SimpleExpandableAdapter.ExpandableBasicViewHolder<out UniqueData>>(){


    init {
        setHasStableIds(true)
    }

    /**
     * 根据不同的类型持有不同ViewHolder的class
     */
    private val viewHolderMap = SparseArray<Class<out ExpandableBasicViewHolder<out UniqueData>>>()

    /**
     * group的数据
     */
    val dataList = ArrayList<SimpleListData<out UniqueData>>()

    /**
     * 二级列表的数据
     */
    val childSparse = SparseArray< ArrayList<SimpleListData<out UniqueData>>>()

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
     * 添加一个数据
     *
     * @param data 要加入列表的数据
     * @param layoutRes 添加的数据对应的布局,fake为true时，可以给定任意值
     * @param position 要插入的位置，默认为列表末尾
     * @param fake 是否做假操作，可以获取到新数据的位置
     * @return 添加的新数据的位置
     */
    @JvmOverloads
    fun addGroupData(data: UniqueData, @LayoutRes layoutRes: Int, position: Int = dataList.size, fake: Boolean = false): Int {
        if (fake) {
            return position
        }
        val simpData = SimpleListData(data , layoutRes)
        simpData.id = dataList.size.toLong()
        dataList.add(position, simpData)
        notifyItemInserted(position)
        return position
    }



    @JvmOverloads
    fun addChildData(data: UniqueData , @LayoutRes layoutRes: Int , groupPosition: Int = dataList.size-1 , childPosition: Int = childSparse[dataList.size-1]?.size?:0 , fake: Boolean = false): Int{
        if(fake){
            return childPosition
        }
        if(childSparse[groupPosition] == null){
            childSparse[groupPosition] = ArrayList()
        }
        val simpleData = SimpleListData(data , layoutRes)
        simpleData.id = childSparse[groupPosition].size.toLong()
        childSparse[groupPosition].add(childPosition , SimpleListData(data , layoutRes))
        notifyDataSetChanged()
        return childPosition
    }



    /**
     * 连续添加多个数据  到一个ｇｒｏｕｐ中
     *
     * @param moreData 要添加的多条数据,如果需要特别定制，部分值可传入SimpleListData，就可以为同一系列数据类型中的其中一个或多个使用不同的layout
     * @layoutRes 数据集对应的布局资源，若数据自身为SimpleListData则采用数据指定的，fake为true时，可以给定任意值
     * @param fake 是否做假操作，可以获得最靠前的新数据的位置或判断是否有数据被添加
     * @return 第一条的位置,null时表示没有任何数据被添加
     */
    @JvmOverloads
    fun addMoreDataInGroup(moreData: List<UniqueData>, @LayoutRes layoutRes: Int, groupPosition: Int = dataList.size-1, childPosition: Int = childSparse[dataList.size -1]?.size?:0 , fake: Boolean = false): Int? {
        if (!moreData.isNullOrEmpty()) {
            if (!fake) {
                moreData.forEach {
                    if (it is SimpleListData<*>) {
                        childSparse[groupPosition].add(childPosition ,it)
                    } else {
                        childSparse[groupPosition].add(childPosition , SimpleListData(it, layoutRes))
                    }
                }
                notifyItemRangeInserted(childPosition, moreData.size)
            }
            return childPosition
        }
        return null
    }






    /**
     * 更新一个数据
     * 根据唯一tag进行更新
     * 只会更新一条（废话，数据唯一标识）
     * 此方法作用为无需判断数据的存在及其位置无脑通知视图更新内容
     *
     * @param data 要更新的数据，不依赖内存地址判断而是根据唯一Tag
     * @param layoutRes 更新数据后的布局，fake为true时，可以给定任意值
     * @param fake 是否做假操作，可以获得被更新的数据的位置或判断是否有数据被更新
     * @return 更新的数据的位置，如果没有数据被更新则为null
     */
    fun updateChildData(data: UniqueData, @LayoutRes layoutRes: Int, fake: Boolean = false): Int? {
        var position = -1
        var id = 0L
        var groupPosition = -1

        childSparse.keyIterator().forEach { gPosition->
            childSparse[gPosition]?.forEachIndexed{index ,simpleListData->
                if(simpleListData.uniqueTag() == data.uniqueTag()){
                    position = index
                    groupPosition = gPosition
                    id = simpleListData.id
                    return@forEachIndexed
                }
            }
        }

        if (position > -1 && groupPosition > -1) {
            if (!fake) {
                val simpleListData = SimpleListData(data , layoutRes)
                simpleListData.id = id
                childSparse[groupPosition][position] = simpleListData
                notifyItemChanged(position)
            }
            return position
        }
        return null
    }


    fun updateGroupData(data: UniqueData , @LayoutRes layoutRes: Int , fake: Boolean = false):Int?{
        var position = -1
        dataList.forEachIndexed { index, simpleListData ->
            if (simpleListData.uniqueTag() == data.uniqueTag()) {
                position = index
                return@forEachIndexed
            }
        }
        if (position > -1) {
            if (!fake) {
                dataList[position] = SimpleListData(data, layoutRes)
                notifyItemChanged(position)
            }
            return position
        }
        return null
    }


    /**
     * 更新多个数据
     * 根据唯一的tag进行更新
     * 此方法作用为无需判断数据的存在及其位置无脑通知视图更新内容
     *
     * @param moreData 要更新的多个数据，更新可能/可以不连续，不依赖内存地址判断而是根据唯一Tag
     * @param layoutRes 数据集对应的布局资源，若数据自身为SimpleListData则采用数据指定的，fake为true时，可以给定任意值
     * @param fake 是否做假操作，可以获得最靠前的被更新的数据的位置或判断是否有数据被更新
     * @return 变更的数据的位置，若没有数据变更则为空的数组
     */
    fun updateMoreData(moreData: List<UniqueData>, @LayoutRes layoutRes: Int, fake: Boolean = false): IntArray {
        val replaceData = SparseArray<SimpleListData<out UniqueData>>()
        moreData.forEach { newData ->
            dataList.forEachIndexed { index, simpleListData ->
                if (newData.uniqueTag() == simpleListData.uniqueTag()) {
                    if (newData is SimpleListData<*>) {
                        replaceData[index] = newData
                    } else {
                        replaceData[index] = SimpleListData(newData, layoutRes)
                    }
                }
            }
        }

        val intArray = IntArray(replaceData.size())
        for (i in 0 until replaceData.size()) {
            intArray[i] = replaceData.keyAt(i)
        }

        return if (replaceData.isNotEmpty()) {
            if (!fake) {
                replaceData.forEach { key, value ->
                    dataList[key] = value
                    notifyItemChanged(key)
                }
            }
            intArray
        } else {
            IntArray(0)
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
    fun removeData(data: UniqueData, fake: Boolean = false): Int? {
        var position = -1
        dataList.forEachIndexed { index, simpleListData ->
            if (simpleListData.uniqueTag() == data.uniqueTag()) {
                position = index
                return@forEachIndexed
            }
        }
        if (position > -1) {
            if (!fake) {
                dataList.removeAt(position)
                notifyItemRemoved(position)
            }

            return position
        }
        return null
    }

    /**
     * 删除多个数据
     * 删除可能/可以是不连续的操作
     * 根据唯一的tag作为判断标准进行删除
     * 此方法可用于无需判断数据是否存在进行安全删除操作调用，并能得知结果
     *
     * @param moreData 要删除的多个数据，删除可能/可以不连续，不依赖内存地址判断而是根据唯一Tag
     * @param fake 是否做假操作，可以获得最靠前的被删除的数据的位置或判断是否有数据被删除，fake为true时，可以给定任意值
     * @return 被删除的数据的所有位置，若没有数据被删除则为空的数组
     */
    fun removeMoreData(moreData: List<UniqueData>, fake: Boolean = false): IntArray {
        val removeData = SparseArray<SimpleListData<out UniqueData>>()
        moreData.forEach { removeTarget ->
            dataList.forEachIndexed { index, simpleListData ->
                if (removeTarget.uniqueTag() == simpleListData.uniqueTag()) {
                    removeData[index] = simpleListData //此处和update不一样
                }
            }
        }

        val intArray = IntArray(removeData.size())
        for (i in 0 until removeData.size()) {
            intArray[i] = removeData.keyAt(i)
        }

        return if (removeData.isNotEmpty()) {
            if (!fake) {
                val idList = arrayListOf<Int>()
                for (i in 0 until removeData.size()) {
                    idList.add(i)
                }
                idList.reversed().forEach {
                    //颠倒过来，从后往前删，避免key(position)变动
                    dataList.removeAt(it)
                    notifyItemRemoved(it)
                }
            }
            intArray
        } else {
            IntArray(0)
        }
    }

    /**
     * 清空表内容
     * @return 移除了多少条数据
     */
    fun clearData(): Int {
        val size = dataList.size
        dataList.clear()
        childSparse.clear()
        notifyDataSetChanged()
        return size
    }


    override fun getChildCount(groupPosition: Int): Int {
        if(childSparse[groupPosition] == null)return 0
        return childSparse[groupPosition].size
    }

    override fun onCheckCanExpandOrCollapseGroup(holder: ExpandableBasicViewHolder<out UniqueData>, groupPosition: Int, x: Int, y: Int, expand: Boolean): Boolean {
       return true
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): ExpandableBasicViewHolder<out UniqueData> {
        if(viewHolderMap.contains(viewType)){
            return buildViewHolder(LayoutInflater.from(parent?.context).inflate(viewType , parent , false), viewHolderMap[viewType])
        }
        return DefaultViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.recycler_view_unprocessed_data, parent, false))
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): ExpandableBasicViewHolder<out UniqueData> {
        if(viewHolderMap.contains(viewType)){
            return buildViewHolder(LayoutInflater.from(parent?.context).inflate(viewType , parent , false) , viewHolderMap[viewType])
        }
        return DefaultViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.recycler_view_unprocessed_data, parent, false))
    }

    override fun getGroupId(groupPosition: Int): Long {
        return dataList[groupPosition].id
    }

    override fun onBindChildViewHolder(holder: ExpandableBasicViewHolder<out UniqueData>, groupPosition: Int, childPosition: Int, viewType: Int) {
        if (holder is DefaultViewHolder) {
            holder.applyData(dataList[groupPosition], groupPosition)
            return
        }
        @Suppress("UNCHECKED_CAST")
        (holder as ExpandableBasicViewHolder<UniqueData>).applyData(childSparse[groupPosition].get(childPosition).listData , childPosition)
    }
    override fun onBindGroupViewHolder(holder: ExpandableBasicViewHolder<out UniqueData>, groupPosition: Int, viewType: Int) {
        if (holder is DefaultViewHolder) {
            holder.applyData(dataList[groupPosition], groupPosition)
            return
        }
        @Suppress("UNCHECKED_CAST")
        (holder as ExpandableBasicViewHolder<UniqueData>).applyData( dataList[groupPosition].listData, groupPosition)
    }


    override fun getChildId(groupPosition: Int, childPosition: Int): Long{
        return childSparse[groupPosition].get(childPosition).id
    }

    override fun getGroupCount(): Int {
        return dataList.size
    }



    override fun getChildItemViewType(groupPosition: Int, childPosition: Int): Int {
        if(childSparse[groupPosition].size > childPosition){
            return childSparse[groupPosition].get(childPosition).layoutRes
        }
        return R.layout.recycler_view_unprocessed_data
    }

    override fun getGroupItemViewType(groupPosition: Int): Int {
        if(groupPosition < dataList.size){
            return dataList[groupPosition].layoutRes
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





    abstract class ExpandableBasicViewHolder<T : UniqueData>(itemView : View) : AbstractExpandableItemViewHolder(itemView) {
        abstract fun applyData(data: T, position: Int)
    }



    /**
     * 简单列表数据类
     * 将UniqueData与布局资源进行绑定
     */
    class SimpleListData<T : UniqueData>(val listData: T, @LayoutRes val layoutRes: Int) : UniqueData {
        var id : Long = 0
        override fun uniqueTag() = listData.uniqueTag()
    }

}