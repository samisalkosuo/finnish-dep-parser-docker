package fi.dep.ported;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Tag {
	public Map allReadings(String input);
	public Set sortUnique(String input);
	public void marmot(String details);
	public String quickParse(String input);
}
