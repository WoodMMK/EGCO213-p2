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
import java.util.logging.Level;
import java.util.logging.Logger;


class Fleet{
    final protected int max;
    private int available;
    
    Fleet(int max){
        this.max = max;
        available = max;
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
    BikeFleet(int num){
        super(num);
    }
}
class TruckFleet extends Fleet{
    TruckFleet(int num){
        super(num);
    }
}

class SellerThread
{

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
        shop.fleetReset();
    }
    
    public synchronized void printDelivery()
    {
        printPacelsToDelivery();
        printRemainingPacels();
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
    Fleet fp;
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
    public Scanner readConfig(){
        String config_filename = "config_1.txt";
        String mainPath = "src/main/java/project2/";
        
        String productInput;
        Scanner productScan = null;
        
        while (productScan == null) {
            try {
                productScan = new Scanner(new File(mainPath + config_filename));
            } catch (FileNotFoundException e) {
                System.err.println(e);
                System.out.println("Enter correct file name: ");
                config_filename = new Scanner(System.in).nextLine();
            }
        }
        return productScan;
    }
    public void runSimulation(){
        Scanner configScan = readConfig();
        
        if(configScan != null){
            while(configScan.hasNextLine()){
                System.out.println(configScan.nextLine());
            }
            configScan.close();
        }
        
        BikeFleet myBikeFleet = new BikeFleet(10);
        MyThread MT1 = new MyThread("M1", myBikeFleet);
        MyThread MT2 = new MyThread("M2", myBikeFleet);
        MyThread MT3 = new MyThread("M3", myBikeFleet);
        
        try {
            MT1.join();
            MT2.join();
            MT3.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Delivery.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        System.out.println("end simulation");
    }
    
}
