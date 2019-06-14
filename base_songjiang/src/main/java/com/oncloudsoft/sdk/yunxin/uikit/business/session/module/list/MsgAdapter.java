package com.oncloudsoft.sdk.yunxin.uikit.business.session.module.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.oncloudsoft.sdk.R;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.oncloudsoft.sdk.yunxin.uikit.business.session.module.Container;
import com.oncloudsoft.sdk.yunxin.uikit.business.session.viewholder.MsgViewHolderBase;
import com.oncloudsoft.sdk.yunxin.uikit.business.session.viewholder.MsgViewHolderFactory;
import com.oncloudsoft.sdk.yunxin.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.oncloudsoft.sdk.yunxin.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.oncloudsoft.sdk.yunxin.uikit.impl.NimUIKitImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangjun on 2016/12/21.
 */
public class MsgAdapter extends BaseMultiItemFetchLoadAdapter<IMMessage, BaseViewHolder> {

    private Map<Class<? extends MsgViewHolderBase>, Integer> holder2ViewType;

    private ViewHolderEventListener eventListener;
    private Map<String, Float> progresses; // 有文件传输，需要显示进度条的消息ID map
    private String messageId;
    private Container container;
    boolean b = false;
    public MsgAdapter(RecyclerView recyclerView, List<IMMessage> data, Container container) {
        super(recyclerView, data);

        timedItems = new HashSet<>();
        progresses = new HashMap<>();

        // view type, view holder
        holder2ViewType = new HashMap<>();
        List<Class<? extends MsgViewHolderBase>> holders = MsgViewHolderFactory.getAllViewHolders();
        int viewType = 0;
        for (Class<? extends MsgViewHolderBase> holder : holders) {
            viewType++;
            addItemType(viewType, R.layout.nim_message_item, holder);
            holder2ViewType.put(holder, viewType);
        }

        this.container = container;
//        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onChanged() {
//                Log.e("TAG","AdapterData Change....");
//            }
//        });
    }
    public MsgAdapter(RecyclerView recyclerView, List<IMMessage> data, Container container,boolean b) {
        super(recyclerView, data);

        timedItems = new HashSet<>();
        progresses = new HashMap<>();

        // view type, view holder
        holder2ViewType = new HashMap<>();
        List<Class<? extends MsgViewHolderBase>> holders = MsgViewHolderFactory.getAllViewHolders();
        int viewType = 0;
        for (Class<? extends MsgViewHolderBase> holder : holders) {
            viewType++;
            addItemType(viewType, R.layout.nim_message_item, holder);
            holder2ViewType.put(holder, viewType);
        }
        this.b = b;
        this.container = container;
    }

    @Override
    protected int getViewType(IMMessage message) {
        return holder2ViewType.get(MsgViewHolderFactory.getViewHolderByType(message));
    }

    @Override
    protected String getItemKey(IMMessage item) {
        return item.getUuid();
    }

    public void setEventListener(ViewHolderEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public ViewHolderEventListener getEventListener() {
        return eventListener;
    }

    public void deleteItem(IMMessage message, boolean isRelocateTime) {
        if (message == null) {
            return;
        }

        int index = 0;
        for (IMMessage item : getData()) {
            if (item.isTheSame(message)) {
                break;
            }
            ++index;
        }

        if (index < getDataSize()) {
            remove(index);
            if (isRelocateTime) {
                relocateShowTimeItemAfterDelete(message, index);
            }
//            notifyDataSetChanged(); // 可以不要！！！
        }
    }

    public float getProgress(IMMessage message) {
        Float progress = progresses.get(message.getUuid());
        return progress == null ? 0 : progress;
    }

    public void putProgress(IMMessage message, float progress) {
        progresses.put(message.getUuid(), progress);
    }

    /**
     * *********************** 时间显示处理 ***********************
     */

    private Set<String> timedItems; // 需要显示消息时间的消息ID
    private IMMessage lastShowTimeItem; // 用于消息时间显示,判断和上条消息间的时间间隔

    public boolean needShowTime(String s) {
        return timedItems.contains(s);
    }

    /**
     * 列表加入新消息时，更新时间显示
     */
    public void updateShowTimeItem(List<IMMessage> items, boolean fromStart, boolean update) {
        IMMessage anchor = fromStart ? null : lastShowTimeItem;
        for (IMMessage message : items) {
            if (setShowTimeFlag(message, anchor)) {
                anchor = message;
            }
        }

        if (update) {
            lastShowTimeItem = anchor;
        }
    }
    /**
     * 列表加入新消息时，更新时间显示
     */
    public void updateShowTimeItem1(List<IMMessage> items, boolean fromStart, boolean update) {
        IMMessage anchor = fromStart ? null : lastShowTimeItem;
        for (IMMessage message : items) {
            if (setShowTimeFlag1(message, anchor)) {
                anchor = message;
            }
        }

        if (update) {
            lastShowTimeItem = anchor;
        }
    }

    /**
     * 是否显示时间item
     */
    private boolean setShowTimeFlag1(IMMessage message, IMMessage anchor) {
        boolean update = false;

        if (hideTimeAlways(message)) {
            setShowTime(message.getTime()+"", false);
        } else {
            if (anchor == null) {
                setShowTime(message.getTime()+"", true);
                update = true;
            } else {
                long time = anchor.getTime();
                long now = message.getTime();

                if (now - time == 0) {
                    // 消息撤回时使用
                    setShowTime(message.getTime()+"", true);
                    lastShowTimeItem = message;
                    update = true;
                } else if (now - time < (NimUIKitImpl.getOptions().displayMsgTimeWithInterval)) {
                    setShowTime(message.getTime()+"", false);
                } else {
                    setShowTime(message.getTime()+"", true);
                    update = true;
                }
            }
        }

        return update;
    }


    /**
     * 从 搜索消息记录进来的将显示消息发送时间 返回ture   不显示消息发送时间的将返回false*/

    public boolean ShowTimeItem() {
        return b;
    }

    /**
     * 是否显示时间item
     */
    private boolean setShowTimeFlag(IMMessage message, IMMessage anchor) {
        boolean update = false;

        if (hideTimeAlways(message)) {
            setShowTime(message.getUuid(), false);
        } else {
            if (anchor == null) {
                setShowTime(message.getUuid(), true);
                update = true;
            } else {
                long time = anchor.getTime();
                long now = message.getTime();

                if (now - time == 0) {
                    // 消息撤回时使用
                    setShowTime(message.getUuid(), true);
                    lastShowTimeItem = message;
                    update = true;
                } else if (now - time < (NimUIKitImpl.getOptions().displayMsgTimeWithInterval)) {
                    setShowTime(message.getUuid(), false);
                } else {
                    setShowTime(message.getUuid(), true);
                    update = true;
                }
            }
        }

        return update;
    }

    private void setShowTime(String message, boolean show) {
        if (show) {
            timedItems.add(message);
        } else {
            timedItems.remove(message);
        }
    }

    private void relocateShowTimeItemAfterDelete(IMMessage messageItem, int index) {
        // 如果被删的项显示了时间，需要继承
        if (needShowTime(messageItem.getUuid())) {
            setShowTime(messageItem.getUuid(), false);
            if (getDataSize() > 0) {
                IMMessage nextItem;
                if (index == getDataSize()) {
                    //删除的是最后一项
                    nextItem = getItem(index - 1);
                } else {
                    //删除的不是最后一项
                    nextItem = getItem(index);
                }

                // 增加其他不需要显示时间的消息类型判断
                if (hideTimeAlways(nextItem)) {
                    setShowTime(messageItem.getUuid(), false);
                    if (lastShowTimeItem != null && lastShowTimeItem != null
                            && lastShowTimeItem.isTheSame(messageItem)) {
                        lastShowTimeItem = null;
                        for (int i = getDataSize() - 1; i >= 0; i--) {
                            IMMessage item = getItem(i);
                            if (needShowTime(item.getUuid())) {
                                lastShowTimeItem = item;
                                break;
                            }
                        }
                    }
                } else {
                    setShowTime(messageItem.getUuid(), true);
                    if (lastShowTimeItem == null
                            || (lastShowTimeItem != null && lastShowTimeItem.isTheSame(messageItem))) {
                        lastShowTimeItem = nextItem;
                    }
                }
            } else {
                lastShowTimeItem = null;
            }
        }
    }

    private boolean hideTimeAlways(IMMessage message) {
        if (message.getSessionType() == SessionTypeEnum.ChatRoom) {
            return true;
        }
        switch (message.getMsgType()) {
            case notification:
                return true;
            default:
                return false;
        }
    }

    public interface ViewHolderEventListener {
        // 长按事件响应处理
        boolean onViewHolderLongClick(View clickView, View viewHolderView, IMMessage item);

        // 发送失败或者多媒体文件下载失败指示按钮点击响应处理
        void onFailedBtnClick(IMMessage resendMessage);

        // viewholder footer按钮点击，如机器人继续会话
        void onFooterClick(IMMessage message);
    }

    public void setUuid(String messageId) {
        this.messageId = messageId;
    }

    public String getUuid() {
        return messageId;
    }

    public Container getContainer() {
        return container;
    }
}