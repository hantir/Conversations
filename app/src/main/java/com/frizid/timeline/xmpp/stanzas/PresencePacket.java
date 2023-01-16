package com.frizid.timeline.xmpp.stanzas;

public class PresencePacket extends AbstractAcknowledgeableStanza {

	public PresencePacket() {
		super("presence");
	}
}
