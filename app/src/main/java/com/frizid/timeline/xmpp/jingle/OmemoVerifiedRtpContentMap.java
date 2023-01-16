package com.frizid.timeline.xmpp.jingle;

import java.util.Map;

import com.frizid.timeline.xmpp.jingle.stanzas.Group;
import com.frizid.timeline.xmpp.jingle.stanzas.OmemoVerifiedIceUdpTransportInfo;

public class OmemoVerifiedRtpContentMap extends RtpContentMap {
    public OmemoVerifiedRtpContentMap(Group group, Map<String, DescriptionTransport> contents) {
        super(group, contents);
        for(final DescriptionTransport descriptionTransport : contents.values()) {
            if (descriptionTransport.transport instanceof OmemoVerifiedIceUdpTransportInfo) {
                ((OmemoVerifiedIceUdpTransportInfo) descriptionTransport.transport).ensureNoPlaintextFingerprint();
                continue;
            }
            throw new IllegalStateException("OmemoVerifiedRtpContentMap contains non-verified transport info");
        }
    }
}
