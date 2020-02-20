/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.moneylaundering;

import java.io.File;
import java.util.List;

/**
 *
 * @author juan.navarro
 */
public class MoneyLauderingThread extends Thread {

    private TransactionReader transactionReader;
    private List<File> transactionFiles;

    public MoneyLauderingThread(List<File> subList) {
        transactionFiles = subList;
        transactionReader = new TransactionReader();
    }

    @Override
    public void run() {
        for (File transactionFile : transactionFiles) {

            List<Transaction> transactions = transactionReader.readTransactionsFromFile(transactionFile);

            for (Transaction transaction : transactions) {
                synchronized (MoneyLaundering.threadMonitor) {
                    if (MoneyLaundering.pause) {
                        try {
                            //Pausa los hilos que estan en el monitor.
                            MoneyLaundering.threadMonitor.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println("calculando");
                MoneyLaundering.transactionAnalyzer.addTransaction(transaction);
            }
            MoneyLaundering.amountOfFilesProcessed.incrementAndGet();
        }
    }

}
