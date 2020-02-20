package edu.eci.arsw.moneylaundering;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoneyLaundering
{
    public static TransactionAnalyzer transactionAnalyzer;
    private TransactionReader transactionReader;
    private int amountOfFilesTotal;
    public static AtomicInteger amountOfFilesProcessed;
    
    public static Object threadMonitor = new Object();
    public static boolean pause = false;
    public static int threads = 5; // Cantidad de hilos requeridos en el problema

    public MoneyLaundering()
    {
        transactionAnalyzer = new TransactionAnalyzer();
        transactionReader = new TransactionReader();
        amountOfFilesProcessed = new AtomicInteger();
    }

    public void processTransactionData()
    {
        amountOfFilesProcessed.set(0);
        List<File> transactionFiles = getTransactionFileList();
        amountOfFilesTotal = transactionFiles.size();
        
        System.out.println(amountOfFilesTotal + "Archivos totales");
        MoneyLauderingThread moneyLauderingThreads[] = new MoneyLauderingThread[threads];


        int step = amountOfFilesTotal / threads;
        int start = 0;
        int end = 0;
        for (int i = 0; i < threads; i++) {
            end = start + step;
            if(i+1==threads){
                end = amountOfFilesTotal;
            }
            System.out.println("Los rangos de los hilos son: " + start +  "  " +  end);
            moneyLauderingThreads[i] = new MoneyLauderingThread(transactionFiles.subList(start, end));
            moneyLauderingThreads[i].start();
            start = end;
        }
        
        
    }

    public List<String> getOffendingAccounts()
    {
        return transactionAnalyzer.listOffendingAccounts();
    }

    private List<File> getTransactionFileList()
    {
        List<File> csvFiles = new ArrayList<>();
        try (Stream<Path> csvFilePaths = Files.walk(Paths.get("src/main/resources/")).filter(path -> path.getFileName().toString().endsWith(".csv"))) {
            csvFiles = csvFilePaths.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFiles;
    }

    public static void main(String[] args)
    {
        System.out.println(getBanner());
        System.out.println(getHelp());
        MoneyLaundering moneyLaundering = new MoneyLaundering();        
        moneyLaundering.processTransactionData();
        while(amountOfFilesProcessed.get() < 22)
        {
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            if(line.contains("exit"))
            {
                System.exit(0);
            }
            if(line.isEmpty()){
                if(pause==false){
                    System.out.println("pause");
                    pause=true;
                }
                else if(pause==true){
                    System.out.println("not Pause");
                    pause=false;
                    synchronized (threadMonitor) {
                        //notifica a todos los hilos que estan en el monitor que pueden continuar.
                        threadMonitor.notifyAll();
                    }
                }
            }
            String message = "Processed %d out of %d files.\nFound %d suspect accounts:\n%s";
            List<String> offendingAccounts = moneyLaundering.getOffendingAccounts();
            String suspectAccounts = offendingAccounts.stream().reduce("", (s1, s2)-> s1 + "\n"+s2);
            message = String.format(message, moneyLaundering.amountOfFilesProcessed.get(), moneyLaundering.amountOfFilesTotal, offendingAccounts.size(), suspectAccounts);
            System.out.println(message);
        }
    }

    private static String getBanner()
    {
        String banner = "\n";
        try {
            banner = String.join("\n", Files.readAllLines(Paths.get("src/main/resources/banner.ascii")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return banner;
    }

    private static String getHelp()
    {
        String help = "Type 'exit' to exit the program. Press 'Enter' to get a status update\n";
        return help;
    }
}