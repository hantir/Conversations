package com.frizid.timeline.xmpp.jingle;

import com.frizid.timeline.entities.DownloadableFile;

public interface OnFileTransmissionStatusChanged {
	void onFileTransmitted(DownloadableFile file);

	void onFileTransferAborted();
}
