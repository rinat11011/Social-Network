package bgu.spl.net.api.bidi;

//import javax.management.Notification;
import java.util.concurrent.LinkedBlockingQueue;

public class User {
    private int userId = -1;
    private String name;
    private String password;
    private boolean isConnected = false;
    private LinkedBlockingQueue<User> followers;
    private LinkedBlockingQueue<User> following;
    private LinkedBlockingQueue<String> msg;
    private LinkedBlockingQueue<String> notifications;
    private int numOfPosts = 0;

    public LinkedBlockingQueue<String> getNotifications() {
        return notifications;
    }

    public LinkedBlockingQueue<String> getMsg() {
        return msg;
    }

    public User(String _name, String _password){
        this.name = _name;
        this.password = _password;
        followers = new LinkedBlockingQueue<>();
        following = new LinkedBlockingQueue<>();
        notifications = new LinkedBlockingQueue<>();
        msg = new LinkedBlockingQueue<>();
    }

    public String getName() {
        return name;
    }

    public String getPassword(){
        return password;
    }

    public synchronized void connect(int id){
        this.isConnected = true;
        this.userId = id;
    }

    public boolean isEqual(User user) {
        if (user.getName().equals(this.name) && user.getPassword().equals(this.password))
            return true;
        else
            return false;
    }

    public boolean addFollower(User follower){
        if(!this.followers.contains(follower)) {
            this.followers.add(follower);
            return true;
        }
        return false;
    }
    public boolean addFollowing(User toFollow){
        if(!this.following.contains(toFollow)) {
            this.following.add(toFollow);
            return true;
        }
        else
            return false;
    }
    public boolean isConnected(){
        return isConnected;
    }
    public int getUserId(){
        return this.userId;
    }
    public synchronized void disconnect(){
        isConnected=false;
        userId = -1;
    }

    public synchronized boolean removeFollower(User follower){
        if(this.followers.contains(follower)){
            this.followers.remove(follower);
            return true;
        }
        return false;
    }

    public synchronized boolean removeFollowing(User following){
        if(this.following.contains((following))){
            this.following.remove((following));
            return true;
        }
        else
            return false;
    }
    public void addMsg(String msg){
        this.msg.add(msg);
    }
    public void addNotification(String msg){
        this.notifications.add(msg);
    }

    public LinkedBlockingQueue<User> getFollowers(){
        return this.followers;
    }

    public synchronized void increaseNumOfPosts(){
        this.numOfPosts++;
    }

    public int getNumOfPosts() {
        return numOfPosts;
    }

    public LinkedBlockingQueue<User> getFollowing() {
        return following;
    }
}
