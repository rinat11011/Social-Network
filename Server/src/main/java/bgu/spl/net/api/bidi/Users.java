package bgu.spl.net.api.bidi;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Users {
    private ConcurrentLinkedQueue<User> userList = new ConcurrentLinkedQueue();

   public synchronized boolean registerUser(User user){
       if(getUser(user.getName()) != null)
           return false;
       else{
           userList.add(user);
           return true;
       }
   }

   public User getUser(String userName){
       for(User user:userList){
           if(user.getName().equals(userName))
               return user;
       }
       return null;
   }
   public User getUser(int connectId){
       for(User user:userList){
           if(user.getUserId()==connectId)
               return user;
       }
       return null;
   }

    public ConcurrentLinkedQueue<User> getUserList() {
        return userList;
    }
}
