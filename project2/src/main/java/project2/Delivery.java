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
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

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
