package com.frizid.timeline.xmpp;

import com.frizid.timeline.crypto.axolotl.AxolotlService;

public interface OnKeyStatusUpdated {
	void onKeyStatusUpdated(AxolotlService.FetchStatus report);
}
