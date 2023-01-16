package com.frizid.timeline.xmpp;

import com.frizid.timeline.entities.Account;
import com.frizid.timeline.xmpp.stanzas.PresencePacket;

public interface OnPresencePacketReceived extends PacketReceived {
	void onPresencePacketReceived(Account account, PresencePacket packet);
}
