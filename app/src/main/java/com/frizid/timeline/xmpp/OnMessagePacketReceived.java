package com.frizid.timeline.xmpp;

import com.frizid.timeline.entities.Account;
import com.frizid.timeline.xmpp.stanzas.MessagePacket;

public interface OnMessagePacketReceived extends PacketReceived {
	void onMessagePacketReceived(Account account, MessagePacket packet);
}
