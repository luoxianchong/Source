
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RL {
	public static void main(String[] args) throws InterruptedException {
		Lock lock=new ReentrantLock();
		RL r=new RL();
		new Thread(r.new loopAdd(lock)).start();
		new Thread(r.new readSize(lock)).start();
		
	}
	
	class loopAdd implements Runnable {
		Lock lock;
		public loopAdd(Lock lock){
			this.lock=lock;
		}
		public void run() {
			try {
				lock.lock();
				System.out.println("=====lock=====");
				TimeUnit.SECONDS.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally {
				lock.unlock();
			}
		}
	}
	
	class readSize implements Runnable {
		Lock lock;
		public readSize(Lock lock){
			this.lock=lock;
		}
		
		public void run() {
			System.out.println("=====fajfjd====");
			try {
				lock.unlock();
			} catch (Exception e) {
				System.out.println("=====fk====");
			}
		}
	}
	
}
