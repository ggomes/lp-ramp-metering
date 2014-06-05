
package beats;

final public class Parameters extends jaxb.Parameters {

	public boolean has(String name) {
		if(name==null)
			return false;
		for (jaxb.Parameter param : getParameter()) {
			if (name.equals(param.getName())) return true;
		}
		return false;
	}

	public String get(String name) {
		if(name==null)
			return null;
		java.util.ListIterator<jaxb.Parameter> iter = getParameter().listIterator(getParameter().size());
		while (iter.hasPrevious()) {
			jaxb.Parameter param = iter.previous();
			if (name.equals(param.getName())) return param.getValue();
		}
		return null;
	}
	
}
