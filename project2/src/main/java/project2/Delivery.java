/*
 * Members
 * Kunanont 115
 * Chatpum 118
 * Maimongkol 268
 * Sakolkiat 273
 */
package project2;

import java.io.*;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


class Fleet{
    final protected int     max;
    private int             available;
    private int             load;
    
    Fleet(int max, int load){
        this.max = max;
        available = max;
        this.load = load;
    }
    synchronized public void Jong(int num){
        if(available-num>=0){
            available-=num;
            System.out.println("current : "+available);
        }
        else{
            System.out.println("No Bike left");
        }
    }
    public int getAvailable(){
        return this.available;
    }
    public void resetAvailable()
    {
        available = max;
    }
    public void report(){
        System.out.println("There is "+ getAvailable()+ " cars left");
    }
}

class BikeFleet extends Fleet{
    BikeFleet(int amount, int load){
        super(amount, load);
    }
}
class TruckFleet extends Fleet{
    TruckFleet(int amount, int load){
        super(amount, load);
    }
}

class SellerThread extends Thread
{
    private int max_drop;
    private int parcel;
    private DeliveryShop shop;
    
    public SellerThread (String name, int m) { super(name); max_drop = m; start();}

    public void setDeliveryShop(DeliveryShop ds) { shop = ds; }

    public void run()
    {
        parcel = (int)(Math.random() * (max_drop - 1 + 1)) + 1;
        System.out.printf("%s  >>  drop %d parcels at %s shop", Thread.currentThread().getName(), parcel, shop.getName());
        shop.addParcels(parcel);
    }
}

class DeliveryShop
{
    private String  name;
    private int     parcels;
    private Fleet   fleet;
    
    public DeliveryShop(String name, Fleet f)   {this.name = name; this.fleet = f;}
    
    public String   getName()                   {return name;}
    public int      getParcels()                {return parcels;}
    public void     addParcels(int parcels)     {this.parcels += parcels;}
    
    public int calculateParcels()
    {
        int parcelsCanSend = 0;
        
        if (parcels >= fleet.getAvailable() / 2 && parcels < fleet.getAvailable())
            parcelsCanSend = parcels;
        else if (parcels > fleet.getAvailable())
            parcelsCanSend = parcels - (parcels % fleet.getAvailable());
        
        parcels -= parcelsCanSend;
        
        return parcelsCanSend;
    }
    
    public int calculateAmountOfVehicles()
    {
        int amountOfVehicles = 0;
        
        if (parcels > 0)
            amountOfVehicles = parcels / fleet.getAvailable();
        
        return amountOfVehicles;    
    }
    
    public void allocateVehicles(int amount)
    {
        fleet.Jong(amount);
    }
    
    public int getVehicleAvailable()
    {
        return fleet.getAvailable();
    }
    
    public void fleetResetAvailable()
    {
        fleet.resetAvailable();
    }
    
}

class DeliveryThread extends Thread
{
    DeliveryShop    shop;
    
    public DeliveryThread(DeliveryShop s)
    {
        super(s.getName());
        shop    = s;   
        start();
    }
    
    @Override
    public void run()
    {
        printDelivery();
    }
    
    public synchronized void printDelivery()
    {
        printPacelsToDelivery();
        printRemainingPacels();
        shop.fleetResetAvailable();
    }
    
    public void printPacelsToDelivery()
    {
        Thread th = Thread.currentThread();
        
        System.out.println(th.getName() + " >>      parcels to deliver " + shop.getParcels());
    }
    
    public void printRemainingPacels()
    {
        Thread th = Thread.currentThread();
        
        int parcelsCanSend = shop.calculateParcels();
        
        int amountOfVehicles = shop.calculateAmountOfVehicles();
        
        shop.allocateVehicles(amountOfVehicles);
        
        System.out.printf("%s >> deliver %4d parcels by %4d %-8s remaining parcels = %4d, remaining %-8s = %4d", th.getName(), parcelsCanSend, amountOfVehicles, shop.getParcels(), shop.getVehicleAvailable());
    }
}

class MyThread extends Thread{
    Fleet       fp;
    public MyThread(String name, Fleet fp){
        super(name);
        this.fp = fp;
        this.start();
    }
    public void run(){
        for(int i = 0; i<3; i++){
           fp.Jong(2); 
        }
    }
}

public class Delivery {
    public static void main(String []args){
        Delivery mainapp = new Delivery();
        mainapp.runSimulation();
    }
    public ArrayList<Integer> readConfig(){
        String config_filename = "config_1.txt";
        String mainPath = "src/main/java/project2/";
        ArrayList<Integer> InputAL = new ArrayList<>();
        
        String productInput;
        Scanner configScan = null;
        
        while (configScan == null) {
            try {
                configScan = new Scanner(new File(mainPath + config_filename));
            } catch (FileNotFoundException e) {
                System.err.println(e);
                System.out.println("Enter correct file name: ");
                config_filename = new Scanner(System.in).nextLine();
            }
        }
        configScan.useDelimiter("[,\\s]+");
        while(configScan.hasNext()){
            if (configScan.hasNextInt()) {
                int number = configScan.nextInt();
                InputAL.add(number);  
                System.out.println("add number "+number);
            }
            else{
                // If it's not an integer, skip it
                configScan.next();
            }
        }
        configScan.close();
        
        return InputAL;
    }
    
    
    public void runSimulation(){
        ArrayList<Integer> InputAL           = readConfig();
        
        // Set up simulation parameters
        int days = InputAL.get(0);
        
        // Set up attributes
        BikeFleet BF = new BikeFleet(InputAL.get(1), InputAL.get(2));
        TruckFleet TF = new TruckFleet(InputAL.get(3), InputAL.get(4));
        int sellerNumThread = InputAL.get(5);
        int maxDrop = InputAL.get(6);
        int numBikeThread = InputAL.get(7);
        int numTruckThread = InputAL.get(8);

        // Create Arraylist components
        ArrayList<SellerThread> sellerThreads = createSellerThreads(sellerNumThread, maxDrop);
        ArrayList<DeliveryShop> deliveryShops = createDeliveryShops(numBikeThread, numTruckThread,BF, TF);
        ArrayList<DeliveryThread> deliveryThreads = createDeliveryThreads(deliveryShops, numBikeThread, numTruckThread);
        
        
        System.out.println("end simulation");
    }
    
    private ArrayList<SellerThread> createSellerThreads(int sellerNumThread, int maxDrop) {
        ArrayList<SellerThread> sellerAL = new ArrayList<>();
        for (int i = 0; i < sellerNumThread; i++) {
            String name = "Seller_" + i;
            sellerAL.add(new SellerThread(name, maxDrop));
        }
        return sellerAL;
    }

    private ArrayList<DeliveryShop> createDeliveryShops(int numBikeThread, int numTruckThread, BikeFleet BF, TruckFleet TF ) {
        ArrayList<DeliveryShop> shopAL = new ArrayList<>();
        
        // Create bike shops
        for (int i = 0; i < numBikeThread; i++) {
            String name = "BikeDelivery_" + i;
            shopAL.add(new DeliveryShop(name, BF));
        }
        
        // Create truck shops
        for (int i = 0; i < numTruckThread; i++) {
            String name = "TruckDelivery_" + i;
            shopAL.add(new DeliveryShop(name, TF));
        }
        
        return shopAL;
    }

    private ArrayList<DeliveryThread> createDeliveryThreads(ArrayList<DeliveryShop> shopAL, int numBikeThread, int numTruckThread) {
        ArrayList<DeliveryThread> deliveryAL = new ArrayList<>();
        
        // Create Bike Delivery Threads
        for (int i = 0; i < numBikeThread; i++) {
            deliveryAL.add(new DeliveryThread(shopAL.get(i)));
        }
        
        // Create Truck Delivery Threads
        for (int i = numBikeThread; i < numBikeThread + numTruckThread; i++) {
            deliveryAL.add(new DeliveryThread(shopAL.get(i)));
        }
        
        return deliveryAL;
    }
    
}
