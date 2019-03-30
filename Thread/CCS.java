
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class CCS {
	public static void main(String[] args) throws InterruptedException {
		//一个线程增加往容器里增加数据，另一个判断容器的数量是否为3，是则结束该线程
		CCS jp=new CCS();
		CountDownLatch latch=new CountDownLatch(1);//可以
		CyclicBarrier barrier=new CyclicBarrier(2);//可以
		Semaphore semaphore=new Semaphore(2,true);//达不到效果
		new Thread(jp.new readSize(latch,barrier,semaphore)).start();
		
		new Thread(jp.new loopAdd(latch,barrier,semaphore)).start();
	}
	
	
	private volatile List<Integer> list=new ArrayList<>();
	
	
	public void add(Integer i){
		list.add(i);
	}
	
	public int size(){return list.size();}
	
	class loopAdd implements Runnable {
		private CountDownLatch latch;
		private CyclicBarrier barrier;
		private Semaphore semaphore;
		public loopAdd(CountDownLatch latch,CyclicBarrier barrier,Semaphore semaphore){
			this.latch=latch;
			this.barrier=barrier;
			this.semaphore=semaphore;
		}
		public void run() {
			int i=0;
			while(i<10){ 
				list.add(++i);
				if(size()==3){
					//latch.countDown();
					//barrier.await();
					semaphore.release();
				}
				System.out.println("t2--"+(i));
				try {
					TimeUnit.MILLISECONDS.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class readSize implements Runnable {
		private CountDownLatch latch;
		private CyclicBarrier barrier;
		private Semaphore semaphore;
		public readSize(CountDownLatch latch,CyclicBarrier barrier,Semaphore semaphore){
			this.latch=latch;
			this.barrier=barrier;
			this.semaphore=semaphore;
		}
		public void run() {
			System.out.println("t1-----start-----");
			if(size()!=3){
					//latch.await();
					System.out.println("======t1===========");
					//barrier.await();
					try {
						semaphore.acquire();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			System.out.println("t1-----work---");
			
			System.out.println("t1-----over------");
		}
	}
	
}
