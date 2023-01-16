package com.frizid.timeline.xmpp;

import com.frizid.timeline.entities.Account;
import com.frizid.timeline.xmpp.stanzas.IqPacket;

public interface OnIqPacketReceived extends PacketReceived {
	void onIqPacketReceived(Account account, IqPacket packet);
}
