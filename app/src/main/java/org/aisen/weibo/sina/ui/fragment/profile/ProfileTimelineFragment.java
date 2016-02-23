package org.aisen.weibo.sina.ui.fragment.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.itemview.AHeaderItemViewCreator;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.fragment.itemview.NormalItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineDetailPagerFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineItemView;

import java.util.ArrayList;

/**
 * 用户的微博
 *
 * Created by wangdan on 16/1/12.
 */
public class ProfileTimelineFragment extends ATimelineFragment {

    public static ProfileTimelineFragment newInstance(WeiBoUser user) {
        Bundle args = new Bundle();
        args.putSerializable("mUser", user);

        ProfileTimelineFragment fragment = new ProfileTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private WeiBoUser mUser;
    private String feature;

    @Override
    public int inflateContentView() {
        return R.layout.ui_timeline_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = savedInstanceState == null ? (WeiBoUser) getArguments().getSerializable("mUser")
                                          : (WeiBoUser) savedInstanceState.getSerializable("mUser");
        feature = savedInstanceState == null ? getArguments().getString("feature", "0")
                                             : savedInstanceState.getString("feature", "0");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("mUser", mUser);
        outState.putString("feature", feature);
    }

    @Override
    public IItemViewCreator<StatusContent> configItemViewCreator() {
        return new NormalItemViewCreator<StatusContent>(R.layout.item_profile_timeline) {

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new TimelineItemView(convertView, ProfileTimelineFragment.this);
            }

        };
    }

    @Override
    protected AHeaderItemViewCreator<StatusContent> configHeaderViewCreator() {
        return new AHeaderItemViewCreator<StatusContent>() {

            @Override
            public int[][] setHeaderLayoutRes() {
                return new int[][]{ { ProfileTimelineHeaderView.LAYOUT_RES, 100 } };
            }

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new ProfileTimelineHeaderView(ProfileTimelineFragment.this, convertView);
            }

        };
    }

    @Override
    public void requestData(RefreshMode mode) {
        new ProfileTimelineTask(mode).execute();
    }

    class ProfileTimelineTask extends ATimelineTask {

        public ProfileTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            // 是否是原创
            if (!TextUtils.isEmpty(feature))
                params.addParameter("feature", feature);

            // 不管user_id字段传值什么，都返回登录用户的微博
            if (AppContext.getAccount().getUser().getIdstr().equals(mUser.getIdstr())) {
                params.addParameter("user_id", mUser.getIdstr());
            }
            else {
                params.addParameter("screen_name", mUser.getScreen_name());
            }

            params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));

            Token token = AppContext.getAccount().getAdvancedToken();
            if (token == null)
                token = AppContext.getAccount().getAccessToken();

            StatusContents statusContents = SinaSDK.getInstance(token, getTaskCacheMode(this)).statusesUserTimeLine(params);

            if (statusContents != null && statusContents.getStatuses() != null && statusContents.getStatuses().size() > 0) {
                for (StatusContent status : statusContents.getStatuses())
                    status.setUser(mUser);
            }

            return statusContents;
        }

    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    @Override
    public boolean onToolbarDoubleClick() {
        if (AisenUtils.checkTabsFragmentCanRequestData(this)) {
            requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
            getRefreshView().scrollToPosition(0);

            return true;
        }

        return false;
    }

}
