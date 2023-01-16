package com.frizid.timeline.crypto.sasl;

import java.security.SecureRandom;

import com.frizid.timeline.entities.Account;
import com.frizid.timeline.xml.TagWriter;

public class Anonymous extends SaslMechanism {

    public static final String MECHANISM = "ANONYMOUS";

    public Anonymous(TagWriter tagWriter, Account account, SecureRandom rng) {
        super(tagWriter, account, rng);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getMechanism() {
        return MECHANISM;
    }

    @Override
    public String getClientFirstMessage() {
        return "";
    }
}
