/**
 * ripv2.java
 * @author: Akash Venkatachalam 
 * Versions: $Id: ripv2.java v1.0 $
 * Revision: Second Revision.
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements a distance vector routing protocol called RIPv2
 */
public class ripv2 implements Runnable
{
    Map<String, Node> network_list;
    String ip;
    int myPort;
    int neighborPort;
    DatagramSocket neighbour1;
    long time;

    /**
     * Constructor of RIPv2 class used for initializing the parameters
     * @param ip
     * @param myPort
     * @param neighborPort
     * @param network_list
     * @throws SocketException
     * @throws UnknownHostException
     */
    ripv2(String ip, int myPort, int neighborPort, Map<String, Node> network_list) throws SocketException,UnknownHostException
    {
        this.ip = ip;
        this.neighborPort = neighborPort;
        this.myPort = myPort;
        this.network_list = network_list;
        this.neighbour1 = new DatagramSocket(myPort);
        this.time = System.currentTimeMillis();
    }
    @Override
    public void run()
    {
        if(Thread.currentThread().getName().equals("time"))
        {
            try
            {
                check();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        receive();
    }

    /**
     * Keeps a check on the change of router's cost
     * @throws InterruptedException
     */
    void check() throws InterruptedException
    {
        while(true)
        {
            Thread.sleep(5000);
            if(System.currentTimeMillis() - time > 6000)
            {
                change_cost();
            }
        }
    }

    /**
     * Checks for the reachability of its neighbour, changes its cost and prints it
     */
    void change_cost()
    {
        String neibhour = ip+":"+Integer.toString(neighborPort);
        for(String key : network_list.keySet())
        {
            if(network_list.get(key).getHop().equals(neibhour) && !(network_list.get(key).getCost() == 16) )
            {
                network_list.get(key).setCost(16);
                print();
            }
        }
    }

    /**
     * Method to receive the packet containing the routing information table
     */
    void receive()
    {
        try
        {
            while(true)
            {
                byte[] buffer = new byte[1024];
                DatagramPacket dpkt = new DatagramPacket(buffer, buffer.length);
                neighbour1.receive(dpkt);
                time = System.currentTimeMillis();

                buffer = dpkt.getData();

                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                ObjectInputStream is = new ObjectInputStream(in);
                Map<String, Node> sampleMapObject = (Map<String, Node>) is.readObject();

                for(String key : sampleMapObject.keySet())
                {
                    if(!network_list.containsKey(key))
                    {
                        network_list.put(key, new Node(key, ip + ":" + Integer.toString(neighborPort),
                                sampleMapObject.get(key).getCost() + 1));
                        print();
                    }
                    if(network_list.containsKey(key) && network_list.get(key).getCost() != 0){
                        if(sampleMapObject.get(key).getCost() != 16)
                        {
                            if(network_list.get(key).getCost() != sampleMapObject.get(key).getCost() + 1 &&
                                    (sampleMapObject.get(key).getCost() < network_list.get(key).getCost()))
                            {
                                network_list.put(key, new Node(key, ip + ":" + Integer.toString(neighborPort),
                                        sampleMapObject.get(key).getCost() + 1));
                                print();
                            }
                        }
                        if((sampleMapObject.get(key).getCost()==16) && (network_list.get(key).getCost() != 16))
                        {
                            network_list.put(key, new Node(key, ip + ":" + Integer.toString(neighborPort),
                                    16));
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Method for printing out the routing table
     */
    void print()
    {
        System.out.println("===============================================");
        System.out.println("Network \t Next hop \t Cost");
        for(String key : network_list.keySet())
        {
            System.out.println(key + "\t" + network_list.get(key).getHop() + "\t " + network_list.get(key).getCost());
        }
    }

    /**
     * Method for sending a packet containing a router's network list, its neighbours
     * @param arrList
     * @param to
     * @param ip
     * @param port
     * @throws Exception
     */
    private static void sendPacket(Map<String, Node> arrList, DatagramSocket to, String ip, int port) throws Exception
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(arrList);
        byte[] sendData = outputStream.toByteArray();
        DatagramPacket dpkt = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ip), port);
        to.send(dpkt);
    }

    /**
     * Method for printing the initial table for each router at the start
     * @param arrayList
     */
    public static void iniPrint(ArrayList arrayList)
    {
        String nxtHop = "0.0.0.0:0";
        int cost=0;
        Iterator itr = arrayList.iterator();
        System.out.println("===============================================");
        System.out.println("Network \t Next hop \t Cost");
        while(itr.hasNext())
        {
            System.out.println(itr.next()+ "\t" +nxtHop+ "\t " +cost);
        }
    }

    /**
     * Main method which reads in from the file and adds the information to the router's table
     * @param args
     */
    public static void main(String[] args)
    {
        String config_file= args[0];                                // Defining the router's config file
        Map<String, Node> network_list = new HashMap<>();
        String myNextHop = "0.0.0.0:0";
        int myCost = 0;
        ArrayList<Integer> port_list = new ArrayList<>();
        ArrayList initNetwork = new ArrayList();
        try(BufferedReader br = new BufferedReader(new FileReader(config_file)))
        {
            int index1,index2, str_len, index3;
            String fullLine, bothAddrs, firstIpPart, secondIppart, hostIP, myport, neighbport, network ;

            String neighbourIp = null;
            int port = 50000;
            while((fullLine = br.readLine()) != null)               // String manipulation of input file begins
            {
                if(fullLine.contains("LINK:"))
                {
                    index1 = fullLine.indexOf(":") + 2;             // Index of : after LINK
                    bothAddrs = fullLine.substring(index1);
                    index2 = bothAddrs.indexOf(" ");
                    firstIpPart = bothAddrs.substring(0, index2);
                    secondIppart = bothAddrs.substring(index2 + 1, bothAddrs.length());

                    index1 = firstIpPart.indexOf(":");
                    index2 = secondIppart.indexOf(":");
                    hostIP = firstIpPart.substring(0, index1);
                    neighbourIp = secondIppart.substring(1,index2);
                    myport = firstIpPart.substring(index1 + 1, firstIpPart.length());
                    neighbport = secondIppart.substring(index2 + 1, secondIppart.length());

                    int ownPort = Integer.parseInt(myport);
                    port = ownPort;
                    int neighbourPort1 = Integer.parseInt(neighbport);

                    ripv2 obj1 = new ripv2(hostIP, ownPort, neighbourPort1, network_list);
                    new Thread(obj1).start();                       // Strating a thread to receive incoming packets
                    new Thread(obj1,"time").start();                // Starting a thread to check for the updated table

                    port_list.add(neighbourPort1);
                }
                if(fullLine.contains("NETWORK:"))
                {
                    str_len = fullLine.length();
                    index3 = fullLine.indexOf(":") + 2;
                    network=fullLine.substring(index3,str_len);
                    network_list.put(network, new Node(network, myNextHop, myCost));
                    initNetwork.add(network);
                }
            }
            iniPrint(initNetwork);
            port += 100;
            DatagramSocket neighbour1 = new DatagramSocket(port);
            while (true)
            {
                Thread.sleep(3000);
                for(int  i = 0 ; i <port_list.size(); i++)
                {
                    sendPacket(network_list, neighbour1, neighbourIp , port_list.get(i));
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}