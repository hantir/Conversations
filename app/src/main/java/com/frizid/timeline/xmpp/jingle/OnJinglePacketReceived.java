package com.frizid.timeline.xmpp.jingle;

import com.frizid.timeline.entities.Account;
import com.frizid.timeline.xmpp.PacketReceived;
import com.frizid.timeline.xmpp.jingle.stanzas.JinglePacket;

public interface OnJinglePacketReceived extends PacketReceived {
	void onJinglePacketReceived(Account account, JinglePacket packet);
}
