/**
 * $Id: GreyListMap.java,v 1.00 2014/12/06 16:20:37 dmorris Exp $
 */
package com.untangle.node.spam;

import java.util.Map;
import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class GreyListMap<K,V> extends LinkedHashMap<K,V>
{
    private static final int MAX_ENTRIES = 10000;

    protected boolean removeEldestEntry(Map.Entry<K,V> eldest)
    {
        return size() > MAX_ENTRIES;
    }
}