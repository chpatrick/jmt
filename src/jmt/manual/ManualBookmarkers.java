package jmt.manual;

/**
 * <b>ManualBookmarkers</b> enum is used to map PDF manual chapters with JMT tools online help.
 * <br>
 * It is very important that the chapterPrefix text matches the beginning of the chapter name
 * @author Lucia Guglielmetti 
 */
public enum ManualBookmarkers {
	INTRO("1 Intro"),
	JMVA("2 JMVA"),
	JSIMgraph("3 JSIMgraph"),
	JSIMwiz("4 JSIMwiz"),
	JMCH("5 JMCH"),
	JABA("6 JABA"),
	JWAT("7 JWAT");
	
	private String chapterPrefix;
	
	/**
	 * Builds a manual bookmark
	 * @param chapterStart the prefix of the chapter of the given tool
	 */
	private ManualBookmarkers(String chapterStart) {
		this.chapterPrefix = chapterStart;
	}
	
	/**
	 * @return the prefix used to identify the chapter of each tool
	 */
	public String getChapterPrefix() {
		return chapterPrefix;
	}
	
}