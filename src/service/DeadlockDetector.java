package service;

import model.Lock;
import model.LockTable;
import model.Transaction;
import model.Variable;

import javax.xml.crypto.Data;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class DeadlockDetector {
    Map<Transaction, List<Lock>> transactionLockMap = new HashMap<>();
    List<String> victimSiteList = new ArrayList<>();

    public boolean isDeadLock(List<DataManager> sites, Map<String, Transaction> transactions) {
        transactionLockMap.clear();
        victimSiteList.clear();

        // get target sites
        List<DataManager> targetSite = sites.stream()
            .filter(s -> s.isUp() && (!s.getCurLock().isEmpty()))
            .collect(Collectors.toList());

        if(targetSite.isEmpty()) {
//            System.out.println("No valid sites are (holding/waiting for) any locks");
            return false;
        }


//        Combined Q using String to List of Strings
//        For some reason, instead of bringing the actual transaction from transaction map
//        just storing the String of the TxID does not give a null pointer when printed
        // Map of <variable name(x1, x2...), List<String>>
        // example <x1:[T1, T2], x2:[T2, T1]>
        Map<String, List<String>> combinedQ = new HashMap<>();

        // add all the acquired locks
        targetSite.forEach(dataManager -> {
            // will only iterate once since only one curLock exists
            // i.e. each DataManager could hold only one lock per variable
            if(!dataManager.getCurLock().isEmpty()) {
                for (Map.Entry<String, LockTable> entry : dataManager.getCurLock().entrySet()) {
                    if(!combinedQ.containsKey(entry.getKey())) {
                        combinedQ.put(entry.getKey(), new ArrayList<>());
                    }

                    if(combinedQ.get(entry.getKey()).contains(entry.getValue().getCurLock().getTxId())) {
                        continue;
                    }
                    combinedQ.get(entry.getKey()).add(entry.getValue().getCurLock().getTxId());
                }
            }
        });

        // add all locks waiting
        targetSite.forEach(dataManager -> {
            // <variable name, List of Locks>
            // example <x1: [L1, L2], x2: [L2, L1]>
            // or <x1: [L2], x2: [L1]>
            Map<String, List<Lock>> lockQueue = dataManager.getLockWaitingList();
            if(!lockQueue.isEmpty()) {
                for (Map.Entry<String, List<Lock>> entry : lockQueue.entrySet()) {
                    for(Lock singleLock : entry.getValue()) {
                        if(combinedQ.get(entry.getKey()).contains(singleLock.getTxId())) {
                            continue;
                        }
                        combinedQ.get(entry.getKey()).add(singleLock.getTxId());
                    }
                }
            }
        });

        // construct blockGraph graph
        // T1 -> T2 is defined as
        // if T1 needs a lock on item x, T2 has a conflicting lock on x
        // or T2 is ahead of T1 on the wait queue for x and T2 seeks
        // a conflicting lock on x
        Map<String, List<String>> blockGraph = new HashMap<>();

        for(Map.Entry<String, List<String>> entry: combinedQ.entrySet()) {

            List<String> waitList = entry.getValue();

            if(waitList.size() == 1) {
                continue;
            }

            for(int i = 1; i < waitList.size(); ++i) {
                if(!blockGraph.containsKey(waitList.get(i))) {
                    blockGraph.put(waitList.get(i) , new ArrayList<>());
                }
                blockGraph.get(waitList.get(i)).add(waitList.get(i-1));
            }
        }

        for(String singleKey : blockGraph.keySet()) {
            victimSiteList.add(singleKey);
        }

        // BFS to detect a Cycle
        // construct indegree map
        Map<String, Integer> computeIndegree = new HashMap<>();

        blockGraph.forEach((k, v) -> {
            for(String s : v) {
                if(!computeIndegree.containsKey(s)) {
                    computeIndegree.put(s, 0);
                }
                computeIndegree.put(s, computeIndegree.get(s).intValue() + 1);
            }
        });

        Queue<String> queueForBFS = new ArrayDeque<>();

        computeIndegree.forEach((k, v) -> {
            if (v.intValue() == 0) {
                queueForBFS.add(k);
            }
        });

        int numOfVisited = 0;

        List<String> nonCycles = new ArrayList<>();

        while(!queueForBFS.isEmpty()) {
            String temp = queueForBFS.poll();
            nonCycles.add(temp);
            blockGraph.forEach((k,v) -> {
                computeIndegree.put(k, computeIndegree.get(k) - 1);

                if(computeIndegree.get(k).intValue() == 0) {
                    queueForBFS.add(k);
                }
            });
            ++numOfVisited;
        }

        // there is a cycle
        if(numOfVisited != victimSiteList.size()) {
            victimSiteList.removeAll(nonCycles);
        }

        // final bool
        // there cannot be a deadlock if there is only one transcation
        if (victimSiteList.size() == 1 || victimSiteList.isEmpty()) {
            return false;
        }

        return true;
    }

    public String getVictimAbortionTxID(Map<String, Transaction> transactions) {
        String vimctimID = "";
        Long minTime = Long.valueOf(-1);

        for(String site : victimSiteList) {
            if(transactions.containsKey(site)) {
                if(transactions.get(site).getTimestamp() > minTime) {
                    vimctimID = site;
                    minTime = transactions.get(site).getTimestamp();
                }
            }
        }

        return vimctimID;
    }

}
