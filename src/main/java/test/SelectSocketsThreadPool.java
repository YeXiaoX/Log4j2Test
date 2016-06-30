package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ivan on 2016/6/27.
 * 利用线程池来为准备数据用于读取的通道提供服务
 */
public class SelectSocketsThreadPool extends SelectSockets {
    private static final int MAX_THREADS = 5;
    private ThreadPool pool = new ThreadPool(MAX_THREADS);
    private static final Logger logger = LogManager.getLogger(SelectSockets.class);

    public static void main(String[] argv) throws Exception {
        new SelectSocketsThreadPool().go(argv);//启动服务端接收客户端请求
    }


    protected void readDataFromSocket(SelectionKey key) throws Exception {
        WorkerThread worker = pool.getWorker();//当有数据准备好要读取时，获得一个线程去做这件事
        if (worker == null) {
            return;
        }
        logger.info("当前处理线程为："+worker.getId());
        worker.serviceChannel(key);
    }

    private class ThreadPool {
        List idle = new LinkedList();

        ThreadPool(int poolSize) {//启动线程池中的五个线程
            for (int i = 0; i < poolSize; i++) {
                WorkerThread thread = new WorkerThread(this);
                thread.setName("Worker" + (i + 1));
                thread.start();
                idle.add(thread);//把新建的线程分配到空闲线程链表中
            }
        }

        WorkerThread getWorker() {
            WorkerThread worker = null;
            synchronized (idle) {
                if (idle.size() > 0) {
                    worker = (WorkerThread) idle.remove(0);//如果当前链表中有空闲线程，则分配一个线程
                }
            }
            return (worker);
        }

        void returnWorker(WorkerThread worker) {
            synchronized (idle) {
                idle.add(worker);//当使用完该线程后，把线程返回到链表中
            }
        }
    }


    private class WorkerThread extends Thread {
        private ByteBuffer buffer = ByteBuffer.allocate(1024);
        private ThreadPool pool;//当前线程属于哪一个线程池
        private SelectionKey key;//当前工作线程的key

        WorkerThread(ThreadPool pool) {
            this.pool = pool;
        }

        public synchronized void run() {
            System.out.println(this.getName() + " is ready");
            while (true) {
                try {
                    this.wait();//当线程启动后就一直处于等待状态，知道被唤醒，然后执行具体业务逻辑
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("线程ID:"+Thread.currentThread().getId());
                if (key == null) {
                    continue;
                }
                System.out.println(this.getName() + " has been awakened");
                try {
                    drainChannel(key);
                } catch (Exception e) {
                    System.out.println("Caught '" + e + "' closing channel");
                    try {
                        key.channel().close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    key.selector().wakeup();//出现异常唤醒select方法
                }
                key = null;
                this.pool.returnWorker(this);//把线程返回到链表中
            }
        }

        synchronized void serviceChannel(SelectionKey key) {//给线程赋key值，并唤醒线程
            this.key = key;
            System.out.println("线程2ID:"+Thread.currentThread().getId());
            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));//取消一个read事件
            this.notify();//唤醒分配的线程
        }

        void drainChannel(SelectionKey key) throws Exception {
            SocketChannel channel = (SocketChannel) key.channel();
            int count;
            buffer.clear();
            while ((count = channel.read(buffer)) > 0) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    //System.out.print((char)buffer.get());
                    channel.write(buffer);
                }
                buffer.clear();
            }
            if (count < 0) {
                channel.close();
                return;
            }
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);//更新一个read事件
            key.selector().wakeup();
        }
    }
}