package com.frizid.timeline.xmpp;

import com.frizid.timeline.entities.Account;

public interface OnStatusChanged {
	void onStatusChanged(Account account);
}
