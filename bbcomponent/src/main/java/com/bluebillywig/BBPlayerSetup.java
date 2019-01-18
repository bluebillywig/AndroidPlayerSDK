package com.bluebillywig;

public class BBPlayerSetup {
	private boolean debug = false;
	private String playout = "default";
	private String adUnit = "";
	private String assetType = "c";
	private String parameter = "";

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

	public void setAdunit(String adUnit) {
		this.adUnit = adUnit;
	}

	public String getAdunit() {
		return adUnit;
	}

	public void setAssetType(String assetType) {
		this.assetType = assetType;
	}

	public String getAssetType() {
		return assetType;
	}

	public String getParameter() { return parameter; }

	public void setParameter(String parameter) { this.parameter = parameter; }
	
	@Override
	public String toString(){
		return "BBPlayerSetup: { playout:'" + getPlayout() + "', assetType:'" + getAssetType() + "' debug:" + (debug?"true":"false") + "', adunit:'" + getAdunit() + "'}";
	}
}