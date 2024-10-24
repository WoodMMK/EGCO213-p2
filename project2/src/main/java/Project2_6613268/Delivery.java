/*
 * Members
 * 1. Kunanont 	    Seehamat 	    6613115
 * 2. Chatiphum     Chidjai 	    6613118
 * 3. Maimongkol    Thokanokwan     6613268
 * 4. Sakonkiat     Rungsaranon     6613273
*/
package Project2_6613268;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class Fleet {

    final protected int max;
    private int         available;
    private int         load;
    private String      name;

    Fleet(int max, int load, String name) {
        this.max    = max;
        available   = max;
        this.load   = load;
        this.name   = name;
    }

    // get and set method
    public int getLoad() {
        return load;
    }

    public int getAvailable() {
        return this.available;
    }
    
    public int getMax() {
        return max;
    }

    public String getName() {
        return this.name;
    }
    
    public void resetAvailable() {
        available = max;
    }

    // print method
    public void report() {
        System.out.println("There is " + getAvailable() + " cars left");
    }
    
    // calulate method
    synchronized public int allocateVehicles(int amountOfVehicles) {
    int allocated = 0;

    if (available >= amountOfVehicles) {
        available -= amountOfVehicles;
        allocated = amountOfVehicles;
    } else {
        allocated = available;
        available = 0;
    }
    
        return allocated;
    }
}

class BikeFleet extends Fleet {

    BikeFleet(int amount, int load) {
        super(amount, load, "bikes");
    }
}

class TruckFleet extends Fleet {

    TruckFleet(int amount, int load) {
        super(amount, load, "trucks");
    }
}

class SellerThread extends Thread {

    public static int               flag        = 1; // flag status
    public static boolean           startSell   = true;
    private int                     max_drop;
    private int                     parcel;
    private DeliveryShop            shop;
    private CyclicBarrier           barrier;
    private ArrayList<SellerThread> sellerThreads;
    private ArrayList<DeliveryShop> deliveryShops;

    public SellerThread(String name, int m) {
        super(name);
        max_drop = m;
    }

    //set method
    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    public void setDeliveryShop(DeliveryShop ds) {
        shop = ds;
    }

    public void setArrayList(ArrayList<SellerThread> sellerThreads, ArrayList<DeliveryShop> deliveryShops) {
        this.sellerThreads = sellerThreads;
        this.deliveryShops = deliveryShops;
    }

    // thread running method
    @Override
    public void run() {
        Random rand = new Random();
        for (int day = 0; day < Delivery.days; day++) {

            // Seller : assign random shop
            int randNum = rand.nextInt(deliveryShops.size());
            setDeliveryShop(deliveryShops.get(randNum));
            if (shop == null) {
                System.err.printf("Error: No shop assigned to thread %s\n", Thread.currentThread().getName());
                return;
            }

            Delivery.reportDay(day + 1, barrier);

            parcel = (int) (Math.random() * (max_drop - 1 + 1)) + 1;

            System.out.printf("%15s  >>  drop%4d parcels at %-15s shop \n", Thread.currentThread().getName(), parcel, shop.getName());
            shop.addParcels(parcel);

            try {barrier.await(); barrier.await(); barrier.await();} catch (Exception e) {System.err.println(e);}
        }
    }
}

class DeliveryShop implements Comparable<DeliveryShop> {

    private String  name;
    private int     parcels;
    private int     remainingParcels;
    private Fleet   fleet;
    private int     flag;
    private int     received;
    private int     delivered;

    public DeliveryShop(String name, Fleet f) {
        this.name   = name;
        this.fleet  = f;
        flag        = 1;
    }

    // get and set method
    public int getFlag() {
        return this.flag;
    }

    public int getParcels() {
        return parcels;
    }

    public int getReceived() {
        return this.received;
    }

    public int getDelivered() {
        return this.delivered;
    }
    
    public String getName() {
        return name;
    }
    
    public Fleet getFleet() {
        return fleet;
    }
    
    public void setFlag(int changeFlag) {
        this.flag = changeFlag;
    }

    public int getRemainParcels(){
        return this.remainingParcels;
    }
    
    public void addParcels(int parcels) {
        this.parcels += parcels;

        received += parcels; // added received parcels.
    }
    
    // print method
    public void printSummarySuccessRate()
    {
        System.out.printf("%15s  >>  %-18s received = %5d, delivered =%5d, success rate = %.2f\n", Thread.currentThread().getName(), getName(), getReceived(), getDelivered(), calculateSuccessRate());
    }
    
    // calculate method
    public int setLimit(int amountOfVehicles, int parcelsCanSend)
    {
        int limitParcels    =   fleet.getAvailable() * fleet.getLoad();
        
        if (amountOfVehicles > fleet.getAvailable())
        {
            remainingParcels    +=  parcels - limitParcels;
            parcelsCanSend      =   limitParcels;
                  
        }
 
        return parcelsCanSend;
    }
    
    public double calculateSuccessRate() {
        double successRate = (this.delivered / 1.0) / (this.received / 1.0);
        
        return successRate;
    }
    
    public int calculateParcels() {
        int parcelsCanSend  = 0;
        int temp_remain     = parcels % fleet.getLoad();
        int halfLoad        = fleet.getLoad() / 2;
        
        if (parcels > fleet.getLoad()) {
            // over max
            int amountOfVehicles = parcels / fleet.getLoad();
            
            if (temp_remain >= halfLoad) 
            { 
                parcelsCanSend      = parcels;
                remainingParcels    = 0;
                parcelsCanSend      = setLimit(amountOfVehicles + 1, parcelsCanSend);
            }
            else
            {
                remainingParcels    = temp_remain;
                parcelsCanSend      = parcels - temp_remain;
                parcelsCanSend      = setLimit(amountOfVehicles, parcelsCanSend);
            }               
        } else if (parcels < halfLoad) {
            // below min
            remainingParcels = parcels;
        } else {
            //between max and min
            parcelsCanSend = parcels;
        }
        
        delivered += parcelsCanSend;

        return parcelsCanSend;
    }

    public int calculateAmountOfVehicles() {
        int amountOfVehicles = 0;
        int tempParcels = parcels;

        if (parcels > 0) {
            amountOfVehicles    += tempParcels / fleet.getLoad();
            tempParcels         %= fleet.getLoad();
            
            if (tempParcels > fleet.getLoad() / 2) {
                amountOfVehicles++;
            }
        }

        parcels = remainingParcels;

        return amountOfVehicles;
    }
    
    @Override
    public int compareTo(DeliveryShop other) {
        if (this.calculateSuccessRate() > other.calculateSuccessRate()) {
            return -1;
        } else if (this.calculateSuccessRate() < other.calculateSuccessRate()) {
            return 1;
        } else {
            return this.getName().compareTo(other.getName());
        }
    }
}

class DeliveryThread extends Thread {

    DeliveryShop            shop;
    private CyclicBarrier   barrier;

    public DeliveryThread(DeliveryShop s) {
        super(s.getName());
        shop = s;
    }

    //set method
    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }
    
    //print method
    synchronized public void printDelivery() {
        printPacelsToDelivery();
    }

    public void printPacelsToDelivery() {
        Thread th = Thread.currentThread();

        System.out.printf("%15s  >>      parcels to deliver =%4d \n", th.getName(), shop.getParcels());

        shop.getFleet().resetAvailable();
    }

    synchronized public void printRemainingPacels() {
        Thread th = Thread.currentThread();

        int parcelsCanSend              = shop.calculateParcels();

        int amountOfVehicles            = shop.calculateAmountOfVehicles();

        int amountOfAllocateVehicles    = shop.getFleet().allocateVehicles(amountOfVehicles);

        System.out.printf("%15s  >>  deliver%4d parcels by%3d %-10s %10s parcels =%4d\n", th.getName(), parcelsCanSend, amountOfAllocateVehicles, shop.getFleet().getName(), "remaining", shop.getRemainParcels());

    }
    
    //thread running method
    @Override
    public void run() {
        for (int day = 0; day < Delivery.days; day++) {
            Delivery.reportDay(day + 1, barrier);

            try {barrier.await();} catch (Exception e) {System.err.println(e);}

            printDelivery();

            // >> wait all delivery thread finished print
            try {barrier.await();} catch (Exception e) {System.err.println(e);}
            // << wait all delivery thread finished print

            printRemainingPacels();

            try {barrier.await();} catch (Exception e) {System.err.println(e);}
        }
    }
}

public class Delivery {
    
    // statics
    static int days;
    public static void reportDay(int currentDay, CyclicBarrier b) {
        // report day
        int x = -1;
        try {
            x = b.await();
        } catch (Exception e) {
            System.err.println(e);
        }
        if (x == 0) {
            System.out.printf("%15s  >>  \n", Thread.currentThread().getName());
            System.out.printf("%15s  >>  %s \n", Thread.currentThread().getName(), "=".repeat(52));
            System.out.printf("%15s  >>  Day %d \n", Thread.currentThread().getName(), currentDay);
        }
        try {
            b.await();
        } catch (Exception e) {
            System.err.println(e);
        }
        // end report day
    }
    
    // setting methods 
    public  ArrayList<Integer> readConfig() {
        String              config_filename = "config_0.txt";
        String              mainPath        = "src/main/java/Project2_6613268/";
        ArrayList<Integer>  InputAL         = new ArrayList<>();
        
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
        while (configScan.hasNext()) {
            if (configScan.hasNextInt()) {
                int number = configScan.nextInt();
                InputAL.add(number);
            } else {
                // If it's not an integer, skip it
                configScan.next();
            }
        }
        configScan.close();

        return InputAL;
    }
    
    private ArrayList<SellerThread> createSellerThreads(int sellerNumThread, int maxDrop) {
        ArrayList<SellerThread> sellerAL = new ArrayList<>();
        for (int i = 0; i < sellerNumThread; i++) {
            String name = "Seller_" + i;
            sellerAL.add(new SellerThread(name, maxDrop));
        }
        return sellerAL;
    }

    private ArrayList<DeliveryShop> createDeliveryShops(int numBikeThread, int numTruckThread, BikeFleet BF, TruckFleet TF) {
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

    // running methods
    
    public static void main(String[] args) {
        Delivery mainapp = new Delivery();
        mainapp.InitializeSimulation();
    }
    
    public void InitializeSimulation() {
        ArrayList<Integer> InputAL  = readConfig();

        // Set up simulation parameters
        days                        = InputAL.get(0);

        // Set up attributes
        BikeFleet BF                = new BikeFleet(InputAL.get(1), InputAL.get(2));
        TruckFleet TF               = new TruckFleet(InputAL.get(3), InputAL.get(4));
        int sellerNumThread         = InputAL.get(5);
        int maxDrop                 = InputAL.get(6);
        int numBikeThread           = InputAL.get(7);
        int numTruckThread          = InputAL.get(8);

        // Create Arraylist components
        ArrayList<SellerThread>     sellerThreads       = createSellerThreads(sellerNumThread, maxDrop);
        ArrayList<DeliveryShop>     deliveryShops       = createDeliveryShops(numBikeThread, numTruckThread, BF, TF);
        ArrayList<DeliveryThread>   deliveryThreads     = createDeliveryThreads(deliveryShops, numBikeThread, numTruckThread);

        // report Initialize
        this.reportInit(InputAL, sellerThreads, deliveryShops);

        // use this function to manage all threads
        this.runSimulation(sellerThreads, deliveryShops, deliveryThreads);

    }
    
    public void reportInit(ArrayList<Integer> InputAL, ArrayList<SellerThread> sellerThreads, ArrayList<DeliveryShop> deliveryShops) {
        Thread me = Thread.currentThread();

//        int days = InputAL.get(0);
        int bikeAmount      = InputAL.get(1);
        int bikeMaxLoad     = InputAL.get(2);
        int truckAmount     = InputAL.get(3);
        int truckMaxLoad    = InputAL.get(4);
        int maxParcelDrop   = InputAL.get(6);

        System.out.printf("%15s  >>  %s Parameters %s \n", me.getName(), "=".repeat(20), "=".repeat(20));
        System.out.printf("%15s  >>  days of simulation = %d \n", me.getName(), Delivery.days);
        System.out.printf("%15s  >>  Bike  Fleet, total bikes  =%4s, max load =%4d parcels, min load =%4d parcels \n", me.getName(), bikeAmount, bikeMaxLoad, bikeMaxLoad / 2);
        System.out.printf("%15s  >>  Truck Fleet, total trucks =%4s, max load =%4d parcels, min load =%4d parcels \n", me.getName(), truckAmount, truckMaxLoad, truckMaxLoad / 2);

        System.out.printf("%15s  >>  SellerThreads    = [", me.getName());

        for (int i = 0; i < sellerThreads.size(); i++) {
            if (i != sellerThreads.size() - 1) {
                System.out.printf("%s, ", sellerThreads.get(i).getName());
            } else {
                System.out.printf("%s", sellerThreads.get(i).getName());
            }
        }

        System.out.printf("] \n");

        System.out.printf("%15s  >>  max parcel drop  = %d \n", me.getName(), maxParcelDrop);

        System.out.printf("%15s  >>  DeliveryThreads  = [", me.getName());

        for (int i = 0; i < deliveryShops.size(); i++) {
            if (i != deliveryShops.size() - 1) {
                System.out.printf("%s, ", deliveryShops.get(i).getName());
            } else {
                System.out.printf("%s", deliveryShops.get(i).getName());
            }
        }

        System.out.printf("] \n");
    }
    
    public void runSimulation(ArrayList<SellerThread> sellerThreads, ArrayList<DeliveryShop> deliveryShops, ArrayList<DeliveryThread> deliveryThreads) {
        // +1 main thread
        CyclicBarrier barrier = new CyclicBarrier(sellerThreads.size() + deliveryThreads.size() + 1);

        for (SellerThread i : sellerThreads) {   
            i.setBarrier(barrier);  
            i.setArrayList(sellerThreads, deliveryShops);
            i.start();
        }

        for (DeliveryThread i : deliveryThreads) {
            i.setBarrier(barrier);
            i.start();
        }

        for (int i = 0; i < days; i++) {
            try {
                    Delivery.reportDay(i + 1, barrier);

                    barrier.await();
                    barrier.await();
                    barrier.await();
                } catch (Exception e) {
            }
        }

        for (SellerThread i : sellerThreads) {
            try {
                i.join();
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        for (DeliveryThread i : deliveryThreads) {
            try {
                i.join();
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        System.out.printf("%15s  >>  \n", Thread.currentThread().getName());
        System.out.printf("%15s  >>  %s \n", Thread.currentThread().getName(), "=".repeat(52));
        System.out.printf("%15s  >>  Summary\n", Thread.currentThread().getName());

        Collections.sort(deliveryShops);

        for (DeliveryShop i : deliveryShops) {
            i.printSummarySuccessRate();
        }
    }
    
}
