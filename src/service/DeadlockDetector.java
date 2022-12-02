package service;

import model.Lock;
import model.LockTable;
import model.Transaction;

import java.util.*;
import java.util.stream.Collectors;

public class DeadlockDetector {
    List<String> victimSiteList = new ArrayList<>();

    /**
     * A helper function to determine if a target Lock has the same timestamp
     * as the locks inside the combinedLockQforVar
     * No side effects.
     * @param combinedLockQforVar
     * @param target
     * @return boolean if the combinedLockQforVar is null or empty
     */
    private boolean isLockIncluded(List<Lock> combinedLockQforVar, Lock target) {
        if (combinedLockQforVar == null || combinedLockQforVar.size() == 0) return false;

        for (Lock curLock: combinedLockQforVar) {
            if (curLock.getTimestamp().equals(target.getTimestamp())) {
                return true;
            }
        }
        return false;
    }

    /**
     * A function that will iterate through all the sites to detect a deadlock
     * No side effects, will not modify any state outside the scope.
     *
     * @param sites
     * @param transactions
     * @return boolean if there is a deadlock in the sites
     */
    public boolean isDeadLock(List<DataManager> sites, Map<String, Transaction> transactions) {
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
        Map<String, List<Lock>> combinedLockQ = new HashMap<>();
        Map<String, List<String>> combinedQ = new HashMap<>();

        // add all the acquired locks
        targetSite.forEach(dataManager -> {
            // will only iterate once since only one curLock exists
            // i.e. each DataManager could hold only one lock per variable
            if(!dataManager.getCurLock().isEmpty()) {
                for (Map.Entry<String, LockTable> entry : dataManager.getCurLock().entrySet()) {
                    if(!combinedQ.containsKey(entry.getKey())) {
                        combinedQ.put(entry.getKey(), new ArrayList<>());
                        combinedLockQ.put(entry.getKey(), new ArrayList<>());
                    }

                    if (entry.getValue().getCurLock() == null) continue;

                    if (isLockIncluded(combinedLockQ.get(entry.getKey()), entry.getValue().getCurLock())) {
                        continue;
                    }
//                    if(combinedLockQ.get(entry.getKey()).contains(entry.getValue().getCurLock().getTimestamp())) {
//                        continue;
//                    }
                    combinedQ.get(entry.getKey()).add(entry.getValue().getCurLock().getTxId());
                    combinedLockQ.get(entry.getKey()).add(entry.getValue().getCurLock());
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
                        if (isLockIncluded(combinedLockQ.get(entry.getKey()), singleLock)) {
                            continue;
                        }
//                        if(combinedQ.get(entry.getKey()).contains(singleLock.getTxId())) {
//                            continue;
//                        }
                        combinedQ.get(entry.getKey()).add(singleLock.getTxId());
                        combinedLockQ.get(entry.getKey()).add(singleLock);
                    }
                }
            }
        });

        // construct blockGraph graph
        // T1 -> T2 is defined as
        // if T1 needs a lock on item x, T2 has a conflicting lock on x
        // or T2 is ahead of T1 on the wait queue for x and T2 seeks
        // a conflicting lock on x
        HashSet<String> allTransactions = new HashSet<>();
        for (String varName: combinedQ.keySet()) {
            List<String> waitList = combinedQ.get(varName);
            for (String txId: waitList) {
                allTransactions.add(txId);
            }
        }

        Map<String, List<String>> blockGraph = new HashMap<>();
        for (String txId: allTransactions) {
            blockGraph.put(txId, new ArrayList<>());
        }

        for(Map.Entry<String, List<String>> variable: combinedQ.entrySet()) {

            List<String> waitList = variable.getValue();

            if(waitList.size() == 1) {
                continue;
            }

            for(int i = 1; i < waitList.size(); ++i) {
//                if (i == 0) {
//                    blockGraph.put(waitList.get(0), new ArrayList<>());
//                    continue;
//                }
//                if(!blockGraph.containsKey(waitList.get(i))) {
//                    blockGraph.put(waitList.get(i) , new ArrayList<>());
//                }
                blockGraph.get(waitList.get(i)).add(waitList.get(i-1));
            }
        }

        for(String singleKey : blockGraph.keySet()) {
            victimSiteList.add(singleKey);
        }

        // BFS to detect a Cycle
        // construct indegree map
        Map<String, Integer> computeIndegree = new HashMap<>();

        for (String key: blockGraph.keySet()) {
            computeIndegree.put(key, 0);
        }

        blockGraph.forEach((k, v) -> {
//            if (v.size() == 0) {
//                computeIndegree.put(v.get(0), 0);
//            }
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

            List<String> tmp  = blockGraph.get(temp);
            if (tmp == null) continue;
            for(String elem : blockGraph.get(temp)) {

                computeIndegree.put(elem, computeIndegree.get(elem) -1);

                if(computeIndegree.get(elem) == 0) {
                    queueForBFS.add(elem);
                }

                ++numOfVisited;
            }
//            blockGraph.forEach((k,v) -> {
//                computeIndegree.put(k, computeIndegree.get(k) - 1);
//
//                if(computeIndegree.get(k).intValue() == 0) {
//                    queueForBFS.add(k);
//                }
//            });
//            ++numOfVisited;
        }

        // there is a cycle
        if(numOfVisited != victimSiteList.size()) {
            victimSiteList.removeAll(nonCycles);
        }

        // final bool
        // there cannot be a deadlock if there is only one transaction
        if (victimSiteList.size() == 1 || victimSiteList.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * This function will be invoked when the above isDeadlock returns true
     * and will give the youngest transaction that is causing the deadlock
     *
     * No side effects.
     * @param transactions
     * @return String of the youngest transaction's TID
     */

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
