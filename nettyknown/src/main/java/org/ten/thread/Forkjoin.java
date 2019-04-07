package org.ten.thread;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * Created by ing on 2019-04-05.
 *
 * ForkJoinTask  direct subclass  RecursiveTask 和 RecursiveAction 、CountedCompleter
 *
 * fork join 是分而治之的概念
 * 即将一个大任务分成多个小任务，小任务还可以分成多个小任务
 *
 * 常用的子类  RecursiveTask （带返回值）和 RecursiveAction 不带返回值
 *
 *
 */
public class Forkjoin {
    static int[] nums=new int[1000000];
    int MAX_LENGTH=50000;
    static Random r=new Random();

    public static void main(String[] args) throws IOException {
        for (int i=0;i<nums.length;i++) {
            nums[i]=r.nextInt(1000);
        }
        long start=System.currentTimeMillis();
        System.out.println(Arrays.stream(nums).sum());
        System.out.println(System.currentTimeMillis()-start);

        ForkJoinPool fjPool=new ForkJoinPool();//守护线程池
        /*SumAction sumAction=new Forkjoin().new SumAction(0,nums.length);
        fjPool.execute(sumAction);*/
        SumTask sumTask=new Forkjoin().new SumTask(0,nums.length);
        fjPool.execute(sumTask);

        System.out.println(sumTask.join());

    }


    class SumAction extends RecursiveAction{

        int start,end;
        SumAction(int start,int end){
            this.start=start;
            this.end=end;
        }

        protected void compute() {
            if(end-start<=MAX_LENGTH){
                long sum=0;
                for (int i=start;i<end;i++){
                    sum+=nums[i];
                }

                System.out.println("start="+start+" end="+end+" sum="+sum);
            }else{
                int middle=start+(end-start)/2;

                SumAction rightTask=new SumAction(start,middle);
                SumAction leftTask=new SumAction(middle,end);
                rightTask.fork();
                leftTask.fork();
            }
        }
    }

    class SumTask extends RecursiveTask<Long>{

        int start,end;
        SumTask(int start,int end){
            this.start=start;
            this.end=end;
        }

        protected Long compute() {
            long sum=0;
            if(end-start<=MAX_LENGTH){
                for (int i=start;i<end;i++){
                    sum+=nums[i];
                }
                System.out.println("start="+start+" end="+end+" sum="+sum);

            }else{
                int middle=start+(end-start)/2;

                SumTask rightTask=new SumTask(start,middle);
                SumTask leftTask=new SumTask(middle,end);
                rightTask.fork();
                leftTask.fork();

                sum=rightTask.join()+leftTask.join();
            }
            return sum;
        }
    }




}
