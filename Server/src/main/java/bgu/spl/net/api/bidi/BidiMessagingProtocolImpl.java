package bgu.spl.net.api.bidi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T> {
    /**
     * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
     **/
    private Connections<T> connections;
    private int connectId;
    private boolean shouldTerminate = false;
    private Users users;
    private LinkedBlockingQueue<User> followUnfollowList;

   public BidiMessagingProtocolImpl(Users users){
        this.users = users;
    }


    public void start(int connectionId, Connections<T> connections){
        this.connectId = connectionId;
        this.connections = connections;
    }

    public synchronized void process(T message) {
        short opCode = getType((String)message);

        switch (opCode){
            case 1: {
                register((String) message);
                break;
            }
            case 2: {
                login((String) message);
                break;
            }
            case 3: {
                logout();
                break;
            }
            case 4: {
                follow((String) message);
                break;
            }
            case 5:{
                post((String)message);
                break;
            }
            case 6:{
                pm((String)message);
                break;
            }
            case 7:{
                userList();
                break;
            }
            case 8: {
                stat((String) message);
                break;
            }
        }
    }

    /**
     * @return true if the connection should be terminated
     */
    public boolean shouldTerminate(){
        return shouldTerminate;
    }

    public synchronized void register(String message) {
        message = message.substring(message.indexOf(" ") + 1);
        String name = message.substring(0, message.indexOf(" "));
        message = message.substring(message.indexOf(" ") + 1);
        String password = message.substring(0, message.indexOf(" "));
        User newUser = new User(name, password);
        if(!users.registerUser(newUser)){
            String errorMsg = "ERROR 1";
            connections.send(connectId,(T)errorMsg);
        }
        else{
            String ackMsg = "ACK 1";
            connections.send(connectId,(T)ackMsg);
        }
    }

    public synchronized void login(String message){
        message = message.substring(message.indexOf(" ") + 1);
        String name = message.substring(0, message.indexOf(" "));
        message = message.substring(message.indexOf(" ") + 1);
        String password = message.substring(0, message.indexOf(" "));
        User user2login = users.getUser(name);
        if      ((user2login==null)||
                (!user2login.getPassword().equals(password))||
                (user2login.isConnected())){
            String errorMsg = "ERROR 2";
            connections.send(connectId,(T)errorMsg);
        }
        else{
            //connecting the user
            user2login.connect(connectId);
            String ackMsg = "ACK 2";
            connections.send(connectId,(T)ackMsg);
            if((user2login.getNotifications()!=null)&&(user2login.getNotifications().size() > 0)){
                //the user has logged-in therefore we'll send him all the messages he had missed
                for(String msg:user2login.getNotifications()){
                    user2login.getMsg().add(msg);
                    connections.send(user2login.getUserId(),(T)msg);
                }
                user2login.getNotifications().clear();
            }


        }
    }
    public synchronized void logout(){
        User user = users.getUser(connectId);
        if(user!=null) {
            if (!user.isConnected()) {
                String errorMsg = "ERROR 3";
                connections.send(connectId, (T) errorMsg);
            } else {
                String ackMsg = "ACK 3";
                connections.send(connectId, (T) ackMsg);
                user.disconnect();
                this.shouldTerminate = true;
            }
        }
        else{
            String errorMsg = "ERROR 3";
            connections.send(connectId, (T) errorMsg);
        }
    }
    public synchronized void follow(String message){
        followUnfollowList = new LinkedBlockingQueue<>();
        message = message.substring(message.indexOf(" ") + 1);
        int followUnfollow = Integer.parseInt(message.substring(0, message.indexOf(" ")));
        message = message.substring(message.indexOf(" ") + 1);
        int numOfFollowers = Integer.parseInt(message.substring(0, message.indexOf(" ")));
        int count;
        message = message.substring(message.indexOf(" ") + 1);
        User currUser = users.getUser(connectId);
        if(currUser!=null) {
            switch (followUnfollow) {
                case 0: {
                    count = follow(currUser, message);
                    if ((count == 0) && (numOfFollowers > 0)){
                        String errorMsg = "ERROR 4";
                        connections.send(connectId,(T)errorMsg);
                    }
                    else{
                        String ackMsg = "ACK 4 ";
                        ackMsg += count +" ";
                        for(User user:followUnfollowList){
                            ackMsg += user.getName()+" ";
                        }
                        ackMsg = ackMsg.trim();
                        connections.send(connectId,(T)ackMsg);
                    }
                    break;
                }
                case 1: {
                    count = unfollow(currUser, message);
                    if ((count == 0) && (numOfFollowers != 0)) {
                        String errorMsg = "ERROR 4";
                        connections.send(connectId, (T) errorMsg);
                    } else {
                        String ackMsg = "ACK 4";
                        ackMsg += count + " ";
                        for (User user : followUnfollowList) {
                            ackMsg += user.getName() + " ";
                        }
                        ackMsg = ackMsg.trim();
                        connections.send(connectId, (T) ackMsg);
                    }
                    break;
                }
            }
        }
        else{
            String errorMsg = "ERROR 4";
            connections.send(connectId,(T)errorMsg);
        }
    }
    public synchronized int follow(User currUser,String message){
        int count = 0;
        boolean addedFollower;

        while (!message.equals("")) {
            String userName = message.substring(0, message.indexOf(" "));
            userName = userName.trim();
            User new2follow = users.getUser(userName);
            if (new2follow != null){
                addedFollower = currUser.addFollowing(new2follow);
                new2follow.addFollower(currUser);
                if(addedFollower) {
                    count++;
                    followUnfollowList.add(new2follow);
                }
            }
            message = message.substring(message.indexOf(" ")+1);
        }
        return count;
    }
    public synchronized int unfollow(User currUser,String message){
        int count = 0;
        boolean addedUnfollower;

        while(!message.equals("")) {
            String userName = message.substring(0, message.indexOf(" "));
            userName = userName.trim();
            User new2unfollow = users.getUser(userName);
            if (new2unfollow != null) {
                addedUnfollower = currUser.removeFollowing(new2unfollow);
                new2unfollow.removeFollower(currUser);
                if (addedUnfollower) {
                    count++;
                    followUnfollowList.add(new2unfollow);
                }
            }
            message = message.substring(message.indexOf(" ")+1);
        }
        return count;
    }

    public synchronized void post(String message){
        message = message.substring(message.indexOf(' ') + 1);
        String content = message.trim();
        User currUser = users.getUser(this.connectId);
        if((currUser==null)||(!currUser.isConnected())){
            String errorMsg = "ERROR 5";
            connections.send(connectId,(T)errorMsg);
        }
        else{
            currUser.increaseNumOfPosts();
            String ackMsg = "ACK 5";
            connections.send(connectId,(T)ackMsg);
            String tempCon = content;
            int tag = tempCon.indexOf('@');
            while(tag!=-1){
                tempCon = tempCon.substring(tag+1);
                String userName = tempCon.substring(0,tempCon.indexOf(' '));
                User taggedUser = users.getUser(userName);
                if(taggedUser==null) {
                    String errorMsg = "ERROR 5";
                    connections.send(connectId, (T) errorMsg);
                }
                else{
                    String sendToCon = "NOTIFICATION PUBLIC "
                            + currUser.getName() + " " + content;
                    if(taggedUser.isConnected()) {
                        taggedUser.addMsg(sendToCon);
                        connections.send(taggedUser.getUserId(),(T)sendToCon);
                    }
                    else
                        taggedUser.addNotification(sendToCon);

                }

                tag = tempCon.indexOf('@');
            }
            for(User user:currUser.getFollowers()){
                String sendToCon = "NOTIFICATION PUBLIC "
                        + currUser.getName() + " " + content;
                if(user.isConnected()) {
                    user.addMsg(sendToCon);
                    connections.send(user.getUserId(), (T) sendToCon);
                }
                else
                    user.addNotification(sendToCon);
            }
        }

    }
    public synchronized void pm(String message){
        message = message.substring(message.indexOf(' ') + 1);
        String name = message.substring(0, message.indexOf(" "));
        User sendTo = users.getUser(name);
        String content = message.substring(message.indexOf(" ") + 1);
        content = content.trim();
        User currUser = users.getUser(this.connectId);
        if((sendTo==null)||(currUser==null)||(!currUser.isConnected())) {
            String errorMsg = "ERROR 6";
            connections.send(connectId, (T) errorMsg);
        }
        else{
            String sendToCon = "NOTIFICATION PM "
                    + sendTo.getName() + " " + content;
            if(sendTo.isConnected()) {
                sendTo.addMsg(sendToCon);
                connections.send(sendTo.getUserId(),(T)sendToCon);
            }
            else
                sendTo.addNotification(sendToCon);
            String ackMsg = "ACK 6";
            connections.send(connectId,(T)ackMsg);
        }


    }
    public synchronized void userList() {
        ConcurrentLinkedQueue<User> userList = users.getUserList();
        User currUser = users.getUser(this.connectId);
        int numOfUsers = userList.size();
        String result = "ACK 7 " + numOfUsers + " ";

        if ((currUser == null) || (!currUser.isConnected())) {
            String errorMsg = "ERROR 7";
            connections.send(connectId, (T) errorMsg);
        } else {
            for (User user : userList) {
                result += user.getName() + " ";
            }

            connections.send(connectId, (T) result);
        }
    }

    public synchronized void stat(String message){
        message = message.substring(message.indexOf(" ") + 1);
        String name = message.substring(0, message.indexOf(" "));
        User currUser = users.getUser(connectId);
        User wantedUser = users.getUser(name);
        if(currUser==null || !currUser.isConnected() || wantedUser==null){
            String errorMsg = "ERROR 8";
            connections.send(connectId,(T)errorMsg);
        }
        else{
            String result = "ACK 8 ";
            if(currUser.getNumOfPosts()==0)
                result += currUser.getNumOfPosts()+" ";
            if(currUser.getFollowers()!=null)
                    result+=currUser.getFollowers().size()+" ";
            else
                result+="0 ";
            if(currUser.getFollowing()!=null)
                result+=currUser.getFollowing().size();
            else
                result+="0";
            connections.send(connectId,(T)result);
        }
    }

    public synchronized short getType(String message){
        String msgType;
        int index = message.indexOf(" ");
        if(index==-1)
            msgType = message;
        else
            msgType = message.substring(0,message.indexOf(" "));

        if(msgType.equals("REGISTER"))
            return 1;
        else if(msgType.equals("LOGIN"))
            return 2;
        else if(msgType.equals("LOGOUT"))
            return 3;
        else if(msgType.equals("FOLLOW"))
            return 4;
        else if(msgType.equals("POST"))
            return 5;
        else if(msgType.equals("PM"))
            return 6;
        else if(msgType.equals("USERLIST"))
            return 7;
        else if(msgType.equals("STAT"))
            return 8;

        return 0;
    }
}
