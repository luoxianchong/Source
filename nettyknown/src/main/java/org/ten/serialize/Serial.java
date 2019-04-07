package org.ten.serialize;

import java.io.*;

/**
 * 1、序列化不保存静态变量的状态
 * 2、transient 关键字修饰的变量不参与序列化
 * 3、父类未序列化、子类序列化，父类的成员变量不序列化
 * Created by ing on 2019-04-03.
 */
public class Serial<T> {

    public void doSerialize(T bean){
        try {

            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(new File(bean.getClass().getSimpleName())));
            oos.writeObject(bean);
            System.out.println("serial "+bean.getClass().getSimpleName()+" bena over!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  T deSerialize(String simpleName){
        try {
            ObjectInputStream ois=new ObjectInputStream(new FileInputStream(new File(simpleName)));
            T bean= (T) ois.readObject();
            return bean;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public byte[] doSerializeToByte(T bean){

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos=new ObjectOutputStream(baos);
            oos.writeObject(bean);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  null;
    }

    public  T deSerialize(byte[] beanBytes){
        ByteArrayInputStream bais=new ByteArrayInputStream(beanBytes);
        try {
            ObjectInputStream ois=new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
