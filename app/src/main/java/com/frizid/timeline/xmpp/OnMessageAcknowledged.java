package com.frizid.timeline.xmpp;

import com.frizid.timeline.entities.Account;

public interface OnMessageAcknowledged {
    boolean onMessageAcknowledged(Account account, Jid to, String id);
}
