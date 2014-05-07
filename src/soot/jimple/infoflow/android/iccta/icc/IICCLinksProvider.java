package soot.jimple.infoflow.android.iccta.icc;

import java.util.List;



/**
 * Interface to retrieve Inter-Component Communication links.
 * @author alex
 *
 */
public interface IICCLinksProvider {
    
    public List<ICCLink> getICCLinks(String[] appName);

}
