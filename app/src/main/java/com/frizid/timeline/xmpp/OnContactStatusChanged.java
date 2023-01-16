package com.frizid.timeline.xmpp;

import com.frizid.timeline.entities.Contact;

public interface OnContactStatusChanged {
	void onContactStatusChanged(final Contact contact, final boolean online);
}
