package com.frizid.timeline.xmpp.jingle;

import com.frizid.timeline.entities.Account;
import com.frizid.timeline.xmpp.Jid;

public interface OngoingRtpSession {
    Account getAccount();
    Jid getWith();
    String getSessionId();
}
