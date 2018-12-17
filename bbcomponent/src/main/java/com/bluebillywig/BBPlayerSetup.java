package com.bluebillywig;

public class BBPlayerSetup {
	private boolean debug = false;
	private String playout = "default";
	private String assetType = "c";

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setPlayout(String playout) {
		this.playout = playout;
	}

	public String getPlayout() {
		return playout;
	}

	public void setAssetType(String assetType) {
		this.assetType = assetType;
	}

	public String getAssetType() {
		return assetType;
	}
	
	@Override
	public String toString(){
		return "BBPlayerSetup: { playout:'" + getPlayout() + "', assetType:'" + getAssetType() + "' debug:" + (debug?"true":"false") + "}";
	}
}