/*
 * Members
 * Kunanont 115
 * Chatpum 118
 * Maimongkol 268
 * Sakolkiat 273
 */
package project2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class Fleet
{

}

class SellerThread extends Thread
{
    private int max_drop;
    private int parcel;
    private DeliveryShop shop;
    
    public SellerThread (String name, int m) { super(name); max_drop = m; }

    public void setDeliveryShop(DeliveryShop ds) { shop = ds; }

    public void run()
    {

        parcel = (int)(Math.random() * (max_drop - 1 + 1)) + 1;
        System.out.printf("%s  >>  drop %d parcels at %s shop", Thread.currentThread().getName(), parcel, shop.getName());
        shop.setParcel(parcel);
    }
}

class DeliveryShop
{

}

class DeliveryThread
{
    
}

public class Delivery {
    public static void main(String []args){
        Delivery mainapp = new Delivery();
        mainapp.runSimulation();
    }
    public Scanner readConfig(){
        String config_filename = "config_.txt";
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

        // this is Atom!
        
        Scanner configScan = readConfig();
        
        if(configScan != null){
            while(configScan.hasNextLine()){
                System.out.println(configScan.nextLine());
            }
            configScan.close();
        }
        System.out.println("end simulation");
    }
    
}
