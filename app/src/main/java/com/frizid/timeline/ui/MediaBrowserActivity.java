package com.frizid.timeline.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import java.util.List;

import com.frizid.timeline.R;
import com.frizid.timeline.databinding.ActivityMediaBrowserBinding;
import com.frizid.timeline.entities.Account;
import com.frizid.timeline.entities.Contact;
import com.frizid.timeline.entities.Conversation;
import com.frizid.timeline.ui.adapter.MediaAdapter;
import com.frizid.timeline.ui.interfaces.OnMediaLoaded;
import com.frizid.timeline.ui.util.Attachment;
import com.frizid.timeline.ui.util.GridManager;
import com.frizid.timeline.xmpp.Jid;

public class MediaBrowserActivity extends XmppActivity implements OnMediaLoaded {

    private ActivityMediaBrowserBinding binding;

    private MediaAdapter mMediaAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this,R.layout.activity_media_browser);
        setSupportActionBar(binding.toolbar);
        configureActionBar(getSupportActionBar());
        mMediaAdapter = new MediaAdapter(this, R.dimen.media_size);
        this.binding.media.setAdapter(mMediaAdapter);
        GridManager.setupLayoutManager(this, this.binding.media, R.dimen.browser_media_size);

    }

    @Override
    protected void refreshUiReal() {

    }

    @Override
    void onBackendConnected() {
        Intent intent = getIntent();
        String account = intent == null ? null : intent.getStringExtra("account");
        String jid = intent == null ? null : intent.getStringExtra("jid");
        if (account != null && jid != null) {
            xmppConnectionService.getAttachments(account, Jid.ofEscaped(jid), 0, this);
        }
    }

    public static void launch(Context context, Contact contact) {
        launch(context, contact.getAccount(), contact.getJid().asBareJid().toEscapedString());
    }

    public static void launch(Context context, Conversation conversation) {
        launch(context, conversation.getAccount(), conversation.getJid().asBareJid().toEscapedString());
    }

    private static void launch(Context context, Account account, String jid) {
        final Intent intent = new Intent(context, MediaBrowserActivity.class);
        intent.putExtra("account",account.getUuid());
        intent.putExtra("jid",jid);
        context.startActivity(intent);
    }

    @Override
    public void onMediaLoaded(List<Attachment> attachments) {
        runOnUiThread(()->{
            mMediaAdapter.setAttachments(attachments);
        });
    }
}
