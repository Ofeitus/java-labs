package com.dashkevich.javalabs.lab_5_2;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Lab_5_2 {

    private static final BlockingQueue<Car> oneSideCars = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Car> anotherSideCars = new LinkedBlockingQueue<>();
    private static boolean side;

    /**
     * Максимальное время до приезда машины с одной или с другой стороны
     */
    private static final int carIntervalTime = 500;

    /**
     * Время, за которое проезжает одна машина
     */
    private static final int carPassageTime = 200;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Ремонт дороги");
        System.out.println();

        Random random = new Random();

        // Поток для генерации подъезжающих машин
        new Thread(() -> {
            while (true) {
                try {
                    // Случайное время до приезда
                    Thread.sleep(random.nextInt(carIntervalTime));
                    if (Math.random() < 0.5) {
                        if (oneSideCars.offer(new Car())) {
                            System.out.println("С одной стороны прибыла машина (с одной: " + oneSideCars.size() + ", с другой: " + anotherSideCars.size() + ")");
                        }
                    } else {
                        if (anotherSideCars.offer(new Car())) {
                            System.out.println("С другой стороны прибыла машина (с одной: " + oneSideCars.size() + ", с другой: " + anotherSideCars.size() + ")");
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        // Пусть наберётся много машин, потом начнут проезжать
        Thread.sleep(5000);

        // Поток для обработки проезда машин
        new Thread(() -> {
            while (true) {
                try {
                    BlockingQueue<Car> queue = side ? oneSideCars : anotherSideCars;
                    int i = 0;
                    while (i < 3 && !queue.isEmpty()) {
                        Thread.sleep(carPassageTime);
                        queue.poll();
                        i++;
                    }
                    if (i > 0) {
                        if (side) {
                            System.out.println("С одной стороны проехало: " + i);
                        } else {
                            System.out.println("С другой стороны проехало: " + i);
                        }
                    }
                    side = !side;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
