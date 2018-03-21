/**
 * Node.java
 * @author: Akash Venkatachalam  
 * Versions: $Id: Node.java v1.0 $
 * Revision: Second Revision.
 */

import java.io.Serializable;

/**
 * Used to create a node, represents a value in the key-value pair, which can store the details for a router
 */

public class Node implements Serializable
{
    public String network;
    public String nexthop;
    public int cost;

    /**
     * Constructor of Node class to initialize the variables
     * @param mynet
     * @param val
     * @param c
     */
    public Node(String mynet, String val, int c)
    {
        this.network = mynet;
        this.nexthop = val;
        this.cost = c;
    }

    public void setCost(int c)
    {
        cost = c;
    }
    public String getHop()
    {
        return nexthop;
    }
    public  int getCost()
    {
        return cost;
    }

    public String getValue()
    {
        return nexthop;
    }
    public void setValue(String v)
    {
        nexthop = v;
    }
    public void setHop(String n)
    {
        nexthop = n;
    }
}