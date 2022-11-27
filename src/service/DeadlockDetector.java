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
//    List<Transaction> victimSiteList = new ArrayList<>();
    List<String> victimSiteList = new ArrayList<>();

    /*
        Map of <variable name(x1, x2...), List<Transaction>>
        example <x1:[T1, T2], x2:[T2, T1]>
     */
//    Map<String, List<Transaction>> combinedQ = new HashMap<>();

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

        // CombinedQ using string - list<Transaction>
//        // Map of <variable name(x1, x2...), List<Transaction>>
//        // example <x1:[T1, T2], x2:[T2, T1]>
//        Map<String, List<Transaction>> combinedQ = new HashMap<>();
//
//        // All locks are present
////        targetSite.forEach(dataManager -> {
////            System.out.println("Site " + dataManager.getId() + "'s information");
////            for (Map.Entry<String, LockTable> entry : dataManager.getCurLock().entrySet()) {
////                System.out.println(entry.getKey() + "'s acquired lock is " + entry.getValue().getCurLock().getTxId());
////            }
////
////            for (Map.Entry<String, List<Lock>> entry : dataManager.getLockWaitingList().entrySet()) {
////                System.out.print(entry.getKey() + " is waiting for ");
////                entry.getValue().forEach(lock -> {
////                    System.out.print(lock.getTxId() + ": " + lock.getLockType() + ", ");
////                });
////                System.out.println();
////            }
////        });
//
//        // add all the acquired locks
//        targetSite.forEach(dataManager -> {
//            // will only iterate once since only one curLock exists
//            // i.e. each DataManager could hold only one lock per variable
//            if(!dataManager.getCurLock().isEmpty()) {
//                for (Map.Entry<String, LockTable> entry : dataManager.getCurLock().entrySet()) {
//                    if(!combinedQ.containsKey(entry.getKey())) {
//                        combinedQ.put(entry.getKey(), new ArrayList<>());
//                    }
//
//                    if(combinedQ.get(entry.getKey()).contains(transactions.get(entry.getValue().getCurLock().getTxId()))) {
////                        System.out.println(entry.getKey() + ": " + entry.getValue().getCurLock().getTxId() + " is skipped.");
////                        System.out.println("here?");
//                        continue;
//                    }
//
//                    combinedQ.get(entry.getKey()).add(transactions.get(entry.getValue().getCurLock().getTxId()));
//                }
//            }
//        });
//
//        // add all locks waiting
//        targetSite.forEach(dataManager -> {
//            // <variable name, List of Locks>
//            // example <x1: [L1, L2], x2: [L2, L1]>
//            // or <x1: [L2], x2: [L1]>
//            Map<String, List<Lock>> lockQueue = dataManager.getLockWaitingList();
//            if(!lockQueue.isEmpty()) {
//                for (Map.Entry<String, List<Lock>> entry : lockQueue.entrySet()) {
//                    for(Lock singleLock : entry.getValue()) {
//                        if(combinedQ.get(entry.getKey()).contains(transactions.get(singleLock.getTxId()))) {
////                        System.out.println(entry.getKey() + ": " + entry.getValue().getCurLock().getTxId() + " is skipped.");
////                        System.out.println("here?");
//                            continue;
//                        }
//                        combinedQ.get(entry.getKey()).add(transactions.get(singleLock.getTxId()));
//                    }
//                }
//            }
//        });


//        Combined Q using String to List of Strings
//        For some reason, instead of bringing the actual transaction from transaction map
//        just storing the String of the TxID does not give a null pointer when printed
        // Map of <variable name(x1, x2...), List<String>>
        // example <x1:[T1, T2], x2:[T2, T1]>
        Map<String, List<String>> combinedQ = new HashMap<>();

        // All locks are present
//        targetSite.forEach(dataManager -> {
//            System.out.println("Site " + dataManager.getId() + "'s information");
//            for (Map.Entry<String, LockTable> entry : dataManager.getCurLock().entrySet()) {
//                System.out.println(entry.getKey() + "'s acquired lock is " + entry.getValue().getCurLock().getTxId());
//            }
//
//            for (Map.Entry<String, List<Lock>> entry : dataManager.getLockWaitingList().entrySet()) {
//                System.out.print(entry.getKey() + " is waiting for ");
//                entry.getValue().forEach(lock -> {
//                    System.out.print(lock.getTxId() + ": " + lock.getLockType() + ", ");
//                });
//                System.out.println();
//            }
//        });

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
//                        System.out.println(entry.getKey() + ": " + entry.getValue().getCurLock().getTxId() + " is skipped.");
//                        System.out.println("here?");
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
//                        System.out.println(entry.getKey() + ": " + entry.getValue().getCurLock().getTxId() + " is skipped.");
//                        System.out.println("here?");
                            continue;
                        }
                        combinedQ.get(entry.getKey()).add(singleLock.getTxId());
                    }
                }
            }
        });

        //combinedQ print function
//        combinedQ.forEach((k, v) -> {
//            System.out.print(k + " variable has the queue of ");
//            if(!v.isEmpty()) {
//                v.forEach(e -> {
//                    System.out.print(e + ", ");
//                });
//            }
//            System.out.println();
//        });

//        // add all the waiting locks
//        targetSite.forEach(dataManager -> {
//            // <variable name, List of Locks>
//            // example <x1: [L1, L2], x2: [L2, L1]>
//            // example <x1: [L2], x2: [L1]>
//            Map<String, List<Lock>> lockQueue = dataManager.getLockWaitingList();
//            if(!lockQueue.isEmpty()) {
////                lockQueue.forEach((variableName, lockList) -> {
////                    System.out.print(variableName + "'s waiting list is ");
////                    lockList.forEach(lock -> {
////                        System.out.print(lock.getTxId() + "'s lock, ");
////                    });
////                    System.out.println();
////                });
////                System.out.println("lock queue is not empty");
//                lockQueue.forEach((variable_name, lockList) -> {
////                    System.out.println("happening?");
//                    lockList.forEach(singleLock -> {
////                        System.out.println("happening?");
////                        if(combinedQ.get(variable_name) == null) {
////                            return;
////                        }
////                        if(combinedQ.get(variable_name).isEmpty()) {
////                            return;
////                        }
////                        if (combinedQ.get(variable_name).contains(transactions.get(singleLock.getTxId()))) {
////                            return;
////                        }
//                        if(!combinedQ.isEmpty() && combinedQ != null) {
//                            if(!combinedQ.containsKey(variable_name)) {
//                                combinedQ.put(variable_name, new ArrayList<>());
//                            }
//                            combinedQ.get(variable_name).add(transactions.get(singleLock.getTxId()));
//                        }
//                    });
//                });

//            combinedQ.forEach((var_name, transactionList) -> {
//                combinedQ.get(var_name).add(transactions.get());
//            });

//                lockQueue.forEach((variable_name, lockList) -> {
//                    lockList.forEach(singleLock -> {
//                        if (combinedQ.get(variable_name).contains(transactions.get(singleLock.getTxId()))) {
//                            System.out.println("here?");
//                            return;
//                        }
//                        System.out.println("here?");
//                        combinedQ.get(variable_name).add(transactions.get(singleLock.getTxId()));
//                    });
//                });
//            }
//        });

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

//        System.out.println("Print blockgraph");
//        blockGraph.forEach((k,v) -> {
//            System.out.print(k + " -> ");
//            v.forEach(elem -> System.out.print(elem + ", "));
//            System.out.println();
//        });

        // create victimSiteList
//        System.out.println("Print out victimSiteList");
        for(String singleKey : blockGraph.keySet()) {
            victimSiteList.add(singleKey);
        }
//        victimSiteList.addAll(blockGraph.keySet());
//        System.out.println("Printing out all victimSiteList");
//        victimSiteList.forEach(elem -> {
//                System.out.print(elem+ " ");
//        });
        System.out.println();

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

        List<String> result = new ArrayList<>();

        while(!queueForBFS.isEmpty()) {
            String temp = queueForBFS.poll();
            result.add(temp);
            blockGraph.forEach((k,v) -> {
                computeIndegree.put(k, computeIndegree.get(k) - 1);

                if(computeIndegree.get(k).intValue() == 0) {
                    queueForBFS.add(k);
                }
            });

            ++numOfVisited;
        }

//        System.out.println("Result array printed out");
//        for (String s : result) {
//            System.out.print(s + " | ");
//        }

        // there is a cycle
        if(numOfVisited != blockGraph.keySet().size()) {
            for(int i = 0; i < victimSiteList.size(); ++i) {
                if(!result.contains(victimSiteList.get(i))) {
//                    System.out.println("removed");
                    victimSiteList.remove(victimSiteList.get(i));
                }
//                victimSiteList.removeIf(element -> !result.contains(element));
            }
        }
        // final bool
        if (victimSiteList.isEmpty()) {
            return false;
        }

        return true;
    }
//        // Map of <variable , List of Transaction names>
//        Map<String, List<Transaction>> txID_transaction_map = new HashMap<>();
//
//        // Map of <txID, List of Variable names>
//        Map<Transaction, List<String>> transaction_txID_map = new HashMap<>();
//
//        // Map of TxID to List of TxIDs
//        Map<Transaction, List<Transaction>> blockGraph = new HashMap<>();
//
//        // filter write target sites that are up and have non-empty LockTables
//        List<DataManager> targetSite = sites.stream()
//                .filter(s -> s.isUp() && (s.getCurLock().isEmpty() || s.getLockWaitingList().isEmpty()))
//                .collect(Collectors.toList());
//
//        // able to get curLock correctly, targetSite is correct
////        for (DataManager dataManager : targetSite) {
////            for(Map.Entry<String, LockTable> entry: dataManager.getCurLock().entrySet()) {
////                System.out.println(entry.getKey() + "'s curLock is " + entry.getValue().getCurLock().getTxId());
////            }
////        }
//
//        // Variable to List of Locks, the first Lock being the acquired Lock
//        Map<Transaction, List<Lock>> combinedLockQueue = new HashMap<>();
//
//        for(DataManager site : targetSite) {
//            // add first lock acquired
//            for (Map.Entry<String, LockTable> entry : site.getCurLock().entrySet()) {
//                if(!combinedLockQueue.containsKey(
//                        transactions.get(entry.getKey())
//                )) {
//                    combinedLockQueue.put(transactions.get(entry.getKey()), new ArrayList<>());
//                }
//                combinedLockQueue.get(entry.getKey()).add(entry.getValue().getCurLock());
////                System.out.println("Combining the lock holding, should only print once for Site: " + site.getId());
//            }
//
////            System.out.println(site.getCurLock().keySet().toString());
////            combinedLockQueue.get(site.getCurLock().keySet().toString()).add(site.getCurLock().set);
//            site.getLockWaitingList().forEach((k, v) -> {
//                if (!combinedLockQueue.containsKey(k)) {
//                    combinedLockQueue.put(k, new ArrayList<>());
//                }
//
//                v.forEach(lock -> {
//                    if(!combinedLockQueue.get(k).contains(lock)) {
//                        System.out.println("Why?");
//                        combinedLockQueue.get(k).add(lock);
//                    }
//                    System.out.println("Never added?");
//                });
//            });
//        }
//
//        System.out.println("Combine Lock Queue are ");
//        combinedLockQueue.forEach((k,v) -> {
//            System.out.print(k + "'s Locks are ");
//            v.forEach(elem -> {
//                System.out.print(elem.getLockType() + ":" + elem.getTxId() + ", ");
//            });
//            System.out.println();
//        });
//
//        // <Variable, a List of Locks>
//        for (Map.Entry<Transaction, List<Lock>> entry : combinedLockQueue.entrySet()) {
//            for(Lock lock: entry.getValue()) {
//                if(!txID_transaction_map.containsKey(entry.getKey())) {
//                    txID_transaction_map.put(entry.getKey().getTxId(), new ArrayList<>());
//                }
//                if(txID_transaction_map.get(entry.getKey().getTxId()).contains(lock.getTxId())) {
//                    continue;
//                }
//                txID_transaction_map.get(entry.getKey()).add(
//                        transactions.get(lock.getTxId()));
//                if(!transaction_txID_map.containsKey(lock.getTxId())) {
//                    transaction_txID_map.put(
//                            transactions.get(lock.getTxId()), new ArrayList<>());
//                }
//                if(transaction_txID_map.get(
//                        transactions.get(lock.getTxId())
//                ).contains(entry.getKey())) {
//                    continue;
//                }
//                transaction_txID_map.get(
//                        transactions.get((lock.getTxId()))
//                ).add(transactions.get(entry.getKey().getTxId()));
//            }
//        }
//
//        // construct blockGraph graph
//        // T1 -> T2 is defined as
//        // if T1 needs a lock on item x, T2 has a conflicting lock on x
//        // or T2 is ahead of T1 on the wait queue for x and T2 seeks
//        // a conflicting lock on x
//        for(Map.Entry<String, List<Transaction>> entry: txID_transaction_map.entrySet()) {
//
//            List<Transaction> waitList = entry.getValue();
//
//            if(waitList.size() == 1) {
//                continue;
//            }
//
//            for(int i = 1; i < waitList.size(); ++i) {
//                if(!blockGraph.containsKey(waitList.get(i))) {
//                    blockGraph.put(waitList.get(i) , new ArrayList<>());
//                }
//                blockGraph.get(waitList.get(i)).add(waitList.get(i-1));
//            }
//        }
//
////        transaction_txID_map.forEach((key, value) -> {
////            System.out.print(key + "'s values are ");
////            value.forEach(elem -> System.out.print(elem + " "));
////            System.out.println();
////        });
////
////        txID_transaction_map.forEach((key, value) -> {
////            System.out.print(key + "'s values are ");
////            value.forEach(elem -> System.out.print(elem + " "));
////            System.out.println();
////        });
//
////        blockGraph.forEach((k,v) -> {
////            System.out.print(k.getTxId() + "'s values are ");
////            v.forEach(elem -> System.out.print(elem.getTxId() + " "));
////            System.out.println();
////        });
//
//        // create victimlist
//        victimSiteList.addAll(blockGraph.keySet());
//
//        // BFS to detect a Cycle
//
//        // construct indegree map
//        Map<Transaction, Integer> computeIndegree = new HashMap<>();
//
//        blockGraph.forEach((k, v) -> {
//            for(Transaction s : v) {
//                if(!computeIndegree.containsKey(s)) {
//                    computeIndegree.put(s, 0);
//                }
//                computeIndegree.put(s, computeIndegree.get(s).intValue() + 1);
//            }
//        });
//
//        Queue<String> queueForBFS = new ArrayDeque<>();
//
//        computeIndegree.forEach((k, v) -> {
//            if (v.intValue() == 0) {
//                queueForBFS.add(k.getTxId());
//            }
//        });
//
//        int numOfVisited = 0;
//
//        List<String> result = new ArrayList<>();
//
//        while(!queueForBFS.isEmpty()) {
//            String temp = queueForBFS.poll();
//            result.add(temp);
//            blockGraph.forEach((k,v) -> {
//                computeIndegree.put(k, computeIndegree.get(k).intValue() - 1);
//
//                if(computeIndegree.get(k).intValue() == 0) {
//                    queueForBFS.add(k.getTxId());
//                }
//            });
//
//            ++numOfVisited;
//        }
//
//        // there is a cycle
//        if(numOfVisited == blockGraph.keySet().size()) {
//
//            for(int i = 0; i < victimSiteList.size(); ++i) {
//                victimSiteList.removeIf(element -> !result.contains(element.getTxId()));
//            }
//        }
//
//        // victimDataMangerList is not empty
//        if (!victimSiteList.isEmpty()) {
//            return true;
//        }
//
//        return false;
//    }

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
//        System.out.println("Victim is " + vimctimID);

        return vimctimID;
    }

}
