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
import java.util.concurrent.*;

class Fleet {

    final protected int max;
    private int available;
    private int load;
    private String name;

    Fleet(int max, int load, String name) {
        this.max = max;
        available = max;
        this.load = load;
        this.name = name;
    }

    synchronized public void Jong(int num) {
        if (available - num >= 0) {
            available -= num;
//            System.out.println("current : "+available);
        } else {
//            System.out.println("No Bike left");
        }
    }

    public int getLoad() {
        return load;
    }

    public int getAvailable() {
        return this.available;
    }

    public void resetAvailable() {
        available = max;
    }

    public void report() {
        System.out.println("There is " + getAvailable() + " cars left");
    }

    public String getName() {
        return this.name;
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

    public static int flag = 1; // flag status
    public static boolean startSell = true;
    private int max_drop;
    private int parcel;
    private DeliveryShop shop;
    private CyclicBarrier barrier;
    private ArrayList<SellerThread> sellerThreads;
    private ArrayList<DeliveryShop> deliveryShops;

    public SellerThread(String name, int m) {
        super(name);
        max_drop = m;
    }

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

    public void reportDay() {
        System.out.printf("%15s  >>  \n", Thread.currentThread().getName());
        System.out.printf("%15s  >>  %s \n", Thread.currentThread().getName(), "=".repeat(52));
        System.out.printf("%15s  >>  Day %d \n", Thread.currentThread().getName(), 1);
    }

    @Override
    public void run() {
        Random rand = new Random();
        for (int days = 0; days < 6; days++) {
            
            int randNum = rand.nextInt(deliveryShops.size() - 1);
            setDeliveryShop(deliveryShops.get(randNum));

            if (shop == null) {
                System.err.printf("Error: No shop assigned to thread %s\n", Thread.currentThread().getName());
                return;
            }

            int parcel = (int) (Math.random() * (max_drop - 1 + 1)) + 1;

            System.out.printf("%15s  >>  drop%4d parcels at %-15s shop \n", Thread.currentThread().getName(), parcel, shop.getName());
            shop.addParcels(parcel);


            try {
                barrier.await();
                barrier.await();
                barrier.await();
            } catch (Exception e) {
                System.err.println(e);
            }
            
            
        }
    }
}

class DeliveryShop {

    private String name;
    private int parcels;
    private int remaningParcels;
    private Fleet fleet;
    private int flag = 1;

    public DeliveryShop(String name, Fleet f) {
        this.name = name;
        this.fleet = f;
    }

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int changeFlag) {
        this.flag = changeFlag;
    }

    public String getName() {
        return name;
    }

    public int getParcels() {
        return parcels;
    }

    public void addParcels(int parcels) {
        this.parcels += parcels;
    }

    public int calculateParcels() {
        int parcelsCanSend = 0;

        if (parcels >= fleet.getLoad() / 2 && parcels < fleet.getLoad()) {
            parcelsCanSend = parcels;
        } else if (parcels > fleet.getLoad()) {
            parcelsCanSend = parcels - (parcels % fleet.getLoad());
        }

        remaningParcels = parcels - parcelsCanSend;

        return parcelsCanSend;
    }

    public int calculateAmountOfVehicles() {
        int amountOfVehicles = 0;

        if (parcels > 0) {
            amountOfVehicles = parcels / fleet.getLoad();
        }

        parcels = remaningParcels;

        return amountOfVehicles;
    }

    public void allocateVehicles(int amount) {
        fleet.Jong(amount);
    }

    public int getVehicleAvailable() {
        return fleet.getAvailable();
    }

    public void fleetResetAvailable() {
        fleet.resetAvailable();
    }

    public Fleet getFleet() {
        return fleet;
    }
}

class DeliveryThread extends Thread {

    DeliveryShop shop;
    private CyclicBarrier barrier;

    public DeliveryThread(DeliveryShop s) {
        super(s.getName());
        shop = s;
    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }
    
    @Override
    public void run() {
        for (int days = 0; days < 6; days++) {

            try {
                barrier.await();
            } catch (Exception e) {
                System.err.println(e);
            }

            printDelivery();

            // >> wait all delivery thread finished print
            try {
                barrier.await();
            } catch (Exception e) {
                System.err.println(e);
            }
            // << wait all delivery thread finished print

            printRemainingPacels();
            
            try {
                barrier.await();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    synchronized public void printDelivery() {
        printPacelsToDelivery();
    }

    public void printPacelsToDelivery() {
        Thread th = Thread.currentThread();

        System.out.printf("%15s  >>      parcels to deliver =%4d \n", th.getName(), shop.getParcels());

        shop.fleetResetAvailable();
    }

    synchronized public void printRemainingPacels() {
        Thread th = Thread.currentThread();

        int parcelsCanSend = shop.calculateParcels();

        int amountOfVehicles = shop.calculateAmountOfVehicles();

        shop.allocateVehicles(amountOfVehicles);

        System.out.printf("%15s  >>  deliver%4d parcels by%3d %-10s %14s parcels =%4d\n", th.getName(), parcelsCanSend, amountOfVehicles, shop.getFleet().getName(), "remaining", shop.getParcels());

    }
}

public class Delivery {

    public static void main(String[] args) {
        Delivery mainapp = new Delivery();
        mainapp.runSimulation();
    }

    public ArrayList<Integer> readConfig() {
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

    public void runThreadSimulation(ArrayList<SellerThread> sellerThreads, ArrayList<DeliveryShop> deliveryShops, ArrayList<DeliveryThread> deliveryThreads) {

        for (SellerThread i : sellerThreads) {
            i.setArrayList(sellerThreads, deliveryShops);
            i.start();
        }

        for (DeliveryThread i : deliveryThreads) {
            i.start();
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
    }

    public void reportInit(ArrayList<Integer> InputAL, ArrayList<SellerThread> sellerThreads, ArrayList<DeliveryShop> deliveryShops) {
        Thread me = Thread.currentThread();

        int days = InputAL.get(0);
        int bikeAmount = InputAL.get(1);
        int bikeMaxLoad = InputAL.get(2);
        int truckAmount = InputAL.get(3);
        int truckMaxLoad = InputAL.get(4);
        int maxParcelDrop = InputAL.get(6);

        System.out.printf("%15s  >>  %s Parameters %s \n", me.getName(), "=".repeat(20), "=".repeat(20));
        System.out.printf("%15s  >>  days of simulation = %d \n", me.getName(), days);
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

    public void runSimulation() {
        ArrayList<Integer> InputAL = readConfig();

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
        ArrayList<DeliveryShop> deliveryShops = createDeliveryShops(numBikeThread, numTruckThread, BF, TF);
        ArrayList<DeliveryThread> deliveryThreads = createDeliveryThreads(deliveryShops, numBikeThread, numTruckThread);

        CyclicBarrier barrier = new CyclicBarrier(sellerThreads.size() + deliveryThreads.size());

        for (SellerThread i : sellerThreads) {
            i.setBarrier(barrier);
        }

        for (DeliveryThread i : deliveryThreads) {
            i.setBarrier(barrier);
        }

        // report Initialize
        this.reportInit(InputAL, sellerThreads, deliveryShops);

        // use this function to manage all threads
        this.runThreadSimulation(sellerThreads, deliveryShops, deliveryThreads);

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

}
