package org.ten.thread;

import java.util.concurrent.Executors;

/**
 * Created by ing on 2019-04-05.
 */
public class Executor {


    public static void main(String[] args) {
        Executors.newCachedThreadPool();
        Executors.newFixedThreadPool(5);
        Executors.newScheduledThreadPool(5);
        Executors.newSingleThreadExecutor();
        Executors.newSingleThreadScheduledExecutor();

        Executors.newWorkStealingPool();//守护线程 deamon



    }
}
