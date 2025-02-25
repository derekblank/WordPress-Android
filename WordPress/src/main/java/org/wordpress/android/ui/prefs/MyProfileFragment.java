package org.wordpress.android.ui.prefs;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.analytics.AnalyticsTracker;
import org.wordpress.android.analytics.AnalyticsTracker.Stat;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.generated.AccountActionBuilder;
import org.wordpress.android.fluxc.model.AccountModel;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.fluxc.store.AccountStore.OnAccountChanged;
import org.wordpress.android.fluxc.store.AccountStore.PushAccountSettingsPayload;
import org.wordpress.android.ui.TextInputDialogFragment;
import org.wordpress.android.util.NetworkUtils;
import org.wordpress.android.util.ToastUtils;
import org.wordpress.android.widgets.WPTextView;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class MyProfileFragment extends Fragment implements TextInputDialogFragment.Callback {
    private WPTextView mFirstName;
    private WPTextView mLastName;
    private WPTextView mDisplayName;
    private WPTextView mAboutMe;

    @Inject Dispatcher mDispatcher;
    @Inject AccountStore mAccountStore;

    private static final String TRACK_PROPERTY_FIELD_NAME = "field_name";
    private static final String TRACK_PROPERTY_PAGE = "page";
    private static final String TRACK_PROPERTY_PAGE_MY_PROFILE = "my_profile";

    public static MyProfileFragment newInstance() {
        return new MyProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WordPress) getActivity().getApplication()).component().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshDetails();
        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            mDispatcher.dispatch(AccountActionBuilder.newFetchSettingsAction());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mDispatcher.register(this);
    }

    @Override
    public void onStop() {
        mDispatcher.unregister(this);
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.my_profile_fragment, container, false);

        mFirstName = rootView.findViewById(R.id.first_name);
        mLastName = rootView.findViewById(R.id.last_name);
        mDisplayName = rootView.findViewById(R.id.display_name);
        mAboutMe = rootView.findViewById(R.id.about_me);

        rootView.findViewById(R.id.first_name_row).setOnClickListener(
                createOnClickListener(
                        getString(R.string.first_name),
                        null,
                        mFirstName,
                        false));
        rootView.findViewById(R.id.last_name_row).setOnClickListener(
                createOnClickListener(
                        getString(R.string.last_name),
                        null,
                        mLastName,
                        false));
        rootView.findViewById(R.id.display_name_row).setOnClickListener(
                createOnClickListener(
                        getString(R.string.public_display_name),
                        getString(R.string.public_display_name_hint),
                        mDisplayName,
                        false));
        rootView.findViewById(R.id.about_me_row).setOnClickListener(
                createOnClickListener(
                        getString(R.string.about_me),
                        getString(R.string.about_me_hint),
                        mAboutMe,
                        true));

        return rootView;
    }

    private void refreshDetails() {
        if (!isAdded()) {
            return;
        }

        AccountModel account = mAccountStore.getAccount();
        updateLabel(mFirstName, account != null ? account.getFirstName() : null);
        updateLabel(mLastName, account != null ? account.getLastName() : null);
        updateLabel(mDisplayName, account != null ? account.getDisplayName() : null);
        updateLabel(mAboutMe, account != null ? account.getAboutMe() : null);
    }

    private void updateLabel(WPTextView textView, String text) {
        textView.setText(text);
        if (TextUtils.isEmpty(text)) {
            if (textView == mDisplayName) {
                mDisplayName.setText(mAccountStore.getAccount().getUserName());
            } else {
                textView.setVisibility(View.GONE);
            }
        } else {
            textView.setVisibility(View.VISIBLE);
        }
    }

    // helper method to create onClickListener to avoid code duplication
    @SuppressWarnings("deprecation")
    private View.OnClickListener createOnClickListener(final String dialogTitle,
                                                       final String hint,
                                                       final WPTextView textView,
                                                       final boolean isMultiline) {
        return v -> {
            TextInputDialogFragment inputDialog = TextInputDialogFragment.newInstance(dialogTitle,
                    textView.getText()
                            .toString(),
                    hint, isMultiline, true,
                    textView.getId());
            inputDialog.setTargetFragment(MyProfileFragment.this, 0);
            inputDialog.show(getFragmentManager(), TextInputDialogFragment.TAG);
        };
    }

    // helper method to get the rest parameter for a text view
    private String restParamForTextView(TextView textView) {
        if (textView == mFirstName) {
            return "first_name";
        } else if (textView == mLastName) {
            return "last_name";
        } else if (textView == mDisplayName) {
            return "display_name";
        } else if (textView == mAboutMe) {
            return "description";
        }
        return null;
    }

    private void updateMyProfileForLabel(TextView textView) {
        PushAccountSettingsPayload payload = new PushAccountSettingsPayload();
        payload.params = new HashMap<>();
        payload.params.put(restParamForTextView(textView), textView.getText().toString());
        mDispatcher.dispatch(AccountActionBuilder.newPushSettingsAction(payload));
        trackSettingsDidChange(restParamForTextView(textView));
    }

    private void trackSettingsDidChange(String fieldName) {
        Map<String, String> props = new HashMap<>();
        props.put(TRACK_PROPERTY_FIELD_NAME, fieldName);
        props.put(TRACK_PROPERTY_PAGE, TRACK_PROPERTY_PAGE_MY_PROFILE);
        AnalyticsTracker.track(Stat.SETTINGS_DID_CHANGE, props);
    }

    @Override
    public void onSuccessfulInput(String input, int callbackId) {
        View rootView = getView();
        if (rootView == null) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            ToastUtils.showToast(getActivity(), R.string.error_post_my_profile_no_connection);
            return;
        }

        WPTextView textView = rootView.findViewById(callbackId);
        updateLabel(textView, input);
        updateMyProfileForLabel(textView);
    }

    @Override
    public void onTextInputDialogDismissed(int callbackId) {
        // noop
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountChanged(OnAccountChanged event) {
        refreshDetails();
    }
}
