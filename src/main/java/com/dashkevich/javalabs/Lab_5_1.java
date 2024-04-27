package com.dashkevich.javalabs;

import java.util.*;

public class Lab_5_1 {

    /**
     * Кол-во свободных лыж (изменяется в процессе работы, нужен синхронный доступ)
     */
    private static int freeSkisCount = 10;

    /**
     * Кол-во работников проката
     */
    private static final int workersCount = 2;

    /**
     * Очередь клиентов (нужен синхронный доступ)
     */
    private static final Deque<Client> clientQueue = new LinkedList<>();

    /**
     * Кол-во клиентов
     */
    private static final int clientsCount = 100;

    /**
     * Вероятность, что клиент - пенсионер
     */
    private static final double pensionerProbability = 0.2;

    /**
     * Время обслуживания клиента, надеть лыжи и т.д. (ms)
     */
    private static final int serveTime = 500;

    /**
     * Время катания на лыжах (ms)
     */
    private static final int skiingTime = 5000;

    /**
     * Время до прихода нового клиента (ms)
     */
    private static final int clientIntervalTime = 400;

    /**
     * Время ожидания в очереди (ms)
     */
    private static final int clientWaitTime = 6000;

    /**
     * Кол-во обслуженных клиентов
     */
    private static int servedClients = 0;

    /**
     * Кол-во клиентов, которые не дождались и ушли
     */
    private static int leftClients = 0;

    private static final boolean logging = true;

    private static void log(String s) {
        if (logging) {
            System.out.println(s);
        }
    }

    /**
     * Обработка очереди, для синхронного доступа к коллекции
     */
    private static synchronized Client processQueueOperation(QueueOperation queueOperation, Client newClient) {
        return switch (queueOperation) {
            case PUSH -> {
                // Если пенсионер, добавить в начало очереди
                if (newClient.pensioner) {
                    if (clientQueue.offerFirst(newClient)) {
                        log("Клиент " + newClient + " встал в начало очереди");
                    }
                } else {
                    if (clientQueue.offer(newClient)) {
                        log("Клиент " + newClient + " встал в очередь");
                    }
                }
                yield newClient;
            }
            case POP -> clientQueue.poll();
            case POP_FROM_END -> {
                Client client = clientQueue.peekLast();
                if (clientQueue.pollLast() != null) {
                    leftClients++;
                    log("Клиент " + client.number + " ушёл из очереди");
                }
                yield client;
            }
        };
    }

    /**
     * Обработка операции с лыжами для синхронного доступа к переменной кол-ва лыж
     */
    private static synchronized boolean processSkisOperation(SkisOperation skisOperation) {
        return switch (skisOperation) {
            case ISSUE -> {
                if (freeSkisCount > 0) {
                    freeSkisCount--;
                    yield true;
                } else {
                    yield false;
                }
            }
            case RETURN -> {
                freeSkisCount++;
                yield true;
            }
        };
    }

    private static void logStat() {
        System.out.println();
        System.out.println("Клиентов обслужено: " + servedClients + "(" + servedClients * 100 / clientsCount + "%)");
        System.out.println("Клиентов ушло: " + leftClients + "(" + leftClients * 100 / clientsCount + "%)");
        System.out.println("Лыж свободно: " + freeSkisCount);
        System.out.println("Клиентов в очереди: " + clientQueue.size());
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Прокат лыж");
        System.out.println();

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                if (!logging) {
                    logStat();
                }
            }
        }, 0, 1000);

        Thread clientQueueThread = getClientQueueThread();

        List<Thread> workerThreads = new ArrayList<>();
        for (int i = 0; i < workersCount; i++) {
            // Поток для одного работника проката
            Thread workerThread = getWorkerThread(i);
            workerThread.start();
            workerThreads.add(workerThread);
        }
        clientQueueThread.start();
        clientQueueThread.join();
        for (Thread workerThread : workerThreads) {
            workerThread.join();
        }

        logStat();
    }

    private static Thread getClientQueueThread() {
        return new Thread(() -> {
            try {
                for (int i = 0; i < clientsCount; i++) {
                    Thread.sleep(clientIntervalTime);
                    processQueueOperation(QueueOperation.PUSH, new Client(i, Math.random() < pensionerProbability));

                    new Thread(() -> {
                        try {
                            Thread.sleep(clientWaitTime);
                            // Если не дождался, уходит из очереди
                            processQueueOperation(QueueOperation.POP_FROM_END, null);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static Thread getWorkerThread(int workerNumber) {
        return new Thread(() -> {
            try {
                while (true) {
                    if (servedClients + leftClients == clientsCount) {
                        System.out.println();
                        log("Работник " + workerNumber + " закончил работу");
                        return;
                    }
                    Client client;
                    synchronized (clientQueue) {
                        // Если нет клиента, ждём
                        if (clientQueue.isEmpty()) {
                            continue;
                        }
                        // Если нет лыж, ждём, пока вернут
                        if (!processSkisOperation(SkisOperation.ISSUE)) {
                            continue;
                        }
                        // Если есть лыжи, но нет клиента, ждём нового клиента, лыжи кладём на место
                        client = processQueueOperation(QueueOperation.POP, null);
                        if (client == null) {
                            processSkisOperation(SkisOperation.RETURN);
                            continue;
                        }
                    }
                    log("Работник " + workerNumber + " обслуживает клиента " + client);
                    Thread.sleep(serveTime);

                    log("Клиент " + client + " пошёл кататься");
                    new Thread(() -> {
                        try {
                            Thread.sleep(skiingTime);
                            // Клиент покатался, возвращает лыжи
                            processSkisOperation(SkisOperation.RETURN);
                            servedClients++;
                            log("Клиент " + client + " закончил кататься");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Клиент проката
     */
    private record Client(int number, boolean pensioner) {

        @Override
        public String toString() {
            return number + (pensioner ? " (пенсионер)" : "");
        }
    }

    private enum QueueOperation {
        /**
         * Добавить в конец очереди
         */
        PUSH,

        /**
         * Забрать из начала очереди
         */
        POP,

        /**
         * Забрать из конца очереди
         */
        POP_FROM_END
    }

    private enum SkisOperation {

        /**
         * Выдать лыжи
         */
        ISSUE,

        /**
         * Вернуть лыжи
         */
        RETURN
    }
}
