package aryanware.lyrix;

import java.util.concurrent.TimeUnit;

public class LyrixLine {
	String text;
	long startTime;
	long endTime;
	
	public LyrixLine(String _text) {
		text = _text;
	}
	
	public void setTime(long start, long end) {
		startTime = start;
		endTime = end;
	}
	
	@Override
	public String toString() {
		String g = "";
		
		g += String.format("%02d", TimeUnit.HOURS.convert(startTime, TimeUnit.MILLISECONDS) % 24) + ":";
		g += String.format("%02d", TimeUnit.MINUTES.convert(startTime, TimeUnit.MILLISECONDS) % 60) + ":";
		g += String.format("%02d", TimeUnit.SECONDS.convert(startTime, TimeUnit.MILLISECONDS) % 60) + ",";
		g += String.format("%03d", TimeUnit.MILLISECONDS.convert(startTime, TimeUnit.MILLISECONDS) % 1000) + " --> ";
		g += String.format("%02d", TimeUnit.HOURS.convert(endTime, TimeUnit.MILLISECONDS) % 24) + ":";
		g += String.format("%02d", TimeUnit.MINUTES.convert(endTime, TimeUnit.MILLISECONDS) % 60) + ":";
		g += String.format("%02d", TimeUnit.SECONDS.convert(endTime, TimeUnit.MILLISECONDS) % 60) + ",";
		g += String.format("%03d", TimeUnit.MILLISECONDS.convert(endTime, TimeUnit.MILLISECONDS) % 1000) + "\n";
		g += text;
		
		return g;
	}
}